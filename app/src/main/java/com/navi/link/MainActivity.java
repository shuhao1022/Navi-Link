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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_IS_MINIMAL = "is_minimal_style";
    private static final String KEY_SCALE = "scale";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final String PREFS_NAME = "floating_config";
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
    private LinearLayout llThemeColors;
    private RadioButton rbMinimal;
    private RadioButton rbNormal;
    private SeekBar sbScale;
    private View[] themeChips;
    private TextView tvScaleValue;
    private TextView tvStatus;

    private boolean isMinimalStyle = false;
    private float scale = 1.0f;
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
        rbNormal = findViewById(R.id.rb_normal);
        rbMinimal = findViewById(R.id.rb_minimal);
        sbScale = findViewById(R.id.sb_scale);
        tvScaleValue = findViewById(R.id.tv_scale_value);
        tvStatus = findViewById(R.id.tv_status);
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
        scale = sp.getFloat(KEY_SCALE, 1.0f);
        themeColor = sp.getInt(KEY_THEME_COLOR, 0xFF4FC3F7);

        updateStyleSelection();

        sbScale.setProgress((int) (((scale - 0.5f) / 1.5f) * 15));
        sbScale.setProgressTintList(ColorStateList.valueOf(getAccentColor()));
        sbScale.setThumbTintList(ColorStateList.valueOf(getAccentColor()));
        tvScaleValue.setTextColor(getAccentColor());
        tvScaleValue.setText(String.format("%.1fx", scale));

        initThemeColorChips();
    }

    private void selectStyle(boolean minimal) {
        if (isMinimalStyle == minimal) return;
        isMinimalStyle = minimal;
        updateStyleSelection();
        savePreferences();
        updateFloatingWindowStyle();
    }

    private void updateStyleSelection() {
        rbNormal.setChecked(!isMinimalStyle);
        rbMinimal.setChecked(isMinimalStyle);
        int accentColor = getAccentColor();
        cardNormal.setStrokeColor(isMinimalStyle ? Color.parseColor("#444444") : accentColor);
        cardMinimal.setStrokeColor(isMinimalStyle ? accentColor : Color.parseColor("#444444"));
    }

    private void savePreferences() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_IS_MINIMAL, isMinimalStyle)
                .putFloat(KEY_SCALE, scale)
                .putInt(KEY_THEME_COLOR, themeColor)
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
        cardNormal.setStrokeColor(isMinimalStyle ? Color.parseColor("#444444") : accentColor);
        cardMinimal.setStrokeColor(isMinimalStyle ? accentColor : Color.parseColor("#444444"));
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
        cardNormal.setOnClickListener(v -> selectStyle(false));
        cardMinimal.setOnClickListener(v -> selectStyle(true));

        sbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scale = (progress / 15.0f) * 1.5f + 0.5f;
                tvScaleValue.setText(String.format("%.1fx", scale));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                savePreferences();
                updateFloatingWindowScale();
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
            manager.updateScale(scale);
        }
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
}