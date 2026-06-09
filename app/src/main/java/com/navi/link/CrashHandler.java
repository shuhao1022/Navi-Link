package com.navi.link;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * 全局异常捕获处理器
 * 捕获未处理的异常并保存到本地日志文件，便于排查线上崩溃问题
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final String CRASH_DIR = "crash";
    private static final int MAX_LOG_FILES = 10;

    private static volatile CrashHandler instance;

    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化异常处理器
     */
    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // 保存崩溃日志
            String logContent = collectDeviceInfo() + "\n" + getStackTrace(throwable);
            saveCrashInfo(logContent);

            // 清理旧日志，保留最近 MAX_LOG_FILES 个
            cleanOldLogs();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 调用默认处理器（系统会杀死进程）
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }

    /**
     * 收集设备信息
     */
    private String collectDeviceInfo() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        sb.append("========== 崩溃日志 ==========\n");
        sb.append("时间: ").append(sdf.format(new Date())).append("\n");
        sb.append("\n");

        // 设备信息
        sb.append("【设备信息】\n");
        sb.append("品牌: ").append(Build.BRAND).append("\n");
        sb.append("型号: ").append(Build.MODEL).append("\n");
        sb.append("设备: ").append(Build.DEVICE).append("\n");
        sb.append("Android版本: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("API Level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("\n");

        // 应用信息
        sb.append("【应用信息】\n");
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            sb.append("包名: ").append(pi.packageName).append("\n");
            sb.append("版本名: ").append(pi.versionName).append("\n");
            sb.append("版本号: ").append(pi.versionCode).append("\n");
        } catch (PackageManager.NameNotFoundException e) {
            sb.append("获取应用信息失败\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * 获取异常堆栈
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return "【异常堆栈】\n" + sw.toString();
    }

    /**
     * 保存崩溃信息到文件
     */
    private void saveCrashInfo(String content) {
        File crashDir = context.getExternalFilesDir(CRASH_DIR);
        if (crashDir == null) {
            // 外部存储不可用时，尝试内部存储
            crashDir = new File(context.getFilesDir(), CRASH_DIR);
        }

        if (!crashDir.exists()) {
            crashDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "crash_" + sdf.format(new Date()) + ".log";
        File logFile = new File(crashDir, fileName);

        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理旧日志，只保留最近 MAX_LOG_FILES 个
     */
    private void cleanOldLogs() {
        File crashDir = context.getExternalFilesDir(CRASH_DIR);
        if (crashDir == null) {
            crashDir = new File(context.getFilesDir(), CRASH_DIR);
        }

        if (!crashDir.exists()) return;

        File[] files = crashDir.listFiles();
        if (files == null || files.length <= MAX_LOG_FILES) return;

        // 按修改时间排序
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        // 删除多余的旧文件
        for (int i = MAX_LOG_FILES; i < files.length; i++) {
            files[i].delete();
        }
    }

    /**
     * 获取日志目录路径（供外部查询使用）
     */
    public File getCrashDir() {
        File dir = context.getExternalFilesDir(CRASH_DIR);
        if (dir == null) {
            dir = new File(context.getFilesDir(), CRASH_DIR);
        }
        return dir;
    }
}
