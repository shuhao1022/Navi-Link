package com.navi.link;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrafficLightView extends LinearLayout {

    private ImageView ivLightIcon;
    private ImageView ivLightArrow;
    private TextView tvLightTime;
    private FrameLayout flIconContainer;

    private boolean isCompact = false;
    private ObjectAnimator blinkAnimator;
    private SharedPreferences sp;
    private float scaleFactor = -1f;

    public void setScaleFactor(float factor) {
        this.scaleFactor = factor;
    }

    private float getScale() {
        if (scaleFactor > 0) return scaleFactor;
        FloatingWindowManager fwm = FloatingWindowManager.getInstance();
        return fwm != null ? fwm.getScale() : 1.0f;
    }

    // 填充背景颜色常量（与图标资源颜色一致）
    private static final int FILL_COLOR_RED = 0xFFFF3333;
    private static final int FILL_COLOR_YELLOW = 0xFFCC9900;
    private static final int FILL_COLOR_GREEN = 0xFF34C759;

    // 倒计时文字颜色（来自shape）
    private static final int COUNTDOWN_COLOR_RED = 0xFFFF3333;
    private static final int COUNTDOWN_COLOR_YELLOW = 0xFFCC9900;
    private static final int COUNTDOWN_COLOR_GREEN = 0xFF34C759;

    // 七组红绿灯图标样式资源
    private static final int[] LIGHT_RED_STYLES = {
        R.drawable.light_red_01, R.drawable.light_red_02,
        R.drawable.light_red_03, R.drawable.light_red_04,
        R.drawable.light_red_05, R.drawable.light_red_06,
        R.drawable.light_red_07
    };
    private static final int[] LIGHT_GREE_STYLES = {
        R.drawable.light_gree_01, R.drawable.light_gree_02,
        R.drawable.light_gree_03, R.drawable.light_gree_04,
        R.drawable.light_gree_05, R.drawable.light_gree_06,
        R.drawable.light_gree_07
    };
    private static final int[] LIGHT_YELLOW_STYLES = {
        R.drawable.light_yellow_01, R.drawable.light_yellow_02,
        R.drawable.light_yellow_03, R.drawable.light_yellow_04,
        R.drawable.light_yellow_05, R.drawable.light_yellow_06,
        R.drawable.light_yellow_07
    };

    public TrafficLightView(Context context) {
        super(context);
        init(context);
    }

    public TrafficLightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dpToPx(1), 0, dpToPx(10), 0);
        setMinimumHeight(dpToPx(50)); // 默认胶囊高度50dp，与导航布局一致

        LayoutInflater.from(context).inflate(R.layout.traffic_light_view, this, true);

        flIconContainer = findViewById(R.id.fl_light_icon_container);
        ivLightIcon = findViewById(R.id.iv_light_icon);
        ivLightArrow = findViewById(R.id.iv_light_arrow);
        tvLightTime = findViewById(R.id.tv_light_time);
    }

    /**
     * 设置红绿灯数据
     * @param status 灯状态码（导航: 4绿/1红/else黄  巡航: 1绿/0红/else黄）
     * @param dir 方向
     * @param countdown 倒计时秒数
     * @param isNavi true=导航, false=巡航
     */
    public void setData(int status, int dir, int countdown, boolean isNavi) {
        setVisibility(View.VISIBLE);

        int styleIndex = sp.getInt("traffic_light_style", 0);
        if (styleIndex < 0 || styleIndex > 6) styleIndex = 0;

        // 设置灯图标（使用样式图片）
        ivLightIcon.setImageResource(getStyleLightRes(status, isNavi, styleIndex));
        // 设置方向箭头
        ivLightArrow.setImageResource(isNavi ? getNaviLightDirRes(dir) : getCruiseLightDirRes(dir));
        // 设置倒计时
        tvLightTime.setText(String.valueOf(countdown));
        updateTextSize();

        boolean ledFontEnabled = sp.getBoolean("traffic_light_led_font_enabled", false);
        if (ledFontEnabled) {
            try {
                android.graphics.Typeface ledTypeface = android.graphics.Typeface.createFromAsset(
                        getContext().getAssets(), "fonts/led_font.ttf");
                tvLightTime.setTypeface(ledTypeface, Typeface.NORMAL);
            } catch (Exception e) {
                tvLightTime.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        } else {
            tvLightTime.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        // 应用胶囊背景样式（填充/默认），内部会读写fillEnabled
        applyFillStyle(status, isNavi);

        // 非填充背景模式：倒计时颜色跟随灯状态，箭头根据灯图显隐变化
        boolean fillEnabled = sp.getBoolean("traffic_light_fill_enabled", false);
        if (!fillEnabled) {
            tvLightTime.setTextColor(getCountdownColor(status, isNavi));
            boolean iconEnabled = sp.getBoolean("traffic_light_icon_enabled", true);
            if (iconEnabled) {
                ivLightArrow.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                ivLightArrow.setColorFilter(getCountdownColor(status, isNavi));
            }
        } else {
            tvLightTime.setTextColor(0xFFFFFFFF);
            ivLightArrow.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // 5秒以内闪烁
        if (countdown <= 5) {
            if (blinkAnimator == null) {
                blinkAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.3f);
                blinkAnimator.setDuration(500);
                blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
                blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
                blinkAnimator.start();
            }
        } else {
            cancelBlink();
            setAlpha(1f);
        }
    }

    private void updateTextSize() {
        if (tvLightTime == null) return;
        boolean ledFontEnabled = sp.getBoolean("traffic_light_led_font_enabled", false);
        int baseSize = isCompact ? 25 : 30;
        int size = ledFontEnabled ? (baseSize + 5) : baseSize;
        float scale = getScale();
        tvLightTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics()) * scale);
    }

    /**
     * 设置为紧凑模式（巡航超过3灯时缩小尺寸）
     */
    public void setCompact(boolean compact) {
        if (isCompact == compact) return;
        isCompact = compact;

        if (compact) {
            // 缩小图标容器 45dp -> 35dp
            ViewGroup.LayoutParams iconLp = flIconContainer.getLayoutParams();
            iconLp.width = dpToPx(35);
            iconLp.height = dpToPx(35);
            flIconContainer.setLayoutParams(iconLp);

            // 缩小箭头 padding 4dp -> 3dp
            ivLightArrow.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            // 动态调节文字大小
            updateTextSize();

            // 缩小容器内边距
            setPadding(dpToPx(2), 0, dpToPx(6), 0);
            setMinimumHeight(dpToPx(40)); // 紧凑胶囊高度40dp
        } else {
            // 恢复图标容器 48dp
            ViewGroup.LayoutParams iconLp = flIconContainer.getLayoutParams();
            iconLp.width = dpToPx(48);
            iconLp.height = dpToPx(48);
            flIconContainer.setLayoutParams(iconLp);

            // 恢复箭头 padding 4dp
            ivLightArrow.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

            // 动态调节文字大小
            updateTextSize();

            // 恢复容器内边距
            setPadding(dpToPx(1), 0, dpToPx(10), 0);
            setMinimumHeight(dpToPx(50));
        }
    }

    /**
     * 重置隐藏，取消闪烁动画
     */
    public void clear() {
        cancelBlink();
        setVisibility(View.GONE);
        setAlpha(1f);
        tvLightTime.setText("");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelBlink();
    }

    private void cancelBlink() {
        if (blinkAnimator != null) {
            blinkAnimator.cancel();
            blinkAnimator = null;
        }
    }

    // ======================== 填充背景样式 ========================

    private void applyFillStyle(int status, boolean isNavi) {
        boolean fillEnabled = sp.getBoolean("traffic_light_fill_enabled", false);

        if (fillEnabled) {
            int fillColor = getFillColor(status, isNavi);
            float density = getResources().getDisplayMetrics().density;

            float currentScale = getScale();

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(fillColor);
            drawable.setCornerRadius(30 * density * currentScale);
            drawable.setStroke((int) (2 * density * currentScale + 0.5f), 0xFFFFFFFF);
            setBackground(drawable);

            ivLightIcon.setVisibility(View.GONE);
        } else {
            boolean capsuleEnabled = sp.getBoolean("traffic_light_capsule_enabled", true);
            if (capsuleEnabled) {
                setBackgroundResource(R.drawable.bg_traffic_light_capsule);
            } else {
                setBackground(null);
            }
            boolean iconEnabled = sp.getBoolean("traffic_light_icon_enabled", true);
            ivLightIcon.setVisibility(iconEnabled ? View.VISIBLE : View.GONE);
        }
    }

    private int getFillColor(int status, boolean isNavi) {
        if (isNavi) {
            if (status == 4) return FILL_COLOR_GREEN;
            if (status == 1) return FILL_COLOR_RED;
            return FILL_COLOR_YELLOW;
        } else {
            if (status == 1) return FILL_COLOR_GREEN;
            if (status == 0) return FILL_COLOR_RED;
            return FILL_COLOR_YELLOW;
        }
    }

    // ======================== 资源映射 ========================

    private int getStyleLightRes(int status, boolean isNavi, int styleIndex) {
        int colorState;
        if (isNavi) {
            if (status == 4) colorState = 0; // green
            else if (status == 1) colorState = 1; // red
            else colorState = 2; // yellow
        } else {
            if (status == 1) colorState = 0; // green
            else if (status == 0) colorState = 1; // red
            else colorState = 2; // yellow
        }
        switch (colorState) {
            case 0: return LIGHT_GREE_STYLES[styleIndex];
            case 1: return LIGHT_RED_STYLES[styleIndex];
            default: return LIGHT_YELLOW_STYLES[styleIndex];
        }
    }

    private int getCountdownColor(int status, boolean isNavi) {
        if (isNavi) {
            if (status == 4) return COUNTDOWN_COLOR_GREEN;
            if (status == 1) return COUNTDOWN_COLOR_RED;
            return COUNTDOWN_COLOR_YELLOW;
        } else {
            if (status == 1) return COUNTDOWN_COLOR_GREEN;
            if (status == 0) return COUNTDOWN_COLOR_RED;
            return COUNTDOWN_COLOR_YELLOW;
        }
    }

    private int getNaviLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_right;
        if (dir == 3) return R.mipmap.light_u_turn;
        if (dir == 4) return R.mipmap.light_straight;
        return R.mipmap.light_straight;
    }

    private int getCruiseLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_straight;
        if (dir == 3) return R.mipmap.light_right;
        return R.mipmap.light_straight;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density * getScale() + 0.5f);
    }
}
