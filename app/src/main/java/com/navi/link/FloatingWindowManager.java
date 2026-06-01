package com.navi.link;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class FloatingWindowManager {

    private static final long LIGHT_HIDE_TIMEOUT_MS = 5000;
    private static final long LONG_PRESS_MS = 500;
    private static final long NAVI_TIMEOUT_MS = 6000;
    private static final long WATCHDOG_TIMEOUT_MS = 5000;

    public static final int MODE_CRUISE = 0;
    public static final int MODE_NAVI = 1;

    private static FloatingWindowManager instance;

    private final Context context;
    private final WindowManager windowManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private View floatingView;
    private WindowManager.LayoutParams layoutParams;
    private View scaleTarget;

    // 巡航模式 UI
    private TextView tvCruiseSpeed;
    private TextView tvCruiseRoadName;
    private LinearLayout llTrafficLightsContainer;

    // 常规导航 UI
    private ImageView ivTurnIcon;
    private TextView tvDistanceNum;
    private TextView tvDistanceUnit;
    private TextView tvAction;
    private TextView tvRoadName;
    private ProgressBar pbRoute;
    private TextView tvSummary;
    private TextView tvEta;
    private View llTrafficLightGroup;
    private ImageView ivLightIcon;
    private ImageView ivLightArrow;
    private TextView tvLightTime;
    private View layoutInfoBar;

    // 灵动岛 UI
    private ImageView ivActionIconMin;
    private TextView tvMinSpeed;
    private TextView tvMinSpeedUnit;
    private TextView tvDistanceNumMin;
    private TextView tvDistanceUnitMin;
    private TextView tvRoadNameMin;
    private View llTrafficLightGroupMin;
    private ImageView ivLightIconMin;
    private ImageView ivLightArrowMin;
    private TextView tvLightTimeMin;

    // 状态
    private int currentMode = MODE_CRUISE;
    private boolean isMinimalStyle = false;
    private float scale = 1.0f;
    private int themeColor = 0xFF4FC3F7;
    private boolean isShowing = false;

    // 拖拽相关
    private float initialTouchX;
    private float initialTouchY;
    private int initialWindowX;
    private int initialWindowY;
    private boolean isPositionLocked = false;
    private boolean isDragging = false;
    private boolean hasLongPressed = false;

    // 尺寸
    private int naturalWidth;
    private int naturalHeight;
    private int savedPosX = -1;
    private int savedPosY = -1;

    private boolean shouldHideAfterRecreate = false;
    private boolean isWindowVisible = true;

    // Runnable
    private final Runnable naviSwitchRunnable = this::doNaviSwitch;
    private final Runnable naviTimeoutRunnable = this::onNaviTimeout;
    private final Runnable cruiseGraceRunnable = this::onCruiseGrace;
    private final Runnable watchdogRunnable = () -> {
        View view = floatingView;
        if (view != null) view.setVisibility(View.GONE);
    };
    private final Runnable trafficLightTimeoutRunnable = this::hideTrafficLightCapsule;

    private final Runnable longPressCheck = new Runnable() {
        @Override
        public void run() {
            if (isDragging) return;
            hasLongPressed = true;
            isPositionLocked = !isPositionLocked;
            Toast.makeText(context, isPositionLocked ? "位置已锁定" : "位置已解锁", Toast.LENGTH_SHORT).show();
        }
    };

    // ======================== 构造与单例 ========================

    private FloatingWindowManager(Context context) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        loadPreferences();
    }

    public static synchronized FloatingWindowManager getInstance(Context context) {
        if (instance == null) {
            instance = new FloatingWindowManager(context);
        }
        return instance;
    }

    public static FloatingWindowManager getInstance() {
        return instance;
    }

    // ======================== 偏好加载 ========================

    private void loadPreferences() {
        SharedPreferences sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        isMinimalStyle = sp.getBoolean("is_minimal_style", false);
        scale = sp.getFloat("scale", 1.0f);
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
        savedPosX = sp.getInt("window_pos_x", -1);
        savedPosY = sp.getInt("window_pos_y", -1);
    }

    // ======================== 窗口显示与隐藏 ========================

    public void show() {
        currentMode = MODE_CRUISE;
        recreateWindow();
    }

    public void refreshWindow() {
        if (isShowing) {
            recreateWindow();
        }
    }

    public void hide() {
        handler.removeCallbacksAndMessages(null);
        if (floatingView == null || !isShowing) return;
        try {
            windowManager.removeView(floatingView);
        } catch (Exception ignored) {
        }
        floatingView = null;
        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setVisible(boolean visible) {
        if (floatingView != null) {
            floatingView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // ======================== 模式切换 ========================

    public void onTrafficLightReceived() {
        if (currentMode != MODE_NAVI) {
            currentMode = MODE_NAVI;
            recreateWindow();
        }
        resetNaviTimeout();
    }

    public void switchToCruiseMode() {
        handler.removeCallbacks(naviTimeoutRunnable);
        handler.removeCallbacks(naviSwitchRunnable);
        handler.removeCallbacks(trafficLightTimeoutRunnable);
        handler.removeCallbacks(cruiseGraceRunnable);
        shouldHideAfterRecreate = false;
        if (currentMode != MODE_CRUISE) {
            currentMode = MODE_CRUISE;
            recreateWindow();
        }
    }

    public void switchToNaviMode() {
        handler.removeCallbacks(naviSwitchRunnable);
        shouldHideAfterRecreate = false;
        if (currentMode != MODE_NAVI) {
            currentMode = MODE_NAVI;
            recreateWindow();
        }
        resetNaviTimeout();
    }

    public int getCurrentMode() {
        return currentMode;
    }

    // ======================== 超时管理 ========================

    void resetNaviTimeout() {
        handler.removeCallbacks(naviTimeoutRunnable);
        handler.removeCallbacks(naviSwitchRunnable);
        handler.removeCallbacks(cruiseGraceRunnable);
        shouldHideAfterRecreate = false;
        handler.postDelayed(naviTimeoutRunnable, NAVI_TIMEOUT_MS);
    }

    void startCruiseGrace() {
        handler.removeCallbacks(cruiseGraceRunnable);
        handler.postDelayed(cruiseGraceRunnable, 3000);
    }

    void cancelCruiseGrace() {
        handler.removeCallbacks(cruiseGraceRunnable);
    }

    public void resetWatchdog() {
        handler.removeCallbacks(watchdogRunnable);
        handler.postDelayed(watchdogRunnable, WATCHDOG_TIMEOUT_MS);
        View view = floatingView;
        if (view == null || view.getVisibility() == View.VISIBLE) return;
        floatingView.setVisibility(View.VISIBLE);
    }

    // ======================== 窗口重建 ========================

    private void recreateWindow() {
        // 先保存旧位置（如果窗口已存在）
        int oldSavedPosX = savedPosX;
        int oldSavedPosY = savedPosY;
        
        loadPreferences();
        scaleTarget = null;

        if (floatingView != null && layoutParams != null) {
            // 只有当用户手动拖拽过才更新保存的位置
            // 如果是自动重建（如模式切换），保留之前保存的位置
            if (oldSavedPosX < 0) {
                savedPosX = layoutParams.x;
                savedPosY = layoutParams.y;
            }
            try {
                if (floatingView.isAttachedToWindow()) {
                    windowManager.removeView(floatingView);
                }
            } catch (Exception ignored) {
            }
            floatingView = null;
        }

        int layoutRes;
        if (currentMode == MODE_NAVI) {
            layoutRes = isMinimalStyle ? R.layout.layout_floating_navi_minimal : R.layout.layout_floating_navi;
        } else {
            layoutRes = R.layout.layout_floating_cruise;
        }

        View inflated = LayoutInflater.from(context).inflate(layoutRes, null);
        floatingView = inflated;

        // 灵动岛模式外层需要 FrameLayout
        if (currentMode == MODE_NAVI && isMinimalStyle) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            frameLayout.setClipToPadding(false);
            frameLayout.addView(inflated, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            floatingView = frameLayout;
            scaleTarget = inflated;
        } else {
            scaleTarget = null;
        }

        bindViews();
        measureNaturalSize();

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        int scaledWidth = naturalWidth;
        int scaledHeight = naturalHeight;
        if (naturalWidth > 0 && naturalHeight > 0 && scale > 1.0f) {
            scaledWidth = (int) (naturalWidth * scale);
            scaledHeight = (int) (naturalHeight * scale);
        }

        if (naturalWidth > 0 && naturalHeight > 0 && scale > 1.0f) {
            layoutParams = new WindowManager.LayoutParams(scaledWidth, scaledHeight, layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    -3);
        } else {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    -3);
        }

        layoutParams.gravity = Gravity.TOP | Gravity.START;

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        
        if (savedPosX >= 0 && savedPosY >= 0) {
            // 有保存的位置，恢复它
            int viewWidth = naturalWidth > 0 ? (int) (naturalWidth * scale) : 0;
            int viewHeight = naturalHeight > 0 ? (int) (naturalHeight * scale) : 0;
            
            // 确保位置在屏幕范围内（处理贴边情况）
            layoutParams.x = Math.max(0, Math.min(savedPosX, screenWidth - viewWidth));
            layoutParams.y = Math.max(0, Math.min(savedPosY, screenHeight - viewHeight));
        } else {
            // 没有保存的位置，使用默认位置
            if (scale <= 1.0f && naturalWidth > 0) {
                layoutParams.x = (screenWidth - (int) (naturalWidth * scale)) / 2;
                layoutParams.y = dpToPx(80);
            } else {
                layoutParams.x = 0;
                layoutParams.y = dpToPx(80);
            }
        }

        applyScale();
        setupTouchListener();
        applyThemeColor();
        windowManager.addView(floatingView, layoutParams);
        isShowing = true;
    }

    private void doNaviSwitch() {
        recreateWindow();
        if (shouldHideAfterRecreate && floatingView != null) {
            floatingView.setVisibility(View.GONE);
        }
        shouldHideAfterRecreate = false;
    }

    private void onNaviTimeout() {
        if (currentMode == MODE_NAVI) {
            currentMode = MODE_CRUISE;
            View view = floatingView;
            shouldHideAfterRecreate = (view == null || view.getVisibility() == View.VISIBLE) ? false : true;
            handler.postDelayed(naviSwitchRunnable, 300);
        }
    }

    private void onCruiseGrace() {
        if (currentMode == MODE_NAVI) {
            currentMode = MODE_CRUISE;
            View view = floatingView;
            shouldHideAfterRecreate = (view == null || view.getVisibility() == View.VISIBLE) ? false : true;
            handler.postDelayed(naviSwitchRunnable, 300);
        }
    }

    // ======================== View 绑定 ========================

    private void bindViews() {
        clearAllRefs();
        if (currentMode == MODE_CRUISE) {
            bindCruiseViews();
        } else if (isMinimalStyle) {
            bindMinimalViews();
        } else {
            bindNormalViews();
        }
    }

    private void clearAllRefs() {
        tvCruiseSpeed = null;
        tvCruiseRoadName = null;
        llTrafficLightsContainer = null;
        ivTurnIcon = null;
        tvDistanceNum = null;
        tvDistanceUnit = null;
        tvAction = null;
        tvRoadName = null;
        pbRoute = null;
        tvSummary = null;
        tvEta = null;
        llTrafficLightGroup = null;
        ivLightIcon = null;
        ivLightArrow = null;
        tvLightTime = null;
        layoutInfoBar = null;
        ivActionIconMin = null;
        tvMinSpeed = null;
        tvMinSpeedUnit = null;
        tvDistanceNumMin = null;
        tvDistanceUnitMin = null;
        tvRoadNameMin = null;
        llTrafficLightGroupMin = null;
        ivLightIconMin = null;
        ivLightArrowMin = null;
        tvLightTimeMin = null;
    }

    private void bindCruiseViews() {
        tvCruiseSpeed = floatingView.findViewById(R.id.tv_cruise_speed);
        tvCruiseRoadName = floatingView.findViewById(R.id.tv_cruise_road_name);
        llTrafficLightsContainer = floatingView.findViewById(R.id.ll_traffic_lights_container);
    }

    private void bindNormalViews() {
        ivTurnIcon = floatingView.findViewById(R.id.iv_turn_icon);
        tvDistanceNum = floatingView.findViewById(R.id.tv_distance_num);
        tvDistanceUnit = floatingView.findViewById(R.id.tv_distance_unit);
        tvAction = floatingView.findViewById(R.id.tv_action);
        tvRoadName = floatingView.findViewById(R.id.tv_road_name);
        pbRoute = floatingView.findViewById(R.id.pb_route);
        tvSummary = floatingView.findViewById(R.id.tv_summary);
        tvEta = floatingView.findViewById(R.id.tv_eta);
        layoutInfoBar = floatingView.findViewById(R.id.layout_info_bar);

        View lightGroup = floatingView.findViewById(R.id.ll_traffic_light_group);
        llTrafficLightGroup = lightGroup;
        if (lightGroup != null) {
            ivLightIcon = lightGroup.findViewById(R.id.iv_light_icon);
            ivLightArrow = lightGroup.findViewById(R.id.iv_light_arrow);
            tvLightTime = lightGroup.findViewById(R.id.tv_light_time);
        }
    }

    private void bindMinimalViews() {
        ivActionIconMin = floatingView.findViewById(R.id.iv_action_icon_min);
        tvMinSpeed = floatingView.findViewById(R.id.tv_min_speed);
        tvMinSpeedUnit = floatingView.findViewById(R.id.tv_min_speed_unit);
        tvDistanceNumMin = floatingView.findViewById(R.id.tv_distance_num_min);
        tvDistanceUnitMin = floatingView.findViewById(R.id.tv_distance_unit_min);
        tvRoadNameMin = floatingView.findViewById(R.id.tv_road_name_min);

        View lightGroup = floatingView.findViewById(R.id.ll_traffic_light_group);
        if (lightGroup != null) {
            llTrafficLightGroupMin = lightGroup;
            ivLightIconMin = lightGroup.findViewById(R.id.iv_light_icon);
            ivLightArrowMin = lightGroup.findViewById(R.id.iv_light_arrow);
            tvLightTimeMin = lightGroup.findViewById(R.id.tv_light_time);
        }
    }

    // ======================== 缩放 ========================

    private void measureNaturalSize() {
        floatingView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        naturalWidth = floatingView.getMeasuredWidth();
        naturalHeight = floatingView.getMeasuredHeight();
    }

    private void applyScale() {
        if (floatingView == null || layoutParams == null) return;
        disableClipOnParents(floatingView);

        View target = scaleTarget;
        if (target == null) {
            target = floatingView.findViewById(R.id.root_layout);
            if (target == null) target = floatingView;
        }

        target.setPivotX(0f);
        target.setPivotY(0f);
        target.setScaleX(scale);
        target.setScaleY(scale);
    }

    private void disableClipOnParents(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            group.setClipChildren(false);
            group.setClipToPadding(false);
            for (int i = 0; i < group.getChildCount(); i++) {
                disableClipOnParents(group.getChildAt(i));
            }
        }
    }

    public void updateScale(float newScale) {
        this.scale = newScale;
        if (!isShowing || floatingView == null || layoutParams == null) return;

        applyScale();
        if (naturalWidth <= 0 || naturalHeight <= 0) return;

        int scaledWidth = (int) (naturalWidth * scale);
        int scaledHeight = (int) (naturalHeight * scale);

        if (scale > 1.0f) {
            layoutParams.width = scaledWidth;
            layoutParams.height = scaledHeight;
        } else {
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        
        // 边界校正，确保位置在屏幕范围内
        layoutParams.x = Math.max(0, Math.min(layoutParams.x, Math.max(0, screenWidth - scaledWidth)));
        layoutParams.y = Math.max(0, Math.min(layoutParams.y, Math.max(0, screenHeight - scaledHeight)));
        
        try {
            windowManager.updateViewLayout(floatingView, layoutParams);
        } catch (Exception ignored) {
        }
        
        // 更新位置后保存
        saveWindowPosition();
    }

    // ======================== 主题颜色 ========================

    public void applyThemeColor(int color) {
        this.themeColor = color;
        saveThemeColor();
        applyThemeColor();
    }

    private void saveThemeColor() {
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit().putInt("theme_color", themeColor).apply();
    }

    private void applyThemeColor() {
        if (floatingView == null) return;

        boolean isDark = isDarkThemeColor(themeColor);
        int accentColor = isDark ? Color.WHITE : themeColor;

        if (tvCruiseSpeed != null) tvCruiseSpeed.setTextColor(accentColor);
        if (tvMinSpeed != null) tvMinSpeed.setTextColor(accentColor);
        if (pbRoute != null) pbRoute.setProgressTintList(ColorStateList.valueOf(accentColor));

        if (layoutInfoBar != null) {
            int bgColor;
            if (isDark) {
                bgColor = 0xFF2A2A2A;
            } else {
                int r = (themeColor >> 16) & 0xFF;
                int g = (themeColor >> 8) & 0xFF;
                int b = themeColor & 0xFF;
                bgColor = 0xFF000000
                        | ((int) (r * 0.15f) << 16)
                        | ((int) (g * 0.15f) << 8)
                        | (int) (b * 0.15f);
            }
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setShape(GradientDrawable.RECTANGLE);
            bgDrawable.setColor(bgColor);
            bgDrawable.setCornerRadii(new float[]{0, 0, 0, 0, dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12)});
            layoutInfoBar.setBackground(bgDrawable);
        }

        View target = scaleTarget;
        if (target == null) target = floatingView.findViewById(R.id.root_layout);
        if (target == null) target = floatingView;

        Drawable background = target.getBackground();
        if (background != null) {
            background.mutate().setColorFilter(
                    new PorterDuffColorFilter((themeColor & 0x00FFFFFF) | 0x1E000000, PorterDuff.Mode.SRC_OVER));
        }
    }

    private boolean isDarkThemeColor(int color) {
        return ((color >> 16) & 0xFF) * 0.299
                + ((color >> 8) & 0xFF) * 0.587
                + (color & 0xFF) * 0.114 < 100;
    }

    // ======================== 触摸监听 ========================

    private void setupTouchListener() {
        floatingView.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                isDragging = false;
                hasLongPressed = false;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                initialWindowX = layoutParams.x;
                initialWindowY = layoutParams.y;
                handler.postDelayed(longPressCheck, LONG_PRESS_MS);
                return true;
            }
            if (action != MotionEvent.ACTION_UP) {
                if (action == MotionEvent.ACTION_MOVE) {
                    float dx = motionEvent.getRawX() - initialTouchX;
                    float dy = motionEvent.getRawY() - initialTouchY;
                    if (!isDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        isDragging = true;
                        handler.removeCallbacks(longPressCheck);
                    }
                    if (isDragging && !isPositionLocked) {
                        layoutParams.x = initialWindowX + (int) dx;
                        layoutParams.y = initialWindowY + (int) dy;
                        try {
                            windowManager.updateViewLayout(floatingView, layoutParams);
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                }
                if (action != MotionEvent.ACTION_CANCEL) return false;
            }
            handler.removeCallbacks(longPressCheck);
            
            // 拖拽结束后保存位置
            if (isDragging && !isPositionLocked) {
                saveWindowPosition();
            }
            
            return true;
        });
    }

    // ======================== 巡航数据更新 ========================

    public void updateCruiseInfo(int speed, String roadName) {
        if (isShowing && floatingView != null && currentMode == MODE_CRUISE) {
            if (tvCruiseSpeed != null) tvCruiseSpeed.setText(String.valueOf(speed));
            if (tvCruiseRoadName != null && roadName != null) tvCruiseRoadName.setText(roadName);
        }
    }

    public void updateCruiseTrafficLights(JSONArray lightsArray) {
        if (!isShowing || floatingView == null || currentMode != MODE_CRUISE || llTrafficLightsContainer == null)
            return;

        int count = lightsArray != null ? lightsArray.length() : 0;
        int childCount = llTrafficLightsContainer.getChildCount();

        if (count == 0) {
            llTrafficLightsContainer.setVisibility(View.GONE);
            if (childCount > 0) llTrafficLightsContainer.removeAllViews();
            return;
        }

        if (count != childCount) {
            llTrafficLightsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < count; i++) {
                try {
                    JSONObject lightObj = lightsArray.getJSONObject(i);
                    View lightView = inflater.inflate(R.layout.item_cruise_traffic_light, llTrafficLightsContainer, false);
                    updateSingleLightView(lightView, lightObj);
                    llTrafficLightsContainer.addView(lightView);
                } catch (Exception ignored) {
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                try {
                    updateSingleLightView(llTrafficLightsContainer.getChildAt(i), lightsArray.getJSONObject(i));
                } catch (Exception ignored) {
                }
            }
        }
        llTrafficLightsContainer.setVisibility(View.VISIBLE);
        // 所有灯都倒计时为0时隐藏容器
        boolean allGone = true;
        for (int i = 0; i < llTrafficLightsContainer.getChildCount(); i++) {
            if (llTrafficLightsContainer.getChildAt(i).getVisibility() == View.VISIBLE) {
                allGone = false;
                break;
            }
        }
        if (allGone) llTrafficLightsContainer.setVisibility(View.GONE);
    }

    private void updateSingleLightView(View view, JSONObject jsonObj) throws Exception {
        int status = jsonObj.getInt("status");
        int countdown = jsonObj.getInt("countdown");
        int dir = jsonObj.getInt("dir");

        ImageView lightIcon = view.findViewById(R.id.iv_light_icon);
        ImageView lightArrow = view.findViewById(R.id.iv_light_arrow);
        TextView lightTime = view.findViewById(R.id.tv_light_time);

        if (lightIcon != null) lightIcon.setImageResource(getCruiseLightIconRes(status));
        if (lightArrow != null) lightArrow.setImageResource(getCruiseLightDirRes(dir));
        if (lightTime != null) lightTime.setText(String.valueOf(countdown));
        view.setVisibility(countdown > 0 ? View.VISIBLE : View.GONE);
    }

    public void hideCruiseTrafficLights() {
        if (llTrafficLightsContainer != null) {
            llTrafficLightsContainer.removeAllViews();
            llTrafficLightsContainer.setVisibility(View.GONE);
        }
    }

    // ======================== 导航数据更新 ========================

    public void updateNaviInfo(int icon, String disNum, String disUnit, String actionStr,
                               String roadName, String summaryStr, String eta,
                               int progress, int curSpeed) {
        if (isShowing && floatingView != null && currentMode == MODE_NAVI) {
            if (isMinimalStyle) {
                updateMinimalNaviInfo(icon, disNum, disUnit, roadName, curSpeed);
            } else {
                updateNormalNaviInfo(icon, disNum, disUnit, actionStr, roadName, summaryStr, eta, progress);
            }
        }
    }

    private void updateNormalNaviInfo(int icon, String disNum, String disUnit, String actionStr,
                                       String roadName, String summaryStr, String eta, int progress) {
        int turnIconRes = getTurnIconRes(icon);
        if (ivTurnIcon != null && turnIconRes != 0) ivTurnIcon.setImageResource(turnIconRes);
        if (tvDistanceNum != null) tvDistanceNum.setText(disNum);
        if (tvDistanceUnit != null) tvDistanceUnit.setText(disNumIsNow(disNum)?"" : disUnit);
        if (tvAction != null) tvAction.setText(actionStr);
        if (tvRoadName != null) tvRoadName.setText(roadName);
        if (pbRoute != null) pbRoute.setProgress(progress);
        if (tvSummary != null) tvSummary.setText(summaryStr);
        if (tvEta != null) tvEta.setText(eta);
    }

    private boolean disNumIsNow(String disNum){
        return "现在".equals(disNum);
    }

    private void updateMinimalNaviInfo(int icon, String disNum, String disUnit, String roadName, int speed) {
        int turnIconRes = getTurnIconRes(icon);
        if (ivActionIconMin != null && turnIconRes != 0) ivActionIconMin.setImageResource(turnIconRes);
        if (tvMinSpeed != null) tvMinSpeed.setText(String.valueOf(speed));
        if (tvDistanceNumMin != null) tvDistanceNumMin.setText(disNum);
        if (tvDistanceUnitMin != null) tvDistanceUnitMin.setText(disNumIsNow(disNum)?"进入" : disUnit);
        if (tvRoadNameMin != null) tvRoadNameMin.setText(roadName);
    }

    // ======================== 红绿灯更新 ========================

    public void updateTrafficLight(int status, int dir, int countdown) {
        if (!isShowing || floatingView == null || currentMode != MODE_NAVI) return;

        View lightGroup;
        ImageView lightIcon;
        ImageView lightArrow;
        TextView lightTime;

        if (isMinimalStyle) {
            lightGroup = llTrafficLightGroupMin;
            lightIcon = ivLightIconMin;
            lightArrow = ivLightArrowMin;
            lightTime = tvLightTimeMin;
        } else {
            lightGroup = llTrafficLightGroup;
            lightIcon = ivLightIcon;
            lightArrow = ivLightArrow;
            lightTime = tvLightTime;
        }

        // 倒计时为 0 时隐藏红绿灯胶囊
        if (countdown <= 0) {
            if (lightGroup != null) lightGroup.setVisibility(View.GONE);
            return;
        }

        if (lightGroup != null) lightGroup.setVisibility(View.VISIBLE);
        if (lightIcon != null) lightIcon.setImageResource(getNaviLightIconRes(status));
        if (lightArrow != null) lightArrow.setImageResource(getNaviLightDirRes(dir));
        if (lightTime != null) lightTime.setText(String.valueOf(countdown));

        handler.removeCallbacks(trafficLightTimeoutRunnable);
        handler.postDelayed(trafficLightTimeoutRunnable, LIGHT_HIDE_TIMEOUT_MS);
    }

    public void hideTrafficLight() {
        if (!isShowing || floatingView == null) return;
        handler.removeCallbacks(trafficLightTimeoutRunnable);
        hideTrafficLightCapsule();
    }

    private void hideTrafficLightCapsule() {
        View view = isMinimalStyle ? llTrafficLightGroupMin : llTrafficLightGroup;
        if (view != null) view.setVisibility(View.GONE);
    }

    // ======================== 图标映射 ========================

    /**
     * 转向图标映射
     * 已验证: 2=左转 3=右转 4=左前方 5=右前方 8=掉头 9=直行 10=途经点 11=进入匝道 12=驶出匝道 15=终点
     */
    private int getTurnIconRes(int icon) {
        switch (icon) {
            case 2: return R.mipmap.ic_navi_left;
            case 3: return R.mipmap.ic_navi_right;
            case 4: return R.mipmap.ic_navi_left_d;
            case 5: return R.mipmap.ic_navi_right_d;
            case 8: return R.mipmap.ic_navi_u_turn;
            case 9: return R.mipmap.ic_navi_straight;
            case 10: return R.mipmap.ic_navi_mid;
            case 11: return R.mipmap.ic_navi_in_dao;
            case 12: return R.mipmap.ic_navi_en_dao;
            case 15: return R.mipmap.ic_navi_end;
            default: return R.mipmap.ic_navi_straight;
        }
    }

    /** 导航模式红绿灯图标 */
    private int getNaviLightIconRes(int status) {
        if (status == 4) return R.drawable.ic_traffic_light_green;
        if (status == 1) return R.drawable.ic_traffic_light_red;
        return R.drawable.ic_traffic_light_yellow;
    }

    /** 导航模式红绿灯方向 */
    private int getNaviLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_right;
        if (dir == 3) return R.mipmap.light_u_turn;
        if (dir == 4) return R.mipmap.light_straight;
        return R.mipmap.light_straight;
    }

    /** 巡航模式红绿灯图标 */
    private int getCruiseLightIconRes(int status) {
        if (status == 1) return R.drawable.ic_traffic_light_green;
        if (status == 0) return R.drawable.ic_traffic_light_red;
        return R.drawable.ic_traffic_light_yellow;
    }

    /** 巡航模式红绿灯方向 */
    private int getCruiseLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_straight;
        if (dir == 3) return R.mipmap.light_right;
        return R.mipmap.light_straight;
    }

    // ======================== 工具方法 ========================

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
    
    // ======================== 位置保存 ========================
    
    private void saveWindowPosition() {
        if (layoutParams == null || naturalWidth <= 0 || naturalHeight <= 0) return;
        
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int viewWidth = (int) (naturalWidth * scale);
        int viewHeight = (int) (naturalHeight * scale);
        
        // 边界校正，确保位置在屏幕范围内
        int correctedX = Math.max(0, Math.min(layoutParams.x, screenWidth - viewWidth));
        int correctedY = Math.max(0, Math.min(layoutParams.y, screenHeight - viewHeight));
        
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit()
                .putInt("window_pos_x", correctedX)
                .putInt("window_pos_y", correctedY)
                .apply();
    }
}
