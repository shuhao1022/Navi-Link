package com.navi.link.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.navi.link.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Navi-Link 配置备份与恢复工具类
 * 兼顾 Android 4.x (API 17+) 及高版本系统的兼容性
 */
public class ConfigBackupManager {

    private static final String TAG = "ConfigBackupManager";
    public static final String PREFS_NAME = "floating_config";

    /**
     * 将当前全部配置导出为 JSON 字符串
     */
    public static String exportConfigToJson(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allEntries = sp.getAll();

            JSONObject root = new JSONObject();
            root.put("appVersion", BuildConfig.VERSION_NAME);
            root.put("exportTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            JSONObject configObj = new JSONObject();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                configObj.put(entry.getKey(), entry.getValue());
            }
            root.put("config", configObj);

            return root.toString(2);
        } catch (Exception e) {
            Log.e(TAG, "导出配置 JSON 失败", e);
            return null;
        }
    }

    /**
     * 从 JSON 字符串恢复配置到 SharedPreferences
     */
    public static boolean importConfigFromJson(Context context, String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) return false;
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (!root.has("config")) return false;

            JSONObject configObj = root.getJSONObject("config");
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            Iterator<?> keys = configObj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object val = configObj.get(key);

                if (val instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) val);
                } else if (val instanceof Integer) {
                    editor.putInt(key, (Integer) val);
                } else if (val instanceof Long) {
                    editor.putLong(key, (Long) val);
                } else if (val instanceof Double || val instanceof Float) {
                    editor.putFloat(key, ((Number) val).floatValue());
                } else if (val instanceof String) {
                    editor.putString(key, (String) val);
                }
            }
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "导入配置 JSON 失败", e);
            return false;
        }
    }

    /**
     * 一键重置所有配置为默认状态
     */
    public static boolean resetDefaultConfig(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return sp.edit().clear().commit();
        } catch (Exception e) {
            Log.e(TAG, "重置默认配置失败", e);
            return false;
        }
    }

    /**
     * 将 JSON 写入指定输出流 (适用于 SAF 及 Standard File Stream)
     */
    public static boolean writeJsonToStream(OutputStream os, String jsonStr) {
        if (os == null || jsonStr == null) return false;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.print(jsonStr);
            writer.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "写入配置到流失败", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 从输入流读取 JSON 字符串 (适用于 SAF 及 Standard File Stream)
     */
    public static String readJsonFromStream(InputStream is) {
        if (is == null) return null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "从流读取配置失败", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 获取默认导出到的 Download 目录文件路径 (/sdcard/Download/NaviLink_Config_xxx.json)
     */
    public static File getDownloadExportFile(Context context) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && !downloadDir.exists()) {
            try {
                downloadDir.mkdirs();
            } catch (Exception ignored) {}
        }
        if (downloadDir == null || !downloadDir.exists() || !downloadDir.canWrite()) {
            downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }
        String fileName = "NaviLink_Config_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
        return new File(downloadDir, fileName);
    }

    /**
     * 兜底：直接导出到默认目录文件 (如 SD 卡或外部存储)
     */
    public static File getFallbackExportFile(Context context) {
        File dir = context.getExternalFilesDir("backups");
        if (dir == null) {
            dir = new File(Environment.getExternalStorageDirectory(), "NaviLink/backups");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = "NaviLink_Config_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
        return new File(dir, fileName);
    }
}
