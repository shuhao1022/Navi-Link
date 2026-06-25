package com.navi.link;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Locale;

/**
 * 更新提示对话框。把 {@link UpdateChecker.UpdateInfo} 渲染成深色风格弹窗，
 * 「立即更新」点击后在应用内下载 APK（显示进度），完成后走三级安装链
 * （{@link ApkInstaller}）。
 */
public final class UpdateDialog {

    private UpdateDialog() {}

    /**
     * 显示更新提示框。
     *
     * @param context     建议传入 Activity context
     * @param currentVer  当前版本名（用于"旧 → 新"展示）
     * @param info        新版本信息
     */
    public static void show(Context context, String currentVer, UpdateChecker.UpdateInfo info) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);

        TextView tvVersion = view.findViewById(R.id.tv_update_version);
        TextView tvSize = view.findViewById(R.id.tv_update_size);
        TextView tvNotes = view.findViewById(R.id.tv_update_notes);
        MaterialButton btnNow = view.findViewById(R.id.btn_update_now);
        MaterialButton btnLater = view.findViewById(R.id.btn_update_later);

        tvVersion.setText(String.format(Locale.getDefault(), "v%s  →  v%s",
                UpdateChecker.stripV(currentVer), info.versionName));

        if (info.apkSize > 0) {
            tvSize.setVisibility(View.VISIBLE);
            tvSize.setText("安装包大小 " + formatSize(info.apkSize));
        }

        tvNotes.setText(cleanNotes(info.notes));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        // 去掉系统默认白色背景，露出自定义圆角背景
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnNow.setOnClickListener(v ->
                startDownloadAndInstall(context, info, dialog, btnNow, btnLater));
        btnLater.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /** 下载并安装。下载过程中按钮显示进度，期间禁止关闭。 */
    private static void startDownloadAndInstall(Context context, UpdateChecker.UpdateInfo info,
                                                AlertDialog dialog, MaterialButton btnNow,
                                                MaterialButton btnLater) {
        String url = info.bestDownloadUrl();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(context, "下载地址不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 进入下载态：禁用按钮、锁定对话框
        btnNow.setEnabled(false);
        btnLater.setEnabled(false);
        dialog.setCancelable(false);
        btnNow.setText("准备下载…");

        String fileName = buildFileName(info);

        ApkDownloader.download(context.getCacheDir(), url, fileName, new ApkDownloader.Callback() {
            @Override
            public void onProgress(long downloaded, long total) {
                if (total > 0) {
                    int pct = (int) (downloaded * 100 / total);
                    btnNow.setText("下载中 " + pct + "%");
                } else {
                    btnNow.setText("下载中 " + formatSize(downloaded));
                }
            }

            @Override
            public void onComplete(File apkFile) {
                btnNow.setText("准备安装…");
                proceedInstall(context, apkFile, dialog, btnNow, btnLater);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, "下载失败：" + message, Toast.LENGTH_LONG).show();
                // 恢复，允许重试
                btnNow.setEnabled(true);
                btnLater.setEnabled(true);
                dialog.setCancelable(true);
                btnNow.setText("重试");
            }
        });
    }

    /** 检查未知来源安装权限，必要时引导开启；有权限则启动安装链。 */
    private static void proceedInstall(Context context, File apkFile, AlertDialog dialog,
                                       MaterialButton btnNow, MaterialButton btnLater) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && !context.getPackageManager().canRequestPackageInstalls()) {
            // 无"安装未知应用"权限：引导用户去开启
            Toast.makeText(context, "请允许安装未知来源应用后重试", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                // 部分车机无该设置页，回退到安装链让系统自行处理
                ApkInstaller.install(context, apkFile);
                dialog.dismiss();
                return;
            }
            // 已下载完成，恢复按钮让用户授权后点击安装
            btnNow.setEnabled(true);
            btnLater.setEnabled(true);
            dialog.setCancelable(true);
            btnNow.setText("安装");
            btnNow.setOnClickListener(v -> {
                ApkInstaller.install(context, apkFile);
                dialog.dismiss();
            });
            return;
        }

        ApkInstaller.install(context, apkFile);
        dialog.dismiss();
    }

    /** 构造保存文件名：优先 release 里的 APK 名，否则按版本生成。 */
    private static String buildFileName(UpdateChecker.UpdateInfo info) {
        String url = info.apkUrl;
        if (!TextUtils.isEmpty(url)) {
            int slash = url.lastIndexOf('/');
            String name = slash >= 0 ? url.substring(slash + 1) : url;
            // 去掉可能的 query
            int q = name.indexOf('?');
            if (q >= 0) name = name.substring(0, q);
            try {
                name = Uri.decode(name);
            } catch (Exception ignore) {}
            if (name.toLowerCase().endsWith(".apk")) return name;
        }
        return "Navi-Link-v" + info.versionName + ".apk";
    }

    /** 简单清洗 markdown，使其在纯 TextView 中更易读（不做完整渲染）。 */
    private static String cleanNotes(String md) {
        if (TextUtils.isEmpty(md)) return "暂无更新说明";
        String s = md.replace("\r\n", "\n");
        StringBuilder out = new StringBuilder();
        for (String line : s.split("\n")) {
            String t = line.trim();
            t = t.replaceAll("^#{1,6}\\s*", "");          // 标题
            t = t.replaceAll("^[*\\-+]\\s+", "• ");        // 无序列表
            t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1");   // 粗体
            t = t.replaceAll("`([^`]+)`", "$1");            // 行内代码
            out.append(t).append('\n');
        }
        return out.toString().trim();
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024));
    }
}
