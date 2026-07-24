package com.navi.link.activity;

import com.navi.link.R;
import com.navi.link.BuildConfig;
import com.navi.link.delegate.*;
import com.navi.link.window.*;
import com.navi.link.receiver.*;
import com.navi.link.utils.*;
import com.navi.link.view.UpdateDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "floating_config";
    private static final String KEY_IS_MINIMAL = "is_minimal_style";
    private static final String KEY_STYLE_MODE = "style_mode";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String KEY_IS_SERVICE_ONLY = "is_service_only";

    public static final int[] THEME_COLORS = {
            0xFF1A1A1A, 0xFFE53935, 0xFFFF4081, 0xFFFF6D00, 0xFFFF9100, 0xFFFFCA28,
            0xFF8D6E63, 0xFF6DFF00, 0xFF00BFA5, 0xFF4FC3F7, 0xFF1199FF, 0xFF5C6BC0, 0xFFAB47BC
    };

    // Delegates
    public SystemAppearanceDelegate systemAppearanceDelegate;
    public NormalPanelDelegate normalPanelDelegate;
    public MinimalPanelDelegate minimalPanelDelegate;
    public FeaturesPanelDelegate featuresPanelDelegate;
    public TrafficLightPanelDelegate trafficLightPanelDelegate;
    public ColorSettingsDelegate colorSettingsDelegate;
    public AboutUsPanelDelegate aboutUsPanelDelegate;

    // Preference State Variables
    public boolean isMinimalStyle = false;
    public int styleMode = 0;
    public int cruiseStyleMode = 0;
    public boolean isServiceOnlyMode = false;
    public int startupMode = 0;
    public String targetAmapPackage = "";
    public boolean cruiseEnabled = true;
    public boolean normalLaneEnabled = false;
    public boolean hideTurnIconBg = false;
    public boolean avoidForegroundEnabled = false;
    public boolean overspeedWarningEnabled = true;
    public int overspeedThreshold = 0;

    public boolean clusterMirrorEnabled = false;
    public int clusterDisplayId = -1;
    public boolean hideMainWhenClusterActive = false;
    public boolean autoStartEnabled = false;

    public boolean normalTmcEnabled = true;
    public boolean normalBottomInfoEnabled = true;
    public boolean normalCruiseInfoEnabled = true;
    public boolean hideNormalCruiseSpeed = false;
    public int normalNaviWindowWidth = 320;
    public int normalCruiseWindowWidth = 320;
    public int fullNaviWindowWidth = 280;
    public int fullCruiseWindowWidth = 360;
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

    public boolean isTrafficLightFillEnabled = false;
    public int trafficLightStyle = 0;
    public boolean isTrafficLightCapsuleEnabled = true;
    public boolean isTrafficLightIconEnabled = true;
    public int countdownFontIndex = 0;
    public boolean crossMapHideEnabled = false;
    public boolean hideLaneLineBg = false;
    public boolean hideCameraCapsuleBg = false;
    public int dayNightOption = 0;

    public int themeColor = 0xFF4FC3F7;

    // Custom Color Settings
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
    public int laneIconColorDay;
    public int laneIconColorNight;

    // UI Elements - Title & Menu Tabs
    public TextView tvStatus;
    public int selectedMenuIndex = 0;

    public MaterialCardView menuSystemAppearance;
    public MaterialCardView menuColorSettings;
    public MaterialCardView menuFeaturesAvoidance;
    public MaterialCardView menuLayoutNormal;
    public MaterialCardView menuLayoutMinimal;
    public MaterialCardView menuTrafficLight;
    public MaterialCardView menuAboutUs;

    public View indicatorSystemAppearance;
    public View indicatorColorSettings;
    public View indicatorFeaturesAvoidance;
    public View indicatorLayoutNormal;
    public View indicatorLayoutMinimal;
    public View indicatorTrafficLight;
    public View indicatorAboutUs;

    public TextView tvMenuSystemAppearance;
    public TextView tvMenuColorSettings;
    public TextView tvMenuFeaturesAvoidance;
    public TextView tvMenuLayoutNormal;
    public TextView tvMenuLayoutMinimal;
    public TextView tvMenuTrafficLight;
    public TextView tvMenuAboutUs;

    public ScrollView panelSystemAppearance;
    public ScrollView panelColorSettings;
    public ScrollView panelFeaturesAvoidance;
    public ScrollView panelLayoutNormal;
    public ScrollView panelLayoutMinimal;
    public ScrollView panelTrafficLight;
    public ScrollView panelAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int dayNightOpt = sp.getInt("app_day_night_option", 0);
        boolean isNight = sp.getBoolean("is_night_mode", true);
        boolean targetNight = (dayNightOpt == 1) ? false : ((dayNightOpt == 2) ? true : isNight);
        int targetMode = targetNight ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != targetMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_NaviLink);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            EdgeToEdge.enable(this);
        }
        setContentView(R.layout.activity_main);

        initDelegates();
        initViews();
        loadPreferences();
        setupListeners();
        updateStatusText();
        setupUpdateEntry();
        setupDayNightListener();

        if (savedInstanceState == null) {
            checkPermissionAndStart();
            UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME, false, updateCallback(false));
        }
    }

    private void initDelegates() {
        systemAppearanceDelegate = new SystemAppearanceDelegate(this);
        colorSettingsDelegate = new ColorSettingsDelegate(this);
        featuresPanelDelegate = new FeaturesPanelDelegate(this);
        normalPanelDelegate = new NormalPanelDelegate(this);
        minimalPanelDelegate = new MinimalPanelDelegate(this);
        trafficLightPanelDelegate = new TrafficLightPanelDelegate(this);
        aboutUsPanelDelegate = new AboutUsPanelDelegate(this);
    }

    private void restartActivity() {
        recreate();
    }

    private void setupDayNightListener() {
        FloatingWindowManager fwm = FloatingWindowManager.getInstance(this);
        if (fwm != null) {
            fwm.setOnDayNightChangeListener(new FloatingWindowManager.OnDayNightChangeListener() {
                @Override
                public void onDayNightChanged(final boolean isNight) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dayNightOption == 0) {
                                int targetMode = isNight ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                                if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != targetMode) {
                                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetMode);
                                    // setDefaultNightMode will trigger Activity recreate automatically
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public TextView tvVersionStatus;

    public void setupUpdateEntry() {
        TextView tvAppVersion = findViewById(R.id.tv_about_app_version);
        tvVersionStatus = tvAppVersion;
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
        tvStatus = findViewById(R.id.tv_status);

        // Bind Menu Tabs
        menuSystemAppearance = findViewById(R.id.menu_system_appearance);
        menuColorSettings = findViewById(R.id.menu_color_settings);
        menuFeaturesAvoidance = findViewById(R.id.menu_features_avoidance);
        menuLayoutNormal = findViewById(R.id.menu_layout_normal);
        menuLayoutMinimal = findViewById(R.id.menu_layout_minimal);
        menuTrafficLight = findViewById(R.id.menu_traffic_light);
        menuAboutUs = findViewById(R.id.menu_about_us);

        indicatorSystemAppearance = findViewById(R.id.indicator_system_appearance);
        indicatorColorSettings = findViewById(R.id.indicator_color_settings);
        indicatorFeaturesAvoidance = findViewById(R.id.indicator_features_avoidance);
        indicatorLayoutNormal = findViewById(R.id.indicator_layout_normal);
        indicatorLayoutMinimal = findViewById(R.id.indicator_layout_minimal);
        indicatorTrafficLight = findViewById(R.id.indicator_traffic_light);
        indicatorAboutUs = findViewById(R.id.indicator_about_us);

        tvMenuSystemAppearance = findViewById(R.id.tv_menu_system_appearance);
        tvMenuColorSettings = findViewById(R.id.tv_menu_color_settings);
        tvMenuFeaturesAvoidance = findViewById(R.id.tv_menu_features_avoidance);
        tvMenuLayoutNormal = findViewById(R.id.tv_menu_layout_normal);
        tvMenuLayoutMinimal = findViewById(R.id.tv_menu_layout_minimal);
        tvMenuTrafficLight = findViewById(R.id.tv_menu_traffic_light);
        tvMenuAboutUs = findViewById(R.id.tv_menu_about_us);

        // Bind Right Side Panels
        panelSystemAppearance = findViewById(R.id.panel_system_appearance);
        panelColorSettings = findViewById(R.id.panel_color_settings);
        panelFeaturesAvoidance = findViewById(R.id.panel_features_avoidance);
        panelLayoutNormal = findViewById(R.id.panel_layout_normal);
        panelLayoutMinimal = findViewById(R.id.panel_layout_minimal);
        panelTrafficLight = findViewById(R.id.panel_traffic_light);
        panelAboutUs = findViewById(R.id.panel_about_us);

        // Initialize Delegate Views
        systemAppearanceDelegate.initViews();
        colorSettingsDelegate.initViews();
        featuresPanelDelegate.initViews();
        normalPanelDelegate.initViews();
        minimalPanelDelegate.initViews();
        trafficLightPanelDelegate.initViews();
        aboutUsPanelDelegate.initViews();

        switchMenu(0);
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
        laneIconColorDay = sp.getInt("lane_icon_color_day", 0xFFFFFFFF);
        laneIconColorNight = sp.getInt("lane_icon_color_night", 0xFFFFFFFF);
        isServiceOnlyMode = sp.getBoolean(KEY_IS_SERVICE_ONLY, false);
        startupMode = sp.getInt("startup_mode", isServiceOnlyMode ? 1 : 0);
        targetAmapPackage = sp.getString("target_amap_package", "");
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
        normalNaviWindowWidth = sp.getInt("normal_navi_window_width", 320);
        normalCruiseWindowWidth = sp.getInt("normal_cruise_window_width", 320);
        fullNaviWindowWidth = sp.getInt("full_navi_window_width", 280);
        fullCruiseWindowWidth = sp.getInt("full_cruise_window_width", 360);
        minimalLaneEnabled = sp.getBoolean("minimal_navi_lane_enabled", false);
        isTrafficLightFillEnabled = sp.getBoolean("traffic_light_fill_enabled", false);
        isTrafficLightCapsuleEnabled = sp.getBoolean("traffic_light_capsule_enabled", true);
        isTrafficLightIconEnabled = sp.getBoolean("traffic_light_icon_enabled", true);
        countdownFontIndex = sp.getInt("countdown_font_index", 0);
        trafficLightStyle = sp.getInt("traffic_light_style", 0);
        crossMapHideEnabled = sp.getBoolean("hide_on_cross_map", false);
        hideLaneLineBg = sp.getBoolean("hide_lane_line_bg", false);
        hideCameraCapsuleBg = sp.getBoolean("hide_camera_capsule_bg", false);
        dayNightOption = sp.getInt("app_day_night_option", 0);

        // Delegates Load Settings
        systemAppearanceDelegate.loadSettings();
        colorSettingsDelegate.loadSettings();
        featuresPanelDelegate.loadSettings();
        normalPanelDelegate.loadSettings();
        minimalPanelDelegate.loadSettings();
        trafficLightPanelDelegate.loadSettings();
        aboutUsPanelDelegate.loadSettings();
    }

    public void savePreferences() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_IS_MINIMAL, isMinimalStyle)
                .putInt(KEY_STYLE_MODE, styleMode)
                .putInt("cruise_style_mode", cruiseStyleMode)
                .putInt(KEY_THEME_COLOR, themeColor)
                .putInt("bg_color_day", bgColorDay)
                .putInt("bg_color_night", bgColorNight)
                .putInt("text_primary_day", textPrimaryDay)
                .putInt("text_primary_night", textPrimaryNight)
                .putInt("text_secondary_day", textSecondaryDay)
                .putInt("text_secondary_night", textSecondaryNight)
                .putInt("text_hint_day", textHintDay)
                .putInt("text_hint_night", textHintNight)
                .putInt("normal_turn_icon_color_day", normalTurnIconColorDay)
                .putInt("normal_turn_icon_color_night", normalTurnIconColorNight)
                .putInt("normal_turn_icon_bg_color_day", normalTurnIconBgColorDay)
                .putInt("normal_turn_icon_bg_color_night", normalTurnIconBgColorNight)
                .putInt("full_middle_bg_color_day", fullMiddleBgColorDay)
                .putInt("full_middle_bg_color_night", fullMiddleBgColorNight)
                .putInt("lane_icon_color_day", laneIconColorDay)
                .putInt("lane_icon_color_night", laneIconColorNight)
                .putBoolean(KEY_IS_SERVICE_ONLY, isServiceOnlyMode)
                .putInt("startup_mode", startupMode)
                .putString("target_amap_package", targetAmapPackage)
                .putBoolean("cruise_enabled", cruiseEnabled)
                .putBoolean("normal_navi_lane_enabled", normalLaneEnabled)
                .putBoolean("hide_turn_icon_bg", hideTurnIconBg)
                .putBoolean("hide_on_amap_foreground", avoidForegroundEnabled)
                .putBoolean("overspeed_warning_enabled", overspeedWarningEnabled)
                .putInt("overspeed_threshold", overspeedThreshold)
                .putBoolean("minimal_camera_enabled", isMinimalCameraEnabled)
                .putBoolean("minimal_road_name_enabled", isMinimalRoadNameEnabled)
                .putBoolean("minimal_direction_enabled", isMinimalDirectionEnabled)
                .putBoolean("minimal_turn_info_enabled", isMinimalTurnInfoEnabled)
                .putBoolean("minimal_speed_enabled", isMinimalSpeedEnabled)
                .putBoolean("minimal_light_count_enabled", isMinimalLightCountEnabled)
                .putBoolean("minimal_accent_navi_info_enabled", isMinimalAccentNaviInfoEnabled)
                .putBoolean("minimal_autocenter_enabled", isMinimalAutocenterEnabled)
                .putBoolean("minimal_speed_limit_enabled", isMinimalSpeedLimitEnabled)
                .putBoolean("cluster_mirror_enabled", clusterMirrorEnabled)
                .putInt("cluster_display_id", clusterDisplayId)
                .putBoolean("hide_main_when_cluster_active", hideMainWhenClusterActive)
                .putBoolean("auto_start", autoStartEnabled)
                .putBoolean("normal_navi_tmc_enabled", normalTmcEnabled)
                .putBoolean("normal_navi_bottom_info_enabled", normalBottomInfoEnabled)
                .putBoolean("normal_cruise_info_enabled", normalCruiseInfoEnabled)
                .putBoolean("hide_normal_cruise_speed", hideNormalCruiseSpeed)
                .putInt("normal_navi_window_width", normalNaviWindowWidth)
                .putInt("normal_cruise_window_width", normalCruiseWindowWidth)
                .putInt("full_navi_window_width", fullNaviWindowWidth)
                .putInt("full_cruise_window_width", fullCruiseWindowWidth)
                .putBoolean("minimal_navi_lane_enabled", minimalLaneEnabled)
                .putBoolean("traffic_light_fill_enabled", isTrafficLightFillEnabled)
                .putBoolean("traffic_light_capsule_enabled", isTrafficLightCapsuleEnabled)
                .putBoolean("traffic_light_icon_enabled", isTrafficLightIconEnabled)
                .putInt("countdown_font_index", countdownFontIndex)
                .putInt("traffic_light_style", trafficLightStyle)
                .putBoolean("hide_on_cross_map", crossMapHideEnabled)
                .putBoolean("hide_lane_line_bg", hideLaneLineBg)
                .putBoolean("hide_camera_capsule_bg", hideCameraCapsuleBg)
                .putInt("app_day_night_option", dayNightOption)
                .apply();
    }

    public int getWidthFromSP(String spKey) {
        switch (spKey) {
            case "normal_navi_window_width": return normalNaviWindowWidth;
            case "normal_cruise_window_width": return normalCruiseWindowWidth;
            case "full_navi_window_width": return fullNaviWindowWidth;
            case "full_cruise_window_width": return fullCruiseWindowWidth;
            default: return 320;
        }
    }

    public void setWidthToSP(String spKey, int width) {
        switch (spKey) {
            case "normal_navi_window_width": normalNaviWindowWidth = width; break;
            case "normal_cruise_window_width": normalCruiseWindowWidth = width; break;
            case "full_navi_window_width": fullNaviWindowWidth = width; break;
            case "full_cruise_window_width": fullCruiseWindowWidth = width; break;
        }
        savePreferences();
    }

    public void setupListeners() {
        // Upper action buttons
        View btnHome = findViewById(R.id.btn_home_app);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> moveTaskToBack(true));
        }
        View btnExit = findViewById(R.id.btn_exit_app);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                stopService(new Intent(MainActivity.this, com.navi.link.service.AutoMapService.class));
                finishAffinity();
                System.exit(0);
            });
        }

        // Left Navigation Tab Clicks
        if (menuSystemAppearance != null) menuSystemAppearance.setOnClickListener(v -> switchMenu(0));
        if (menuColorSettings != null) menuColorSettings.setOnClickListener(v -> switchMenu(1));
        if (menuFeaturesAvoidance != null) menuFeaturesAvoidance.setOnClickListener(v -> switchMenu(2));
        if (menuLayoutNormal != null) menuLayoutNormal.setOnClickListener(v -> switchMenu(3));
        if (menuLayoutMinimal != null) menuLayoutMinimal.setOnClickListener(v -> switchMenu(4));
        if (menuTrafficLight != null) menuTrafficLight.setOnClickListener(v -> switchMenu(5));
        if (menuAboutUs != null) menuAboutUs.setOnClickListener(v -> switchMenu(6));

        // Delegates setup listeners
        systemAppearanceDelegate.setupListeners();
        colorSettingsDelegate.setupListeners();
        featuresPanelDelegate.setupListeners();
        normalPanelDelegate.setupListeners();
        minimalPanelDelegate.setupListeners();
        trafficLightPanelDelegate.setupListeners();
        aboutUsPanelDelegate.setupListeners();
    }

    public void switchMenu(int index) {
        selectedMenuIndex = index;
        ScrollView[] panels = {
                panelSystemAppearance, panelColorSettings, panelFeaturesAvoidance,
                panelLayoutNormal, panelLayoutMinimal, panelTrafficLight, panelAboutUs
        };
        MaterialCardView[] menus = {
                menuSystemAppearance, menuColorSettings, menuFeaturesAvoidance,
                menuLayoutNormal, menuLayoutMinimal, menuTrafficLight, menuAboutUs
        };
        View[] indicators = {
                indicatorSystemAppearance, indicatorColorSettings, indicatorFeaturesAvoidance,
                indicatorLayoutNormal, indicatorLayoutMinimal, indicatorTrafficLight, indicatorAboutUs
        };
        TextView[] textViews = {
                tvMenuSystemAppearance, tvMenuColorSettings, tvMenuFeaturesAvoidance,
                tvMenuLayoutNormal, tvMenuLayoutMinimal, tvMenuTrafficLight, tvMenuAboutUs
        };

        int accentColor = getAccentColor();

        for (int i = 0; i < panels.length; i++) {
            boolean active = (i == index);
            if (panels[i] != null) panels[i].setVisibility(active ? View.VISIBLE : View.GONE);
            if (indicators[i] != null) indicators[i].setVisibility(active ? View.VISIBLE : View.INVISIBLE);
            if (textViews[i] != null) {
                textViews[i].setTextColor(active ? accentColor : getThemeColorAttr(R.attr.panelTextColorSecondary));
            }
            if (menus[i] != null) {
                menus[i].setCardBackgroundColor(active ? getThemeColorAttr(R.attr.panelCardBgColor) : Color.TRANSPARENT);
            }
        }
    }

    public void applyThemeToViews() {
        switchMenu(selectedMenuIndex);
        systemAppearanceDelegate.updateThemeColors();
        colorSettingsDelegate.updateThemeColors();
        featuresPanelDelegate.updateThemeColors();
        normalPanelDelegate.updateThemeColors();
        minimalPanelDelegate.updateThemeColors();
        trafficLightPanelDelegate.updateThemeColors();
        aboutUsPanelDelegate.updateThemeColors();
    }

    public void updateStatusText() {
        if (tvStatus == null) return;
        boolean hasPermission = OverlayPermissionCompat.canDrawOverlays(this);
        if (hasPermission) {
            tvStatus.setText("● 悬浮窗正常运行");
            tvStatus.setTextColor(0xFF4CAF50);
        } else {
            tvStatus.setText("○ 悬浮窗未获取悬浮窗权限");
            tvStatus.setTextColor(0xFFFF9800);
        }
    }

    public void checkPermissionAndStart() {
        if (!OverlayPermissionCompat.canDrawOverlays(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            }
        } else {
            startFloatingService();
        }
    }

    public void startFloatingService() {
        PlatformCompat.startService(this, new Intent(this, com.navi.link.service.AutoMapService.class));
        updateStatusText();
    }

    public void refreshFloatingWindow() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance(this);
        if (manager != null) {
            manager.refreshWindow();
        }
    }

    public void updateFloatingWindowStyle() {
        refreshFloatingWindow();
    }

    // Delegation Helper Actions
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

    private void setStartupMode(int mode) {
        if (this.startupMode == mode) return;
        this.startupMode = mode;
        this.isServiceOnlyMode = (mode == 1);
        systemAppearanceDelegate.updateStartupSelection();
        savePreferences();
    }

    private void showAmapSelectionDialog() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // Android 15 上 getInstalledApplications 可能被系统阉割过滤，按包名硬拿补充
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
                    if (kp.equals(a.packageName)) { exists = true; break; }
                }
                if (!exists) apps.add(info);
            } catch (Exception ignored) { }
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
                    name.setTextColor(getThemeColorAttr(R.attr.panelTextColorPrimary));
                    pkg.setText(appInfo.packageName);
                    pkg.setTextColor(getThemeColorAttr(R.attr.panelTextColorSecondary));
                }
                return convertView;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("选择高德地图应用")
                .setAdapter(adapter, (dialog, which) -> {
                    targetAmapPackage = amapApps.get(which).packageName;
                    setStartupMode(2);
                })
                .show();
    }

    public void selectStyle(int mode) {
        if (this.styleMode == mode) return;
        this.styleMode = mode;
        this.isMinimalStyle = (mode == 1);
        systemAppearanceDelegate.updateStyleSelection();
        savePreferences();
        updateFloatingWindowStyle();
    }

    public void selectCruiseStyle(int mode) {
        if (this.cruiseStyleMode == mode) return;
        this.cruiseStyleMode = mode;
        systemAppearanceDelegate.updateCruiseStyleSelection();
        savePreferences();
        updateFloatingWindowStyle();
    }

    public void selectDayNightOption(int option) {
        if (this.dayNightOption == option) return;
        this.dayNightOption = option;
        savePreferences();
        systemAppearanceDelegate.updateDayNightSelection();
        restartActivity();
    }

    public void updateDayNightSelection() {
        systemAppearanceDelegate.updateDayNightSelection();
    }

    public void selectCountdownFont(int index) {
        if (this.countdownFontIndex == index) return;
        this.countdownFontIndex = index;
        trafficLightPanelDelegate.updateCountdownFontSelection();
        savePreferences();
        refreshFloatingWindow();
    }

    public void selectTrafficLightStyle(int style) {
        if (this.trafficLightStyle == style) return;
        this.trafficLightStyle = style;
        trafficLightPanelDelegate.updateTrafficLightStyleSelection();
        savePreferences();
        refreshFloatingWindow();
    }

    public void updateThresholdChips() {
        systemAppearanceDelegate.updateThresholdChips();
    }

    public void resetToDefaultColors() {
        colorSettingsDelegate.resetToDefaultColors();
    }

    public void updateColorPreviews() {
        colorSettingsDelegate.updateColorPreviews();
    }

    // Color and Pixel Density Utilities
    public int getAccentColor() {
        return themeColor;
    }

    public int getThemeColorAttr(int attrId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public boolean isDarkColor(int color) {
        return ((color >> 16) & 0xFF) * 0.299
                + ((color >> 8) & 0xFF) * 0.587
                + (color & 0xFF) * 0.114 < 100;
    }

    public void updateSeekBarToCurrentScale() {
        if (systemAppearanceDelegate != null) {
            systemAppearanceDelegate.updateSeekBarToCurrentScale();
        }
    }

    public void showClusterDisplaySelectionDialog() {
        if (featuresPanelDelegate != null) {
            featuresPanelDelegate.showClusterDisplaySelectionDialog();
        }
    }

    public void showColorPickerDialog(String title, int initialColor, ColorSettingsDelegate.OnColorSelectedListener listener) {
        if (colorSettingsDelegate != null) {
            colorSettingsDelegate.showColorPickerDialog(title, initialColor, listener);
        }
    }

    public void updateColorPreview(View view, int color) {
        if (colorSettingsDelegate != null) {
            colorSettingsDelegate.updateColorPreview(view, color);
        }
    }

    public void updateSwitchTheme(androidx.appcompat.widget.SwitchCompat switchView, int color) {
        if (switchView == null) return;
        int thumbChecked = color;
        int thumbUnchecked = 0xFF71717A;
        int trackChecked = (color & 0x00FFFFFF) | 0x4D000000;
        int trackUnchecked = 0x3371717A;

        int[][] thumbStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] thumbColors = new int[]{thumbChecked, thumbUnchecked};
        switchView.setThumbTintList(new ColorStateList(thumbStates, thumbColors));

        int[][] trackStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] trackColors = new int[]{trackChecked, trackUnchecked};
        switchView.setTrackTintList(new ColorStateList(trackStates, trackColors));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusText();
        if (OverlayPermissionCompat.canDrawOverlays(this)) {
            startFloatingService();
        }
    }
}