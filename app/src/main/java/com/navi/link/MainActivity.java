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
    private TextView btnGoHome;
    private CardView cvGoHome;
    private SeekBar sbScale;
    private View[] themeChips;
    private TextView tvScaleValue;
    private TextView tvStatus;
    private CompoundButton cbCruiseEnabled;
    private TextView tvCruiseStatus;
    private MaterialCardView cardCruiseToggle;
    private CompoundButton cbNormalLaneEnabled;
    private TextView tvNormalLaneStatus;
    private MaterialCardView cardNormalLaneToggle;
    private CompoundButton cbAvoidForegroundEnabled;
    private TextView tvAvoidForegroundStatus;
    private MaterialCardView cardAvoidForegroundToggle;

    private boolean isMinimalStyle = false;
    private int styleMode = 0;
    private boolean isServiceOnlyMode = false;
    private int backgroundMode = 0; // 0=深色, 1=半透明, 2=全透明
    private boolean cruiseEnabled = true;
    private boolean normalLaneEnabled = false;
    private boolean avoidForegroundEnabled = false;

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
        btnGoHome = findViewById(R.id.btn_go_home);
        cvGoHome = findViewById(R.id.cv_go_home);
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
        llThemeColors = findViewById(R.id.ll_theme_colors);

        View contentView = findViewById(android.R.id.content);
        if (contentView instanceof ScrollView) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView, (view, windowInsetsCompat) -> {
                Insets insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), insets.top, view.getPaddingRight(), insets.bottom);
                return windowInsetsCompat;
            });
        }
    }

    private void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isMinimalStyle = sp.getBoolean(KEY_IS_MINIMAL, false);
        styleMode = sp.getInt(KEY_STYLE_MODE, isMinimalStyle ? 1 : 0);
        themeColor = sp.getInt(KEY_THEME_COLOR, 0xFF4FC3F7);
        isServiceOnlyMode = sp.getBoolean(KEY_IS_SERVICE_ONLY, false);
        backgroundMode = sp.getInt("background_mode", 0);
        cruiseEnabled = sp.getBoolean("cruise_enabled", true);
        normalLaneEnabled = sp.getBoolean("normal_navi_lane_enabled", false);
        avoidForegroundEnabled = sp.getBoolean("hide_on_amap_foreground", false);
 
        updateStartupSelection();
        updateStyleSelection();
        updateBackgroundModeSelection();
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

        sbScale.setProgressTintList(ColorStateList.valueOf(getAccentColor()));
        sbScale.setThumbTintList(ColorStateList.valueOf(getAccentColor()));
        // 回到桌面按钮：深色主题时保持暗底白字，浅色主题时跟随主题色
        if (isDarkColor(themeColor)) {
            cvGoHome.setCardBackgroundColor(0xFF1E1E1E);
            btnGoHome.setTextColor(Color.WHITE);
        } else {
            cvGoHome.setCardBackgroundColor(themeColor);
            btnGoHome.setTextColor(Color.WHITE);
        }
        tvScaleValue.setTextColor(getAccentColor());

        initThemeColorChips();
    }

    private void selectStartupMode(boolean serviceOnly) {
        if (isServiceOnlyMode == serviceOnly) return;
        isServiceOnlyMode = serviceOnly;
        updateStartupSelection();
        savePreferences();
    }

    private void updateStartupSelection() {
        rbServiceOnly.setChecked(isServiceOnlyMode);
        rbNormalStart.setChecked(!isServiceOnlyMode);
        int accentColor = getAccentColor();
        cardServiceOnly.setStrokeColor(isServiceOnlyMode ? accentColor : Color.parseColor("#444444"));
        cardNormalStart.setStrokeColor(isServiceOnlyMode ? Color.parseColor("#444444") : accentColor);
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
                .putBoolean(KEY_IS_SERVICE_ONLY, isServiceOnlyMode)
                .putInt("background_mode", backgroundMode)
                .putBoolean("cruise_enabled", cruiseEnabled)
                .putBoolean("normal_navi_lane_enabled", normalLaneEnabled)
                .putBoolean("hide_on_amap_foreground", avoidForegroundEnabled)
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

        int accentColor = getAccentColor();
        cardNormal.setStrokeColor(styleMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardMinimal.setStrokeColor(styleMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardFull.setStrokeColor(styleMode == 2 ? accentColor : Color.parseColor("#444444"));
        cardServiceOnly.setStrokeColor(isServiceOnlyMode ? accentColor : Color.parseColor("#444444"));
        cardNormalStart.setStrokeColor(isServiceOnlyMode ? Color.parseColor("#444444") : accentColor);
//        btnGoHome.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        // 回到桌面按钮：深色主题时保持暗底白字
        if (isDarkColor(themeColor)) {
            cvGoHome.setCardBackgroundColor(0xFF1E1E1E);
        } else {
            cvGoHome.setCardBackgroundColor(themeColor);
        }
        rbNormal.setButtonTintList(ColorStateList.valueOf(accentColor));
        rbMinimal.setButtonTintList(ColorStateList.valueOf(accentColor));
        rbFull.setButtonTintList(ColorStateList.valueOf(accentColor));
        rbServiceOnly.setButtonTintList(ColorStateList.valueOf(accentColor));
        rbNormalStart.setButtonTintList(ColorStateList.valueOf(accentColor));
        sbScale.setProgressTintList(ColorStateList.valueOf(accentColor));
        sbScale.setThumbTintList(ColorStateList.valueOf(accentColor));
        tvScaleValue.setTextColor(accentColor);

        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null) {
            manager.applyThemeColor(themeColor);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int getAccentColor() {
        return isDarkColor(themeColor) ? Color.WHITE : themeColor;
    }

    private void setupListeners() {
        cardServiceOnly.setOnClickListener(v -> selectStartupMode(true));
        cardNormalStart.setOnClickListener(v -> selectStartupMode(false));
        cardNormal.setOnClickListener(v -> selectStyle(0));
        cardMinimal.setOnClickListener(v -> selectStyle(1));
        cardFull.setOnClickListener(v -> selectStyle(2));
        cardBgDark.setOnClickListener(v -> selectBackgroundMode(0));
        cardBgSemi.setOnClickListener(v -> selectBackgroundMode(1));
        cardBgTransparent.setOnClickListener(v -> selectBackgroundMode(2));
        btnGoHome.setOnClickListener(v -> moveTaskToBack(true));

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
        // 如果 manager 正在巡航，显示巡航缩放（=灵动岛的值）
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null && manager.isActive() && manager.getCurrentMode() == FloatingWindowManager.MODE_CRUISE) {
            s = sp.getFloat("scale_minimal", 0.5f); // 巡航复用灵动岛
        } else {
            String[] keys = {"scale_normal", "scale_minimal", "scale_full"};
            float[] defaults = {1.0f, 0.5f, 0.9f};
            int idx = Math.max(0, Math.min(styleMode, 2));
            s = sp.getFloat(keys[idx], defaults[idx]);
        }
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
}