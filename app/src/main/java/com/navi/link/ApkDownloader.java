package com.navi.link;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * APK 下载器。将更新服务提供的安装包下载到应用缓存目录，带进度回调。
 *
 * <p>下载目标目录为 {@code context.getCacheDir()/apk/}，与 {@code file_paths.xml}
 * 中的 {@code cache-path name="apk_cache" path="apk/"} 对应，供 FileProvider 暴露。</p>
 */
public final class ApkDownloader {

    private static final String TAG = "ApkDownloader";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int MAX_REDIRECTS = 5;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ApkDownloader() {}

    /** 下载进度回调，全部在主线程触发。 */
    public interface Callback {
        /** 进度更新。total 为 -1 表示未知总大小（无 Content-Length）。 */
        void onProgress(long downloaded, long total);

        /** 下载完成，apkFile 为本地文件。 */
        void onComplete(File apkFile);

        /** 下载失败。 */
        void onError(String message);
    }

    /**
     * 异步下载 APK。
     *
     * @param cacheDir   通常传 context.getCacheDir()
     * @param url        下载地址（更新服务返回的绝对地址）
     * @param fileName   保存的文件名
     * @param callback   进度/结果回调（主线程）
     */
    public static void download(File cacheDir, String url, String fileName, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection conn = null;
            try {
                File dir = new File(cacheDir, "apk");
                if (!dir.exists() && !dir.mkdirs()) {
                    postError(callback, "无法创建下载目录");
                    return;
                }
                // 清理同名旧文件，避免半成品复用
                File out = new File(dir, sanitize(fileName));
                if (out.exists() && !out.delete()) {
                    Log.w(TAG, "无法删除旧文件: " + out);
                }

                conn = openWithRedirects(url);
                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    postError(callback, "下载失败 HTTP " + code);
                    return;
                }

                final long total = conn.getContentLength();
                long downloaded = 0;
                long lastPosted = 0;

                try (InputStream in = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                        downloaded += n;
                        // 节流：每累计 ~64KB 或读到末尾回调一次，避免主线程过载
                        if (downloaded - lastPosted >= 65536) {
                            lastPosted = downloaded;
                            final long d = downloaded;
                            MAIN.post(() -> callback.onProgress(d, total));
                        }
                    }
                    fos.flush();
                }

                final long finalSize = downloaded;
                if (finalSize <= 0) {
                    postError(callback, "下载内容为空");
                    return;
                }
                MAIN.post(() -> {
                    callback.onProgress(finalSize, total > 0 ? total : finalSize);
                    callback.onComplete(out);
                });
            } catch (Exception e) {
                Log.w(TAG, "下载 APK 失败", e);
                postError(callback, e.getMessage() != null ? e.getMessage() : "网络错误");
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    /** 打开连接并手动跟随重定向（HttpURLConnection 不会在 http↔https 间自动跳转）。 */
    private static HttpURLConnection openWithRedirects(String urlStr) throws Exception {
        String current = urlStr;
        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            URL url = new URL(current);
            HttpURLConnection conn = NetworkCompat.open(url);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "Navi-Link-Android");
            conn.setRequestProperty("Accept", "application/vnd.android.package-archive, */*");

            int code = conn.getResponseCode();
            if (code >= 300 && code < 400) {
                String loc = conn.getHeaderField("Location");
                conn.disconnect();
                if (TextUtils.isEmpty(loc)) throw new Exception("重定向缺少 Location");
                // 处理相对重定向
                current = new URL(new URL(current), loc).toString();
                continue;
            }
            return conn;
        }
        throw new Exception("重定向次数过多");
    }

    private static void postError(Callback cb, String msg) {
        MAIN.post(() -> cb.onError(msg));
    }

    /** 文件名兜底清洗，避免空名或非法字符。 */
    private static String sanitize(String name) {
        if (TextUtils.isEmpty(name)) return "update.apk";
        String s = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!s.toLowerCase().endsWith(".apk")) s = s + ".apk";
        return s;
    }
}
