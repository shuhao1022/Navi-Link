package com.navi.link;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用内更新检查器。
 *
 * <p>通过自有更新服务（{@link BuildConfig#UPDATE_BASE_URL}）获取最新版本信息，
 * 与当前 versionName 比对，判断是否有新版本可用。版本数据与安装包均由服务器
 * 缓存自 GitHub，客户端无需直连 GitHub（国内网络更稳定）。</p>
 *
 * <p>不依赖任何第三方库。服务地址仅为编译期常量，不在界面中展示。</p>
 *
 * <p>用法：</p>
 * <pre>
 *   UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME, false, callback);
 * </pre>
 */
public final class UpdateChecker {

    private static final String TAG = "UpdateChecker";

    /** 更新服务基地址（自有服务器，不在 UI 显示）。 */
    private static final String BASE_URL = trimTrailingSlash(BuildConfig.UPDATE_BASE_URL);

    /** 最新版本信息接口。 */
    private static final String API_LATEST = BASE_URL + "/api/latest";

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private UpdateChecker() {}

    /** 检查结果回调，全部在主线程触发。 */
    public interface Callback {
        /** 发现新版本。 */
        void onUpdateAvailable(UpdateInfo info);

        /** 已是最新版本。manual 为 true 时（用户手动点击）才需要提示。 */
        void onNoUpdate(boolean manual);

        /** 检查失败（网络异常、解析失败等）。manual 为 true 时才需要提示。 */
        void onError(String message, boolean manual);
    }

    /** 新版本信息。 */
    public static final class UpdateInfo {
        public final String versionName;   // 去掉前缀 v 的版本号，如 2.5.4
        public final String tagName;       // 原始 tag，如 v2.5.4
        public final String releaseName;   // release 标题
        public final String notes;         // 更新说明（markdown 原文）
        public final String apkUrl;        // APK 直接下载地址（可能为空）
        public final String htmlUrl;       // release 页面地址（兜底）
        public final long apkSize;         // APK 字节数（可能为 0）

        UpdateInfo(String versionName, String tagName, String releaseName,
                   String notes, String apkUrl, String htmlUrl, long apkSize) {
            this.versionName = versionName;
            this.tagName = tagName;
            this.releaseName = releaseName;
            this.notes = notes;
            this.apkUrl = apkUrl;
            this.htmlUrl = htmlUrl;
            this.apkSize = apkSize;
        }

        /** 下载地址：优先 APK 直链，否则回退到 release 页面。 */
        public String bestDownloadUrl() {
            return !TextUtils.isEmpty(apkUrl) ? apkUrl : htmlUrl;
        }
    }

    /**
     * 异步检查更新。
     *
     * @param currentVersionName 当前应用 versionName（通常取自 BuildConfig.VERSION_NAME）
     * @param manual             是否用户手动触发（决定"已最新/失败"是否提示）
     * @param callback           结果回调（主线程）
     */
    public static void checkForUpdate(String currentVersionName, boolean manual, Callback callback) {
        EXECUTOR.execute(() -> {
            try {
                String json = httpGet(API_LATEST);
                UpdateInfo info = parseRelease(json);
                if (info == null) {
                    post(() -> callback.onError("解析发布信息失败", manual));
                    return;
                }
                if (isNewer(info.versionName, currentVersionName)) {
                    post(() -> callback.onUpdateAvailable(info));
                } else {
                    post(() -> callback.onNoUpdate(manual));
                }
            } catch (NotFoundException e) {
                // 仓库尚无 release
                post(() -> callback.onNoUpdate(manual));
            } catch (Exception e) {
                Log.w(TAG, "检查更新失败", e);
                final String msg = e.getMessage() != null ? e.getMessage() : "网络错误";
                post(() -> callback.onError(msg, manual));
            }
        });
    }

    // ── 网络 ────────────────────────────────────────────────────
    private static String httpGet(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = NetworkCompat.open(url);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Navi-Link-Android");

            int code = conn.getResponseCode();
            if (code == 404) {
                throw new NotFoundException();
            }
            if (code < 200 || code >= 300) {
                throw new Exception("更新服务 HTTP " + code);
            }
            try (InputStream is = conn.getInputStream()) {
                return readAll(is);
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readAll(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    // ── 解析 ────────────────────────────────────────────────────
    // 解析更新服务返回的 JSON（字段：version / name / body / html_url(htmlUrl) /
    // apk:{name,size,downloadUrl} ）。downloadUrl 可能是相对路径，需补全为绝对地址。
    private static UpdateInfo parseRelease(String json) {
        try {
            JSONObject o = new JSONObject(json);

            // 服务端若返回 error 字段，视为失败
            if (o.has("error")) {
                Log.w(TAG, "更新服务返回错误: " + o.optString("message", o.optString("error")));
                return null;
            }

            String tag = o.optString("version", o.optString("name", ""));
            if (TextUtils.isEmpty(tag)) return null;
            String version = stripV(tag);
            String name = o.optString("name", tag);
            String notes = o.optString("body", "");
            String htmlUrl = o.optString("htmlUrl", o.optString("html_url", ""));

            String apkUrl = "";
            long apkSize = 0;
            JSONObject apk = o.optJSONObject("apk");
            if (apk != null) {
                apkUrl = apk.optString("downloadUrl", "");
                apkSize = apk.optLong("size", 0);
            }
            // 把相对下载地址补全为绝对地址（基于更新服务 base url）
            apkUrl = absolutize(apkUrl);
            htmlUrl = absolutize(htmlUrl);

            return new UpdateInfo(version, tag, name, notes, apkUrl, htmlUrl, apkSize);
        } catch (Exception e) {
            Log.w(TAG, "解析更新信息失败", e);
            return null;
        }
    }

    /** 把相对路径（/download/xxx）补全为基于更新服务的绝对 URL。 */
    private static String absolutize(String url) {
        if (TextUtils.isEmpty(url)) return "";
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        if (url.startsWith("/")) return BASE_URL + url;
        return BASE_URL + "/" + url;
    }

    /** 去掉 BASE_URL 末尾斜杠。 */
    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    // ── 版本比较 ─────────────────────────────────────────────────
    /** 去掉版本号前缀的 v / V。 */
    static String stripV(String v) {
        if (v == null) return "";
        v = v.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        return v.trim();
    }

    /**
     * 判断 remote 是否比 local 新。语义化数字比较（点分），
     * 非数字后缀（如 -beta）忽略其数值，仅按数字段比较；段数不足按 0 补齐。
     */
    static boolean isNewer(String remote, String local) {
        int[] r = parseVersion(stripV(remote));
        int[] l = parseVersion(stripV(local));
        int n = Math.max(r.length, l.length);
        for (int i = 0; i < n; i++) {
            int rv = i < r.length ? r[i] : 0;
            int lv = i < l.length ? l[i] : 0;
            if (rv != lv) return rv > lv;
        }
        return false;
    }

    private static int[] parseVersion(String v) {
        if (TextUtils.isEmpty(v)) return new int[]{0};
        // 取首个连续的 "数字.数字..." 片段，丢弃后缀（如 2.5.4-rc1 → 2.5.4）
        String core = v.split("[^0-9.]")[0];
        if (TextUtils.isEmpty(core)) return new int[]{0};
        String[] parts = core.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    // ── 工具 ────────────────────────────────────────────────────
    private static void post(Runnable r) {
        MAIN.post(r);
    }

    /** GitHub 仓库无 release 时返回 404，用此专用异常区分。 */
    private static final class NotFoundException extends Exception {}
}
