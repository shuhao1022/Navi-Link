package com.navi.link.activity;
import com.navi.link.R;
import com.navi.link.BuildConfig;
import com.navi.link.activity.*;
import com.navi.link.delegate.*;
import com.navi.link.window.*;
import com.navi.link.view.*;
import com.navi.link.receiver.*;
import com.navi.link.service.*;
import com.navi.link.utils.*;


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
import android.widget.EditText;
import android.widget.Button;
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
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.CompoundButtonCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_IS_MINIMAL = "is_minimal_style";
    private static final String KEY_STYLE_MODE = "style_mode";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_IS_SERVICE_ONLY = "is_service_only";
    public static final String PREFS_NAME = "floating_config";
    private static final int REQUEST_OVERLAY_PERMISSION = 100;

    public static final int[] THEME_COLORS = {
            0xFF1A1A1A,  // 黑色
            0xFFE53935,  // 朱红
            0xFFFF4081,  // 粉红
            0xFFFF6D00,  // 深橙
            0xFFFF9100,  // 橙
            0xFFFFCA28,  // 琥珀
            0xFF8D6E63,  // 棕色
            0xFF6DFF00,  // 青绿
            0xFF00BFA5,  // 翡翠
            0xFF4FC3F7,  // 浅蓝
            0xFF1199FF,  // 蓝
            0xFF5C6BC0,  // 靛蓝
            0xFFAB47BC,  // 紫
    };

    public MaterialCardView cardMinimal;
    public MaterialCardView cardNormal;
    public MaterialCardView cardFull;
    public MaterialCardView cardServiceOnly;
    public MaterialCardView cardNormalStart;
    public MaterialCardView cardBgDark;
    public MaterialCardView cardBgSemi;
    public MaterialCardView cardBgTransparent;
    public LinearLayout llThemeColors;
    private RadioButton rbMinimal;
    private RadioButton rbNormal;
    private RadioButton rbFull;
    // 巡航窗口样式
    public MaterialCardView cardCruiseNormal;
    public MaterialCardView cardCruiseMinimal;
    public MaterialCardView cardCruiseFull;
    private RadioButton rbCruiseNormal;
    private RadioButton rbCruiseMinimal;
    private RadioButton rbCruiseFull;
    private RadioButton rbServiceOnly;
    private RadioButton rbNormalStart;
    private RadioButton rbBgDark;
    private RadioButton rbBgSemi;
    private RadioButton rbBgTransparent;
    public MaterialCardView cardStartAmap;
    private RadioButton rbStartAmap;
    public TextView tvStartAmapDesc;
    public SeekBar sbScale;
    public SeekBar sbClusterScale;
    public View[] themeChips;
    public TextView tvScaleValue;
    public TextView tvClusterScaleValue;
    public TextView tvStatus;
    private SwitchCompat cbCruiseEnabled;
    public TextView tvCruiseStatus;
    public MaterialCardView cardCruiseToggle;
    private SwitchCompat cbNormalLaneEnabled;
    public TextView tvNormalLaneStatus;
    public MaterialCardView cardNormalLaneToggle;

    private SwitchCompat cbHideTurnIconBg;
    public TextView tvHideTurnIconBgStatus;
    public MaterialCardView cardHideTurnIconBgToggle;
    private SwitchCompat cbAvoidForegroundEnabled;
    public TextView tvAvoidForegroundStatus;
    public MaterialCardView cardAvoidForegroundToggle;

    public boolean crossMapHideEnabled = false;
    private SwitchCompat cbCrossMapHideEnabled;
    public TextView tvCrossMapHideStatus;
    public MaterialCardView cardCrossMapHideToggle;

    public boolean hideLaneLineBg = false;
    private SwitchCompat cbHideLaneLineBgEnabled;
    public TextView tvHideLaneLineBgStatus;
    public MaterialCardView cardHideLaneLineBgToggle;
    public TextView tvSys;
    public TextView tvStyle;
    public TextView tvOperation;
    public TextView tvTitleClusterSettings;
    public TextView tvTitleLayoutNormal;
    public TextView tvTitleLayoutMinimal;
    public TextView tvTitleAboutSoftware;
    public TextView tvTitleAboutDevice;
    public TextView tvTitleDisplayInfo;
    private SwitchCompat cbOverspeedWarningEnabled;
    public TextView tvOverspeedWarningStatus;
    public MaterialCardView cardOverspeedWarningToggle;
    private SwitchCompat cbMinimalCameraEnabled;
    public TextView tvMinimalCameraStatus;
    public MaterialCardView cardMinimalCameraToggle;
    public MaterialCardView cardMinimalAutocenterToggle;
    private SwitchCompat cbMinimalAutocenterEnabled;
    public MaterialCardView cardClusterMirrorToggle;
    private SwitchCompat cbClusterMirrorEnabled;
    public TextView tvClusterMirrorStatus;
    public MaterialCardView cardClusterDisplaySelect;
    public TextView tvClusterDisplaySelectStatus;
    public TextView btnAdjustClusterPos;
    public MaterialCardView cardHideMainWhenClusterActive;
    private SwitchCompat cbHideMainWhenClusterActive;
    public TextView tvHideMainWhenClusterActiveStatus;

    public MaterialCardView cardNormalTmcToggle;
    private SwitchCompat cbNormalTmcEnabled;
    public TextView tvNormalTmcStatus;

    public MaterialCardView cardNormalBottomInfoToggle;
    private SwitchCompat cbNormalBottomInfoEnabled;
    public TextView tvNormalBottomInfoStatus;

    public MaterialCardView cardNormalCruiseInfoToggle;
    private SwitchCompat cbNormalCruiseInfoEnabled;
    public TextView tvNormalCruiseInfoStatus;

    public MaterialCardView cardHideNormalCruiseSpeedToggle;
    private SwitchCompat cbHideNormalCruiseSpeedEnabled;
    public TextView tvHideNormalCruiseSpeedStatus;
    public boolean hideNormalCruiseSpeed = false;

    public MaterialCardView cardMinimalLaneToggle;
    private SwitchCompat cbMinimalLaneEnabled;
    public TextView tvMinimalLaneStatus;

    public MaterialCardView cardMinimalRoadNameToggle;
    private SwitchCompat cbMinimalRoadNameEnabled;
    public TextView tvMinimalRoadNameStatus;

    public MaterialCardView cardMinimalDirectionToggle;
    private SwitchCompat cbMinimalDirectionEnabled;
    public TextView tvMinimalDirectionStatus;

    public MaterialCardView cardMinimalTurnInfoToggle;
    private SwitchCompat cbMinimalTurnInfoEnabled;
    public TextView tvMinimalTurnInfoStatus;

    public MaterialCardView cardMinimalSpeedToggle;
    private SwitchCompat cbMinimalSpeedEnabled;
    public TextView tvMinimalSpeedStatus;

    public MaterialCardView cardMinimalLightCountToggle;
    private SwitchCompat cbMinimalLightCountEnabled;
    public TextView tvMinimalLightCountStatus;

    public MaterialCardView cardMinimalAccentNaviInfoToggle;
    private SwitchCompat cbMinimalAccentNaviInfoEnabled;
    public TextView tvMinimalAccentNaviInfoStatus;

    public MaterialCardView cardMinimalSpeedLimitToggle;
    private SwitchCompat cbMinimalSpeedLimitEnabled;
    public TextView tvMinimalSpeedLimitStatus;

    public MaterialCardView cardAutoStartToggle;
    private SwitchCompat cbAutoStartEnabled;
    public TextView tvAutoStartStatus;

    // 红绿灯填充背景样式
    public MaterialCardView cardTrafficLightFillToggle;
    private SwitchCompat cbTrafficLightFillEnabled;
    public TextView tvTrafficLightFillStatus;
    public boolean isTrafficLightFillEnabled = false;

    // 红绿灯图标样式
    private MaterialCardView[] cardTrafficLightStyle = new MaterialCardView[7];
    public int trafficLightStyle = 0;

    // 默认胶囊透明
    public MaterialCardView cardTrafficLightCapsuleToggle;
    private SwitchCompat cbTrafficLightCapsuleEnabled;
    public TextView tvTrafficLightCapsuleStatus;
    public boolean isTrafficLightCapsuleEnabled = true;

    // 胶囊灯图显示
    public MaterialCardView cardTrafficLightIconToggle;
    private SwitchCompat cbTrafficLightIconEnabled;
    public TextView tvTrafficLightIconStatus;
    public boolean isTrafficLightIconEnabled = true;

    // 红绿灯倒计时字体选择
    public MaterialCardView cardFontDefault, cardFontOne, cardFontTwo, cardFontThree;
    private RadioButton rbFontDefault, rbFontOne, rbFontTwo, rbFontThree;


    public int countdownFontIndex = 0; // 0=默认, 1=字体一, 2=字体二, 3=字体三

    // Menu elements
    public MaterialCardView menuSystemAppearance;
    public MaterialCardView menuFeaturesAvoidance;
    public MaterialCardView menuLayoutNormal;
    public MaterialCardView menuLayoutMinimal;
    public MaterialCardView menuTrafficLight;

    public View indicatorSystemAppearance;
    public View indicatorFeaturesAvoidance;
    public View indicatorLayoutNormal;
    public View indicatorLayoutMinimal;
    public View indicatorTrafficLight;

    public TextView tvMenuSystemAppearance;
    public TextView tvMenuFeaturesAvoidance;
    public TextView tvMenuLayoutNormal;
    public TextView tvMenuLayoutMinimal;
    public TextView tvMenuTrafficLight;

    // Right side panels
    private ScrollView panelSystemAppearance;
    private ScrollView panelFeaturesAvoidance;
    private ScrollView panelLayoutNormal;
    private ScrollView panelLayoutMinimal;
    private ScrollView panelTrafficLight;
    private ScrollView panelAboutUs;

    public MaterialCardView menuAboutUs;
    public View indicatorAboutUs;
    public TextView tvMenuAboutUs;

    public TextView tvAboutAppVersion;
    public TextView tvAboutCpuInfo;
    public TextView tvAboutRamInfo;
    public TextView tvAboutRomInfo;
    public TextView tvAboutApiLevel;
    public TextView tvDisplayPhysicalRes;
    public TextView tvDisplayAppRes;
    public TextView tvDisplayDensity;
    public TextView tvDisplayRefreshRate;
    public TextView tvAboutQqGroup;
    public TextView tvAboutGitUrl;

    public int selectedMenuIndex = 0;

    public boolean isMinimalStyle = false;
    public int styleMode = 0;
    public int cruiseStyleMode = 0; // 0=常规巡航, 1=灵动岛巡航, 2=全数据巡航
    public boolean isServiceOnlyMode = false;
    public int startupMode = 0; // 0=正常, 1=纯服务, 2=启动高德地图
    public String targetAmapPackage = "";
    public int backgroundMode = 0; // 0=深色, 1=半透明, 2=全透明
    public boolean cruiseEnabled = true;
    public boolean normalLaneEnabled = false;
    public boolean hideTurnIconBg = false;
    public boolean avoidForegroundEnabled = false;
    public boolean overspeedWarningEnabled = true;
    public int overspeedThreshold = 0;
    private View[] overspeedThresholdChips;
    public LinearLayout llOverspeedThresholdRow;

    public boolean clusterMirrorEnabled = false;
    public int clusterDisplayId = -1;
    public boolean hideMainWhenClusterActive = false;
    public boolean autoStartEnabled = false;

    public boolean normalTmcEnabled = true;
    public boolean normalBottomInfoEnabled = true;
    public boolean normalCruiseInfoEnabled = true;
    public boolean minimalLaneEnabled = false;

    public boolean isMinimalCameraEnabled = false;
    public boolean isMinimalRoadNameEnabled = true;
    public boolean isMinimalDirectionEnabled = false;
    public boolean isMinimalTurnInfoEnabled = true;
    public boolean isMinimalSpeedEnabled = true;
    public boolean isMinimalLightCountEnabled = false;
    public boolean isMinimalAccentNaviInfoEnabled = false;
    public boolean isMinimalAutocenterEnabled = false;
    public boolean isMinimalSpeedLimitEnabled = false;

    public int themeColor = 0xFF4FC3F7;

    // Color Settings Menu & Panels
    private com.google.android.material.card.MaterialCardView menuColorSettings;
    public View indicatorColorSettings;
    public TextView tvMenuColorSettings;
    private ScrollView panelColorSettings;

    // Color Previews
    public View viewColorPreviewBgDay;
    public View viewColorPreviewBgNight;
    public View viewColorPreviewPrimaryDay;
    public View viewColorPreviewPrimaryNight;
    public View viewColorPreviewSecondaryDay;
    public View viewColorPreviewSecondaryNight;
    public View viewColorPreviewHintDay;
    public View viewColorPreviewHintNight;
    public View viewColorPreviewNormalTurnIconDay;
    public View viewColorPreviewNormalTurnIconNight;
    public View viewColorPreviewNormalTurnBgDay;
    public View viewColorPreviewNormalTurnBgNight;
    public View viewColorPreviewFullMiddleBgDay;
    public View viewColorPreviewFullMiddleBgNight;
    private com.google.android.material.card.MaterialCardView btnResetColors;

    // Color Settings values
    public int bgColorDay;
    public int bgColorNight;
    public int textPrimaryDay;
    public int textPrimaryNight;
    public int textSecondaryDay;
    public int textSecondaryNight;
    public int textHintDay;
    public int textHintNight;
    public int normalTurnIconColorDay;
    public int normalTurnIconColorNight;
    public int normalTurnIconBgColorDay;
    public int normalTurnIconBgColorNight;
    public int fullMiddleBgColorDay;
    public int fullMiddleBgColorNight;

    public boolean isDarkColor(int color) {
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
        setupUpdateEntry();
        if (savedInstanceState == null) {
            checkPermissionAndStart();
            // 启动时静默检查更新（仅在有新版本时弹窗）
            UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME, false, updateCallback(false));
        }
    }

    // ── 应用内更新 ──────────────────────────────────────────────
    public TextView tvVersionStatus;

    /** 绑定"软件版本"入口，点击手动检查更新。 */
    public void setupUpdateEntry() {
        tvVersionStatus = tvAboutAppVersion;
        if (tvVersionStatus != null) {
            tvVersionStatus.setText("v" + BuildConfig.VERSION_NAME);
        }
        View versionRow = findViewById(R.id.ll_about_version);
        if (versionRow != null) {
            versionRow.setOnClickListener(v -> {
                if (tvVersionStatus != null) tvVersionStatus.setText("正在检查更新…");
                UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME, true, updateCallback(true));
            });
        }
    }

    /** 构造更新检查回调。manual=true 时，"已最新/失败"也会给出提示。 */
    private UpdateChecker.Callback updateCallback(boolean manual) {
        return new UpdateChecker.Callback() {
            @Override
            public void onUpdateAvailable(UpdateChecker.UpdateInfo info) {
                if (isFinishing() || isDestroyed()) return;
                if (tvVersionStatus != null) {
                    tvVersionStatus.setText("有新版 v" + info.versionName);
                }
                UpdateDialog.show(MainActivity.this, BuildConfig.VERSION_NAME, info);
            }

            @Override
            public void onNoUpdate(boolean manual) {
                if (tvVersionStatus != null) {
                    tvVersionStatus.setText("v" + BuildConfig.VERSION_NAME + " (已最新)");
                }
                if (manual) {
                    Toast.makeText(MainActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message, boolean manual) {
                if (tvVersionStatus != null) {
                    tvVersionStatus.setText("v" + BuildConfig.VERSION_NAME);
                }
                if (manual) {
                    Toast.makeText(MainActivity.this, "检查更新失败：" + message, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public void initViews() {
        cardNormal = findViewById(R.id.card_normal);
        cardMinimal = findViewById(R.id.card_minimal);
        cardFull = findViewById(R.id.card_full);
        cardServiceOnly = findViewById(R.id.card_service_only);
        cardNormalStart = findViewById(R.id.card_normal_start);
        cardBgDark = null;
        cardBgSemi = null;
        cardBgTransparent = null;
        rbNormal = findViewById(R.id.rb_normal);
        rbMinimal = findViewById(R.id.rb_minimal);
        rbFull = findViewById(R.id.rb_full);
        cardCruiseNormal = findViewById(R.id.card_cruise_normal);
        cardCruiseMinimal = findViewById(R.id.card_cruise_minimal);
        cardCruiseFull = findViewById(R.id.card_cruise_full);
        rbCruiseNormal = findViewById(R.id.rb_cruise_normal);
        rbCruiseMinimal = findViewById(R.id.rb_cruise_minimal);
        rbCruiseFull = findViewById(R.id.rb_cruise_full);
        rbServiceOnly = findViewById(R.id.rb_service_only);
        rbNormalStart = findViewById(R.id.rb_normal_start);
        rbBgDark = null;
        rbBgSemi = null;
        rbBgTransparent = null;
        cardStartAmap = findViewById(R.id.card_start_amap);
        rbStartAmap = findViewById(R.id.rb_start_amap);
        tvStartAmapDesc = findViewById(R.id.tv_start_amap_desc);
        sbScale = findViewById(R.id.sb_scale);
        sbClusterScale = findViewById(R.id.sb_cluster_scale);
        tvScaleValue = findViewById(R.id.tv_scale_value);
        tvClusterScaleValue = findViewById(R.id.tv_cluster_scale_value);
        tvStatus = findViewById(R.id.tv_status);
        cbCruiseEnabled = findViewById(R.id.cb_cruise_enabled);
        tvCruiseStatus = findViewById(R.id.tv_cruise_status);
        cardCruiseToggle = findViewById(R.id.card_cruise_toggle);
        cbNormalLaneEnabled = findViewById(R.id.cb_normal_lane_enabled);
        tvNormalLaneStatus = findViewById(R.id.tv_normal_lane_status);
        cardNormalLaneToggle = findViewById(R.id.card_normal_lane_toggle);

        cbHideTurnIconBg = findViewById(R.id.cb_hide_turn_icon_bg);
        tvHideTurnIconBgStatus = findViewById(R.id.tv_hide_turn_icon_bg_status);
        cardHideTurnIconBgToggle = findViewById(R.id.card_hide_turn_icon_bg_toggle);
        cbAvoidForegroundEnabled = findViewById(R.id.cb_avoid_foreground_enabled);
        tvAvoidForegroundStatus = findViewById(R.id.tv_avoid_foreground_status);
        cardAvoidForegroundToggle = findViewById(R.id.card_avoid_foreground_toggle);

        cbCrossMapHideEnabled = findViewById(R.id.cb_cross_map_hide_enabled);
        tvCrossMapHideStatus = findViewById(R.id.tv_cross_map_hide_status);
        cardCrossMapHideToggle = findViewById(R.id.card_cross_map_hide_toggle);

        cbHideLaneLineBgEnabled = findViewById(R.id.cb_hide_lane_line_bg_enabled);
        tvHideLaneLineBgStatus = findViewById(R.id.tv_hide_lane_line_bg_status);
        cardHideLaneLineBgToggle = findViewById(R.id.card_hide_lane_line_bg_toggle);
        cbOverspeedWarningEnabled = findViewById(R.id.cb_overspeed_warning_enabled);
        tvOverspeedWarningStatus = findViewById(R.id.tv_overspeed_warning_status);
        cardOverspeedWarningToggle = findViewById(R.id.card_overspeed_warning_toggle);
        llOverspeedThresholdRow = findViewById(R.id.ll_overspeed_threshold_row);
        overspeedThresholdChips = new View[]{
                findViewById(R.id.chip_overspeed_0),
                findViewById(R.id.chip_overspeed_10),
                findViewById(R.id.chip_overspeed_20),
                findViewById(R.id.chip_overspeed_30),
                findViewById(R.id.chip_overspeed_50)
        };
        cbMinimalCameraEnabled = findViewById(R.id.cb_minimal_camera_enabled);
        tvMinimalCameraStatus = findViewById(R.id.tv_minimal_camera_status);
        cardMinimalCameraToggle = findViewById(R.id.card_minimal_camera_toggle);
        cardMinimalAutocenterToggle = findViewById(R.id.card_minimal_autocenter_toggle);
        cbMinimalAutocenterEnabled = findViewById(R.id.cb_minimal_autocenter_enabled);
        cbClusterMirrorEnabled = findViewById(R.id.cb_cluster_mirror_enabled);
        tvClusterMirrorStatus = findViewById(R.id.tv_cluster_mirror_status);
        cardClusterMirrorToggle = findViewById(R.id.card_cluster_mirror_toggle);
        cardClusterDisplaySelect = findViewById(R.id.card_cluster_display_select);
        tvClusterDisplaySelectStatus = findViewById(R.id.tv_cluster_display_select_status);
        btnAdjustClusterPos = findViewById(R.id.btn_adjust_cluster_pos);
        cardHideMainWhenClusterActive = findViewById(R.id.card_hide_main_when_cluster_active);
        cbHideMainWhenClusterActive = findViewById(R.id.cb_hide_main_when_cluster_active);
        tvHideMainWhenClusterActiveStatus = findViewById(R.id.tv_hide_main_when_cluster_active_status);
        cardAutoStartToggle = findViewById(R.id.card_auto_start_toggle);
        cbAutoStartEnabled = findViewById(R.id.cb_auto_start_enabled);
        tvAutoStartStatus = findViewById(R.id.tv_auto_start_status);
        cardTrafficLightFillToggle = findViewById(R.id.card_traffic_light_fill_toggle);
        cbTrafficLightFillEnabled = findViewById(R.id.cb_traffic_light_fill_enabled);
        tvTrafficLightFillStatus = findViewById(R.id.tv_traffic_light_fill_status);
        cardTrafficLightCapsuleToggle = findViewById(R.id.card_traffic_light_capsule_toggle);
        cbTrafficLightCapsuleEnabled = findViewById(R.id.cb_traffic_light_capsule_enabled);
        tvTrafficLightCapsuleStatus = findViewById(R.id.tv_traffic_light_capsule_status);
        cardTrafficLightIconToggle = findViewById(R.id.card_traffic_light_icon_toggle);
        cbTrafficLightIconEnabled = findViewById(R.id.cb_traffic_light_icon_enabled);
        tvTrafficLightIconStatus = findViewById(R.id.tv_traffic_light_icon_status);
        cardFontDefault = findViewById(R.id.card_font_default);
        cardFontOne = findViewById(R.id.card_font_one);
        cardFontTwo = findViewById(R.id.card_font_two);
        cardFontThree = findViewById(R.id.card_font_three);
        rbFontDefault = findViewById(R.id.rb_font_default);
        rbFontOne = findViewById(R.id.rb_font_one);
        rbFontTwo = findViewById(R.id.rb_font_two);
        rbFontThree = findViewById(R.id.rb_font_three);


        cardTrafficLightStyle[0] = findViewById(R.id.card_traffic_light_style_0);
        cardTrafficLightStyle[1] = findViewById(R.id.card_traffic_light_style_1);
        cardTrafficLightStyle[2] = findViewById(R.id.card_traffic_light_style_2);
        cardTrafficLightStyle[3] = findViewById(R.id.card_traffic_light_style_3);
        cardTrafficLightStyle[4] = findViewById(R.id.card_traffic_light_style_4);
        cardTrafficLightStyle[5] = findViewById(R.id.card_traffic_light_style_5);
        cardTrafficLightStyle[6] = findViewById(R.id.card_traffic_light_style_6);
        llThemeColors = null;
        tvSys = findViewById(R.id.tv_sys);
        tvStyle = findViewById(R.id.tv_style);
        tvOperation = findViewById(R.id.tv_operation);
        tvTitleClusterSettings = findViewById(R.id.tv_title_cluster_settings);
        tvTitleLayoutNormal = findViewById(R.id.tv_title_layout_normal);
        tvTitleLayoutMinimal = findViewById(R.id.tv_title_layout_minimal);
        tvTitleAboutSoftware = findViewById(R.id.tv_title_about_software);
        tvTitleAboutDevice = findViewById(R.id.tv_title_about_device);
        tvTitleDisplayInfo = findViewById(R.id.tv_title_display_info);
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

        // Bind menu UI elements
        menuSystemAppearance = findViewById(R.id.menu_system_appearance);
        menuColorSettings = findViewById(R.id.menu_color_settings);
        menuFeaturesAvoidance = findViewById(R.id.menu_features_avoidance);
        menuLayoutNormal = findViewById(R.id.menu_layout_normal);
        menuLayoutMinimal = findViewById(R.id.menu_layout_minimal);
        menuTrafficLight = findViewById(R.id.menu_traffic_light);

        indicatorSystemAppearance = findViewById(R.id.indicator_system_appearance);
        indicatorColorSettings = findViewById(R.id.indicator_color_settings);
        indicatorFeaturesAvoidance = findViewById(R.id.indicator_features_avoidance);
        indicatorLayoutNormal = findViewById(R.id.indicator_layout_normal);
        indicatorLayoutMinimal = findViewById(R.id.indicator_layout_minimal);
        indicatorTrafficLight = findViewById(R.id.indicator_traffic_light);

        tvMenuSystemAppearance = findViewById(R.id.tv_menu_system_appearance);
        tvMenuColorSettings = findViewById(R.id.tv_menu_color_settings);
        tvMenuFeaturesAvoidance = findViewById(R.id.tv_menu_features_avoidance);
        tvMenuLayoutNormal = findViewById(R.id.tv_menu_layout_normal);
        tvMenuLayoutMinimal = findViewById(R.id.tv_menu_layout_minimal);
        tvMenuTrafficLight = findViewById(R.id.tv_menu_traffic_light);

        // Bind right side panel scroll views
        panelSystemAppearance = findViewById(R.id.panel_system_appearance);
        panelColorSettings = findViewById(R.id.panel_color_settings);
        panelFeaturesAvoidance = findViewById(R.id.panel_features_avoidance);
        panelLayoutNormal = findViewById(R.id.panel_layout_normal);
        panelLayoutMinimal = findViewById(R.id.panel_layout_minimal);
        panelTrafficLight = findViewById(R.id.panel_traffic_light);
        panelAboutUs = findViewById(R.id.panel_about_us);

        menuAboutUs = findViewById(R.id.menu_about_us);
        indicatorAboutUs = findViewById(R.id.indicator_about_us);
        tvMenuAboutUs = findViewById(R.id.tv_menu_about_us);

        tvAboutAppVersion = findViewById(R.id.tv_about_app_version);
        tvAboutCpuInfo = findViewById(R.id.tv_about_cpu_info);
        tvAboutRamInfo = findViewById(R.id.tv_about_ram_info);
        tvAboutRomInfo = findViewById(R.id.tv_about_rom_info);
        tvAboutApiLevel = findViewById(R.id.tv_about_api_level);
        tvDisplayPhysicalRes = findViewById(R.id.tv_display_physical_res);
        tvDisplayAppRes = findViewById(R.id.tv_display_app_res);
        tvDisplayDensity = findViewById(R.id.tv_display_density);
        tvDisplayRefreshRate = findViewById(R.id.tv_display_refresh_rate);
        tvAboutQqGroup = findViewById(R.id.tv_about_qq_group);
        tvAboutGitUrl = findViewById(R.id.tv_about_git_url);

        initAboutUsPanel();

        cardNormalTmcToggle = findViewById(R.id.card_normal_tmc_toggle);
        cbNormalTmcEnabled = findViewById(R.id.cb_normal_tmc_enabled);
        tvNormalTmcStatus = findViewById(R.id.tv_normal_tmc_status);

        cardNormalBottomInfoToggle = findViewById(R.id.card_normal_bottom_info_toggle);
        cbNormalBottomInfoEnabled = findViewById(R.id.cb_normal_bottom_info_enabled);
        tvNormalBottomInfoStatus = findViewById(R.id.tv_normal_bottom_info_status);

        cardNormalCruiseInfoToggle = findViewById(R.id.card_normal_cruise_info_toggle);
        cbNormalCruiseInfoEnabled = findViewById(R.id.cb_normal_cruise_info_enabled);
        tvNormalCruiseInfoStatus = findViewById(R.id.tv_normal_cruise_info_status);

        cardHideNormalCruiseSpeedToggle = findViewById(R.id.card_hide_normal_cruise_speed_toggle);
        cbHideNormalCruiseSpeedEnabled = findViewById(R.id.cb_hide_normal_cruise_speed_enabled);
        tvHideNormalCruiseSpeedStatus = findViewById(R.id.tv_hide_normal_cruise_speed_status);

        cardMinimalLaneToggle = findViewById(R.id.card_minimal_lane_toggle);
        cbMinimalLaneEnabled = findViewById(R.id.cb_minimal_lane_enabled);
        tvMinimalLaneStatus = findViewById(R.id.tv_minimal_lane_status);

        cardMinimalRoadNameToggle = findViewById(R.id.card_minimal_road_name_toggle);
        cbMinimalRoadNameEnabled = findViewById(R.id.cb_minimal_road_name_enabled);
        tvMinimalRoadNameStatus = findViewById(R.id.tv_minimal_road_name_status);

        cardMinimalDirectionToggle = findViewById(R.id.card_minimal_direction_toggle);
        cbMinimalDirectionEnabled = findViewById(R.id.cb_minimal_direction_enabled);
        tvMinimalDirectionStatus = findViewById(R.id.tv_minimal_direction_status);

        cardMinimalTurnInfoToggle = findViewById(R.id.card_minimal_turn_info_toggle);
        cbMinimalTurnInfoEnabled = findViewById(R.id.cb_minimal_turn_info_enabled);
        tvMinimalTurnInfoStatus = findViewById(R.id.tv_minimal_turn_info_status);

        cardMinimalSpeedToggle = findViewById(R.id.card_minimal_speed_toggle);
        cbMinimalSpeedEnabled = findViewById(R.id.cb_minimal_speed_enabled);
        tvMinimalSpeedStatus = findViewById(R.id.tv_minimal_speed_status);

        cardMinimalLightCountToggle = findViewById(R.id.card_minimal_light_count_toggle);
        cbMinimalLightCountEnabled = findViewById(R.id.cb_minimal_light_count_enabled);
        tvMinimalLightCountStatus = findViewById(R.id.tv_minimal_light_count_status);

        cardMinimalAccentNaviInfoToggle = null;
        cbMinimalAccentNaviInfoEnabled = null;
        tvMinimalAccentNaviInfoStatus = null;

        cardMinimalSpeedLimitToggle = findViewById(R.id.card_minimal_speed_limit_toggle);
        cbMinimalSpeedLimitEnabled = findViewById(R.id.cb_minimal_speed_limit_enabled);
        tvMinimalSpeedLimitStatus = findViewById(R.id.tv_minimal_speed_limit_status);

        // Bind color settings views and listeners
        viewColorPreviewBgDay = findViewById(R.id.view_color_preview_bg_day);
        viewColorPreviewBgNight = findViewById(R.id.view_color_preview_bg_night);
        viewColorPreviewPrimaryDay = findViewById(R.id.view_color_preview_primary_day);
        viewColorPreviewPrimaryNight = findViewById(R.id.view_color_preview_primary_night);
        viewColorPreviewSecondaryDay = findViewById(R.id.view_color_preview_secondary_day);
        viewColorPreviewSecondaryNight = findViewById(R.id.view_color_preview_secondary_night);
        viewColorPreviewHintDay = findViewById(R.id.view_color_preview_hint_day);
        viewColorPreviewHintNight = findViewById(R.id.view_color_preview_hint_night);
        viewColorPreviewNormalTurnIconDay = findViewById(R.id.view_color_preview_normal_turn_icon_day);
        viewColorPreviewNormalTurnIconNight = findViewById(R.id.view_color_preview_normal_turn_icon_night);
        viewColorPreviewNormalTurnBgDay = findViewById(R.id.view_color_preview_normal_turn_bg_day);
        viewColorPreviewNormalTurnBgNight = findViewById(R.id.view_color_preview_normal_turn_bg_night);
        viewColorPreviewFullMiddleBgDay = findViewById(R.id.view_color_preview_full_middle_bg_day);
        viewColorPreviewFullMiddleBgNight = findViewById(R.id.view_color_preview_full_middle_bg_night);
        btnResetColors = findViewById(R.id.btn_reset_colors);

        if (viewColorPreviewBgDay != null) {
            viewColorPreviewBgDay.setOnClickListener(v -> showColorPickerDialog("白天背景色", bgColorDay, color -> {
                bgColorDay = color;
                updateColorPreview(viewColorPreviewBgDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewBgNight != null) {
            viewColorPreviewBgNight.setOnClickListener(v -> showColorPickerDialog("夜间背景色", bgColorNight, color -> {
                bgColorNight = color;
                updateColorPreview(viewColorPreviewBgNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewPrimaryDay != null) {
            viewColorPreviewPrimaryDay.setOnClickListener(v -> showColorPickerDialog("白天主文字颜色", textPrimaryDay, color -> {
                textPrimaryDay = color;
                updateColorPreview(viewColorPreviewPrimaryDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewPrimaryNight != null) {
            viewColorPreviewPrimaryNight.setOnClickListener(v -> showColorPickerDialog("夜间主文字颜色", textPrimaryNight, color -> {
                textPrimaryNight = color;
                updateColorPreview(viewColorPreviewPrimaryNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewSecondaryDay != null) {
            viewColorPreviewSecondaryDay.setOnClickListener(v -> showColorPickerDialog("白天次文字颜色", textSecondaryDay, color -> {
                textSecondaryDay = color;
                updateColorPreview(viewColorPreviewSecondaryDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewSecondaryNight != null) {
            viewColorPreviewSecondaryNight.setOnClickListener(v -> showColorPickerDialog("夜间次文字颜色", textSecondaryNight, color -> {
                textSecondaryNight = color;
                updateColorPreview(viewColorPreviewSecondaryNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewHintDay != null) {
            viewColorPreviewHintDay.setOnClickListener(v -> showColorPickerDialog("白天提示文字颜色", textHintDay, color -> {
                textHintDay = color;
                updateColorPreview(viewColorPreviewHintDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewHintNight != null) {
            viewColorPreviewHintNight.setOnClickListener(v -> showColorPickerDialog("夜间提示文字颜色", textHintNight, color -> {
                textHintNight = color;
                updateColorPreview(viewColorPreviewHintNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }

        if (viewColorPreviewNormalTurnIconDay != null) {
            viewColorPreviewNormalTurnIconDay.setOnClickListener(v -> showColorPickerDialog("常规转向图标白天颜色", normalTurnIconColorDay, color -> {
                normalTurnIconColorDay = color;
                updateColorPreview(viewColorPreviewNormalTurnIconDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnIconNight != null) {
            viewColorPreviewNormalTurnIconNight.setOnClickListener(v -> showColorPickerDialog("常规转向图标夜间颜色", normalTurnIconColorNight, color -> {
                normalTurnIconColorNight = color;
                updateColorPreview(viewColorPreviewNormalTurnIconNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnBgDay != null) {
            viewColorPreviewNormalTurnBgDay.setOnClickListener(v -> showColorPickerDialog("常规转向背景白天颜色", normalTurnIconBgColorDay, color -> {
                normalTurnIconBgColorDay = color;
                updateColorPreview(viewColorPreviewNormalTurnBgDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnBgNight != null) {
            viewColorPreviewNormalTurnBgNight.setOnClickListener(v -> showColorPickerDialog("常规转向背景夜间颜色", normalTurnIconBgColorNight, color -> {
                normalTurnIconBgColorNight = color;
                updateColorPreview(viewColorPreviewNormalTurnBgNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewFullMiddleBgDay != null) {
            viewColorPreviewFullMiddleBgDay.setOnClickListener(v -> showColorPickerDialog("全数据卡片白天背景", fullMiddleBgColorDay, color -> {
                fullMiddleBgColorDay = color;
                updateColorPreview(viewColorPreviewFullMiddleBgDay, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewFullMiddleBgNight != null) {
            viewColorPreviewFullMiddleBgNight.setOnClickListener(v -> showColorPickerDialog("全数据卡片夜间背景", fullMiddleBgColorNight, color -> {
                fullMiddleBgColorNight = color;
                updateColorPreview(viewColorPreviewFullMiddleBgNight, color);
                savePreferences();
                refreshFloatingWindow();
            }));
        }

        if (btnResetColors != null) {
            btnResetColors.setOnClickListener(v -> new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("确定要将所有色调配置恢复为默认吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        resetToDefaultColors();
                        savePreferences();
                        refreshFloatingWindow();
                        Toast.makeText(MainActivity.this, "已恢复默认颜色配置", Toast.LENGTH_SHORT).show();
                    }).show());
        }
    }

    public void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isMinimalStyle = sp.getBoolean(KEY_IS_MINIMAL, false);
        styleMode = sp.getInt(KEY_STYLE_MODE, isMinimalStyle ? 1 : 0);
        cruiseStyleMode = sp.getInt("cruise_style_mode", 0);
        themeColor = sp.getInt(KEY_THEME_COLOR, 0xFF4FC3F7);
        bgColorDay = sp.getInt("bg_color_day", 0xE6F5F5F5);
        bgColorNight = sp.getInt("bg_color_night", 0xCC121212);
        textPrimaryDay = sp.getInt("text_primary_day", 0xFF1A1A1A);
        textPrimaryNight = sp.getInt("text_primary_night", 0xFFFFFFFF);
        textSecondaryDay = sp.getInt("text_secondary_day", 0xFF333333);
        textSecondaryNight = sp.getInt("text_secondary_night", 0xBBFFFFFF);
        textHintDay = sp.getInt("text_hint_day", 0xFF999999);
        textHintNight = sp.getInt("text_hint_night", 0xFF888888);
        normalTurnIconColorDay = sp.getInt("normal_turn_icon_color_day", 0xFFFFFFFF);
        normalTurnIconColorNight = sp.getInt("normal_turn_icon_color_night", 0xFFFFFFFF);
        normalTurnIconBgColorDay = sp.getInt("normal_turn_icon_bg_color_day", 0xFF007D5E);
        normalTurnIconBgColorNight = sp.getInt("normal_turn_icon_bg_color_night", 0xFF007D5E);
        fullMiddleBgColorDay = sp.getInt("full_middle_bg_color_day", 0xFF0099FF);
        fullMiddleBgColorNight = sp.getInt("full_middle_bg_color_night", 0xFF0099FF);
        updateColorPreviews();
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
        hideTurnIconBg = sp.getBoolean("hide_turn_icon_bg", false);
        avoidForegroundEnabled = sp.getBoolean("hide_on_amap_foreground", false);
        overspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
        overspeedThreshold = sp.getInt("overspeed_threshold", 0);
        isMinimalCameraEnabled = sp.getBoolean("minimal_camera_enabled", false);
        isMinimalRoadNameEnabled = sp.getBoolean("minimal_road_name_enabled", true);
        isMinimalDirectionEnabled = sp.getBoolean("minimal_direction_enabled", false);
        isMinimalTurnInfoEnabled = sp.getBoolean("minimal_turn_info_enabled", true);
        isMinimalSpeedEnabled = sp.getBoolean("minimal_speed_enabled", true);
        isMinimalLightCountEnabled = sp.getBoolean("minimal_light_count_enabled", false);
        isMinimalAccentNaviInfoEnabled = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        isMinimalAutocenterEnabled = sp.getBoolean("minimal_autocenter_enabled", false);
        isMinimalSpeedLimitEnabled = sp.getBoolean("minimal_speed_limit_enabled", false);
        clusterMirrorEnabled = sp.getBoolean("cluster_mirror_enabled", false);
        clusterDisplayId = sp.getInt("cluster_display_id", -1);
        hideMainWhenClusterActive = sp.getBoolean("hide_main_when_cluster_active", false);
        autoStartEnabled = sp.getBoolean("auto_start", false);
        normalTmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
        normalBottomInfoEnabled = sp.getBoolean("normal_navi_bottom_info_enabled", true);
        normalCruiseInfoEnabled = sp.getBoolean("normal_cruise_info_enabled", true);
        hideNormalCruiseSpeed = sp.getBoolean("hide_normal_cruise_speed", false);
        minimalLaneEnabled = sp.getBoolean("minimal_navi_lane_enabled", false);
        isTrafficLightFillEnabled = sp.getBoolean("traffic_light_fill_enabled", false);
        isTrafficLightCapsuleEnabled = sp.getBoolean("traffic_light_capsule_enabled", true);
        isTrafficLightIconEnabled = sp.getBoolean("traffic_light_icon_enabled", true);
        countdownFontIndex = sp.getInt("countdown_font_index", 0);
        trafficLightStyle = sp.getInt("traffic_light_style", 0);
        crossMapHideEnabled = sp.getBoolean("hide_on_cross_map", false);
        hideLaneLineBg = sp.getBoolean("hide_lane_line_bg", false);
 
        updateSeekBarToCurrentScale();
        
        if (cbNormalLaneEnabled != null) {
            cbNormalLaneEnabled.setChecked(normalLaneEnabled);
        }
        if (tvNormalLaneStatus != null) {
            tvNormalLaneStatus.setText(normalLaneEnabled ? "车道线已启用" : "车道线已禁用");
        }
        if (cbHideTurnIconBg != null) {
            cbHideTurnIconBg.setChecked(hideTurnIconBg);
        }
        if (tvHideTurnIconBgStatus != null) {
            tvHideTurnIconBgStatus.setText(hideTurnIconBg ? "背景已隐藏" : "背景已显示");
        }
        if (cbAvoidForegroundEnabled != null) {
            cbAvoidForegroundEnabled.setChecked(avoidForegroundEnabled);
        }
        if (tvAvoidForegroundStatus != null) {
            tvAvoidForegroundStatus.setText(avoidForegroundEnabled ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
        }
        if (cbCrossMapHideEnabled != null) {
            cbCrossMapHideEnabled.setChecked(crossMapHideEnabled);
        }
        if (tvCrossMapHideStatus != null) {
            tvCrossMapHideStatus.setText(crossMapHideEnabled ? "路口放大图时隐藏悬浮窗" : "路口放大图时正常显示浮窗");
        }
        if (cbHideLaneLineBgEnabled != null) {
            cbHideLaneLineBgEnabled.setChecked(hideLaneLineBg);
        }
        if (tvHideLaneLineBgStatus != null) {
            tvHideLaneLineBgStatus.setText(hideLaneLineBg ? "背景已隐藏" : "背景已显示");
        }
        if (cbOverspeedWarningEnabled != null) {
            cbOverspeedWarningEnabled.setChecked(overspeedWarningEnabled);
        }
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        if (cbMinimalCameraEnabled != null) {
            cbMinimalCameraEnabled.setChecked(isMinimalCameraEnabled);
        }
        if (tvMinimalCameraStatus != null) {
            tvMinimalCameraStatus.setText(isMinimalCameraEnabled ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
        }
        if (cbMinimalAutocenterEnabled != null) {
            cbMinimalAutocenterEnabled.setChecked(isMinimalAutocenterEnabled);
        }
        if (cbMinimalRoadNameEnabled != null) {
            cbMinimalRoadNameEnabled.setChecked(isMinimalRoadNameEnabled);
        }
        if (tvMinimalRoadNameStatus != null) {
            tvMinimalRoadNameStatus.setText(isMinimalRoadNameEnabled ? "道路名称已启用" : "道路名称已禁用");
        }
        if (cbMinimalDirectionEnabled != null) {
            cbMinimalDirectionEnabled.setChecked(isMinimalDirectionEnabled);
        }
        if (tvMinimalDirectionStatus != null) {
            tvMinimalDirectionStatus.setText(isMinimalDirectionEnabled ? "方向显示已启用" : "方向显示已禁用");
        }
        if (cbMinimalTurnInfoEnabled != null) {
            cbMinimalTurnInfoEnabled.setChecked(isMinimalTurnInfoEnabled);
        }
        if (tvMinimalTurnInfoStatus != null) {
            tvMinimalTurnInfoStatus.setText(isMinimalTurnInfoEnabled ? "转向信息已启用" : "转向信息已禁用");
        }
        if (cbMinimalSpeedEnabled != null) {
            cbMinimalSpeedEnabled.setChecked(isMinimalSpeedEnabled);
        }
        if (tvMinimalSpeedStatus != null) {
            tvMinimalSpeedStatus.setText(isMinimalSpeedEnabled ? "车速显示已启用" : "车速显示已禁用");
        }
        if (cbMinimalLightCountEnabled != null) {
            cbMinimalLightCountEnabled.setChecked(isMinimalLightCountEnabled);
        }
        if (tvMinimalLightCountStatus != null) {
            tvMinimalLightCountStatus.setText(isMinimalLightCountEnabled ? "红绿灯计数已启用" : "红绿灯计数已禁用");
        }
        if (cbMinimalAccentNaviInfoEnabled != null) {
            cbMinimalAccentNaviInfoEnabled.setChecked(isMinimalAccentNaviInfoEnabled);
        }
        if (tvMinimalAccentNaviInfoStatus != null) {
            tvMinimalAccentNaviInfoStatus.setText(isMinimalAccentNaviInfoEnabled ? "已启用" : "已禁用");
        }
        if (cbMinimalSpeedLimitEnabled != null) {
            cbMinimalSpeedLimitEnabled.setChecked(isMinimalSpeedLimitEnabled);
        }
        if (tvMinimalSpeedLimitStatus != null) {
            tvMinimalSpeedLimitStatus.setText(isMinimalSpeedLimitEnabled ? "限速显示已启用" : "限速显示已禁用");
        }
        if (cbClusterMirrorEnabled != null) {
            cbClusterMirrorEnabled.setChecked(clusterMirrorEnabled);
        }
        if (tvClusterMirrorStatus != null) {
            tvClusterMirrorStatus.setText(clusterMirrorEnabled ? "已启用副屏镜像投屏" : "已禁用副屏投屏");
        }
        if (cbAutoStartEnabled != null) {
            cbAutoStartEnabled.setChecked(autoStartEnabled);
        }
        if (tvAutoStartStatus != null) {
            tvAutoStartStatus.setText(autoStartEnabled ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
        }
        if (cbTrafficLightFillEnabled != null) {
            cbTrafficLightFillEnabled.setChecked(isTrafficLightFillEnabled);
        }
        if (tvTrafficLightFillStatus != null) {
            tvTrafficLightFillStatus.setText(isTrafficLightFillEnabled ? "红绿灯胶囊背景已填充灯色" : "深蓝胶囊背景");
        }

        if (cbTrafficLightCapsuleEnabled != null) {
            cbTrafficLightCapsuleEnabled.setChecked(isTrafficLightCapsuleEnabled);
        }
        if (tvTrafficLightCapsuleStatus != null) {
            tvTrafficLightCapsuleStatus.setText(isTrafficLightCapsuleEnabled ? "显示胶囊深蓝色背景" : "隐藏胶囊背景");
        }

        if (cbTrafficLightIconEnabled != null) {
            cbTrafficLightIconEnabled.setChecked(isTrafficLightIconEnabled);
        }
        if (tvTrafficLightIconStatus != null) {
            tvTrafficLightIconStatus.setText(isTrafficLightIconEnabled ? "胶囊灯图图标已显示" : "胶囊灯图图标已隐藏");
        }
        updateCountdownFontSelection();
        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setVisibility(clusterMirrorEnabled ? View.VISIBLE : View.GONE);
        }
        updateClusterDisplaySelectStatus();

        if (cbNormalTmcEnabled != null) {
            cbNormalTmcEnabled.setChecked(normalTmcEnabled);
        }
        if (tvNormalTmcStatus != null) {
            tvNormalTmcStatus.setText(normalTmcEnabled ? "TMC路况进度条已启用" : "TMC路况进度条已禁用");
        }
        if (cbNormalBottomInfoEnabled != null) {
            cbNormalBottomInfoEnabled.setChecked(normalBottomInfoEnabled);
        }
        if (tvNormalBottomInfoStatus != null) {
            tvNormalBottomInfoStatus.setText(normalBottomInfoEnabled ? "底栏到达信息已启用" : "底栏到达信息已禁用");
        }
        if (cbNormalCruiseInfoEnabled != null) {
            cbNormalCruiseInfoEnabled.setChecked(normalCruiseInfoEnabled);
        }
        if (tvNormalCruiseInfoStatus != null) {
            tvNormalCruiseInfoStatus.setText(normalCruiseInfoEnabled ? "第一排图文信息已启用" : "第一排图文信息已禁用");
        }
        if (cbHideNormalCruiseSpeedEnabled != null) {
            cbHideNormalCruiseSpeedEnabled.setChecked(hideNormalCruiseSpeed);
        }
        if (tvHideNormalCruiseSpeedStatus != null) {
            tvHideNormalCruiseSpeedStatus.setText(hideNormalCruiseSpeed ? "已隐藏常规巡航车速" : "常规巡航时显示车速");
        }
        if (cbMinimalLaneEnabled != null) {
            cbMinimalLaneEnabled.setChecked(minimalLaneEnabled);
        }
        if (tvMinimalLaneStatus != null) {
            tvMinimalLaneStatus.setText(minimalLaneEnabled ? "车道线已启用" : "车道线已禁用");
        }

        applyThemeToViews();

        initThemeColorChips();
    }

    public void selectStartupMode(int mode) {
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

    public void setStartupMode(int mode) {
        if (startupMode == mode) return;
        startupMode = mode;
        updateStartupSelection();
        savePreferences();
    }

    public void showAmapSelectionDialog() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        // 在 Android 15 上，有时即使加了 queries，getInstalledApplications 依然会被系统阉割过滤
        // 我们直接按包名硬拿一次，如果存在就手动塞进列表里
        String[] knownPackages = {
            "com.autonavi.amapautp", 
            "com.autonavi.amapauto", 
            "com.autonavi.minimap"
        };
        for (String kp : knownPackages) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(kp, PackageManager.GET_META_DATA);
                boolean exists = false;
                for (ApplicationInfo a : apps) {
                    if (kp.equals(a.packageName)) {
                        exists = true; break;
                    }
                }
                if (!exists) {
                    apps.add(info);
                }
            } catch (Exception e) {
                // 不存在该包名，忽略
            }
        }

        List<ApplicationInfo> amapApps = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            CharSequence labelSeq = pm.getApplicationLabel(app);
            String label = labelSeq != null ? labelSeq.toString() : "";
            if (app.packageName != null) {
                String pkg = app.packageName.toLowerCase();
                if (pkg.contains("autonavi") || pkg.contains("amap") || label.contains("高德")) {
                    amapApps.add(app);
                }
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

    public void updateStartupSelection() {
        rbNormalStart.setChecked(startupMode == 0);
        rbServiceOnly.setChecked(startupMode == 1);
        if (rbStartAmap != null) rbStartAmap.setChecked(startupMode == 2);
        int accentColor = getAccentColor();
        cardNormalStart.setStrokeColor(startupMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardServiceOnly.setStrokeColor(startupMode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardStartAmap != null) cardStartAmap.setStrokeColor(startupMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    public void selectStyle(int mode) {
        if (styleMode == mode) return;
        styleMode = mode;
        isMinimalStyle = (mode == 1);
        updateStyleSelection();
        updateSeekBarToCurrentScale();
        savePreferences();
        updateFloatingWindowStyle();
    }

    public void updateStyleSelection() {
        rbNormal.setChecked(styleMode == 0);
        rbMinimal.setChecked(styleMode == 1);
        rbFull.setChecked(styleMode == 2);
        int accentColor = getAccentColor();
        cardNormal.setStrokeColor(styleMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardMinimal.setStrokeColor(styleMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardFull.setStrokeColor(styleMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    public void selectCruiseStyle(int mode) {
        if (cruiseStyleMode == mode) return;
        cruiseStyleMode = mode;
        updateCruiseStyleSelection();
        updateSeekBarToCurrentScale();
        savePreferences();
        updateFloatingWindowStyle();
    }

    public void updateCruiseStyleSelection() {
        rbCruiseNormal.setChecked(cruiseStyleMode == 0);
        rbCruiseMinimal.setChecked(cruiseStyleMode == 1);
        rbCruiseFull.setChecked(cruiseStyleMode == 2);
        int accentColor = getAccentColor();
        cardCruiseNormal.setStrokeColor(cruiseStyleMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardCruiseMinimal.setStrokeColor(cruiseStyleMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardCruiseFull.setStrokeColor(cruiseStyleMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    public void selectBackgroundMode(int mode) {
        if (backgroundMode == mode) return;
        backgroundMode = mode;
        updateBackgroundModeSelection();
        savePreferences();
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.setBackgroundMode(mode);
        }
    }

    public void updateBackgroundModeSelection() {
        if (rbBgDark != null) rbBgDark.setChecked(backgroundMode == 0);
        if (rbBgSemi != null) rbBgSemi.setChecked(backgroundMode == 1);
        if (rbBgTransparent != null) rbBgTransparent.setChecked(backgroundMode == 2);
        int accentColor = getAccentColor();
        if (cardBgDark != null) cardBgDark.setStrokeColor(backgroundMode == 0 ? accentColor : Color.parseColor("#444444"));
        if (cardBgSemi != null) cardBgSemi.setStrokeColor(backgroundMode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardBgTransparent != null) cardBgTransparent.setStrokeColor(backgroundMode == 2 ? accentColor : Color.parseColor("#444444"));
    }

    public void selectCountdownFont(int index) {
        if (countdownFontIndex == index) return;
        countdownFontIndex = index;
        updateCountdownFontSelection();
        savePreferences();
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.refreshWindow();
        }
    }

    public void updateCountdownFontSelection() {
        if (rbFontDefault != null) rbFontDefault.setChecked(countdownFontIndex == 0);
        if (rbFontOne != null) rbFontOne.setChecked(countdownFontIndex == 1);
        if (rbFontTwo != null) rbFontTwo.setChecked(countdownFontIndex == 2);
        if (rbFontThree != null) rbFontThree.setChecked(countdownFontIndex == 3);

        int accentColor = getAccentColor();
        if (cardFontDefault != null) cardFontDefault.setStrokeColor(countdownFontIndex == 0 ? accentColor : Color.parseColor("#444444"));
        if (cardFontOne != null) cardFontOne.setStrokeColor(countdownFontIndex == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardFontTwo != null) cardFontTwo.setStrokeColor(countdownFontIndex == 2 ? accentColor : Color.parseColor("#444444"));
        if (cardFontThree != null) cardFontThree.setStrokeColor(countdownFontIndex == 3 ? accentColor : Color.parseColor("#444444"));
    }



    public void selectTrafficLightStyle(int style) {
        if (trafficLightStyle == style) return;
        trafficLightStyle = style;
        updateTrafficLightStyleSelection();
        savePreferences();
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.refreshWindow();
        }
    }

    public void updateTrafficLightStyleSelection() {
        int accentColor = getAccentColor();
        for (int i = 0; i < cardTrafficLightStyle.length; i++) {
            if (cardTrafficLightStyle[i] != null) {
                cardTrafficLightStyle[i].setStrokeColor(i == trafficLightStyle ? accentColor : Color.parseColor("#444444"));
            }
        }
    }

    public void savePreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_MINIMAL, isMinimalStyle)
                .putInt(KEY_STYLE_MODE, styleMode)
                .putInt("cruise_style_mode", cruiseStyleMode)
                .putInt(KEY_THEME_COLOR, themeColor)
                .putBoolean(KEY_IS_SERVICE_ONLY, startupMode == 1)
                .putInt("startup_mode", startupMode)
                .putString("target_amap_package", targetAmapPackage)
                .putInt("background_mode", backgroundMode)
                .putBoolean("cruise_enabled", cruiseEnabled)
                .putBoolean("normal_navi_lane_enabled", normalLaneEnabled)
                .putBoolean("hide_turn_icon_bg", hideTurnIconBg)
                .putBoolean("hide_lane_line_bg", hideLaneLineBg)
                .putBoolean("hide_on_amap_foreground", avoidForegroundEnabled)
                .putBoolean("hide_on_cross_map", crossMapHideEnabled)
                .putBoolean("overspeed_warning_enabled", overspeedWarningEnabled)
                .putInt("overspeed_threshold", overspeedThreshold)
                .putBoolean("cluster_mirror_enabled", clusterMirrorEnabled)
                .putInt("cluster_display_id", clusterDisplayId)
                .putBoolean("hide_main_when_cluster_active", hideMainWhenClusterActive)
                .putBoolean("auto_start", autoStartEnabled)
                .putBoolean("traffic_light_fill_enabled", isTrafficLightFillEnabled)
                .putBoolean("traffic_light_capsule_enabled", isTrafficLightCapsuleEnabled)
                .putBoolean("traffic_light_icon_enabled", isTrafficLightIconEnabled)
                .putInt("countdown_font_index", countdownFontIndex)
                .putInt("traffic_light_style", trafficLightStyle)
                .putBoolean("normal_navi_tmc_enabled", normalTmcEnabled)
                .putBoolean("normal_navi_bottom_info_enabled", normalBottomInfoEnabled)
                .putBoolean("normal_cruise_info_enabled", normalCruiseInfoEnabled)
                .putBoolean("minimal_navi_lane_enabled", minimalLaneEnabled);
        editor.putBoolean("hide_normal_cruise_speed", hideNormalCruiseSpeed);
        editor.putBoolean("minimal_camera_enabled", isMinimalCameraEnabled);
        editor.putBoolean("minimal_road_name_enabled", isMinimalRoadNameEnabled);
        editor.putBoolean("minimal_direction_enabled", isMinimalDirectionEnabled);
        editor.putBoolean("minimal_turn_info_enabled", isMinimalTurnInfoEnabled);
        editor.putBoolean("minimal_speed_enabled", isMinimalSpeedEnabled);
        editor.putBoolean("minimal_light_count_enabled", isMinimalLightCountEnabled);
        editor.putBoolean("minimal_accent_navi_info_enabled", isMinimalAccentNaviInfoEnabled);
        editor.putBoolean("minimal_autocenter_enabled", isMinimalAutocenterEnabled);
        editor.putBoolean("minimal_speed_limit_enabled", isMinimalSpeedLimitEnabled);
        editor.putInt("bg_color_day", bgColorDay);
        editor.putInt("bg_color_night", bgColorNight);
        editor.putInt("text_primary_day", textPrimaryDay);
        editor.putInt("text_primary_night", textPrimaryNight);
        editor.putInt("text_secondary_day", textSecondaryDay);
        editor.putInt("text_secondary_night", textSecondaryNight);
        editor.putInt("text_hint_day", textHintDay);
        editor.putInt("text_hint_night", textHintNight);
        editor.putInt("normal_turn_icon_color_day", normalTurnIconColorDay);
        editor.putInt("normal_turn_icon_color_night", normalTurnIconColorNight);
        editor.putInt("normal_turn_icon_bg_color_day", normalTurnIconBgColorDay);
        editor.putInt("normal_turn_icon_bg_color_night", normalTurnIconBgColorNight);
        editor.putInt("full_middle_bg_color_day", fullMiddleBgColorDay);
        editor.putInt("full_middle_bg_color_night", fullMiddleBgColorNight);
        editor.apply();
    }

    public void initThemeColorChips() {
        if (llThemeColors == null) return;
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

    public void updateSwitchTheme(SwitchCompat switchView, int activeColor) {
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

    public void applyThemeToViews() {
        int accentColor = getAccentColor();
        ColorStateList accentColorStateList = ColorStateList.valueOf(accentColor);

        // 更新选择项卡片的描边颜色
        updateStartupSelection();
        updateStyleSelection();
        updateCruiseStyleSelection();
        updateBackgroundModeSelection();
        updateCountdownFontSelection();
        updateTrafficLightStyleSelection();

        // 单选按钮（RadioButton）的着色
        if (rbStartAmap != null) {
            CompoundButtonCompat.setButtonTintList(rbStartAmap, accentColorStateList);
        }

        // 更新单选按钮（RadioButton）的着色
        CompoundButtonCompat.setButtonTintList(rbNormal, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbMinimal, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbFull, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbCruiseNormal, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbCruiseMinimal, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbCruiseFull, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbServiceOnly, accentColorStateList);
        CompoundButtonCompat.setButtonTintList(rbNormalStart, accentColorStateList);
        if (rbBgDark != null) CompoundButtonCompat.setButtonTintList(rbBgDark, accentColorStateList);
        if (rbBgSemi != null) CompoundButtonCompat.setButtonTintList(rbBgSemi, accentColorStateList);
        if (rbBgTransparent != null) CompoundButtonCompat.setButtonTintList(rbBgTransparent, accentColorStateList);
        if (rbFontDefault != null) CompoundButtonCompat.setButtonTintList(rbFontDefault, accentColorStateList);
        if (rbFontOne != null) CompoundButtonCompat.setButtonTintList(rbFontOne, accentColorStateList);
        if (rbFontTwo != null) CompoundButtonCompat.setButtonTintList(rbFontTwo, accentColorStateList);
        if (rbFontThree != null) CompoundButtonCompat.setButtonTintList(rbFontThree, accentColorStateList);

        // 更新开关（SwitchCompat）的主题颜色
        updateSwitchTheme(cbCruiseEnabled, accentColor);
        updateSwitchTheme(cbNormalLaneEnabled, accentColor);
        updateSwitchTheme(cbHideTurnIconBg, accentColor);
        updateSwitchTheme(cbHideLaneLineBgEnabled, accentColor);
        updateSwitchTheme(cbAvoidForegroundEnabled, accentColor);
        updateSwitchTheme(cbCrossMapHideEnabled, accentColor);
        updateSwitchTheme(cbOverspeedWarningEnabled, accentColor);
        updateSwitchTheme(cbMinimalCameraEnabled, accentColor);
        updateSwitchTheme(cbMinimalAutocenterEnabled, accentColor);
        updateSwitchTheme(cbClusterMirrorEnabled, accentColor);
        updateSwitchTheme(cbHideMainWhenClusterActive, accentColor);
        updateSwitchTheme(cbAutoStartEnabled, accentColor);
        updateSwitchTheme(cbTrafficLightFillEnabled, accentColor);
        updateSwitchTheme(cbTrafficLightCapsuleEnabled, accentColor);
        updateSwitchTheme(cbTrafficLightIconEnabled, accentColor);
        updateSwitchTheme(cbNormalTmcEnabled, accentColor);
        updateSwitchTheme(cbNormalBottomInfoEnabled, accentColor);
        updateSwitchTheme(cbNormalCruiseInfoEnabled, accentColor);
        updateSwitchTheme(cbHideNormalCruiseSpeedEnabled, accentColor);
        updateSwitchTheme(cbMinimalLaneEnabled, accentColor);
        updateSwitchTheme(cbMinimalRoadNameEnabled, accentColor);
        updateSwitchTheme(cbMinimalDirectionEnabled, accentColor);
        updateSwitchTheme(cbMinimalTurnInfoEnabled, accentColor);
        updateSwitchTheme(cbMinimalSpeedEnabled, accentColor);
        updateSwitchTheme(cbMinimalLightCountEnabled, accentColor);
        updateSwitchTheme(cbMinimalAccentNaviInfoEnabled, accentColor);
        updateSwitchTheme(cbMinimalSpeedLimitEnabled, accentColor);
 
        // 更新 SeekBar 与文本颜色
        if (sbScale.getProgressDrawable() != null) {
            android.graphics.drawable.Drawable progressDrawable =
                    DrawableCompat.wrap(sbScale.getProgressDrawable().mutate());
            DrawableCompat.setTint(progressDrawable, accentColor);
            sbScale.setProgressDrawable(progressDrawable);
        }
        if (sbScale.getThumb() != null) {
            android.graphics.drawable.Drawable thumbDrawable =
                    DrawableCompat.wrap(sbScale.getThumb().mutate());
            DrawableCompat.setTintList(thumbDrawable, accentColorStateList);
            sbScale.setThumb(thumbDrawable);
        }
        tvScaleValue.setTextColor(accentColor);

        if (sbClusterScale != null) {
            if (sbClusterScale.getProgressDrawable() != null) {
                android.graphics.drawable.Drawable progressDrawable =
                        DrawableCompat.wrap(sbClusterScale.getProgressDrawable().mutate());
                DrawableCompat.setTint(progressDrawable, accentColor);
                sbClusterScale.setProgressDrawable(progressDrawable);
            }
            if (sbClusterScale.getThumb() != null) {
                android.graphics.drawable.Drawable thumbDrawable =
                        DrawableCompat.wrap(sbClusterScale.getThumb().mutate());
                DrawableCompat.setTintList(thumbDrawable, accentColorStateList);
                sbClusterScale.setThumb(thumbDrawable);
            }
        }
        if (tvClusterScaleValue != null) {
            tvClusterScaleValue.setTextColor(accentColor);
        }

        tvStyle.setTextColor(accentColor);
        tvSys.setTextColor(accentColor);
        tvOperation.setTextColor(accentColor);

        if (tvTitleClusterSettings != null) tvTitleClusterSettings.setTextColor(accentColor);
        if (tvTitleLayoutNormal != null) tvTitleLayoutNormal.setTextColor(accentColor);
        if (tvTitleLayoutMinimal != null) tvTitleLayoutMinimal.setTextColor(accentColor);
        if (tvTitleAboutSoftware != null) tvTitleAboutSoftware.setTextColor(accentColor);
        if (tvTitleAboutDevice != null) tvTitleAboutDevice.setTextColor(accentColor);
        if (tvTitleDisplayInfo != null) tvTitleDisplayInfo.setTextColor(accentColor);

        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setTextColor(accentColor);
        }

        // Apply dynamic accent color to left menu indicator lines
        if (indicatorSystemAppearance != null) {
            indicatorSystemAppearance.setBackgroundColor(accentColor);
        }
        if (indicatorFeaturesAvoidance != null) {
            indicatorFeaturesAvoidance.setBackgroundColor(accentColor);
        }
        if (indicatorLayoutNormal != null) {
            indicatorLayoutNormal.setBackgroundColor(accentColor);
        }
        if (indicatorLayoutMinimal != null) {
            indicatorLayoutMinimal.setBackgroundColor(accentColor);
        }

        if (indicatorAboutUs != null) {
            indicatorAboutUs.setBackgroundColor(accentColor);
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

    public void selectThemeColor(int index) {
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

    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public int getAccentColor() {
        return isDarkColor(themeColor) ? Color.WHITE : themeColor;
    }

    public void setupListeners() {
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
        cardCruiseNormal.setOnClickListener(v -> selectCruiseStyle(0));
        cardCruiseMinimal.setOnClickListener(v -> selectCruiseStyle(1));
        cardCruiseFull.setOnClickListener(v -> selectCruiseStyle(2));
        if (cardBgDark != null) cardBgDark.setOnClickListener(v -> selectBackgroundMode(0));
        if (cardBgSemi != null) cardBgSemi.setOnClickListener(v -> selectBackgroundMode(1));
        if (cardBgTransparent != null) cardBgTransparent.setOnClickListener(v -> selectBackgroundMode(2));
        for (int i = 0; i < cardTrafficLightStyle.length; i++) {
            final int style = i;
            if (cardTrafficLightStyle[i] != null) {
                cardTrafficLightStyle[i].setOnClickListener(v -> selectTrafficLightStyle(style));
            }
        }
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

        cbHideTurnIconBg.setChecked(hideTurnIconBg);
        if (tvHideTurnIconBgStatus != null) {
            tvHideTurnIconBgStatus.setText(hideTurnIconBg ? "背景已隐藏" : "背景已显示");
        }
        CompoundButton.OnCheckedChangeListener hideTurnIconBgListener = (buttonView, isChecked) -> {
            hideTurnIconBg = isChecked;
            savePreferences();
            if (tvHideTurnIconBgStatus != null) {
                tvHideTurnIconBgStatus.setText(isChecked ? "背景已隐藏" : "背景已显示");
            }
            // 立即刷新悬浮窗
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        };
        cbHideTurnIconBg.setOnCheckedChangeListener(hideTurnIconBgListener);
        if (cardHideTurnIconBgToggle != null) {
            cardHideTurnIconBgToggle.setOnClickListener(v -> cbHideTurnIconBg.toggle());
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

        cbCrossMapHideEnabled.setChecked(crossMapHideEnabled);
        if (tvCrossMapHideStatus != null) {
            tvCrossMapHideStatus.setText(crossMapHideEnabled ? "路口放大图时隐藏悬浮窗" : "路口放大图时正常显示浮窗");
        }
        CompoundButton.OnCheckedChangeListener crossMapHideListener = (buttonView, isChecked) -> {
            crossMapHideEnabled = isChecked;
            savePreferences();
            if (tvCrossMapHideStatus != null) {
                tvCrossMapHideStatus.setText(isChecked ? "路口放大图时隐藏悬浮窗" : "路口放大图时正常显示浮窗");
            }
            // 立即更新悬浮窗可见性
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.updateFloatingWindowVisibility();
            }
        };
        cbCrossMapHideEnabled.setOnCheckedChangeListener(crossMapHideListener);
        if (cardCrossMapHideToggle != null) {
            cardCrossMapHideToggle.setOnClickListener(v -> cbCrossMapHideEnabled.toggle());
        }

        cbHideLaneLineBgEnabled.setChecked(hideLaneLineBg);
        if (tvHideLaneLineBgStatus != null) {
            tvHideLaneLineBgStatus.setText(hideLaneLineBg ? "背景已隐藏" : "背景已显示");
        }
        CompoundButton.OnCheckedChangeListener hideLaneLineBgListener = (buttonView, isChecked) -> {
            hideLaneLineBg = isChecked;
            savePreferences();
            if (tvHideLaneLineBgStatus != null) {
                tvHideLaneLineBgStatus.setText(isChecked ? "背景已隐藏" : "背景已显示");
            }
            // 立即刷新悬浮窗
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        };
        cbHideLaneLineBgEnabled.setOnCheckedChangeListener(hideLaneLineBgListener);
        if (cardHideLaneLineBgToggle != null) {
            cardHideLaneLineBgToggle.setOnClickListener(v -> cbHideLaneLineBgEnabled.toggle());
        }

        cbOverspeedWarningEnabled.setChecked(overspeedWarningEnabled);
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        updateThresholdChips();
        if (llOverspeedThresholdRow != null) {
            llOverspeedThresholdRow.setVisibility(overspeedWarningEnabled ? View.VISIBLE : View.GONE);
        }
        CompoundButton.OnCheckedChangeListener overspeedWarningListener = (buttonView, isChecked) -> {
            overspeedWarningEnabled = isChecked;
            savePreferences();
            if (tvOverspeedWarningStatus != null) {
                tvOverspeedWarningStatus.setText(isChecked ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
            }
            if (llOverspeedThresholdRow != null) {
                llOverspeedThresholdRow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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

        // 阈值 chip 点击监听
        int[] thresholdValues = {0, 10, 20, 30, 50};
        for (int i = 0; i < overspeedThresholdChips.length; i++) {
            final int value = thresholdValues[i];
            final View chip = overspeedThresholdChips[i];
            chip.setOnClickListener(v -> {
                overspeedThreshold = value;
                savePreferences();
                updateThresholdChips();
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }

        cbMinimalCameraEnabled.setChecked(isMinimalCameraEnabled);
        if (tvMinimalCameraStatus != null) {
            tvMinimalCameraStatus.setText(isMinimalCameraEnabled ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
        }
        CompoundButton.OnCheckedChangeListener minimalCameraListener = (buttonView, isChecked) -> {
            isMinimalCameraEnabled = isChecked;
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

        cbHideMainWhenClusterActive.setChecked(hideMainWhenClusterActive);
        if (tvHideMainWhenClusterActiveStatus != null) {
            tvHideMainWhenClusterActiveStatus.setText(hideMainWhenClusterActive ? "副屏成功显示后自动隐藏主屏悬浮窗" : "已关闭该功能，主副屏同时显示");
        }
        CompoundButton.OnCheckedChangeListener hideMainListener = (buttonView, isChecked) -> {
            hideMainWhenClusterActive = isChecked;
            savePreferences();
            if (tvHideMainWhenClusterActiveStatus != null) {
                tvHideMainWhenClusterActiveStatus.setText(isChecked ? "副屏成功显示后自动隐藏主屏悬浮窗" : "已关闭该功能，主副屏同时显示");
            }
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.updateFloatingWindowVisibility();
            }
        };
        cbHideMainWhenClusterActive.setOnCheckedChangeListener(hideMainListener);
        if (cardHideMainWhenClusterActive != null) {
            cardHideMainWhenClusterActive.setOnClickListener(v -> cbHideMainWhenClusterActive.toggle());
        }

        cbAutoStartEnabled.setChecked(autoStartEnabled);
        if (tvAutoStartStatus != null) {
            tvAutoStartStatus.setText(autoStartEnabled ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
        }
        CompoundButton.OnCheckedChangeListener autoStartListener = (buttonView, isChecked) -> {
            autoStartEnabled = isChecked;
            savePreferences();
            if (tvAutoStartStatus != null) {
                tvAutoStartStatus.setText(isChecked ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
            }
        };
        cbAutoStartEnabled.setOnCheckedChangeListener(autoStartListener);
        if (cardAutoStartToggle != null) {
            cardAutoStartToggle.setOnClickListener(v -> cbAutoStartEnabled.toggle());
        }

        // 红绿灯填充背景样式开关
        if (cbTrafficLightFillEnabled != null) {
            cbTrafficLightFillEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isTrafficLightFillEnabled = isChecked;
                savePreferences();
                if (tvTrafficLightFillStatus != null) {
                    tvTrafficLightFillStatus.setText(isChecked ? "红绿灯胶囊背景已填充灯色" : "深蓝胶囊背景");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardTrafficLightFillToggle != null) {
            cardTrafficLightFillToggle.setOnClickListener(v -> {
                if (cbTrafficLightFillEnabled != null) {
                    cbTrafficLightFillEnabled.toggle();
                }
            });
        }

        // 默认胶囊透明开关
        if (cbTrafficLightCapsuleEnabled != null) {
            cbTrafficLightCapsuleEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isTrafficLightCapsuleEnabled = isChecked;
                savePreferences();
                if (tvTrafficLightCapsuleStatus != null) {
                    tvTrafficLightCapsuleStatus.setText(isChecked ? "显示胶囊深蓝色背景" : "隐藏胶囊背景");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardTrafficLightCapsuleToggle != null) {
            cardTrafficLightCapsuleToggle.setOnClickListener(v -> {
                if (cbTrafficLightCapsuleEnabled != null) {
                    cbTrafficLightCapsuleEnabled.toggle();
                }
            });
        }

        // 胶囊灯图显示开关
        if (cbTrafficLightIconEnabled != null) {
            cbTrafficLightIconEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isTrafficLightIconEnabled = isChecked;
                savePreferences();
                if (tvTrafficLightIconStatus != null) {
                    tvTrafficLightIconStatus.setText(isChecked ? "胶囊灯图图标已显示" : "胶囊灯图图标已隐藏");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardTrafficLightIconToggle != null) {
            cardTrafficLightIconToggle.setOnClickListener(v -> {
                if (cbTrafficLightIconEnabled != null) {
                    cbTrafficLightIconEnabled.toggle();
                }
            });
        }

        // 倒计时字体选择绑定
        if (cardFontDefault != null) cardFontDefault.setOnClickListener(v -> selectCountdownFont(0));
        if (cardFontOne != null) cardFontOne.setOnClickListener(v -> selectCountdownFont(1));
        if (cardFontTwo != null) cardFontTwo.setOnClickListener(v -> selectCountdownFont(2));
        if (cardFontThree != null) cardFontThree.setOnClickListener(v -> selectCountdownFont(3));



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

        if (sbClusterScale != null) {
            sbClusterScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float currentScale = (progress / 15.0f) * 1.5f + 0.5f;
                    if (tvClusterScaleValue != null) {
                        tvClusterScaleValue.setText(String.format("%.1fx", currentScale));
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    float newScale = (seekBar.getProgress() / 15.0f) * 1.5f + 0.5f;
                    FloatingWindowManager manager = FloatingWindowManager.getInstance();
                    if (manager != null) {
                        manager.setClusterScale(newScale);
                    }
                }
            });
        }

        if (cbNormalTmcEnabled != null) {
            cbNormalTmcEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                normalTmcEnabled = isChecked;
                savePreferences();
                if (tvNormalTmcStatus != null) {
                    tvNormalTmcStatus.setText(isChecked ? "TMC路况进度条已启用" : "TMC路况进度条已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalTmcToggle != null) {
            cardNormalTmcToggle.setOnClickListener(v -> {
                if (cbNormalTmcEnabled != null) {
                    cbNormalTmcEnabled.toggle();
                }
            });
        }

        if (cbNormalBottomInfoEnabled != null) {
            cbNormalBottomInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                normalBottomInfoEnabled = isChecked;
                savePreferences();
                if (tvNormalBottomInfoStatus != null) {
                    tvNormalBottomInfoStatus.setText(isChecked ? "底栏到达信息已启用" : "底栏到达信息已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalBottomInfoToggle != null) {
            cardNormalBottomInfoToggle.setOnClickListener(v -> {
                if (cbNormalBottomInfoEnabled != null) {
                    cbNormalBottomInfoEnabled.toggle();
                }
            });
        }

        if (cbNormalCruiseInfoEnabled != null) {
            cbNormalCruiseInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                normalCruiseInfoEnabled = isChecked;
                savePreferences();
                if (tvNormalCruiseInfoStatus != null) {
                    tvNormalCruiseInfoStatus.setText(isChecked ? "第一排图文信息已启用" : "第一排图文信息已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalCruiseInfoToggle != null) {
            cardNormalCruiseInfoToggle.setOnClickListener(v -> {
                if (cbNormalCruiseInfoEnabled != null) {
                    cbNormalCruiseInfoEnabled.toggle();
                }
            });
        }

        if (cbHideNormalCruiseSpeedEnabled != null) {
            cbHideNormalCruiseSpeedEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                hideNormalCruiseSpeed = isChecked;
                savePreferences();
                if (tvHideNormalCruiseSpeedStatus != null) {
                    tvHideNormalCruiseSpeedStatus.setText(isChecked ? "已隐藏常规巡航车速" : "常规巡航时显示车速");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardHideNormalCruiseSpeedToggle != null) {
            cardHideNormalCruiseSpeedToggle.setOnClickListener(v -> {
                if (cbHideNormalCruiseSpeedEnabled != null) {
                    cbHideNormalCruiseSpeedEnabled.toggle();
                }
            });
        }

        if (cbMinimalLaneEnabled != null) {
            cbMinimalLaneEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                minimalLaneEnabled = isChecked;
                savePreferences();
                if (tvMinimalLaneStatus != null) {
                    tvMinimalLaneStatus.setText(isChecked ? "车道线已启用" : "车道线已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalLaneToggle != null) {
            cardMinimalLaneToggle.setOnClickListener(v -> {
                if (cbMinimalLaneEnabled != null) {
                    cbMinimalLaneEnabled.toggle();
                }
            });
        }

        if (cbMinimalRoadNameEnabled != null) {
            cbMinimalRoadNameEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalRoadNameEnabled = isChecked;
                savePreferences();
                if (tvMinimalRoadNameStatus != null) {
                    tvMinimalRoadNameStatus.setText(isChecked ? "道路名称已启用" : "道路名称已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalRoadNameToggle != null) {
            cardMinimalRoadNameToggle.setOnClickListener(v -> {
                if (cbMinimalRoadNameEnabled != null) {
                    cbMinimalRoadNameEnabled.toggle();
                }
            });
        }

        if (cbMinimalDirectionEnabled != null) {
            cbMinimalDirectionEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalDirectionEnabled = isChecked;
                savePreferences();
                if (tvMinimalDirectionStatus != null) {
                    tvMinimalDirectionStatus.setText(isChecked ? "方向显示已启用" : "方向显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalDirectionToggle != null) {
            cardMinimalDirectionToggle.setOnClickListener(v -> {
                if (cbMinimalDirectionEnabled != null) {
                    cbMinimalDirectionEnabled.toggle();
                }
            });
        }

        if (cbMinimalTurnInfoEnabled != null) {
            cbMinimalTurnInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalTurnInfoEnabled = isChecked;
                savePreferences();
                if (tvMinimalTurnInfoStatus != null) {
                    tvMinimalTurnInfoStatus.setText(isChecked ? "转向信息已启用" : "转向信息已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalTurnInfoToggle != null) {
            cardMinimalTurnInfoToggle.setOnClickListener(v -> {
                if (cbMinimalTurnInfoEnabled != null) {
                    cbMinimalTurnInfoEnabled.toggle();
                }
            });
        }

        if (cbMinimalSpeedEnabled != null) {
            cbMinimalSpeedEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalSpeedEnabled = isChecked;
                savePreferences();
                if (tvMinimalSpeedStatus != null) {
                    tvMinimalSpeedStatus.setText(isChecked ? "车速显示已启用" : "车速显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalSpeedToggle != null) {
            cardMinimalSpeedToggle.setOnClickListener(v -> {
                if (cbMinimalSpeedEnabled != null) {
                    cbMinimalSpeedEnabled.toggle();
                }
            });
        }

        if (cbMinimalLightCountEnabled != null) {
            cbMinimalLightCountEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalLightCountEnabled = isChecked;
                savePreferences();
                if (tvMinimalLightCountStatus != null) {
                    tvMinimalLightCountStatus.setText(isChecked ? "红绿灯计数已启用" : "红绿灯计数已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalLightCountToggle != null) {
            cardMinimalLightCountToggle.setOnClickListener(v -> {
                if (cbMinimalLightCountEnabled != null) {
                    cbMinimalLightCountEnabled.toggle();
                }
            });
        }

        if (cbMinimalAccentNaviInfoEnabled != null) {
            cbMinimalAccentNaviInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalAccentNaviInfoEnabled = isChecked;
                savePreferences();
                if (tvMinimalAccentNaviInfoStatus != null) {
                    tvMinimalAccentNaviInfoStatus.setText(isChecked ? "已启用" : "已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalAccentNaviInfoToggle != null) {
            cardMinimalAccentNaviInfoToggle.setOnClickListener(v -> {
                if (cbMinimalAccentNaviInfoEnabled != null) {
                    cbMinimalAccentNaviInfoEnabled.toggle();
                }
            });
        }

        if (cbMinimalSpeedLimitEnabled != null) {
            cbMinimalSpeedLimitEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalSpeedLimitEnabled = isChecked;
                savePreferences();
                if (tvMinimalSpeedLimitStatus != null) {
                    tvMinimalSpeedLimitStatus.setText(isChecked ? "限速显示已启用" : "限速显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalSpeedLimitToggle != null) {
            cardMinimalSpeedLimitToggle.setOnClickListener(v -> {
                if (cbMinimalSpeedLimitEnabled != null) {
                    cbMinimalSpeedLimitEnabled.toggle();
                }
            });
        }

        if (cbMinimalAutocenterEnabled != null) {
            cbMinimalAutocenterEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMinimalAutocenterEnabled = isChecked;
                savePreferences();
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.setAutoCenteringEnabled(isChecked);
                }
            });
        }
        if (cardMinimalAutocenterToggle != null) {
            cardMinimalAutocenterToggle.setOnClickListener(v -> {
                if (cbMinimalAutocenterEnabled != null) {
                    cbMinimalAutocenterEnabled.toggle();
                }
            });
        }

        // Set up click listeners for left menu items
        if (menuSystemAppearance != null) {
            menuSystemAppearance.setOnClickListener(v -> switchMenu(0));
        }
        if (menuColorSettings != null) {
            menuColorSettings.setOnClickListener(v -> switchMenu(6));
        }
        if (menuFeaturesAvoidance != null) {
            menuFeaturesAvoidance.setOnClickListener(v -> switchMenu(1));
        }
        if (menuLayoutNormal != null) {
            menuLayoutNormal.setOnClickListener(v -> switchMenu(2));
        }
        if (menuLayoutMinimal != null) {
            menuLayoutMinimal.setOnClickListener(v -> switchMenu(3));
        }
        if (menuTrafficLight != null) {
            menuTrafficLight.setOnClickListener(v -> switchMenu(4));
        }
        if (menuAboutUs != null) {
            menuAboutUs.setOnClickListener(v -> switchMenu(5));
        }

        if (tvAboutQqGroup != null) {
            tvAboutQqGroup.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("QQ Group", "1106923186");
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "QQ交流群已复制到剪贴板", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (tvAboutGitUrl != null) {
            tvAboutGitUrl.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Git Repo", "https://github.com/shuhao1022/Navi-Link");
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "开源地址已复制到剪贴板", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize default selected panel
        switchMenu(0);
    }

    public void switchMenu(int index) {
        selectedMenuIndex = index;

        // 1. Panels visibility
        if (panelSystemAppearance != null) panelSystemAppearance.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        if (panelColorSettings != null) panelColorSettings.setVisibility(index == 6 ? View.VISIBLE : View.GONE);
        if (panelFeaturesAvoidance != null) panelFeaturesAvoidance.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        if (panelLayoutNormal != null) panelLayoutNormal.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        if (panelLayoutMinimal != null) panelLayoutMinimal.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        if (panelTrafficLight != null) panelTrafficLight.setVisibility(index == 4 ? View.VISIBLE : View.GONE);
        if (panelAboutUs != null) panelAboutUs.setVisibility(index == 5 ? View.VISIBLE : View.GONE);

        // 2. Indicators visibility
        if (indicatorSystemAppearance != null) indicatorSystemAppearance.setVisibility(index == 0 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorColorSettings != null) indicatorColorSettings.setVisibility(index == 6 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorFeaturesAvoidance != null) indicatorFeaturesAvoidance.setVisibility(index == 1 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorLayoutNormal != null) indicatorLayoutNormal.setVisibility(index == 2 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorLayoutMinimal != null) indicatorLayoutMinimal.setVisibility(index == 3 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorTrafficLight != null) indicatorTrafficLight.setVisibility(index == 4 ? View.VISIBLE : View.INVISIBLE);
        if (indicatorAboutUs != null) indicatorAboutUs.setVisibility(index == 5 ? View.VISIBLE : View.INVISIBLE);

        // 3. Menu card background colors (selected gets #FF262626, others transparent)
        if (menuSystemAppearance != null) menuSystemAppearance.setCardBackgroundColor(ColorStateList.valueOf(index == 0 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuColorSettings != null) menuColorSettings.setCardBackgroundColor(ColorStateList.valueOf(index == 6 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuFeaturesAvoidance != null) menuFeaturesAvoidance.setCardBackgroundColor(ColorStateList.valueOf(index == 1 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuLayoutNormal != null) menuLayoutNormal.setCardBackgroundColor(ColorStateList.valueOf(index == 2 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuLayoutMinimal != null) menuLayoutMinimal.setCardBackgroundColor(ColorStateList.valueOf(index == 3 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuTrafficLight != null) menuTrafficLight.setCardBackgroundColor(ColorStateList.valueOf(index == 4 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));
        if (menuAboutUs != null) menuAboutUs.setCardBackgroundColor(ColorStateList.valueOf(index == 5 ? Color.parseColor("#FF262626") : Color.TRANSPARENT));

        // 4. Menu text colors (selected gets #FFFFFFFF, others #FF888888)
        if (tvMenuSystemAppearance != null) tvMenuSystemAppearance.setTextColor(index == 0 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuColorSettings != null) tvMenuColorSettings.setTextColor(index == 6 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuFeaturesAvoidance != null) tvMenuFeaturesAvoidance.setTextColor(index == 1 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuLayoutNormal != null) tvMenuLayoutNormal.setTextColor(index == 2 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuLayoutMinimal != null) tvMenuLayoutMinimal.setTextColor(index == 3 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuTrafficLight != null) tvMenuTrafficLight.setTextColor(index == 4 ? Color.WHITE : Color.parseColor("#FF888888"));
        if (tvMenuAboutUs != null) tvMenuAboutUs.setTextColor(index == 5 ? Color.WHITE : Color.parseColor("#FF888888"));
    }

    public void checkPermissionAndStart() {
        if (!OverlayPermissionCompat.canDrawOverlays(this)) {
            tvStatus.setText("需要悬浮窗权限");
            try {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName())),
                        REQUEST_OVERLAY_PERMISSION);
            } catch (android.content.ActivityNotFoundException e) {
                // 车机系统可能被阉割了原生的悬浮窗权限界面，尝试跳转到应用详情页
                Toast.makeText(this, "由于车机系统限制，请在系统设置中手动开启悬浮窗权限", Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                } catch (Exception ex) {
                    Toast.makeText(this, "无法打开设置页面，请前往系统设置授权", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            startFloatingService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION
                && OverlayPermissionCompat.canDrawOverlays(this)) {
            startFloatingService();
        }
    }

    public void startFloatingService() {
        PlatformCompat.startService(this, new Intent(this, AutoMapService.class));
        updateStatusText();
        scheduleStatusRefresh();
    }

    public void updateFloatingWindowStyle() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager == null || !manager.isShowing()) return;
        manager.refreshWindow();
    }

    public void updateFloatingWindowScale() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            float currentScale = (sbScale.getProgress() / 15.0f) * 1.5f + 0.5f;
            manager.updateScale(currentScale);
        }
    }

    /** 切换样式时，把 SeekBar 跳到该样式对应的缩放值 */
    public void updateSeekBarToCurrentScale() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float s;
        String[] keys = {"scale_normal", "scale_minimal", "scale_full"};
        float[] defaults = {1.0f, 1.0f, 1.0f};

        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        int idx;
        if (manager != null && manager.isActive() && manager.getCurrentMode() == FloatingWindowManager.MODE_CRUISE) {
            idx = (cruiseStyleMode == 1) ? 1 : (cruiseStyleMode == 2 ? 2 : 0); // 灵动岛巡航用1，全数据巡航用2，常规巡航用0
        } else {
            idx = Math.max(0, Math.min(styleMode, 2));
        }
        s = sp.getFloat(keys[idx], defaults[idx]);

        sbScale.setProgress((int) (((s - 0.5f) / 1.5f) * 15));
        tvScaleValue.setText(String.format("%.1fx", s));

        // 更新副屏缩放 Seekbar
        float cs = sp.getFloat("scale_cluster", 1.0f);
        if (sbClusterScale != null) {
            sbClusterScale.setProgress((int) (((cs - 0.5f) / 1.5f) * 15));
        }
        if (tvClusterScaleValue != null) {
            tvClusterScaleValue.setText(String.format("%.1fx", cs));
        }
    }

    public void updateStatusText() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null && manager.isShowing()) {
            tvStatus.setText("● 悬浮窗运行中");
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvStatus.setText("○ 悬浮窗未启动");
            tvStatus.setTextColor(Color.parseColor("#888888"));
        }
    }

    public void scheduleStatusRefresh() {
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

    public void showClusterDisplaySelectionDialog() {
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
                    clusterDisplayId = choice.displayId;
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

    public void updateClusterDisplaySelectStatus() {
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

    public void initAboutUsPanel() {
        // App Version
        if (tvAboutAppVersion != null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvAboutAppVersion.setText("v" + versionName);
            } catch (Exception e) {
                tvAboutAppVersion.setText("未知");
            }
        }

        // CPU Info
        if (tvAboutCpuInfo != null) {
            tvAboutCpuInfo.setText(getAboutCpuInfo());
        }

        // RAM Info
        if (tvAboutRamInfo != null) {
            tvAboutRamInfo.setText(getAboutRamInfo());
        }

        // ROM Info
        if (tvAboutRomInfo != null) {
            tvAboutRomInfo.setText(getAboutRomInfo());
        }

        // Android API Level
        if (tvAboutApiLevel != null) {
            tvAboutApiLevel.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        }

        // Display Info
        initDisplayInfo();

    }

    public String getAboutCpuInfo() {
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("/proc/cpuinfo"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Hardware") || line.contains("model name") || line.contains("Processor")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        String hardware = parts[1].trim();
                        int cores = Runtime.getRuntime().availableProcessors();
                        String arch = System.getProperty("os.arch");
                        return hardware + " (" + cores + "核 / " + arch + ")";
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cores = Runtime.getRuntime().availableProcessors();
        String arch = System.getProperty("os.arch");
        return Build.HARDWARE + " (" + cores + "核 / " + arch + ")";
    }

    public String getAboutRamInfo() {
        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(android.content.Context.ACTIVITY_SERVICE);
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(mi);
            long totalMem = mi.totalMem;
            long availMem = mi.availMem;
            return formatByteSize(availMem) + " 可用 / 共 " + formatByteSize(totalMem);
        }
        return "未知";
    }

    public String getAboutRomInfo() {
        try {
            java.io.File path = android.os.Environment.getDataDirectory();
            android.os.StatFs stat = new android.os.StatFs(path.getPath());
            long[] storageStats = PlatformCompat.getStorageStats(stat);
            long blockSize = storageStats[0];
            long totalBlocks = storageStats[1];
            long availableBlocks = storageStats[2];
            long totalRom = totalBlocks * blockSize;
            long availRom = availableBlocks * blockSize;
            return formatByteSize(availRom) + " 可用 / 共 " + formatByteSize(totalRom);
        } catch (Exception e) {
            return "未知";
        }
    }

    public String formatByteSize(long size) {
        double gb = size / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1.0) {
            return String.format(java.util.Locale.US, "%.2f GB", gb);
        }
        double mb = size / (1024.0 * 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", mb);
    }

    public void initDisplayInfo() {
        try {
            android.view.WindowManager wm = (android.view.WindowManager) getSystemService(android.content.Context.WINDOW_SERVICE);
            if (wm != null) {
                android.view.Display display = wm.getDefaultDisplay();
                android.util.DisplayMetrics realMetrics = new android.util.DisplayMetrics();
                android.util.DisplayMetrics appMetrics = new android.util.DisplayMetrics();
                
                display.getRealMetrics(realMetrics);
                display.getMetrics(appMetrics);
                
                // 1. 物理分辨率
                if (tvDisplayPhysicalRes != null) {
                    tvDisplayPhysicalRes.setText(realMetrics.widthPixels + " × " + realMetrics.heightPixels);
                }
                
                // 2. 应用分辨率
                if (tvDisplayAppRes != null) {
                    tvDisplayAppRes.setText(appMetrics.widthPixels + " × " + appMetrics.heightPixels);
                }
                
                // 3. 屏幕密度
                if (tvDisplayDensity != null) {
                    tvDisplayDensity.setText(realMetrics.densityDpi + " dpi (" + String.format(java.util.Locale.US, "%.2f", realMetrics.density) + ")");
                }
                
                // 4. 刷新率
                if (tvDisplayRefreshRate != null) {
                    float refreshRate = display.getRefreshRate();
                    tvDisplayRefreshRate.setText(String.format(java.util.Locale.US, "%.0f Hz", refreshRate));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateThresholdChips() {
        int[] values = {0, 10, 20, 30, 50};
        boolean isDark = ((themeColor >> 16) & 0xFF) * 0.299
                + ((themeColor >> 8) & 0xFF) * 0.587
                + (themeColor & 0xFF) * 0.114 < 100;
        int accentColor = isDark ? Color.WHITE : themeColor;
        for (int i = 0; i < overspeedThresholdChips.length; i++) {
            TextView chip = (TextView) overspeedThresholdChips[i];
            boolean selected = overspeedThreshold == values[i];
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(14));
            if (selected) {
                bg.setColor(accentColor);
                chip.setTextColor(0xFFFFFFFF);
            } else {
                bg.setColor(0x33FFFFFF);
                chip.setTextColor(0xFFAAAAAA);
            }
            chip.setBackground(bg);
        }
    }

    // --- Custom Color Settings logic ---

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public void resetToDefaultColors() {
        bgColorDay = 0xE6F5F5F5;
        bgColorNight = 0xCC121212;
        textPrimaryDay = 0xFF1A1A1A;
        textPrimaryNight = 0xFFFFFFFF;
        textSecondaryDay = 0xFF333333;
        textSecondaryNight = 0xBBFFFFFF;
        textHintDay = 0xFF999999;
        textHintNight = 0xFF888888;
        normalTurnIconColorDay = 0xFFFFFFFF;
        normalTurnIconColorNight = 0xFFFFFFFF;
        normalTurnIconBgColorDay = 0xFF007D5E;
        normalTurnIconBgColorNight = 0xFF007D5E;
        fullMiddleBgColorDay = 0xFF0099FF;
        fullMiddleBgColorNight = 0xFF0099FF;
        updateColorPreviews();
    }

    public void updateColorPreviews() {
        updateColorPreview(viewColorPreviewBgDay, bgColorDay);
        updateColorPreview(viewColorPreviewBgNight, bgColorNight);
        updateColorPreview(viewColorPreviewPrimaryDay, textPrimaryDay);
        updateColorPreview(viewColorPreviewPrimaryNight, textPrimaryNight);
        updateColorPreview(viewColorPreviewSecondaryDay, textSecondaryDay);
        updateColorPreview(viewColorPreviewSecondaryNight, textSecondaryNight);
        updateColorPreview(viewColorPreviewHintDay, textHintDay);
        updateColorPreview(viewColorPreviewHintNight, textHintNight);
        updateColorPreview(viewColorPreviewNormalTurnIconDay, normalTurnIconColorDay);
        updateColorPreview(viewColorPreviewNormalTurnIconNight, normalTurnIconColorNight);
        updateColorPreview(viewColorPreviewNormalTurnBgDay, normalTurnIconBgColorDay);
        updateColorPreview(viewColorPreviewNormalTurnBgNight, normalTurnIconBgColorNight);
        updateColorPreview(viewColorPreviewFullMiddleBgDay, fullMiddleBgColorDay);
        updateColorPreview(viewColorPreviewFullMiddleBgNight, fullMiddleBgColorNight);
    }

    public void updateColorPreview(View view, int color) {
        if (view == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(color);
        gd.setCornerRadius(dpToPx(6));
        gd.setStroke(dpToPx(1), 0x55FFFFFF); // Add thin semi-transparent white border
        view.setBackground(gd);
    }

    public void refreshFloatingWindow() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.applyThemeColor(themeColor);
        }
    }

    public void showColorPickerDialog(String title, int initialColor, OnColorSelectedListener listener) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        if (dialogView == null) return;

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Ensure background is transparent so custom layout round corners show properly
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        if (tvTitle != null) tvTitle.setText(title);

        ColorWheelView colorWheel = dialogView.findViewById(R.id.color_wheel);
        SeekBar sbBrightness = dialogView.findViewById(R.id.sb_brightness);
        SeekBar sbAlpha = dialogView.findViewById(R.id.sb_alpha);
        View viewPreviewColor = dialogView.findViewById(R.id.view_preview_color);
        EditText etHexInput = dialogView.findViewById(R.id.et_hex_input);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_picker);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_picker);

        // Keep HSV + Alpha states
        final float[] hsv = new float[3];
        Color.colorToHSV(initialColor, hsv);
        final int[] curAlpha = {Color.alpha(initialColor)};

        // Helper to update preview card and sliders
        Runnable updateUI = new Runnable() {
            @Override
            public void run() {
                int rgbColor = Color.HSVToColor(hsv);
                int finalColor = (curAlpha[0] << 24) | (rgbColor & 0x00FFFFFF);

                // 1. Update circular wheel pointer color (internal view updates it)
                if (colorWheel != null) {
                    colorWheel.setColor(hsv[0], hsv[1]);
                }

                // 2. Update preview color box background
                if (viewPreviewColor != null) {
                    GradientDrawable previewDrawable = new GradientDrawable();
                    previewDrawable.setColor(finalColor);
                    previewDrawable.setCornerRadius(dpToPx(4));
                    previewDrawable.setStroke(dpToPx(1), 0x33FFFFFF);
                    viewPreviewColor.setBackground(previewDrawable);
                }

                // 3. Update brightness seekbar background gradient
                if (sbBrightness != null) {
                    int pureColor = Color.HSVToColor(new float[]{hsv[0], hsv[1], 1f});
                    GradientDrawable brightnessGrad = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{ 0xFF000000, pureColor }
                    );
                    brightnessGrad.setCornerRadius(dpToPx(6));
                    sbBrightness.setBackground(brightnessGrad);
                    sbBrightness.setProgress((int) (hsv[2] * 100));
                }

                // 4. Update alpha seekbar background gradient
                if (sbAlpha != null) {
                    int transparentColor = 0x00FFFFFF & rgbColor;
                    int opaqueColor = 0xFF000000 | rgbColor;
                    GradientDrawable alphaGrad = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{ transparentColor, opaqueColor }
                    );
                    alphaGrad.setCornerRadius(dpToPx(6));
                    sbAlpha.setBackground(alphaGrad);
                    sbAlpha.setProgress(curAlpha[0]);
                }

                // 5. Update Hex Input box without losing focus
                if (etHexInput != null && !etHexInput.hasFocus()) {
                    etHexInput.setText(String.format("#%08X", finalColor));
                }
            }
        };

        // Initialize UI with initial color
        updateUI.run();

        // 1. Color Wheel Listener
        if (colorWheel != null) {
            colorWheel.setOnColorSelectedListener((hue, saturation) -> {
                hsv[0] = hue;
                hsv[1] = saturation;
                updateUI.run();
            });
        }

        // 2. Brightness Slider Listener
        if (sbBrightness != null) {
            sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        hsv[2] = progress / 100f;
                        updateUI.run();
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // 3. Alpha Slider Listener
        if (sbAlpha != null) {
            sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        curAlpha[0] = progress;
                        updateUI.run();
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // 4. Hex Input Text Box Listener
        if (etHexInput != null) {
            etHexInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (etHexInput.hasFocus()) {
                        String input = s.toString().trim();
                        if (input.startsWith("#")) {
                            input = input.substring(1);
                        }
                        if (input.length() == 8) { // AARRGGBB
                            try {
                                int color = (int) Long.parseLong(input, 16);
                                Color.colorToHSV(color, hsv);
                                curAlpha[0] = Color.alpha(color);
                                updateUI.run();
                            } catch (NumberFormatException ignored) {}
                        } else if (input.length() == 6) { // RRGGBB
                            try {
                                int color = 0xFF000000 | (int) Long.parseLong(input, 16);
                                Color.colorToHSV(color, hsv);
                                curAlpha[0] = 255;
                                updateUI.run();
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    int rgbColor = Color.HSVToColor(hsv);
                    int finalColor = (curAlpha[0] << 24) | (rgbColor & 0x00FFFFFF);
                    listener.onColorSelected(finalColor);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}