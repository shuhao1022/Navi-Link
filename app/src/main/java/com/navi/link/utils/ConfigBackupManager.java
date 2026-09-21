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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Navi-Link 配置备份与恢复工具类
 * 兼顾 Android 4.x (API 17+) 及高版本系统的兼容性与类型安全解析
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
     * 从 JSON 字符串恢复配置到 SharedPreferences (类型安全防崩溃重构)
     */
    public static boolean importConfigFromJson(Context context, String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) return false;
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (!root.has("config")) return false;

            JSONObject configObj = root.getJSONObject("config");
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> existingAll = sp.getAll();

            SharedPreferences.Editor editor = sp.edit();
            editor.clear(); // 清空原有存储，彻底消除 ClassCastException 隐患

            Iterator<?> keys = configObj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (configObj.isNull(key)) continue;

                Object val = configObj.get(key);
                Object oldVal = existingAll != null ? existingAll.get(key) : null;

                if (oldVal instanceof Float) {
                    editor.putFloat(key, parseAsFloat(val, (Float) oldVal));
                } else if (oldVal instanceof Integer) {
                    editor.putInt(key, parseAsInt(val, (Integer) oldVal));
                } else if (oldVal instanceof Long) {
                    editor.putLong(key, parseAsLong(val, (Long) oldVal));
                } else if (oldVal instanceof Boolean) {
                    editor.putBoolean(key, parseAsBoolean(val, (Boolean) oldVal));
                } else if (oldVal instanceof String) {
                    editor.putString(key, String.valueOf(val));
                } else {
                    // 新配置项，按 Key 特征或原始 JSON 类型推断
                    if (isFloatKey(key)) {
                        editor.putFloat(key, parseAsFloat(val, 1.0f));
                    } else if (val instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) val);
                    } else if (val instanceof Double || val instanceof Float) {
                        editor.putFloat(key, ((Number) val).floatValue());
                    } else if (val instanceof Integer) {
                        editor.putInt(key, (Integer) val);
                    } else if (val instanceof Long) {
                        editor.putLong(key, (Long) val);
                    } else if (val instanceof String) {
                        editor.putString(key, String.valueOf(val));
                    } else if (val instanceof Number) {
                        editor.putFloat(key, ((Number) val).floatValue());
                    }
                }
            }
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "导入配置 JSON 失败", e);
            return false;
        }
    }

    private static boolean isFloatKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("scale") || lower.contains("alpha") || lower.contains("ratio") || lower.contains("opacity");
    }

    private static float parseAsFloat(Object val, float defVal) {
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        try {
            return Float.parseFloat(String.valueOf(val));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static int parseAsInt(Object val, int defVal) {
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static long parseAsLong(Object val, long defVal) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static boolean parseAsBoolean(Object val, boolean defVal) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val != null) {
            String s = String.valueOf(val).trim();
            if ("true".equalsIgnoreCase(s) || "1".equals(s)) return true;
            if ("false".equalsIgnoreCase(s) || "0".equals(s)) return false;
        }
        return defVal;
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
     * 自动扫描本地常见备份目录，查找已导出的配置文件列表 (按修改时间倒序排列)
     */
    public static List<File> findLocalConfigBackupFiles(Context context) {
        List<File> list = new ArrayList<File>();
        Set<String> pathSet = new HashSet<String>();

        File[] dirsToScan = new File[]{
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                new File(Environment.getExternalStorageDirectory(), "Download"),
                new File(Environment.getExternalStorageDirectory(), "NaviLink/Transfers"),
                new File(Environment.getExternalStorageDirectory(), "NaviLink/backups"),
                context != null ? context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) : null,
                context != null ? context.getExternalFilesDir("Transfers") : null,
                context != null ? context.getExternalFilesDir("backups") : null
        };

        for (File dir : dirsToScan) {
            if (dir == null || !dir.exists() || !dir.isDirectory()) continue;
            File[] files = dir.listFiles();
            if (files == null) continue;

            for (File f : files) {
                if (!f.isFile()) continue;
                String name = f.getName().toLowerCase(Locale.ROOT);
                if ((name.endsWith(".json") || name.contains("navilink") || name.contains("管家配置") || name.contains("config"))
                        && !pathSet.contains(f.getAbsolutePath())) {
                    pathSet.add(f.getAbsolutePath());
                    list.add(f);
                }
            }
        }

        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        return list;
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