package com.navi.link;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * APK 安装器 —— 三级安装链。
 *
 * <p>按以下顺序尝试安装，前一种失败则回退到后一种：</p>
 * <ol>
 *   <li>系统 {@link PackageInstaller}：应用内创建安装会话（会弹系统确认框）</li>
 *   <li>InstallerX（{@code com.rosan.installer.x}）：显式调用其安装界面</li>
 *   <li>系统安装器：标准 {@code ACTION_VIEW}，由系统默认安装器处理</li>
 * </ol>
 */
public final class ApkInstaller {

    private static final String TAG = "ApkInstaller";

    /** InstallerX 包名。 */
    private static final String INSTALLERX_PKG = "com.rosan.installer.x";

    private static final String APK_MIME = "application/vnd.android.package-archive";

    private ApkInstaller() {}

    /**
     * 安装 APK。依次尝试三级安装链。
     *
     * @param context 建议传 Activity context
     * @param apkFile 已下载到本地的 APK 文件
     */
    public static void install(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            Toast.makeText(context, "安装包不存在或为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) 系统 PackageInstaller
        if (tryPackageInstaller(context, apkFile)) {
            Log.i(TAG, "使用 PackageInstaller 安装");
            return;
        }

        // 2) InstallerX
        if (tryInstallerX(context, apkFile)) {
            Log.i(TAG, "回退到 InstallerX 安装");
            return;
        }

        // 3) 系统安装器
        if (trySystemInstaller(context, apkFile)) {
            Log.i(TAG, "回退到系统安装器");
            return;
        }

        Toast.makeText(context, "未找到可用的安装方式", Toast.LENGTH_LONG).show();
    }

    // ── 1) PackageInstaller ─────────────────────────────────────
    private static boolean tryPackageInstaller(Context context, File apkFile) {
        PackageInstaller.Session session = null;
        try {
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);

            int sessionId = installer.createSession(params);
            session = installer.openSession(sessionId);

            // 写入 APK 数据
            try (InputStream in = new FileInputStream(apkFile);
                 OutputStream out = session.openWrite("base.apk", 0, apkFile.length())) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                session.fsync(out);
            }

            // 结果回调 PendingIntent → InstallResultReceiver
            Intent intent = new Intent(context, InstallResultReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, sessionId, intent, flags);

            session.commit(pi.getIntentSender());
            // commit 成功（系统会随后通过 receiver 弹确认框）
            return true;
        } catch (Exception e) {
            Log.w(TAG, "PackageInstaller 安装失败，准备回退", e);
            if (session != null) {
                try { session.abandon(); } catch (Exception ignore) {}
            }
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    // ── 2) InstallerX ───────────────────────────────────────────
    private static boolean tryInstallerX(Context context, File apkFile) {
        if (!isPackageInstalled(context, INSTALLERX_PKG)) {
            Log.i(TAG, "未安装 InstallerX，跳过");
            return false;
        }
        try {
            Uri uri = uriFor(context, apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, APK_MIME);
            intent.setPackage(INSTALLERX_PKG);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 确认 InstallerX 能处理该 intent
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                Log.i(TAG, "InstallerX 无法处理安装 intent");
                return false;
            }
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "InstallerX 安装失败，准备回退", e);
            return false;
        }
    }

    // ── 3) 系统安装器 ───────────────────────────────────────────
    private static boolean trySystemInstaller(Context context, File apkFile) {
        try {
            Uri uri = uriFor(context, apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, APK_MIME);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) == null) {
                Log.w(TAG, "系统无可处理 APK 安装的应用");
                return false;
            }
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "系统安装器调用失败", e);
            return false;
        }
    }

    // ── 工具 ────────────────────────────────────────────────────
    private static Uri uriFor(Context context, File apkFile) {
        String authority = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, authority, apkFile);
    }

    private static boolean isPackageInstalled(Context context, String pkg) {
        try {
            context.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
