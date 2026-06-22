package com.navi.link;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.appcompat.widget.SwitchCompat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.text.TextUtils;
import java.util.List;
import java.util.ArrayList;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_IS_MINIMAL = "is_minimal_style";
    private static final String KEY_STYLE_MODE = "style_mode";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_IS_SERVICE_ONLY = "is_service_only";
    public static final String PREFS_NAME = "floating_config";
    private static final int REQUEST_OVERLAY_PERMISSION = 100;

    private static final int[] THEME_COLORS = {
            0xFF1A1A1A,  // 黑色
            0xFF1199FF,  // 蓝
            0xFF4FC3F7,  // 浅蓝
            0xFFFF9100,  // 橙
            0xFFFF4081,  // 粉红
            0xFFAB47BC,  // 紫
            0xFFFF6D00,  // 深橙
            0xFF6DFF00,  // 青绿
    };

    private MaterialCardView cardMinimal;
    private MaterialCardView cardNormal;
    private MaterialCardView cardFull;
    private MaterialCardView cardServiceOnly;
    private MaterialCardView cardNormalStart;
    private MaterialCardView cardBgDark;
    private MaterialCardView cardBgSemi;
    private MaterialCardView cardBgTransparent;
    private LinearLayout llThemeColors;
    private RadioButton rbMinimal;
    private RadioButton rbNormal;
    private RadioButton rbFull;
    private RadioButton rbServiceOnly;
    private RadioButton rbNormalStart;
    private RadioButton rbBgDark;
    private RadioButton rbBgSemi;
    private RadioButton rbBgTransparent;
    private MaterialCardView cardStartAmap;
    private RadioButton rbStartAmap;
    private TextView tvStartAmapDesc;
    private SeekBar sbScale;
    private View[] themeChips;
    private TextView tvScaleValue;
    private TextView tvStatus;
    private SwitchCompat cbCruiseEnabled;
    private TextView tvCruiseStatus;
    private MaterialCardView cardCruiseToggle;
    private SwitchCompat cbNormalLaneEnabled;
    private TextView tvNormalLaneStatus;
    private MaterialCardView cardNormalLaneToggle;
    private SwitchCompat cbAvoidForegroundEnabled;
    private TextView tvAvoidForegroundStatus;
    private MaterialCardView cardAvoidForegroundToggle;
    private TextView tvSys;
    private TextView tvStyle;
    private TextView tvOperation;
    private SwitchCompat cbOverspeedWarningEnabled;
    private TextView tvOverspeedWarningStatus;
    private MaterialCardView cardOverspeedWarningToggle;
    private SwitchCompat cbMinimalCameraEnabled;
    private TextView tvMinimalCameraStatus;
    private MaterialCardView cardMinimalCameraToggle;
    private MaterialCardView cardClusterMirrorToggle;
    private SwitchCompat cbClusterMirrorEnabled;
    private TextView tvClusterMirrorStatus;
    private MaterialCardView cardClusterDisplaySelect;
    private TextView tvClusterDisplaySelectStatus;
    private TextView btnAdjustClusterPos;

    private boolean isMinimalStyle = false;
    private int styleMode = 0;
    private boolean isServiceOnlyMode = false;
    private int startupMode = 0; // 0=正常, 1=纯服务, 2=启动高德地图
    private String targetAmapPackage = "";
    private int backgroundMode = 0; // 0=深色, 1=半透明, 2=全透明
    private boolean cruiseEnabled = true;
    private boolean normalLaneEnabled = false;
    private boolean avoidForegroundEnabled = false;
    private boolean overspeedWarningEnabled = true;
    private boolean minimalCameraEnabled = false;
    private boolean clusterMirrorEnabled = false;
    private int clusterDisplayId = -1;

    private int themeColor = 0xFF4FC3F7;

    private boolean isDarkColor(int color) {
        return ((color >> 16) & 0xFF) * 0.299
                + ((color >> 8) & 0xFF) * 0.587
                + (color & 0xFF) * 0.114 < 100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        loadPreferences();
        setupListeners();
        updateStatusText();
        if (savedInstanceState == null) {
            checkPermissionAndStart();
        }
    }

    private void initViews() {
        cardNormal = findViewById(R.id.card_normal);
        cardMinimal = findViewById(R.id.card_minimal);
        cardFull = findViewById(R.id.card_full);
        cardServiceOnly = findViewById(R.id.card_service_only);
        cardNormalStart = findViewById(R.id.card_normal_start);
        cardBgDark = findViewById(R.id.card_bg_dark);
        cardBgSemi = findViewById(R.id.card_bg_semi);
        cardBgTransparent = findViewById(R.id.card_bg_transparent);
        rbNormal = findViewById(R.id.rb_normal);
        rbMinimal = findViewById(R.id.rb_minimal);
        rbFull = findViewById(R.id.rb_full);
        rbServiceOnly = findViewById(R.id.rb_service_only);
        rbNormalStart = findViewById(R.id.rb_normal_start);
        rbBgDark = findViewById(R.id.rb_bg_dark);
        rbBgSemi = findViewById(R.id.rb_bg_semi);
        rbBgTransparent = findViewById(R.id.rb_bg_transparent);
        cardStartAmap = findViewById(R.id.card_start_amap);
        rbStartAmap = findViewById(R.id.rb_start_amap);
        tvStartAmapDesc = findViewById(R.id.tv_start_amap_desc);
        sbScale = findViewById(R.id.sb_scale);
        tvScaleValue = findViewById(R.id.tv_scale_value);
        tvStatus = findViewById(R.id.tv_status);
        cbCruiseEnabled = findViewById(R.id.cb_cruise_enabled);
        tvCruiseStatus = findViewById(R.id.tv_cruise_status);
        cardCruiseToggle = findViewById(R.id.card_cruise_toggle);
        cbNormalLaneEnabled = findViewById(R.id.cb_normal_lane_enabled);
        tvNormalLaneStatus = findViewById(R.id.tv_normal_lane_status);
        cardNormalLaneToggle = findViewById(R.id.card_normal_lane_toggle);
        cbAvoidForegroundEnabled = findViewById(R.id.cb_avoid_foreground_enabled);
        tvAvoidForegroundStatus = findViewById(R.id.tv_avoid_foreground_status);
        cardAvoidForegroundToggle = findViewById(R.id.card_avoid_foreground_toggle);
        cbOverspeedWarningEnabled = findViewById(R.id.cb_overspeed_warning_enabled);
        tvOverspeedWarningStatus = findViewById(R.id.tv_overspeed_warning_status);
        cardOverspeedWarningToggle = findViewById(R.id.card_overspeed_warning_toggle);
        cbMinimalCameraEnabled = findViewById(R.id.cb_minimal_camera_enabled);
        tvMinimalCameraStatus = findViewById(R.id.tv_minimal_camera_status);
        cardMinimalCameraToggle = findViewById(R.id.card_minimal_camera_toggle);
        cbClusterMirrorEnabled = findViewById(R.id.cb_cluster_mirror_enabled);
        tvClusterMirrorStatus = findViewById(R.id.tv_cluster_mirror_status);
        cardClusterMirrorToggle = findViewById(R.id.card_cluster_mirror_toggle);
        cardClusterDisplaySelect = findViewById(R.id.card_cluster_display_select);
        tvClusterDisplaySelectStatus = findViewById(R.id.tv_cluster_display_select_status);
        btnAdjustClusterPos = findViewById(R.id.btn_adjust_cluster_pos);
        llThemeColors = findViewById(R.id.ll_theme_colors);
        tvSys = findViewById(R.id.tv_sys);
        tvStyle = findViewById(R.id.tv_style);
        tvOperation = findViewById(R.id.tv_operation);
        android.view.ViewGroup contentView = findViewById(android.R.id.content);
        View root = contentView.getChildAt(0);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsetsCompat) -> {
                Insets insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsetsCompat;
            });
        }

        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);
    }

    private void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isMinimalStyle = sp.getBoolean(KEY_IS_MINIMAL, false);
        styleMode = sp.getInt(KEY_STYLE_MODE, isMinimalStyle ? 1 : 0);
        themeColor = sp.getInt(KEY_THEME_COLOR, 0xFF4FC3F7);
        isServiceOnlyMode = sp.getBoolean(KEY_IS_SERVICE_ONLY, false);
        startupMode = sp.getInt("startup_mode", isServiceOnlyMode ? 1 : 0);
        targetAmapPackage = sp.getString("target_amap_package", "");
        if (startupMode == 2 && !TextUtils.isEmpty(targetAmapPackage)) {
            if (tvStartAmapDesc != null) {
                tvStartAmapDesc.setText("已选: " + targetAmapPackage);
            }
        }
        backgroundMode = sp.getInt("background_mode", 0);
        cruiseEnabled = sp.getBoolean("cruise_enabled", true);
        normalLaneEnabled = sp.getBoolean("normal_navi_lane_enabled", false);
        avoidForegroundEnabled = sp.getBoolean("hide_on_amap_foreground", false);
        overspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
        minimalCameraEnabled = sp.getBoolean("minimal_camera_enabled", false);
        clusterMirrorEnabled = sp.getBoolean("cluster_mirror_enabled", false);
        clusterDisplayId = sp.getInt("cluster_display_id", -1);
 
        updateSeekBarToCurrentScale();
        
        if (cbNormalLaneEnabled != null) {
            cbNormalLaneEnabled.setChecked(normalLaneEnabled);
        }
        if (tvNormalLaneStatus != null) {
            tvNormalLaneStatus.setText(normalLaneEnabled ? "车道线已启用" : "车道线已禁用");
        }
        if (cbAvoidForegroundEnabled != null) {
            cbAvoidForegroundEnabled.setChecked(avoidForegroundEnabled);
        }
        if (tvAvoidForegroundStatus != null) {
            tvAvoidForegroundStatus.setText(avoidForegroundEnabled ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
        }
        if (cbOverspeedWarningEnabled != null) {
            cbOverspeedWarningEnabled.setChecked(overspeedWarningEnabled);
        }
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        if (cbMinimalCameraEnabled != null) {
            cbMinimalCameraEnabled.setChecked(minimalCameraEnabled);
        }
        if (tvMinimalCameraStatus != null) {
            tvMinimalCameraStatus.setText(minimalCameraEnabled ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
        }
        if (cbClusterMirrorEnabled != null) {
            cbClusterMirrorEnabled.setChecked(clusterMirrorEnabled);
        }
        if (tvClusterMirrorStatus != null) {
            tvClusterMirrorStatus.setText(clusterMirrorEnabled ? "已启用副屏镜像投屏" : "已禁用副屏投屏");
        }
        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setVisibility(clusterMirrorEnabled ? View.VISIBLE : View.GONE);
        }
        updateClusterDisplaySelectStatus();

        applyThemeToViews();

        initThemeColorChips();
    }

    private void selectStartupMode(int mode) {
        if (mode == 2) {
            if (startupMode == 2 || TextUtils.isEmpty(targetAmapPackage)) {
                showAmapSelectionDialog();
            } else {
                setStartupMode(2);
            }
            return;
        }
        setStartupMode(mode);
    }

    private void setStartupMode(int mode) {
        if (startupMode == mode) return;
        startupMode = mode;
        updateStartupSelection();
        savePreferences();
    }

    private void showAmapSelectionDialog() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> amapApps = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            if (app.packageName != null && app.packageName.contains("com.autonavi")) {
                amapApps.add(app);
            }
        }

        if (amapApps.isEmpty()) {
            Toast.makeText(this, "未找到已安装的高德地图应用", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<ApplicationInfo> adapter = new ArrayAdapter<ApplicationInfo>(this, R.layout.item_app_list, amapApps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app_list, parent, false);
                }
                
                ApplicationInfo appInfo = getItem(position);
                ImageView icon = convertView.findViewById(R.id.iv_app_icon);
                TextView name = convertView.findViewById(R.id.tv_app_name);
                TextView pkg = convertView.findViewById(R.id.tv_app_package);
                
                if (appInfo != null) {
                    icon.setImageDrawable(appInfo.loadIcon(pm));
                    name.setText(appInfo.loadLabel(pm).toString());
                    pkg.setText(appInfo.packageName);
                }
                
                return convertView;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("选择高德地图应用")
                .setAdapter(adapter, (dialog, which) -> {
                    targetAmapPackage = amapApps.get(which).packageName;
                    if (tvStartAmapDesc != null) {
                        tvStartAmapDesc.setText("已选: " + targetAmapPackage);
                    }
                    setStartupMode(2);
                })
                .show();
    }

    private void updateStartupSelection() {
        rbNormalStart.setChecked(startupMode == 0);
        rbServiceOnly.setChecked(startupMode == 1);
        if (rbStartAmap != null) rbStartAmap.setChecked(startupMode == 2);
        int accentColor = getAccentColor();
        cardNormalStart.setStrokeColor(startupMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardServiceOnly.setStrokeColor(startupMode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardStartAmap != null) cardStartAmap.setStrokeColor(startupMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    private void selectStyle(int mode) {
        if (styleMode == mode) return;
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager == null || !manager.isActive()) {
            Toast.makeText(this, "悬浮窗未收到导航数据，无法切换样式", Toast.LENGTH_SHORT).show();
            return;
        }
        styleMode = mode;
        isMinimalStyle = (mode == 1);
        updateStyleSelection();
        updateSeekBarToCurrentScale();
        savePreferences();
        updateFloatingWindowStyle();
    }

    private void updateStyleSelection() {
        rbNormal.setChecked(styleMode == 0);
        rbMinimal.setChecked(styleMode == 1);
        rbFull.setChecked(styleMode == 2);
        int accentColor = getAccentColor();
        cardNormal.setStrokeColor(styleMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardMinimal.setStrokeColor(styleMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardFull.setStrokeColor(styleMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    private void selectBackgroundMode(int mode) {
        if (backgroundMode == mode) return;
        backgroundMode = mode;
        updateBackgroundModeSelection();
        savePreferences();
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.setBackgroundMode(mode);
        }
    }

    private void updateBackgroundModeSelection() {
        rbBgDark.setChecked(backgroundMode == 0);
        rbBgSemi.setChecked(backgroundMode == 1);
        rbBgTransparent.setChecked(backgroundMode == 2);
        int accentColor = getAccentColor();
        cardBgDark.setStrokeColor(backgroundMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardBgSemi.setStrokeColor(backgroundMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardBgTransparent.setStrokeColor(backgroundMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    private void savePreferences() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_IS_MINIMAL, isMinimalStyle)
                .putInt(KEY_STYLE_MODE, styleMode)
                .putInt(KEY_THEME_COLOR, themeColor)
                .putBoolean(KEY_IS_SERVICE_ONLY, startupMode == 1)
                .putInt("startup_mode", startupMode)
                .putString("target_amap_package", targetAmapPackage)
                .putInt("background_mode", backgroundMode)
                .putBoolean("cruise_enabled", cruiseEnabled)
                .putBoolean("normal_navi_lane_enabled", normalLaneEnabled)
                .putBoolean("hide_on_amap_foreground", avoidForegroundEnabled)
                .putBoolean("overspeed_warning_enabled", overspeedWarningEnabled)
                .putBoolean("minimal_camera_enabled", minimalCameraEnabled)
                .putBoolean("cluster_mirror_enabled", clusterMirrorEnabled)
                .putInt("cluster_display_id", clusterDisplayId)
                .apply();
    }

    private void initThemeColorChips() {
        llThemeColors.removeAllViews();
        int sizePx = dpToPx(36);
        int marginPx = dpToPx(6);
        themeChips = new View[THEME_COLORS.length];

        for (int i = 0; i < THEME_COLORS.length; i++) {
            int color = THEME_COLORS[i];
            View chip = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            chip.setLayoutParams(lp);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(dpToPx(2), color == themeColor ? Color.WHITE : 0x33FFFFFF);
            chip.setBackground(drawable);

            chip.setClickable(true);
            chip.setFocusable(true);
            final int index = i;
            chip.setOnClickListener(v -> selectThemeColor(index));

            llThemeColors.addView(chip);
            themeChips[i] = chip;
        }
    }

    private void updateSwitchTheme(SwitchCompat switchView, int activeColor) {
        if (switchView == null) return;
        
        // Thumb ColorStateList:
        // Checked state: activeColor
        // Unchecked state: light white/grey (#FFD0D0D0)
        int[][] thumbStates = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[] { -android.R.attr.state_checked }
        };
        int[] thumbColors = new int[] {
            activeColor,
            Color.parseColor("#FFD0D0D0")
        };
        switchView.setThumbTintList(new ColorStateList(thumbStates, thumbColors));

        // Track ColorStateList:
        // Checked state: activeColor with 40% opacity (0x66 alpha)
        // Unchecked state: grey (#FF555555)
        int[][] trackStates = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[] { -android.R.attr.state_checked }
        };
        int trackActiveColor = Color.argb(
            0x66,
            Color.red(activeColor),
            Color.green(activeColor),
            Color.blue(activeColor)
        );
        int[] trackColors = new int[] {
            trackActiveColor,
            Color.parseColor("#FF555555")
        };
        switchView.setTrackTintList(new ColorStateList(trackStates, trackColors));
    }

    private void applyThemeToViews() {
        int accentColor = getAccentColor();
        ColorStateList accentColorStateList = ColorStateList.valueOf(accentColor);

        // 更新选择项卡片的描边颜色
        updateStartupSelection();
        updateStyleSelection();
        updateBackgroundModeSelection();

        // 单选按钮（RadioButton）的着色
        if (rbStartAmap != null) rbStartAmap.setButtonTintList(accentColorStateList);

        // 更新单选按钮（RadioButton）的着色
        rbNormal.setButtonTintList(accentColorStateList);
        rbMinimal.setButtonTintList(accentColorStateList);
        rbFull.setButtonTintList(accentColorStateList);
        rbServiceOnly.setButtonTintList(accentColorStateList);
        rbNormalStart.setButtonTintList(accentColorStateList);
        rbBgDark.setButtonTintList(accentColorStateList);
        rbBgSemi.setButtonTintList(accentColorStateList);
        rbBgTransparent.setButtonTintList(accentColorStateList);

        // 更新开关（SwitchCompat）的主题颜色
        updateSwitchTheme(cbCruiseEnabled, accentColor);
        updateSwitchTheme(cbNormalLaneEnabled, accentColor);
        updateSwitchTheme(cbAvoidForegroundEnabled, accentColor);
        updateSwitchTheme(cbOverspeedWarningEnabled, accentColor);
        updateSwitchTheme(cbMinimalCameraEnabled, accentColor);
        updateSwitchTheme(cbClusterMirrorEnabled, accentColor);

        // 更新 SeekBar 与文本颜色
        sbScale.setProgressTintList(accentColorStateList);
        sbScale.setThumbTintList(accentColorStateList);
        tvScaleValue.setTextColor(accentColor);

        tvStyle.setTextColor(accentColor);
        tvSys.setTextColor(accentColor);
        tvOperation.setTextColor(accentColor);

        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setTextColor(accentColor);
        }

        MaterialCardView btnExitApp = findViewById(R.id.btn_exit_app);
        if (btnExitApp != null) {
            btnExitApp.setCardBackgroundColor(themeColor);
        }
        MaterialCardView btnHomeApp = findViewById(R.id.btn_home_app);
        if (btnHomeApp != null) {
            btnHomeApp.setCardBackgroundColor(themeColor);
        }

        // 通知悬浮窗管理器更新主题色
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.applyThemeColor(themeColor);
        }
    }

    private void selectThemeColor(int index) {
        int color = THEME_COLORS[index];
        if (themeColor == color) return;
        themeColor = color;
        savePreferences();

        for (int i = 0; i < themeChips.length; i++) {
            GradientDrawable drawable = (GradientDrawable) themeChips[i].getBackground();
            if (drawable != null) {
                drawable.setStroke(dpToPx(2), THEME_COLORS[i] == themeColor ? Color.WHITE : 0x33FFFFFF);
            }
        }

        applyThemeToViews();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int getAccentColor() {
        return isDarkColor(themeColor) ? Color.WHITE : themeColor;
    }

    private void setupListeners() {
        View btnExitApp = findViewById(R.id.btn_exit_app);
        if (btnExitApp != null) {
            btnExitApp.setOnClickListener(v -> {
                stopService(new Intent(MainActivity.this, AutoMapService.class));
                finishAffinity();
                System.exit(0);
            });
        }

        cardServiceOnly.setOnClickListener(v -> selectStartupMode(1));
        cardNormalStart.setOnClickListener(v -> selectStartupMode(0));
        if (cardStartAmap != null) cardStartAmap.setOnClickListener(v -> selectStartupMode(2));
        cardNormal.setOnClickListener(v -> selectStyle(0));
        cardMinimal.setOnClickListener(v -> selectStyle(1));
        cardFull.setOnClickListener(v -> selectStyle(2));
        cardBgDark.setOnClickListener(v -> selectBackgroundMode(0));
        cardBgSemi.setOnClickListener(v -> selectBackgroundMode(1));
        cardBgTransparent.setOnClickListener(v -> selectBackgroundMode(2));
        MaterialCardView btnHomeApp = findViewById(R.id.btn_home_app);
        if (btnHomeApp != null) {
            btnHomeApp.setOnClickListener(v -> moveTaskToBack(true));
        }

        cbCruiseEnabled.setChecked(cruiseEnabled);
        if (tvCruiseStatus != null) tvCruiseStatus.setText(cruiseEnabled ? "巡航窗已启用" : "巡航窗已禁用");
        CompoundButton.OnCheckedChangeListener cruiseListener = (buttonView, isChecked) -> {
            cruiseEnabled = isChecked;
            savePreferences();
            if (tvCruiseStatus != null) tvCruiseStatus.setText(isChecked ? "巡航窗已启用" : "巡航窗已禁用");
            // 直接操作悬浮窗，立即生效
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                if (isChecked) {
                    fwm.show();
                } else if (fwm.getCurrentMode() == FloatingWindowManager.MODE_CRUISE) {
                    fwm.hide();
                }
            }
        };
        cbCruiseEnabled.setOnCheckedChangeListener(cruiseListener);
        if (cardCruiseToggle != null) {
            cardCruiseToggle.setOnClickListener(v -> cbCruiseEnabled.toggle());
        }

        cbNormalLaneEnabled.setChecked(normalLaneEnabled);
        if (tvNormalLaneStatus != null) {
            tvNormalLaneStatus.setText(normalLaneEnabled ? "车道线已启用" : "车道线已禁用");
        }
        CompoundButton.OnCheckedChangeListener normalLaneListener = (buttonView, isChecked) -> {
            normalLaneEnabled = isChecked;
            savePreferences();
            if (tvNormalLaneStatus != null) {
                tvNormalLaneStatus.setText(isChecked ? "车道线已启用" : "车道线已禁用");
            }
            // 立即刷新悬浮窗
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        };
        cbNormalLaneEnabled.setOnCheckedChangeListener(normalLaneListener);
        if (cardNormalLaneToggle != null) {
            cardNormalLaneToggle.setOnClickListener(v -> cbNormalLaneEnabled.toggle());
        }

        cbAvoidForegroundEnabled.setChecked(avoidForegroundEnabled);
        if (tvAvoidForegroundStatus != null) {
            tvAvoidForegroundStatus.setText(avoidForegroundEnabled ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
        }
        CompoundButton.OnCheckedChangeListener avoidForegroundListener = (buttonView, isChecked) -> {
            avoidForegroundEnabled = isChecked;
            savePreferences();
            if (tvAvoidForegroundStatus != null) {
                tvAvoidForegroundStatus.setText(isChecked ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
            }
            // 立即更新悬浮窗可见性
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.updateFloatingWindowVisibility();
            }
        };
        cbAvoidForegroundEnabled.setOnCheckedChangeListener(avoidForegroundListener);
        if (cardAvoidForegroundToggle != null) {
            cardAvoidForegroundToggle.setOnClickListener(v -> cbAvoidForegroundEnabled.toggle());
        }

        cbOverspeedWarningEnabled.setChecked(overspeedWarningEnabled);
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        CompoundButton.OnCheckedChangeListener overspeedWarningListener = (buttonView, isChecked) -> {
            overspeedWarningEnabled = isChecked;
            savePreferences();
            if (tvOverspeedWarningStatus != null) {
                tvOverspeedWarningStatus.setText(isChecked ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
            }
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        };
        cbOverspeedWarningEnabled.setOnCheckedChangeListener(overspeedWarningListener);
        if (cardOverspeedWarningToggle != null) {
            cardOverspeedWarningToggle.setOnClickListener(v -> cbOverspeedWarningEnabled.toggle());
        }

        cbMinimalCameraEnabled.setChecked(minimalCameraEnabled);
        if (tvMinimalCameraStatus != null) {
            tvMinimalCameraStatus.setText(minimalCameraEnabled ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
        }
        CompoundButton.OnCheckedChangeListener minimalCameraListener = (buttonView, isChecked) -> {
            minimalCameraEnabled = isChecked;
            savePreferences();
            if (tvMinimalCameraStatus != null) {
                tvMinimalCameraStatus.setText(isChecked ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
            }
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        };
        cbMinimalCameraEnabled.setOnCheckedChangeListener(minimalCameraListener);
        if (cardMinimalCameraToggle != null) {
            cardMinimalCameraToggle.setOnClickListener(v -> cbMinimalCameraEnabled.toggle());
        }

        cbClusterMirrorEnabled.setChecked(clusterMirrorEnabled);
        if (tvClusterMirrorStatus != null) {
            tvClusterMirrorStatus.setText(clusterMirrorEnabled ? "已启用副屏镜像投屏" : "已禁用副屏投屏");
        }
        CompoundButton.OnCheckedChangeListener clusterMirrorListener = (buttonView, isChecked) -> {
            clusterMirrorEnabled = isChecked;
            savePreferences();
            if (tvClusterMirrorStatus != null) {
                tvClusterMirrorStatus.setText(isChecked ? "已启用副屏镜像投屏" : "已禁用副屏投屏");
            }
            if (btnAdjustClusterPos != null) {
                btnAdjustClusterPos.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.onClusterMirrorConfigChanged();
            }
        };
        cbClusterMirrorEnabled.setOnCheckedChangeListener(clusterMirrorListener);
        if (cardClusterMirrorToggle != null) {
            cardClusterMirrorToggle.setOnClickListener(v -> cbClusterMirrorEnabled.toggle());
        }

        if (cardClusterDisplaySelect != null) {
            cardClusterDisplaySelect.setOnClickListener(v -> showClusterDisplaySelectionDialog());
        }

        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setOnClickListener(v -> {
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm == null || !fwm.isClusterMirrorActive()) {
                    Toast.makeText(MainActivity.this, "副屏投屏未开启，请先开启投屏", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ClusterPositionActivity.class);
                startActivity(intent);
            });
        }

        sbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentScale = (progress / 15.0f) * 1.5f + 0.5f;
                tvScaleValue.setText(String.format("%.1fx", currentScale));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float newScale = (seekBar.getProgress() / 15.0f) * 1.5f + 0.5f;
                FloatingWindowManager manager = FloatingWindowManager.getInstance();
                if (manager != null) {
                    manager.updateScale(newScale);
                }
            }
        });
    }

    private void checkPermissionAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            tvStatus.setText("需要悬浮窗权限");
            startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())), 100);
        } else {
            startFloatingService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && Settings.canDrawOverlays(this)) {
            startFloatingService();
        }
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, AutoMapService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        updateStatusText();
        scheduleStatusRefresh();
    }

    private void updateFloatingWindowStyle() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager == null || !manager.isShowing()) return;
        manager.refreshWindow();
    }

    private void updateFloatingWindowScale() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            float currentScale = (sbScale.getProgress() / 15.0f) * 1.5f + 0.5f;
            manager.updateScale(currentScale);
        }
    }

    /** 切换样式时，把 SeekBar 跳到该样式对应的缩放值 */
    private void updateSeekBarToCurrentScale() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float s;
        String[] keys = {"scale_normal", "scale_minimal", "scale_full"};
        float[] defaults = {1.0f, 1.0f, 1.0f};

        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        int idx;
        if (manager != null && manager.isActive() && manager.getCurrentMode() == FloatingWindowManager.MODE_CRUISE) {
            idx = (styleMode == 1) ? 1 : 0; // 灵动岛巡航用1，常规巡航/全数据用0
        } else {
            idx = Math.max(0, Math.min(styleMode, 2));
        }
        s = sp.getFloat(keys[idx], defaults[idx]);

        sbScale.setProgress((int) (((s - 0.5f) / 1.5f) * 15));
        tvScaleValue.setText(String.format("%.1fx", s));
    }

    private void updateStatusText() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null && manager.isShowing()) {
            tvStatus.setText("● 悬浮窗运行中");
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvStatus.setText("○ 悬浮窗未启动");
            tvStatus.setTextColor(Color.parseColor("#888888"));
        }
    }

    private void scheduleStatusRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(this::updateStatusText, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusText();
        scheduleStatusRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showClusterDisplaySelectionDialog() {
        DisplayManager manager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (manager == null) {
            Toast.makeText(this, "系统显示管理服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        Display[] displays = manager.getDisplays();
        ArrayList<DisplayChoice> choices = new ArrayList<>();
        
        // Add default/auto option
        choices.add(new DisplayChoice(-1, "自动选择 (首个副屏幕)"));
        
        int selectedIndex = 0;
        int currentSelectedId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt("cluster_display_id", -1);
                
        for (Display display : displays) {
            if (display == null || display.getDisplayId() == Display.DEFAULT_DISPLAY) {
                continue;
            }
            String name = display.getName();
            if (TextUtils.isEmpty(name)) {
                name = "副屏幕";
            }
            choices.add(new DisplayChoice(display.getDisplayId(), name + " (ID: " + display.getDisplayId() + ")"));
            if (display.getDisplayId() == currentSelectedId) {
                selectedIndex = choices.size() - 1;
            }
        }
        
        if (choices.size() <= 1) {
            Toast.makeText(this, "未检测到可用的副屏幕", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] items = new String[choices.size()];
        for (int i = 0; i < choices.size(); i++) {
            items[i] = choices.get(i).label;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("选择投屏屏幕")
                .setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
                    DisplayChoice choice = choices.get(which);
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putInt("cluster_display_id", choice.displayId)
                            .apply();
                    updateClusterDisplaySelectStatus();
                    
                    FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                    if (fwm != null) {
                        fwm.onClusterMirrorConfigChanged();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateClusterDisplaySelectStatus() {
        if (tvClusterDisplaySelectStatus == null) return;
        int selectedId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt("cluster_display_id", -1);
        if (selectedId < 0) {
            tvClusterDisplaySelectStatus.setText("当前选择: 自动选择 (首个副屏幕)");
        } else {
            tvClusterDisplaySelectStatus.setText("当前选择: 屏幕 ID " + selectedId);
        }
    }


    private static class DisplayChoice {
        final int displayId;
        final String label;

        DisplayChoice(int displayId, String label) {
            this.displayId = displayId;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}