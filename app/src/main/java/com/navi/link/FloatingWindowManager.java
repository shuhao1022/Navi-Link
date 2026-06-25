package com.navi.link;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.util.Log;

import org.json.JSONArray;

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
    private BaseFloatingWindow activeWindow;

    // 副屏相关变量
    private boolean isClusterMirrorEnabled = false;
    private int clusterDisplayId = -1;
    private View clusterFloatingView;
    private BaseFloatingWindow clusterActiveWindow;
    private WindowManager clusterWindowManager;
    private WindowManager.LayoutParams clusterLayoutParams;
    private int clusterSavedPosX = -1;
    private int clusterSavedPosY = -1;
    private int clusterNaturalWidth = 0;
    private int clusterNaturalHeight = 0;
    private View clusterScaleTarget = null;
    private Context clusterContext = null;
    private boolean clusterIsDragging = false;
    private float clusterInitialTouchX;
    private float clusterInitialTouchY;
    private int clusterInitialWindowX;
    private int clusterInitialWindowY;
    private static final String TAG = "FloatingWindowManager";

    // 状态
    private int currentMode = MODE_CRUISE;
    private int styleMode = 0; // 0=normal, 1=minimal, 2=full
    // 各模式独立缩放: [0]=常规/常规巡航, [1]=灵动岛/灵动岛巡航, [2]=全数据
    private float[] scales = {1.0f, 1.0f, 1.0f};
    private int themeColor = 0xFF4FC3F7;
    private boolean isShowing = false;
    private boolean hasActiveData = false; // 是否收到过实际导航/巡航广播数据

    // 昼夜模式 + 透明背景
    private boolean isNightMode = true; // 默认夜间（深色文字）
    private int backgroundMode = 0; // 0=深色, 1=半透明, 2=全透明
    private boolean isAmapForeground = false;

    // 透明主题文字颜色常量
    // 亮色背景（高德白天）用深色文字
    private static final int TEXT_PRIMARY_LIGHT = 0xFF1a1a1a;
    private static final int TEXT_SECONDARY_LIGHT = 0xFF333333;
    private static final int TEXT_HINT_LIGHT = 0xFF999999;
    // 暗色背景（高德夜间）用浅色文字
    private static final int TEXT_PRIMARY_DARK = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY_DARK = 0xBBFFFFFF;
    private static final int TEXT_HINT_DARK = 0x88FFFFFF;

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

    private long lastNavigationEndTimestamp = 0;
    private long lastCruiseEndTimestamp = 0;

    // 数据缓存：recreateWindow 后立即恢复，避免闪烁默认内容
    private boolean hasCachedData = false;
    private int cachedSpeed = 0;
    private String cachedRoadName = "";
    private int cachedIcon = -1;
    private String cachedDisNum = "";
    private String cachedDisUnit = "";
    private String cachedActionStr = "";
    private String cachedSummaryStr = "";
    private String cachedEta = "";
    private int cachedProgress = 0;
    private int cachedLightStatus = -1;
    private int cachedLightDir = -1;
    private int cachedLightCountdown = 0;
    private int cachedLimitedSpeed = 0;
    private int cachedCameraDist = 0;
    private int cachedCameraSpeed = 0;
    private int cachedCameraType = 0;
    private String cachedEndPoiName = "";
    private int cachedTotalLightNum = 0;
    private int cachedRemainLightNum = 0;
    private String cachedCurRoadName = "";
    private int cachedCarDirection = 0;
    private String cachedTmcJson = null;
    private String cachedDriveWayJson = null;
    private String cachedExitName = "";
    private String cachedExitDirection = "";
    private String cachedSapaName = "";
    private String cachedSapaDist = "";
    private String cachedNextSapaName = "";
    private String cachedNextSapaDist = "";

    // Runnable
    private final Runnable naviSwitchRunnable = this::doNaviSwitch;
    private final Runnable naviTimeoutRunnable = this::onNaviTimeout;
    private final Runnable cruiseGraceRunnable = this::onCruiseGrace;
    private final Runnable watchdogRunnable = () -> {
        hasActiveData = false;
        updateFloatingWindowVisibility();
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
        styleMode = sp.getInt("style_mode", sp.getBoolean("is_minimal_style", false) ? 1 : 0);
        scales[0] = sp.getFloat("scale_normal", 1.0f);
        scales[1] = sp.getFloat("scale_minimal", 1.0f);
        scales[2] = sp.getFloat("scale_full", 1.0f);
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
        savedPosX = sp.getInt("window_pos_x", -1);
        savedPosY = sp.getInt("window_pos_y", -1);
        isNightMode = sp.getBoolean("is_night_mode", true); // 默认夜间
        backgroundMode = sp.getInt("background_mode", 0); // 默认深色背景
        isClusterMirrorEnabled = sp.getBoolean("cluster_mirror_enabled", false);
        clusterDisplayId = sp.getInt("cluster_display_id", -1);
        clusterSavedPosX = sp.getInt("cluster_window_pos_x", -1);
        clusterSavedPosY = sp.getInt("cluster_window_pos_y", -1);
    }

    /** 当前模式对应的缩放索引: 常规/常规巡航=0, 灵动岛/灵动岛巡航=1, 全数据=2 */
    private int getScaleIndex() {
        if (currentMode == MODE_CRUISE) return styleMode == 1 ? 1 : 0; // 灵动岛巡航用1，常规巡航用0
        if (styleMode == 0) return 0; // 常规
        return styleMode; // 1=灵动岛, 2=全数据
    }

    public float getScale() {
        return scales[getScaleIndex()];
    }

    private void saveScalePreferences() {
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE).edit()
                .putFloat("scale_normal", scales[0])
                .putFloat("scale_minimal", scales[1])
                .putFloat("scale_full", scales[2])
                .apply();
    }

    // ======================== 窗口显示与隐藏 ========================

    public void show() {
        currentMode = MODE_CRUISE;
        if (!isCruiseEnabled()) {
            isShowing = true;
            return;
        }
        recreateWindow();
    }

    public void refreshWindow() {
        if (isShowing) {
            recreateWindow();
        }
    }

    public void hide() {
        handler.removeCallbacksAndMessages(null);
        dismissClusterMirror();
        if (floatingView == null || !isShowing) return;
        try {
            windowManager.removeView(floatingView);
        } catch (Exception ignored) {
        }
        if (activeWindow != null) {
            activeWindow.onDestroy();
            activeWindow = null;
        }
        floatingView = null;
        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public boolean isActive() {
        return hasActiveData;
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
        if (!isCruiseEnabled()) {
            // 巡航未启用，移除窗口
            dismissClusterMirror();
            if (floatingView != null) {
                try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
                if (activeWindow != null) {
                    activeWindow.onDestroy();
                    activeWindow = null;
                }
                floatingView = null;
                isShowing = false;
            }
            return;
        }
        shouldHideAfterRecreate = false;
        if (currentMode != MODE_CRUISE || isNaviWindowActive()) {
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

    public void onNavigationEnded() {
        lastNavigationEndTimestamp = System.currentTimeMillis();
        // 1. 清理缓存数据
        cachedIcon = -1;
        cachedDisNum = "";
        cachedDisUnit = "";
        cachedActionStr = "";
        cachedSummaryStr = "";
        cachedEta = "";
        cachedProgress = 0;
        cachedLightStatus = -1;
        cachedLightDir = -1;
        cachedLightCountdown = 0;
        cachedSpeed = 0;
        cachedLimitedSpeed = 0;
        cachedCameraType = 0;
        cachedCameraDist = 0;
        cachedCameraSpeed = 0;
        cachedEndPoiName = "";
        cachedTotalLightNum = 0;
        cachedRemainLightNum = 0;
        cachedCurRoadName = "";
        cachedCarDirection = 0;
        cachedTmcJson = null;
        cachedDriveWayJson = null;
        cachedExitName = "";
        cachedExitDirection = "";
        cachedSapaName = "";
        cachedSapaDist = "";
        cachedNextSapaName = "";
        cachedNextSapaDist = "";

        hasActiveData = false;
        currentMode = MODE_CRUISE;

        // 取消所有超时与切换的延迟任务
        handler.removeCallbacks(naviTimeoutRunnable);
        handler.removeCallbacks(naviSwitchRunnable);
        handler.removeCallbacks(cruiseGraceRunnable);
        handler.removeCallbacks(watchdogRunnable);

        // 2. 立即隐藏视图
        if (floatingView != null) {
            floatingView.setVisibility(View.GONE);
        }
    }

    public void onCruiseEnded() {
        lastCruiseEndTimestamp = System.currentTimeMillis();
        // 1. 清理缓存数据
        cachedSpeed = 0;
        cachedRoadName = "";
        cachedCameraType = 0;
        cachedCameraSpeed = 0;
        cachedCameraDist = 0;
        cachedDriveWayJson = null;

        hasActiveData = false;

        // 取消所有延迟任务
        handler.removeCallbacks(naviTimeoutRunnable);
        handler.removeCallbacks(naviSwitchRunnable);
        handler.removeCallbacks(cruiseGraceRunnable);
        handler.removeCallbacks(watchdogRunnable);

        // 2. 立即隐藏视图
        if (floatingView != null) {
            floatingView.setVisibility(View.GONE);
        }
    }

    public boolean isNavigationJustEnded() {
        return System.currentTimeMillis() - lastNavigationEndTimestamp < 3000;
    }

    public boolean isCruiseJustEnded() {
        return System.currentTimeMillis() - lastCruiseEndTimestamp < 3000;
    }

    public boolean isNaviWindowActive() {
        return activeWindow instanceof NormalNaviWindow 
                || activeWindow instanceof MinimalNaviWindow 
                || activeWindow instanceof FullNaviWindow;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public boolean isCruiseEnabled() {
        SharedPreferences sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        return sp.getBoolean("cruise_enabled", true);
    }

    public boolean isNormalNaviLaneEnabled() {
        SharedPreferences sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        return sp.getBoolean("normal_navi_lane_enabled", false);
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
        hasActiveData = true;
        handler.removeCallbacks(watchdogRunnable);
        handler.postDelayed(watchdogRunnable, WATCHDOG_TIMEOUT_MS);
        updateFloatingWindowVisibility();
    }

    // ======================== 窗口重建 ========================

    private void recreateWindow() {
        // 先保存旧位置（如果窗口已存在）
        int oldSavedPosX = savedPosX;
        int oldSavedPosY = savedPosY;
        
        loadPreferences();
        scaleTarget = null;
        dismissClusterMirror();

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
            if (activeWindow != null) {
                activeWindow.onDestroy();
                activeWindow = null;
            }
            floatingView = null;
        }

        int layoutRes;
        if (currentMode == MODE_NAVI) {
            if (styleMode == 2) layoutRes = R.layout.layout_floating_navi_full;
            else if (styleMode == 1) layoutRes = R.layout.layout_floating_navi_minimal;
            else layoutRes = R.layout.layout_floating_navi_normal;
        } else {
            layoutRes = styleMode == 1
                    ? R.layout.layout_floating_cruise_minimal
                    : R.layout.layout_floating_cruise_normal;
        }

        View inflated = LayoutInflater.from(context).inflate(layoutRes, null);
        floatingView = inflated;

        // 灵动岛/全数据模式外层需要 FrameLayout
        if (currentMode == MODE_NAVI && styleMode >= 1) {
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

        // 物理缩放内容：缩小或放大时直接调整文字尺寸/padding/margin，窗口用 WRAP_CONTENT 自然撑开
        float scale = getScale();
        if (scale != 1.0f) {
            physicalScaleContent(inflated);
        }

        if (activeWindow != null) {
            activeWindow.onDestroy();
        }
        activeWindow = FloatingWindowFactory.createWindow(currentMode, styleMode, context, inflated);

        restoreCachedData();
        measureNaturalSize();

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        // 始终 WRAP_CONTENT，物理缩放后内容尺寸已改变
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                -3);

        layoutParams.gravity = Gravity.TOP | Gravity.START;

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // 物理缩放后 naturalWidth/Height 已是最终尺寸，无需再乘 scale
        int viewWidth = naturalWidth;
        int viewHeight = naturalHeight;

        if (savedPosX >= 0 && savedPosY >= 0) {
            // 有保存的位置，恢复它
            layoutParams.x = Math.max(0, Math.min(savedPosX, screenWidth - Math.max(viewWidth, 1)));
            layoutParams.y = Math.max(0, Math.min(savedPosY, screenHeight - Math.max(viewHeight, 1)));
        } else {
            // 没有保存的位置，使用默认位置
            if (viewWidth > 0) {
                layoutParams.x = (screenWidth - viewWidth) / 2;
            } else {
                layoutParams.x = 0;
            }
            layoutParams.y = dpToPx(80);
        }

        applyScale();
        setupTouchListener();
        applyThemeColor();
        windowManager.addView(floatingView, layoutParams);
        isShowing = true;
        ensureClusterMirror();
        updateFloatingWindowVisibility();
    }

    private void doNaviSwitch() {
        if (currentMode == MODE_CRUISE && !isCruiseEnabled()) {
            // 巡航未启用，移除窗口
            dismissClusterMirror();
            if (floatingView != null) {
                try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
                if (activeWindow != null) {
                    activeWindow.onDestroy();
                    activeWindow = null;
                }
                floatingView = null;
                isShowing = false;
            }
            return;
        }
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



    /**
     * recreateWindow 后立即恢复缓存数据，避免布局短暂显示默认值闪烁
     */
    private void restoreCachedData() {
        if (!hasCachedData || activeWindow == null) return;
        if (currentMode == MODE_CRUISE) {
            if (activeWindow != null) {
                activeWindow.updateCruiseInfo(cachedSpeed, cachedRoadName, cachedCameraType, cachedCameraSpeed, cachedCameraDist, cachedCarDirection);
            }
            if (cachedDriveWayJson != null) {
                activeWindow.updateLaneLines(cachedDriveWayJson);
            }
        } else if (currentMode == MODE_NAVI) {
            activeWindow.updateNaviInfo(
                    cachedIcon,
                    cachedDisNum,
                    cachedDisUnit,
                    cachedActionStr,
                    cachedRoadName,
                    cachedSummaryStr,
                    cachedEta,
                    cachedProgress,
                    cachedSpeed,
                    cachedLimitedSpeed,
                    cachedCameraType,
                    cachedCameraDist,
                    cachedCameraSpeed,
                    cachedEndPoiName,
                    cachedTotalLightNum,
                    cachedRemainLightNum,
                    cachedCurRoadName,
                    cachedCarDirection
            );
            if (cachedTmcJson != null) {
                activeWindow.updateTmcData(cachedTmcJson);
            }
            if (cachedDriveWayJson != null) {
                activeWindow.updateLaneLines(cachedDriveWayJson);
            }
            if (cachedExitName != null) {
                activeWindow.updateExitInfo(cachedExitName, cachedExitDirection);
            }
            if (cachedSapaName != null && !cachedSapaName.isEmpty()) {
                activeWindow.updateSapaInfo(cachedSapaName, cachedSapaDist, cachedNextSapaName, cachedNextSapaDist);
            }
            if (cachedLightCountdown > 0) {
                activeWindow.updateTrafficLight(cachedLightStatus, cachedLightDir, cachedLightCountdown);
            }
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

    /** 物理缩放：递归调整文字大小、padding、margin、固定宽高 */
    private void physicalScaleContent(View root) {
        scaleViewRecursive(root, getScale());
    }

    private void scaleViewRecursive(View view, float factor) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() * factor);
        }

        view.setPadding(
                Math.round(view.getPaddingLeft() * factor),
                Math.round(view.getPaddingTop() * factor),
                Math.round(view.getPaddingRight() * factor),
                Math.round(view.getPaddingBottom() * factor));

        // 缩放背景 drawable 圆角 (getCornerRadius/getCornerRadii 均为 API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Drawable bg = view.getBackground();
            if (bg instanceof GradientDrawable) {
                GradientDrawable gd = (GradientDrawable) bg.mutate();
                float r = gd.getCornerRadius();
                if (r > 0) {
                    gd.setCornerRadius(r * factor);
                } else {
                    try {
                        float[] radii = gd.getCornerRadii();
                        if (radii != null) {
                            for (int i = 0; i < radii.length; i++) radii[i] *= factor;
                            gd.setCornerRadii(radii);
                        }
                    } catch (Exception e) {
                        // Ignore NPE from some Android versions when radii array is null
                    }
                }
            }
        }

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            if (lp.width > 0) lp.width = Math.round(lp.width * factor);
            if (lp.height > 0) lp.height = Math.round(lp.height * factor);
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.leftMargin = Math.round(mlp.leftMargin * factor);
                mlp.topMargin = Math.round(mlp.topMargin * factor);
                mlp.rightMargin = Math.round(mlp.rightMargin * factor);
                mlp.bottomMargin = Math.round(mlp.bottomMargin * factor);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                scaleViewRecursive(vg.getChildAt(i), factor);
            }
        }
    }

    public void updateScale(float newScale) {
        int idx = getScaleIndex();
        if (Math.abs(scales[idx] - newScale) < 0.01f) return;
        scales[idx] = newScale;
        saveScalePreferences();
        if (isShowing) {
            refreshWindow();
        }
    }

    /** 更新 TMC 路况数据（KEY_TYPE=13011） */
    public void updateTmcData(String tmcJson) {
        if (tmcJson == null || tmcJson.isEmpty()) return;
        cachedTmcJson = tmcJson;
        if (isShowing && currentMode == MODE_NAVI) {
            if (activeWindow != null && floatingView != null) {
                activeWindow.updateTmcData(tmcJson);
            }
            if (clusterActiveWindow != null && clusterFloatingView != null) {
                clusterActiveWindow.updateTmcData(tmcJson);
            }
        }
    }

    /** ETA 文字精简："预计明天09:14到达" → "明09:14到" */
    private String formatEta(String eta) {
        if (eta == null || eta.isEmpty()) return "";
        String result = eta;
        // 去掉"预计"
        if (result.startsWith("预计")) {
            result = result.substring(2);
        }
        // "到达" 改为 "到"
        if (result.endsWith("到达")) {
            result = result.substring(0, result.length() - 2) + "到";
        }
        // 缩短日期前缀
        result = result.replace("明天", "明");
        result = result.replace("后天", "后");
        result = result.replace("大后天", "大后");
        return result;
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
        if (floatingView != null) {
            if (activeWindow != null) {
                activeWindow.applyThemeColor(themeColor);
            }
            applyThemeColorToView(floatingView, scaleTarget);
        }

        if (clusterFloatingView != null) {
            if (clusterActiveWindow != null) {
                clusterActiveWindow.applyThemeColor(themeColor);
            }
            applyThemeColorToView(clusterFloatingView, clusterScaleTarget);
        }

        // 仅全透明模式下应用昼夜文字颜色，其他模式恢复默认
        if (backgroundMode == 2) {
            applyDayNightTextColors();
        } else {
            resetToDefaultTextColors();
        }
    }

    private void applyThemeColorToView(View root, View targetScale) {
        View target = root.findViewById(R.id.root_layout);
        if (target == null) target = targetScale;
        if (target == null) target = root;

        // 透明背景模式处理
        // 根据样式确定圆角: 灵动岛/灵动岛巡航=40dp, 常规/常规巡航/全数据=12dp
        boolean isIslandStyle = styleMode == 1;
        int cornerDp = isIslandStyle ? 40 : 12;
        int cornerPx = Math.round(dpToPx(cornerDp) * getScale());

        if (backgroundMode == 2) {
            // 全透明
            target.setBackground(null);
        } else if (backgroundMode == 1) {
            // 半透明 - 跟随主题色
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setShape(GradientDrawable.RECTANGLE);
            int semiColor = (themeColor & 0x00FFFFFF) | 0x80000000;
            bgDrawable.setColor(semiColor);
            bgDrawable.setCornerRadius(cornerPx);
            target.setBackground(bgDrawable);
        } else {
            // 深色背景 - 重建背景确保不残留透明度
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setShape(GradientDrawable.RECTANGLE);
            int bgColor;
            if (isDarkThemeColor(themeColor)) {
                bgColor = 0xFF121212;
            } else {
                int r = (themeColor >> 16) & 0xFF;
                int g = (themeColor >> 8) & 0xFF;
                int b = themeColor & 0xFF;
                bgColor = 0xFF000000
                        | ((int) (r * 0.12f) << 16)
                        | ((int) (g * 0.12f) << 8)
                        | (int) (b * 0.12f);
            }
            bgDrawable.setColor(bgColor);
            bgDrawable.setCornerRadius(cornerPx);
            target.setBackground(bgDrawable);
        }
    }

    /**
     * 应用昼夜模式文字颜色（仅透明模式下生效）
     */
    private void applyDayNightTextColors() {
        if (activeWindow != null) {
            activeWindow.applyDayNightTextColors(isNightMode);
        }
        if (clusterActiveWindow != null) {
            clusterActiveWindow.applyDayNightTextColors(isNightMode);
        }
    }

    /**
     * 恢复默认文字颜色（深色/半透明模式下使用白色系）
     */
    private void resetToDefaultTextColors() {
        if (activeWindow != null) {
            activeWindow.resetToDefaultTextColors();
        }
        if (clusterActiveWindow != null) {
            clusterActiveWindow.resetToDefaultTextColors();
        }
    }

    /**
     * 高德昼夜模式变化回调
     */
    public void onDayNightChanged(boolean isNight) {
        if (this.isNightMode == isNight) return; // 无变化不刷新
        this.isNightMode = isNight;
        saveDayNightState();
        if (backgroundMode > 0) {
            applyThemeColor(); // 重新应用颜色
        }
    }

    /**
     * 高德前后台运行状态变化回调
     */
    public void onAmapForegroundChanged(boolean isForeground) {
        if (this.isAmapForeground == isForeground) return;
        this.isAmapForeground = isForeground;
        updateFloatingWindowVisibility();
    }

    /**
     * 统一更新悬浮窗显隐状态的方法，高德前后台切换、用户改变配置、看门狗复位时都会触发此方法
     */
    public void updateFloatingWindowVisibility() {
        SharedPreferences sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        boolean hideOnForeground = sp.getBoolean("hide_on_amap_foreground", false);
        boolean hideMainWhenClusterActive = sp.getBoolean("hide_main_when_cluster_active", false);

        boolean isClusterActive = isClusterMirrorEnabled && clusterFloatingView != null;
        boolean shouldHideMain = (hideOnForeground && isAmapForeground)
                || (hideMainWhenClusterActive && isClusterActive);

        if (floatingView != null) {
            if (shouldHideMain) {
                floatingView.setVisibility(View.GONE);
            } else {
                if (currentMode == MODE_NAVI || (isCruiseEnabled() && hasActiveData)) {
                    floatingView.setVisibility(View.VISIBLE);
                } else {
                    floatingView.setVisibility(View.GONE);
                }
            }
        }
        updateClusterFloatingWindowVisibility();
    }

    /**
     * 设置背景模式: 0=深色, 1=半透明, 2=全透明
     */
    public void setBackgroundMode(int mode) {
        this.backgroundMode = mode;
        saveBackgroundMode();
        applyThemeColor();
    }

    public int getBackgroundMode() {
        return backgroundMode;
    }

    public boolean isNightMode() {
        return isNightMode;
    }

    private void saveDayNightState() {
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit().putBoolean("is_night_mode", isNightMode).apply();
    }

    private void saveBackgroundMode() {
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit().putInt("background_mode", backgroundMode).apply();
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

            // 单击（非拖拽、非长按）→ 打开设置页
            if (!isDragging && !hasLongPressed && action == MotionEvent.ACTION_UP) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }
                    PendingIntent.getActivity(context, 0, intent, flags).send();
                } catch (PendingIntent.CanceledException e) {
                    context.startActivity(intent);
                }
            }

            // 拖拽结束后保存位置
            if (isDragging && !isPositionLocked) {
                saveWindowPosition();
            }

            return true;
        });
    }

    // ======================== 巡航数据更新 ========================

    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        hasCachedData = true;
        cachedSpeed = speed;
        cachedRoadName = roadName;
        cachedCameraType = cameraType;
        cachedCameraSpeed = cameraSpeed;
        cachedCameraDist = cameraDist;
        cachedCarDirection = carDirection;
        if (isShowing && currentMode == MODE_CRUISE) {
            if (activeWindow != null && floatingView != null) {
                activeWindow.updateCruiseInfo(speed, roadName, cameraType, cameraSpeed, cameraDist, carDirection);
            }
            if (clusterActiveWindow != null && clusterFloatingView != null) {
                clusterActiveWindow.updateCruiseInfo(speed, roadName, cameraType, cameraSpeed, cameraDist, carDirection);
            }
            // 速度/路名文字变化后重新测量窗口，避免内容变宽时被旧宽度截断
            remeasureWindow();
        }
    }

    public void updateLaneLines(String driveWayJson) {
        cachedDriveWayJson = driveWayJson;
        if (isShowing) {
            boolean updated = false;
            if (activeWindow != null && floatingView != null) {
                activeWindow.updateLaneLines(driveWayJson);
                updated = true;
            }
            if (clusterActiveWindow != null && clusterFloatingView != null) {
                clusterActiveWindow.updateLaneLines(driveWayJson);
                updated = true;
            }
            if (updated) {
                remeasureWindow();
            }
        }
    }

    public void updateCruiseTrafficLights(JSONArray lightsArray) {
        if (!isShowing || currentMode != MODE_CRUISE) return;
        boolean updated = false;
        if (activeWindow != null && floatingView != null) {
            activeWindow.updateCruiseTrafficLights(lightsArray);
            updated = true;
        }
        if (clusterActiveWindow != null && clusterFloatingView != null) {
            clusterActiveWindow.updateCruiseTrafficLights(lightsArray);
            updated = true;
        }
        if (updated) {
            remeasureWindow();
        }
    }

    /**
     * 重新测量并更新窗口尺寸，用于红绿灯动态增减后自适应宽度
     */
    private void remeasureWindow() {
        if (floatingView != null && layoutParams != null) {
            measureNaturalSize();
            layoutParams.width = naturalWidth;
            layoutParams.height = naturalHeight;
            try {
                windowManager.updateViewLayout(floatingView, layoutParams);
            } catch (Exception ignored) {
            }
        }
        if (clusterFloatingView != null && clusterLayoutParams != null && clusterWindowManager != null) {
            measureNaturalSizeForCluster(clusterFloatingView);
            clusterLayoutParams.width = clusterNaturalWidth;
            clusterLayoutParams.height = clusterNaturalHeight;
            try {
                clusterWindowManager.updateViewLayout(clusterFloatingView, clusterLayoutParams);
            } catch (Exception ignored) {
            }
        }
    }

    // ======================== 导航数据更新 ========================

    public void updateNaviInfo(int icon, String disNum, String disUnit, String actionStr,
                               String roadName, String summaryStr, String eta,
                               int progress, int curSpeed,
                               int limitedSpeed, int cameraType, int cameraDist, int cameraSpeed,
                               String endPoiName, int totalLightNum, int remainLightNum,
                               String curRoadName, int carDirection) {
        hasCachedData = true;
        cachedIcon = icon;
        cachedDisNum = disNum;
        cachedDisUnit = disUnit;
        cachedActionStr = actionStr;
        cachedRoadName = roadName;
        cachedSummaryStr = summaryStr;
        cachedEta = eta;
        cachedProgress = progress;
        cachedSpeed = curSpeed;
        cachedLimitedSpeed = limitedSpeed;
        cachedCameraType = cameraType;
        cachedCameraDist = cameraDist;
        cachedCameraSpeed = cameraSpeed;
        cachedEndPoiName = endPoiName != null ? endPoiName : "";
        cachedTotalLightNum = totalLightNum;
        cachedRemainLightNum = remainLightNum;
        cachedCurRoadName = curRoadName != null ? curRoadName : "";
        cachedCarDirection = carDirection;
        if (isShowing && currentMode == MODE_NAVI) {
            if (activeWindow != null && floatingView != null) {
                activeWindow.updateNaviInfo(
                        icon, disNum, disUnit, actionStr, roadName, summaryStr, eta,
                        progress, curSpeed, limitedSpeed, cameraType, cameraDist, cameraSpeed,
                        endPoiName, totalLightNum, remainLightNum, curRoadName, carDirection
                );
            }
            if (clusterActiveWindow != null && clusterFloatingView != null) {
                clusterActiveWindow.updateNaviInfo(
                        icon, disNum, disUnit, actionStr, roadName, summaryStr, eta,
                        progress, curSpeed, limitedSpeed, cameraType, cameraDist, cameraSpeed,
                        endPoiName, totalLightNum, remainLightNum, curRoadName, carDirection
                );
            }
            remeasureWindow();
        }
    }

    /**
     * 更新出口信息（常规模式专用）
     * @param exitName     出口名称，如 "出口37"，null 或空表示无出口信息
     * @param exitDirection 出口方向，如 "平山 塘厦"
     */
    public void updateExitInfo(String exitName, String exitDirection) {
        cachedExitName = exitName != null ? exitName.trim() : "";
        cachedExitDirection = exitDirection != null ? exitDirection.trim() : "";
        if (!isShowing || currentMode != MODE_NAVI) return;
        if (activeWindow != null && floatingView != null) {
            activeWindow.updateExitInfo(exitName, exitDirection);
        }
        if (clusterActiveWindow != null && clusterFloatingView != null) {
            clusterActiveWindow.updateExitInfo(exitName, exitDirection);
        }
    }

    public void updateSapaInfo(String sapaName, String sapaDist, String nextSapaName, String nextSapaDist) {
        cachedSapaName = sapaName != null ? sapaName.trim() : "";
        cachedSapaDist = sapaDist != null ? sapaDist.trim() : "";
        cachedNextSapaName = nextSapaName != null ? nextSapaName.trim() : "";
        cachedNextSapaDist = nextSapaDist != null ? nextSapaDist.trim() : "";
        if (!isShowing || currentMode != MODE_NAVI) return;
        if (activeWindow != null && floatingView != null) {
            activeWindow.updateSapaInfo(cachedSapaName, cachedSapaDist, cachedNextSapaName, cachedNextSapaDist);
        }
        if (clusterActiveWindow != null && clusterFloatingView != null) {
            clusterActiveWindow.updateSapaInfo(cachedSapaName, cachedSapaDist, cachedNextSapaName, cachedNextSapaDist);
        }
    }

    // ======================== 红绿灯更新 ========================

    public void updateTrafficLight(int status, int dir, int countdown) {
        hasCachedData = true;
        cachedLightStatus = status;
        cachedLightDir = dir;
        cachedLightCountdown = countdown;
        if (!isShowing || currentMode != MODE_NAVI) return;

        if (activeWindow != null && floatingView != null) {
            activeWindow.updateTrafficLight(status, dir, countdown);
        }
        if (clusterActiveWindow != null && clusterFloatingView != null) {
            clusterActiveWindow.updateTrafficLight(status, dir, countdown);
        }
        remeasureWindow();

        handler.removeCallbacks(trafficLightTimeoutRunnable);
        handler.postDelayed(trafficLightTimeoutRunnable, LIGHT_HIDE_TIMEOUT_MS);
    }

    public void hideTrafficLight() {
        if (!isShowing) return;
        handler.removeCallbacks(trafficLightTimeoutRunnable);
        hideTrafficLightCapsule();
    }

    private void hideTrafficLightCapsule() {
        if (activeWindow != null && floatingView != null) {
            activeWindow.updateTrafficLight(0, 0, 0);
        }
        if (clusterActiveWindow != null && clusterFloatingView != null) {
            clusterActiveWindow.updateTrafficLight(0, 0, 0);
        }
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
        // 物理缩放后 naturalWidth/Height 已是最终尺寸
        int viewWidth = naturalWidth;
        int viewHeight = naturalHeight;
        
        // 边界校正，确保位置在屏幕范围内
        int correctedX = Math.max(0, Math.min(layoutParams.x, screenWidth - viewWidth));
        int correctedY = Math.max(0, Math.min(layoutParams.y, screenHeight - viewHeight));
        
        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit()
                .putInt("window_pos_x", correctedX)
                .putInt("window_pos_y", correctedY)
                .apply();
    }

    // ======================== 副屏投屏支持 (Cluster Mirror) ========================

    private Display findClusterDisplay() {
        DisplayManager manager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        if (manager == null) {
            return null;
        }
        if (clusterDisplayId >= 0) {
            Display[] displays = manager.getDisplays();
            for (Display display : displays) {
                if (display != null && display.getDisplayId() == clusterDisplayId) {
                    return display;
                }
            }
            Log.w(TAG, "Preferred cluster display missing: " + clusterDisplayId);
            return null;
        }
        // 自动选择首个非主屏
        Display[] presentationDisplays = manager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        for (Display display : presentationDisplays) {
            if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
                return display;
            }
        }
        Display[] displays = manager.getDisplays();
        for (Display display : displays) {
            if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
                return display;
            }
        }
        return null;
    }

    private void ensureClusterMirror() {
        if (!isClusterMirrorEnabled) {
            dismissClusterMirror();
            return;
        }

        Display display = findClusterDisplay();
        if (display == null) {
            dismissClusterMirror();
            Log.w(TAG, "Cluster mirror enabled but no secondary display found.");
            return;
        }

        if (clusterFloatingView == null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    clusterContext = context.createDisplayContext(display);
                } else {
                    clusterContext = context;
                }
            } catch (Throwable t) {
                Log.e(TAG, "createDisplayContext failed", t);
                clusterContext = context;
            }

            clusterWindowManager = (WindowManager) clusterContext.getSystemService(Context.WINDOW_SERVICE);
            if (clusterWindowManager == null) {
                Log.e(TAG, "Cluster WindowManager is null");
                return;
            }

            int layoutRes;
            if (currentMode == MODE_NAVI) {
                if (styleMode == 2) layoutRes = R.layout.layout_floating_navi_full;
                else if (styleMode == 1) layoutRes = R.layout.layout_floating_navi_minimal;
                else layoutRes = R.layout.layout_floating_navi_normal;
            } else {
                layoutRes = styleMode == 1
                        ? R.layout.layout_floating_cruise_minimal
                        : R.layout.layout_floating_cruise_normal;
            }

            View inflated = LayoutInflater.from(clusterContext).inflate(layoutRes, null);
            clusterFloatingView = inflated;

            if (currentMode == MODE_NAVI && styleMode >= 1) {
                FrameLayout frameLayout = new FrameLayout(clusterContext);
                frameLayout.setClipChildren(false);
                frameLayout.setClipToPadding(false);
                frameLayout.addView(inflated, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                clusterFloatingView = frameLayout;
                clusterScaleTarget = inflated;
            } else {
                clusterScaleTarget = null;
            }

            float scale = getScale();
            if (scale != 1.0f) {
                physicalScaleContent(inflated);
            }

            clusterActiveWindow = FloatingWindowFactory.createWindow(currentMode, styleMode, clusterContext, inflated);

            restoreCachedDataForCluster();
            measureNaturalSizeForCluster(clusterFloatingView);

            int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

            clusterLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    -3);
            clusterLayoutParams.gravity = Gravity.TOP | Gravity.START;

            int screenWidth = clusterContext.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = clusterContext.getResources().getDisplayMetrics().heightPixels;

            int viewWidth = clusterNaturalWidth;
            int viewHeight = clusterNaturalHeight;

            if (clusterSavedPosX >= 0 && clusterSavedPosY >= 0) {
                clusterLayoutParams.x = Math.max(0, Math.min(clusterSavedPosX, screenWidth - Math.max(viewWidth, 1)));
                clusterLayoutParams.y = Math.max(0, Math.min(clusterSavedPosY, screenHeight - Math.max(viewHeight, 1)));
            } else {
                if (viewWidth > 0) {
                    clusterLayoutParams.x = (screenWidth - viewWidth) / 2;
                } else {
                    clusterLayoutParams.x = 0;
                }
                clusterLayoutParams.y = dpToPx(80);
            }

            applyScaleForCluster();
            setupTouchListenerForCluster();
            applyThemeColorForCluster();

            try {
                clusterWindowManager.addView(clusterFloatingView, clusterLayoutParams);
            } catch (Exception e) {
                Log.e(TAG, "Failed to add cluster floating view", e);
            }
        }

        updateFloatingWindowVisibility();
    }

    private void dismissClusterMirror() {
        if (clusterFloatingView != null && clusterWindowManager != null) {
            try {
                if (clusterFloatingView.isAttachedToWindow() || clusterFloatingView.getParent() != null) {
                    clusterWindowManager.removeView(clusterFloatingView);
                }
            } catch (Exception ignored) {}
            if (clusterActiveWindow != null) {
                clusterActiveWindow.onDestroy();
                clusterActiveWindow = null;
            }
            clusterFloatingView = null;
            clusterContext = null;
            updateFloatingWindowVisibility();
        }
    }

    public void updateClusterFloatingWindowVisibility() {
        if (clusterFloatingView == null) return;

        SharedPreferences sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        boolean hideOnForeground = sp.getBoolean("hide_on_amap_foreground", false);
        boolean shouldHide = hideOnForeground && isAmapForeground;

        if (shouldHide || !isShowing) {
            clusterFloatingView.setVisibility(View.GONE);
        } else {
            if (currentMode == MODE_NAVI || (isCruiseEnabled() && hasActiveData)) {
                clusterFloatingView.setVisibility(View.VISIBLE);
            } else {
                clusterFloatingView.setVisibility(View.GONE);
            }
        }
    }

    private void applyScaleForCluster() {
        if (clusterFloatingView == null) return;
        disableClipOnParents(clusterFloatingView);
    }

    private void measureNaturalSizeForCluster(View view) {
        if (view == null) return;
        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        clusterNaturalWidth = view.getMeasuredWidth();
        clusterNaturalHeight = view.getMeasuredHeight();
    }

    private void setupTouchListenerForCluster() {
        if (clusterFloatingView == null) return;
        clusterFloatingView.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                clusterIsDragging = false;
                clusterInitialTouchX = motionEvent.getRawX();
                clusterInitialTouchY = motionEvent.getRawY();
                clusterInitialWindowX = clusterLayoutParams.x;
                clusterInitialWindowY = clusterLayoutParams.y;
                return true;
            }
            if (action != MotionEvent.ACTION_UP) {
                if (action == MotionEvent.ACTION_MOVE) {
                    float dx = motionEvent.getRawX() - clusterInitialTouchX;
                    float dy = motionEvent.getRawY() - clusterInitialTouchY;
                    if (!clusterIsDragging && (Math.abs(dx) > 10 || Math.abs(dy) > 10)) {
                        clusterIsDragging = true;
                    }
                    if (clusterIsDragging && !isPositionLocked) {
                        clusterLayoutParams.x = clusterInitialWindowX + (int) dx;
                        clusterLayoutParams.y = clusterInitialWindowY + (int) dy;
                        try {
                            clusterWindowManager.updateViewLayout(clusterFloatingView, clusterLayoutParams);
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                }
                if (action != MotionEvent.ACTION_CANCEL) return false;
            }

            if (clusterIsDragging && !isPositionLocked) {
                saveClusterWindowPosition();
            }
            return true;
        });
    }

    private void saveClusterWindowPosition() {
        if (clusterLayoutParams == null || clusterNaturalWidth <= 0 || clusterNaturalHeight <= 0 || clusterContext == null) return;

        int screenWidth = clusterContext.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = clusterContext.getResources().getDisplayMetrics().heightPixels;
        int viewWidth = clusterNaturalWidth;
        int viewHeight = clusterNaturalHeight;

        int correctedX = Math.max(0, Math.min(clusterLayoutParams.x, screenWidth - viewWidth));
        int correctedY = Math.max(0, Math.min(clusterLayoutParams.y, screenHeight - viewHeight));

        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit()
                .putInt("cluster_window_pos_x", correctedX)
                .putInt("cluster_window_pos_y", correctedY)
                .apply();

        clusterSavedPosX = correctedX;
        clusterSavedPosY = correctedY;
    }

    private void applyThemeColorForCluster() {
        if (clusterActiveWindow != null) {
            clusterActiveWindow.applyThemeColor(themeColor);
        }
        if (clusterFloatingView != null) {
            applyThemeColorToView(clusterFloatingView, clusterScaleTarget);
        }
        if (backgroundMode == 2) {
            if (clusterActiveWindow != null) {
                clusterActiveWindow.applyDayNightTextColors(isNightMode);
            }
        } else {
            if (clusterActiveWindow != null) {
                clusterActiveWindow.resetToDefaultTextColors();
            }
        }
    }

    public void onClusterMirrorConfigChanged() {
        dismissClusterMirror();
        loadPreferences();
        if (isClusterMirrorEnabled) {
            ensureClusterMirror();
        }
    }

    private void restoreCachedDataForCluster() {
        if (!hasCachedData || clusterActiveWindow == null) return;
        if (currentMode == MODE_CRUISE) {
            if (clusterActiveWindow != null) {
                clusterActiveWindow.updateCruiseInfo(cachedSpeed, cachedRoadName, cachedCameraType, cachedCameraSpeed, cachedCameraDist, cachedCarDirection);
            }
            if (cachedDriveWayJson != null) {
                clusterActiveWindow.updateLaneLines(cachedDriveWayJson);
            }
        } else if (currentMode == MODE_NAVI) {
            clusterActiveWindow.updateNaviInfo(
                    cachedIcon,
                    cachedDisNum,
                    cachedDisUnit,
                    cachedActionStr,
                    cachedRoadName,
                    cachedSummaryStr,
                    cachedEta,
                    cachedProgress,
                    cachedSpeed,
                    cachedLimitedSpeed,
                    cachedCameraType,
                    cachedCameraDist,
                    cachedCameraSpeed,
                    cachedEndPoiName,
                    cachedTotalLightNum,
                    cachedRemainLightNum,
                    cachedCurRoadName,
                    cachedCarDirection
            );
            if (cachedTmcJson != null) {
                clusterActiveWindow.updateTmcData(cachedTmcJson);
            }
            if (cachedDriveWayJson != null) {
                clusterActiveWindow.updateLaneLines(cachedDriveWayJson);
            }
            if (cachedExitName != null && !cachedExitName.isEmpty()) {
                clusterActiveWindow.updateExitInfo(cachedExitName, cachedExitDirection);
            }
            if (cachedSapaName != null && !cachedSapaName.isEmpty()) {
                clusterActiveWindow.updateSapaInfo(cachedSapaName, cachedSapaDist, cachedNextSapaName, cachedNextSapaDist);
            }
            if (cachedLightStatus != -1) {
                clusterActiveWindow.updateTrafficLight(cachedLightStatus, cachedLightDir, cachedLightCountdown);
            }
        }
        clusterActiveWindow.applyThemeColor(themeColor);
        clusterActiveWindow.applyDayNightTextColors(isNightMode);
    }

    public int getClusterNaturalWidth() {
        return clusterNaturalWidth;
    }

    public int getClusterNaturalHeight() {
        return clusterNaturalHeight;
    }

    public int getClusterSavedPosX() {
        if (clusterLayoutParams != null) {
            return clusterLayoutParams.x;
        }
        return clusterSavedPosX >= 0 ? clusterSavedPosX : 0;
    }

    public int getClusterSavedPosY() {
        if (clusterLayoutParams != null) {
            return clusterLayoutParams.y;
        }
        return clusterSavedPosY >= 0 ? clusterSavedPosY : 0;
    }

    public int getClusterScreenWidth() {
        if (clusterContext != null) {
            return clusterContext.getResources().getDisplayMetrics().widthPixels;
        }
        return 0;
    }

    public int getClusterScreenHeight() {
        if (clusterContext != null) {
            return clusterContext.getResources().getDisplayMetrics().heightPixels;
        }
        return 0;
    }

    public boolean isClusterMirrorActive() {
        return clusterFloatingView != null;
    }

    public void updateClusterPosition(int x, int y) {
        if (clusterContext == null) return;

        int screenWidth = getClusterScreenWidth();
        int screenHeight = getClusterScreenHeight();
        int viewWidth = clusterNaturalWidth;
        int viewHeight = clusterNaturalHeight;

        if (viewWidth <= 0) viewWidth = dpToPx(160);
        if (viewHeight <= 0) viewHeight = dpToPx(120);

        int correctedX = Math.max(0, Math.min(x, screenWidth - viewWidth));
        int correctedY = Math.max(0, Math.min(y, screenHeight - viewHeight));

        clusterSavedPosX = correctedX;
        clusterSavedPosY = correctedY;

        context.getSharedPreferences("floating_config", Context.MODE_PRIVATE)
                .edit()
                .putInt("cluster_window_pos_x", correctedX)
                .putInt("cluster_window_pos_y", correctedY)
                .apply();

        if (clusterLayoutParams != null && clusterFloatingView != null && clusterWindowManager != null) {
            clusterLayoutParams.x = correctedX;
            clusterLayoutParams.y = correctedY;
            try {
                clusterWindowManager.updateViewLayout(clusterFloatingView, clusterLayoutParams);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update cluster position layout", e);
            }
        }
    }
}
