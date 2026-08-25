package com.navi.link.delegate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.transfer.FileTransferServer;
import com.navi.link.transfer.QrCodeGenerator;
import com.navi.link.view.SwitchButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 文件快传面板 Delegate
 * 对应 panel_file_transfer.xml
 * 支持后台持续服务、存储路径实时渲染、运行时一键授权弹窗与智能权限降级适配
 */
public class FileTransferPanelDelegate implements FileTransferServer.OnFileReceivedListener {

    private static final String TAG = "FileTransferDelegate";
    private static final String KEY_SERVER_ENABLED = "file_transfer_server_enabled";
    private static final long POLL_INTERVAL_MS = 1500L;
    private static final int[] SUPPORTED_PORTS = new int[]{8888, 8080, 8090, 12345, 9999};

    private final MainActivity activity;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Set<String> mDisplayedFilePaths = new HashSet<>();

    private SwitchButton mSwServerEnable;
    private TextView mTvServerStatus;
    private ImageView mIvQrCode;
    private TextView mTvIpAddress;
    private TextView mTvNetworkHint;
    private LinearLayout mLayoutFileList;
    private TextView mTvEmptyFileHint;
    private TextView mBtnClearAllFiles;
    private TextView mBtnSwitchPort;
    private TextView mTvStoragePathHint;
    private FileTransferServer mServer;

    private boolean mIsServerEnabled = true;
    private boolean mHasPromptedPermission = false;

    private final Runnable mPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (activity != null && !activity.isFinishing()) {
                loadExistingFiles();
                mMainHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    public FileTransferPanelDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        mSwServerEnable = activity.findViewById(R.id.sw_transfer_server_enable);
        mTvServerStatus = activity.findViewById(R.id.tv_transfer_server_status);
        mIvQrCode = activity.findViewById(R.id.iv_transfer_qr_code);
        mTvIpAddress = activity.findViewById(R.id.tv_transfer_ip_address);
        mTvNetworkHint = activity.findViewById(R.id.tv_transfer_network_hint);
        mLayoutFileList = activity.findViewById(R.id.layout_transfer_file_list);
        mTvEmptyFileHint = activity.findViewById(R.id.tv_transfer_empty_hint);
        mBtnClearAllFiles = activity.findViewById(R.id.btn_transfer_clear_all);
        mBtnSwitchPort = activity.findViewById(R.id.btn_transfer_switch_port);
        mTvStoragePathHint = activity.findViewById(R.id.tv_transfer_storage_path_hint);

        mServer = FileTransferServer.getInstance(activity);
    }

    public void setupListeners() {
        if (mSwServerEnable != null) {
            mSwServerEnable.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
                    mIsServerEnabled = isChecked;
                    saveServerEnabledPref(isChecked);
                    if (isChecked) {
                        if (mServer != null) {
                            mServer.setOnFileReceivedListener(FileTransferPanelDelegate.this);
                            mServer.start();
                        }
                    } else {
                        if (mServer != null) {
                            mServer.setOnFileReceivedListener(null);
                            mServer.stop();
                        }
                    }
                    updateNetworkAndQrCode();
                }
            });
        }
        if (mBtnClearAllFiles != null) {
            mBtnClearAllFiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { confirmClearAllFiles(); }
            });
        }
        if (mBtnSwitchPort != null) {
            mBtnSwitchPort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { showPortSelectDialog(); }
            });
        }
        if (mTvStoragePathHint != null) {
            mTvStoragePathHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { checkAndRequestStoragePermission(true); }
            });
        }
    }

    public void loadSettings() {
        SharedPreferences sp = activity.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        mIsServerEnabled = sp.getBoolean(KEY_SERVER_ENABLED, true);
        if (mSwServerEnable != null) {
            mSwServerEnable.setChecked(mIsServerEnabled);
        }
    }

    private void saveServerEnabledPref(boolean enabled) {
        if (activity != null) {
            activity.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_SERVER_ENABLED, enabled)
                    .apply();
        }
    }

    public void onResume() {
        loadSettings();
        checkAndRequestStoragePermission(false);

        if (mIsServerEnabled && mServer != null) {
            mServer.setOnFileReceivedListener(this);
            mServer.start();
        }
        updateNetworkAndQrCode();
        mDisplayedFilePaths.clear();
        if (mLayoutFileList != null) mLayoutFileList.removeAllViews();
        loadExistingFiles();
        mMainHandler.removeCallbacks(mPollRunnable);
        mMainHandler.postDelayed(mPollRunnable, POLL_INTERVAL_MS);
    }

    public void onPause() {
        mMainHandler.removeCallbacks(mPollRunnable);
        if (mServer != null) {
            mServer.setOnFileReceivedListener(null);
        }
    }

    public void updateThemeColors() {
    }

    public void checkAndRequestStoragePermission(boolean forcePrompt) {
        if (activity == null || activity.isFinishing()) return;

        // 检查 SD 卡公共目录是否可写
        File pubDir = new File(Environment.getExternalStorageDirectory(), "NaviLink/Transfers");
        boolean canWritePubDir = false;
        try {
            if (!pubDir.exists()) pubDir.mkdirs();
            if (pubDir.exists()) {
                File testFile = new File(pubDir, ".permission_test_" + System.currentTimeMillis());
                if (testFile.createNewFile()) {
                    testFile.delete();
                    canWritePubDir = true;
                }
            }
        } catch (Exception ignored) {}

        if (canWritePubDir) return; // 已经拥有 SD 卡根目录写权限

        if (!forcePrompt && mHasPromptedPermission) return;
        mHasPromptedPermission = true;

        // Android 11+ (API 30+) 所有文件访问权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(activity)
                        .setTitle("授权存储权限")
                        .setMessage("为了直接将快传文件保存至 SD 卡根目录 (/sdcard/NaviLink/Transfers/)，请允许“所有文件访问权限”。")
                        .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    try {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                        activity.startActivity(intent);
                                    } catch (Exception ignored) {}
                                }
                            }
                        })
                        .setNegativeButton("暂不开启 (使用专属目录)", null)
                        .show();
                return;
            }
        }

        // Android 6.0 ~ 10 动态运行时权限弹窗 (系统原生 [拒绝 / 允许] 弹窗)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 200);
            }
        }
    }

    private void showPortSelectDialog() {
        if (activity == null) return;
        final String[] items = new String[SUPPORTED_PORTS.length];
        int currentPort = mServer != null ? mServer.getBoundPort() : 8888;
        int checkedItem = 0;
        for (int i = 0; i < SUPPORTED_PORTS.length; i++) {
            items[i] = "端口 " + SUPPORTED_PORTS[i] + (SUPPORTED_PORTS[i] == currentPort ? " (当前使用)" : "");
            if (SUPPORTED_PORTS[i] == currentPort) checkedItem = i;
        }

        new AlertDialog.Builder(activity)
                .setTitle("选择快传服务端口")
                .setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedPort = SUPPORTED_PORTS[which];
                        dialog.dismiss();
                        switchServerPort(selectedPort);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void switchServerPort(int port) {
        if (mServer != null && mIsServerEnabled) {
            mServer.stop();
            mServer.setCustomPort(port);
            boolean success = mServer.start();
            if (success) {
                Toast.makeText(activity, "已切换至端口: " + mServer.getBoundPort(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "端口 " + port + " 绑定失败", Toast.LENGTH_SHORT).show();
            }
            updateNetworkAndQrCode();
        }
    }

    private void updateNetworkAndQrCode() {
        File activeSaveDir = FileTransferServer.getSaveDirectory(activity);
        if (mTvStoragePathHint != null && activeSaveDir != null) {
            mTvStoragePathHint.setText("\uD83D\uDCA1 文件保存目录：" + activeSaveDir.getAbsolutePath() + " (点击可授权SD卡权限)");
        }

        boolean isRunning = mServer != null && mServer.isRunning() && mIsServerEnabled;

        if (mTvServerStatus != null) {
            if (isRunning) {
                mTvServerStatus.setText("● 后台服务运行中 (切换应用不中断)");
                mTvServerStatus.setTextColor(0xFF4CAF50);
            } else {
                mTvServerStatus.setText("○ 服务已停止");
                mTvServerStatus.setTextColor(0xFFFF9800);
            }
        }

        if (!isRunning) {
            if (mTvIpAddress != null) mTvIpAddress.setText("服务已关闭");
            if (mTvNetworkHint != null) mTvNetworkHint.setText("请开启上方开关启动文件快传服务");
            if (mIvQrCode != null) {
                mIvQrCode.setImageBitmap(QrCodeGenerator.generate("", 300, 300));
            }
            return;
        }

        String ip = FileTransferServer.getLocalIpAddress();
        int port = mServer != null ? mServer.getBoundPort() : FileTransferServer.DEFAULT_PORT;
        boolean connected = ip != null && !ip.isEmpty() && !ip.equals("127.0.0.1");

        List<String> allIps = FileTransferServer.getAllLocalIpAddresses();
        StringBuilder urlBuilder = new StringBuilder();
        if (connected) {
            urlBuilder.append("http://").append(ip).append(":").append(port);
            if (allIps.size() > 1) {
                urlBuilder.append("\n(备用IP: ").append(allIps.get(1)).append(":").append(port).append(")");
            }
        } else {
            urlBuilder.append("未连接局域网");
        }

        String qrUrl = connected ? "http://" + ip + ":" + port : "http://127.0.0.1";

        if (mTvIpAddress != null) mTvIpAddress.setText(urlBuilder.toString());
        if (mTvNetworkHint != null) {
            mTvNetworkHint.setText(connected
                    ? "\uD83D\uDCF1 手机与车机同网络，扫码打开网页上传文件 [端口 " + port + "]"
                    : "\u26A0\uFE0F 请连接手机热点或连入车载 Wi-Fi");
        }
        if (mIvQrCode != null) {
            Bitmap qr = QrCodeGenerator.generate(qrUrl, 300, 300);
            mIvQrCode.setImageBitmap(qr);
        }
    }

    private synchronized void loadExistingFiles() {
        if (activity == null || mLayoutFileList == null) return;

        File activeDir = FileTransferServer.getSaveDirectory(activity);
        scanDirectoryFiles(activeDir);

        if (activity != null) {
            File extAppDir = activity.getExternalFilesDir("Transfers");
            if (extAppDir != null && !extAppDir.equals(activeDir)) {
                scanDirectoryFiles(extAppDir);
            }
        }

        if (!mDisplayedFilePaths.isEmpty() && mTvEmptyFileHint != null) {
            mTvEmptyFileHint.setVisibility(View.GONE);
        } else if (mDisplayedFilePaths.isEmpty() && mTvEmptyFileHint != null) {
            mTvEmptyFileHint.setVisibility(View.VISIBLE);
        }
    }

    private void scanDirectoryFiles(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return;

        for (File f : files) {
            if (!f.isFile() || f.getName().endsWith(".tmp") || f.getName().startsWith(".")) continue;
            String path = f.getAbsolutePath();
            View existingView = mLayoutFileList.findViewWithTag(path);
            String category = FileTransferServer.getFileCategory(f.getName());
            String timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(f.lastModified()));
            String sizeStr = formatFileSize(f.length());

            if (existingView != null) {
                TextView tv2 = existingView.findViewById(android.R.id.text2);
                if (tv2 != null) tv2.setText("[" + category + "] " + sizeStr + "  -  " + timeStr);
            } else {
                mDisplayedFilePaths.add(path);
                addFileItemView(f, category);
            }
        }
    }

    @Override
    public void onFileReceived(final File file, final String category) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (activity == null || activity.isFinishing()) return;
                loadExistingFiles();
                Toast.makeText(activity,
                        "收到新文件: " + file.getName() + " (" + formatFileSize(file.length()) + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFileItemView(final File file, String category) {
        if (activity == null) return;
        float den = activity.getResources().getDisplayMetrics().density;
        int pH = (int) (12 * den);
        int pV = (int) (10 * den);

        int textPrimary = getThemeColor(R.attr.panelTextColorPrimary, 0xFF1A1A1A);
        int textSecondary = getThemeColor(R.attr.panelTextColorSecondary, 0xFF666666);
        int itemBgColor = getThemeColor(R.attr.panelCardBgColor, 0xFFF5F6F8);
        int borderColor = getThemeColor(R.attr.panelDividerColor, 0xFFE0E0E0);

        LinearLayout itemView = new LinearLayout(activity);
        itemView.setTag(file.getAbsolutePath());
        itemView.setOrientation(LinearLayout.HORIZONTAL);
        itemView.setGravity(Gravity.CENTER_VERTICAL);
        itemView.setPadding(pH, pV, pH, pV);

        LinearLayout leftLayout = new LinearLayout(activity);
        leftLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        leftLayout.setLayoutParams(leftLp);

        TextView tv1 = new TextView(activity);
        tv1.setId(android.R.id.text1);
        tv1.setText(file.getName());
        tv1.setTextColor(textPrimary);
        tv1.setTextSize(13);
        tv1.setSingleLine(true);
        tv1.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);

        String timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(file.lastModified()));
        String sizeStr = formatFileSize(file.length());
        TextView tv2 = new TextView(activity);
        tv2.setId(android.R.id.text2);
        tv2.setText("[" + category + "] " + sizeStr + "  -  " + timeStr);
        tv2.setTextColor(textSecondary);
        tv2.setTextSize(11);
        tv2.setPadding(0, (int) (2 * den), 0, 0);

        leftLayout.addView(tv1);
        leftLayout.addView(tv2);

        TextView btnDelete = new TextView(activity);
        btnDelete.setTag("btn_delete_file");
        btnDelete.setText("\u5220\u9664");
        btnDelete.setTextColor(0xFFFF4D4F);
        btnDelete.setTextSize(12);
        btnDelete.setPadding((int) (10 * den), (int) (6 * den), (int) (10 * den), (int) (6 * den));
        btnDelete.setClickable(true);
        btnDelete.setFocusable(true);
        GradientDrawable delBg = new GradientDrawable();
        delBg.setShape(GradientDrawable.RECTANGLE);
        delBg.setCornerRadius(6 * den);
        delBg.setColor(0x1AFF4D4F);
        btnDelete.setBackground(delBg);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { confirmDeleteFile(file, itemView); }
        });

        itemView.addView(leftLayout);
        itemView.addView(btnDelete);

        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setShape(GradientDrawable.RECTANGLE);
        itemBg.setCornerRadius(8 * den);
        itemBg.setColor(itemBgColor);
        itemBg.setStroke((int) (1 * den), borderColor);
        itemView.setBackground(itemBg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, (int) (8 * den));
        itemView.setLayoutParams(lp);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { handleFileClick(file); }
        });

        mLayoutFileList.addView(itemView, 0);
    }

    private void confirmDeleteFile(final File file, final View itemView) {
        if (activity == null) return;
        new AlertDialog.Builder(activity)
                .setTitle("\u786e\u8ba4\u5220\u9664")
                .setMessage("\u786e\u5b9a\u8981\u5220\u9664\u6587\u4ef6 " + file.getName() + " \u5417\uff1f")
                .setPositiveButton("\u5220\u9664", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.exists() && file.delete()) {
                            mDisplayedFilePaths.remove(file.getAbsolutePath());
                            mLayoutFileList.removeView(itemView);
                            if (mDisplayedFilePaths.isEmpty() && mTvEmptyFileHint != null) {
                                mTvEmptyFileHint.setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(activity, "\u5df2\u5220\u9664: " + file.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "\u5220\u9664\u5931\u8d25", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("\u53d6\u6d88", null)
                .show();
    }

    private void confirmClearAllFiles() {
        if (activity == null) return;
        new AlertDialog.Builder(activity)
                .setTitle("\u6e05\u7a7a\u6240\u6709\u6587\u4ef6")
                .setMessage("\u786e\u5b9a\u8981\u6e05\u7a7a\u5feb\u4f20\u63a5\u6536\u7684\u6240\u6709\u6587\u4ef6\u5417\uff1f\u6b64\u64cd\u4f5c\u4e0d\u53ef\u6062\u590d\u3002")
                .setPositiveButton("\u5168\u90e8\u6e05\u7a7a", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File activeDir = FileTransferServer.getSaveDirectory(activity);
                        File extAppDir = activity.getExternalFilesDir("Transfers");
                        int count = 0;
                        if (activeDir != null && activeDir.exists()) {
                            File[] files = activeDir.listFiles();
                            if (files != null) {
                                for (File f : files) { if (f.isFile() && f.delete()) count++; }
                            }
                        }
                        if (extAppDir != null && extAppDir.exists() && !extAppDir.equals(activeDir)) {
                            File[] files = extAppDir.listFiles();
                            if (files != null) {
                                for (File f : files) { if (f.isFile() && f.delete()) count++; }
                            }
                        }
                        mDisplayedFilePaths.clear();
                        if (mLayoutFileList != null) mLayoutFileList.removeAllViews();
                        if (mTvEmptyFileHint != null) mTvEmptyFileHint.setVisibility(View.VISIBLE);
                        Toast.makeText(activity, "\u5df2\u6e05\u7a7a " + count + " \u4e2a\u5feb\u4f20\u6587\u4ef6", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("\u53d6\u6d88", null)
                .show();
    }

    private void handleFileClick(File file) {
        if (!file.exists() || activity == null) return;
        String name = file.getName().toLowerCase(Locale.ROOT);
        try {
            try {
                java.lang.reflect.Method m = android.os.StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception ignored) {}
            Uri fileUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (name.endsWith(".apk")) {
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                Toast.makeText(activity, "\u6b63\u5728\u8c03\u8d77\u5b89\u88c5...", Toast.LENGTH_SHORT).show();
            } else if (name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".wav")
                    || name.endsWith(".aac") || name.endsWith(".m4a")) {
                intent.setDataAndType(fileUri, "audio/*");
            } else if (name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi")
                    || name.endsWith(".mov")) {
                intent.setDataAndType(fileUri, "video/*");
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                    || name.endsWith(".webp")) {
                intent.setDataAndType(fileUri, "image/*");
            } else {
                Toast.makeText(activity, "\u5df2\u4fdd\u5b58\u5230: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return;
            }
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "\u6253\u5f00\u6587\u4ef6\u5931\u8d25: " + file.getName(), e);
            Toast.makeText(activity, "\u5df2\u4fdd\u5b58\u5230: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    private int getThemeColor(int attrResId, int fallback) {
        try {
            android.util.TypedValue tv = new android.util.TypedValue();
            if (activity.getTheme().resolveAttribute(attrResId, tv, true)) return tv.data;
        } catch (Exception ignored) {}
        return fallback;
    }

    private static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int d = (int) (Math.log10(size) / Math.log10(1024));
        if (d >= units.length) d = units.length - 1;
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, d), units[d]);
    }
}