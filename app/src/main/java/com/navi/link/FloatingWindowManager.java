package com.navi.link;

import android.content.Context;
import android.content.Intent;
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
import android.util.TypedValue;
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
import androidx.cardview.widget.CardView;
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
    private View tvCruiseMargin;

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

    // 全数据 UI
    private TextView tvFullSpeed;
    private TextView tvFullSpeedLimit;
    private TextView tvFullCurRoadName;
    private TextView tvDistanceNumFull;
    private TextView tvDistanceUnitFull;
    private TextView tvRoadNameMinFull;
    private TextView tvSummaryFull;
    private TextView tvEtaFull;
    private TextView tvFullEndPoiName;
    private TextView tvFullCameraDist;
    private TextView tvFullLightCount;
    private TextView tvFullLabelCurrent;
    private TextView tvFullLabelEnd;
    private TextView tvFullDirection;
    private View llTrafficLightGroupFull;
    private ImageView ivLightIconFull;
    private ImageView ivLightArrowFull;
    private TextView tvLightTimeFull;
    private ImageView ivActionIconFull;
    private CardView cvFullMiddle;

    // 状态
    private int currentMode = MODE_CRUISE;
    private int styleMode = 0; // 0=normal, 1=minimal, 2=full
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
    private String cachedEndPoiName = "";
    private int cachedTotalLightNum = 0;
    private int cachedRemainLightNum = 0;
    private String cachedCurRoadName = "";
    private int cachedCarDirection = 0;

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
        styleMode = sp.getInt("style_mode", sp.getBoolean("is_minimal_style", false) ? 1 : 0);
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
            if (styleMode == 2) layoutRes = R.layout.layout_floating_navi_full;
            else if (styleMode == 1) layoutRes = R.layout.layout_floating_navi_minimal;
            else layoutRes = R.layout.layout_floating_navi;
        } else {
            layoutRes = R.layout.layout_floating_cruise;
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
        if (scale != 1.0f) {
            physicalScaleContent(inflated);
        }

        bindViews();
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
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
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
        } else if (styleMode == 2) {
            bindFullViews();
        } else if (styleMode == 1) {
            bindMinimalViews();
        } else {
            bindNormalViews();
        }
    }

    private void clearAllRefs() {
        tvCruiseSpeed = null;
        tvCruiseRoadName = null;
        llTrafficLightsContainer = null;
        tvCruiseMargin = null;
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
        tvFullSpeed = null;
        tvFullSpeedLimit = null;
        tvFullCurRoadName = null;
        tvDistanceNumFull = null;
        tvDistanceUnitFull = null;
        tvRoadNameMinFull = null;
        tvSummaryFull = null;
        tvEtaFull = null;
        tvFullEndPoiName = null;
        tvFullCameraDist = null;
        tvFullLightCount = null;
        tvFullLabelCurrent = null;
        tvFullLabelEnd = null;
        tvFullDirection = null;
        llTrafficLightGroupFull = null;
        ivLightIconFull = null;
        ivLightArrowFull = null;
        tvLightTimeFull = null;
        ivActionIconFull = null;
        cvFullMiddle = null;
    }

    private void bindCruiseViews() {
        tvCruiseSpeed = floatingView.findViewById(R.id.tv_cruise_speed);
        tvCruiseRoadName = floatingView.findViewById(R.id.tv_cruise_road_name);
        llTrafficLightsContainer = floatingView.findViewById(R.id.ll_traffic_lights_container);
        tvCruiseMargin = floatingView.findViewById(R.id.tv_margin);
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

    private void bindFullViews() {
        tvFullSpeed = floatingView.findViewById(R.id.tv_full_speed);
        tvFullSpeedLimit = floatingView.findViewById(R.id.tv_full_speed_limit);
        tvFullCurRoadName = floatingView.findViewById(R.id.tv_full_cur_road_name);
        tvDistanceNumFull = floatingView.findViewById(R.id.tv_distance_num_full);
        tvDistanceUnitFull = floatingView.findViewById(R.id.tv_distance_unit_full);
        tvRoadNameMinFull = floatingView.findViewById(R.id.tv_road_name_min);
        tvSummaryFull = floatingView.findViewById(R.id.tv_summary_full);
        tvEtaFull = floatingView.findViewById(R.id.tv_eta_full);
        tvFullEndPoiName = floatingView.findViewById(R.id.tv_full_end_poi_name);
        tvFullCameraDist = floatingView.findViewById(R.id.tv_full_camera_dist);
        tvFullLightCount = floatingView.findViewById(R.id.tv_full_light_count);
        cvFullMiddle = floatingView.findViewById(R.id.cv_full_middle);

        View lightGroup = floatingView.findViewById(R.id.ll_traffic_light_group);
        if (lightGroup != null) {
            llTrafficLightGroupFull = lightGroup;
            ivLightIconFull = lightGroup.findViewById(R.id.iv_light_icon);
            ivLightArrowFull = lightGroup.findViewById(R.id.iv_light_arrow);
            tvLightTimeFull = lightGroup.findViewById(R.id.tv_light_time);
        }

        ivActionIconFull = floatingView.findViewById(R.id.iv_action_icon_full);
        tvFullLabelCurrent = floatingView.findViewById(R.id.tv_full_label_current);
        tvFullLabelEnd = floatingView.findViewById(R.id.tv_full_label_end);
        tvFullDirection = floatingView.findViewById(R.id.tv_full_direction);
    }

    /**
     * recreateWindow 后立即恢复缓存数据，避免布局短暂显示默认值闪烁
     */
    private void restoreCachedData() {
        if (!hasCachedData) return;
        if (currentMode == MODE_CRUISE) {
            if (tvCruiseSpeed != null) tvCruiseSpeed.setText(String.valueOf(cachedSpeed));
            if (tvCruiseRoadName != null && !cachedRoadName.isEmpty()) tvCruiseRoadName.setText(cachedRoadName);
        } else if (currentMode == MODE_NAVI) {
            if (styleMode == 2) {
                if (cachedIcon >= 0) {
                    int res = getTurnIconRes(cachedIcon);
                    if (ivActionIconFull != null && res != 0) ivActionIconFull.setImageResource(res);
                }
                if (tvFullSpeed != null) tvFullSpeed.setText(String.valueOf(cachedSpeed));
                if (tvFullSpeedLimit != null && cachedLimitedSpeed > 0)
                    tvFullSpeedLimit.setText("限速 " + cachedLimitedSpeed);
                if (tvFullCurRoadName != null && !cachedCurRoadName.isEmpty())
                    tvFullCurRoadName.setText(cachedCurRoadName);
                if (tvDistanceNumFull != null) tvDistanceNumFull.setText(cachedDisNum);
                if (tvDistanceUnitFull != null)
                    tvDistanceUnitFull.setText(disNumIsNow(cachedDisNum) ? "进入" : cachedDisUnit);
                if (tvRoadNameMinFull != null && !cachedRoadName.isEmpty())
                    tvRoadNameMinFull.setText(cachedRoadName);
                if (tvSummaryFull != null) tvSummaryFull.setText(cachedSummaryStr);
                if (tvEtaFull != null) tvEtaFull.setText(cachedEta);
                if (tvFullEndPoiName != null && !cachedEndPoiName.isEmpty())
                    tvFullEndPoiName.setText(cachedEndPoiName);
                if (tvFullCameraDist != null && cachedCameraDist > 0)
                    tvFullCameraDist.setText(cachedCameraDist + "米");
                if (tvFullLightCount != null && cachedTotalLightNum > 0)
                    tvFullLightCount.setText(cachedTotalLightNum + "个");
                if (tvFullDirection != null && cachedCarDirection > 0)
                    tvFullDirection.setText("朝向 " + getDirectionText(cachedCarDirection));
            } else if (styleMode == 1) {
                if (cachedIcon >= 0) {
                    int res = getTurnIconRes(cachedIcon);
                    if (ivActionIconMin != null && res != 0) ivActionIconMin.setImageResource(res);
                }
                if (tvMinSpeed != null) tvMinSpeed.setText(String.valueOf(cachedSpeed));
                if (tvDistanceNumMin != null) tvDistanceNumMin.setText(cachedDisNum);
                if (tvDistanceUnitMin != null)
                    tvDistanceUnitMin.setText(disNumIsNow(cachedDisNum) ? "进入" : cachedDisUnit);
                if (tvRoadNameMin != null && !cachedRoadName.isEmpty()) tvRoadNameMin.setText(cachedRoadName);
            } else {
                if (cachedIcon >= 0) {
                    int res = getTurnIconRes(cachedIcon);
                    if (ivTurnIcon != null && res != 0) ivTurnIcon.setImageResource(res);
                }
                if (tvDistanceNum != null) tvDistanceNum.setText(cachedDisNum);
                if (tvDistanceUnit != null)
                    tvDistanceUnit.setText(disNumIsNow(cachedDisNum) ? "" : cachedDisUnit);
                if (tvAction != null) tvAction.setText(cachedActionStr);
                if (tvRoadName != null && !cachedRoadName.isEmpty()) tvRoadName.setText(cachedRoadName);
                if (pbRoute != null) pbRoute.setProgress(cachedProgress);
                if (tvSummary != null) tvSummary.setText(cachedSummaryStr);
                if (tvEta != null) tvEta.setText(cachedEta);
            }
            // 恢复红绿灯胶囊
            if (cachedLightCountdown > 0) {
                View lightGroup;
                ImageView lightIcon;
                ImageView lightArrow;
                TextView lightTime;
                if (styleMode == 1) {
                    lightGroup = llTrafficLightGroupMin;
                    lightIcon = ivLightIconMin;
                    lightArrow = ivLightArrowMin;
                    lightTime = tvLightTimeMin;
                } else if (styleMode == 2) {
                    lightGroup = llTrafficLightGroupFull;
                    lightIcon = ivLightIconFull;
                    lightArrow = ivLightArrowFull;
                    lightTime = tvLightTimeFull;
                } else {
                    lightGroup = llTrafficLightGroup;
                    lightIcon = ivLightIcon;
                    lightArrow = ivLightArrow;
                    lightTime = tvLightTime;
                }
                if (lightGroup != null) lightGroup.setVisibility(View.VISIBLE);
                if (lightIcon != null) lightIcon.setImageResource(getNaviLightIconRes(cachedLightStatus));
                if (lightArrow != null) lightArrow.setImageResource(getNaviLightDirRes(cachedLightDir));
                if (lightTime != null) lightTime.setText(String.valueOf(cachedLightCountdown));
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
        scaleViewRecursive(root, scale);
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

        // 缩放背景 drawable 圆角
        Drawable bg = view.getBackground();
        if (bg instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) bg.mutate();
            float r = gd.getCornerRadius();
            if (r > 0) {
                gd.setCornerRadius(r * factor);
            } else {
                float[] radii = gd.getCornerRadii();
                if (radii != null) {
                    for (int i = 0; i < radii.length; i++) radii[i] *= factor;
                    gd.setCornerRadii(radii);
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
        if (Math.abs(this.scale - newScale) < 0.01f) return;
        this.scale = newScale;
        if (isShowing) {
            refreshWindow();
        }
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
        if (tvFullSpeed != null) tvFullSpeed.setTextColor(accentColor);
        if (tvFullLabelCurrent != null) tvFullLabelCurrent.setTextColor(accentColor);
        if (tvFullLabelEnd != null) tvFullLabelEnd.setTextColor(accentColor);
        if (tvFullDirection != null) tvFullDirection.setTextColor(accentColor);
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
            int cornerPx = Math.round(dpToPx(12) * scale);
            bgDrawable.setCornerRadii(new float[]{0, 0, 0, 0, cornerPx, cornerPx, cornerPx, cornerPx});
            layoutInfoBar.setBackground(bgDrawable);
        }

        View target = floatingView.findViewById(R.id.root_layout);
        if (target == null) target = scaleTarget;
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

            // 单击（非拖拽、非长按）→ 打开设置页
            if (!isDragging && !hasLongPressed && action == MotionEvent.ACTION_UP) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            // 拖拽结束后保存位置
            if (isDragging && !isPositionLocked) {
                saveWindowPosition();
            }

            return true;
        });
    }

    // ======================== 巡航数据更新 ========================

    public void updateCruiseInfo(int speed, String roadName) {
        hasCachedData = true;
        cachedSpeed = speed;
        cachedRoadName = roadName != null ? roadName : "";
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
            if (tvCruiseMargin != null) tvCruiseMargin.setVisibility(View.GONE);
            if (childCount > 0) llTrafficLightsContainer.removeAllViews();
            remeasureWindow();
            return;
        }

        if (count != childCount) {
            llTrafficLightsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < count; i++) {
                try {
                    JSONObject lightObj = lightsArray.getJSONObject(i);
                    View lightView = inflater.inflate(R.layout.item_cruise_traffic_light, llTrafficLightsContainer, false);
                    if (scale != 1.0f) scaleViewRecursive(lightView, scale);
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
        if (tvCruiseMargin != null) tvCruiseMargin.setVisibility(View.VISIBLE);
        // 所有灯都倒计时为0时隐藏容器
        boolean allGone = true;
        for (int i = 0; i < llTrafficLightsContainer.getChildCount(); i++) {
            if (llTrafficLightsContainer.getChildAt(i).getVisibility() == View.VISIBLE) {
                allGone = false;
                break;
            }
        }
        if (allGone) {
            llTrafficLightsContainer.setVisibility(View.GONE);
            if (tvCruiseMargin != null) tvCruiseMargin.setVisibility(View.GONE);
        }
        remeasureWindow();
    }

    /**
     * 重新测量并更新窗口尺寸，用于红绿灯动态增减后自适应宽度
     */
    private void remeasureWindow() {
        if (floatingView == null || layoutParams == null) return;
        measureNaturalSize();
        layoutParams.width = naturalWidth;
        layoutParams.height = naturalHeight;
        try {
            windowManager.updateViewLayout(floatingView, layoutParams);
        } catch (Exception ignored) {
        }
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
                               int progress, int curSpeed,
                               int limitedSpeed, int cameraDist, int cameraSpeed,
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
        cachedCameraDist = cameraDist;
        cachedCameraSpeed = cameraSpeed;
        cachedEndPoiName = endPoiName != null ? endPoiName : "";
        cachedTotalLightNum = totalLightNum;
        cachedRemainLightNum = remainLightNum;
        cachedCurRoadName = curRoadName != null ? curRoadName : "";
        cachedCarDirection = carDirection;
        if (isShowing && floatingView != null && currentMode == MODE_NAVI) {
            if (styleMode == 2) {
                updateFullNaviInfo(icon, disNum, disUnit, curSpeed, limitedSpeed, cameraDist,
                        roadName, summaryStr, eta, endPoiName, totalLightNum, curRoadName, carDirection);
            } else if (styleMode == 1) {
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

    private void updateFullNaviInfo(int icon, String disNum, String disUnit, int curSpeed,
                                     int limitedSpeed, int cameraDist, String roadName,
                                     String summaryStr, String eta, String endPoiName,
                                     int totalLightNum, String curRoadName, int carDirection) {
        int turnIconRes = getTurnIconRes(icon);
        if (ivActionIconFull != null && turnIconRes != 0) ivActionIconFull.setImageResource(turnIconRes);
        if (tvFullSpeed != null) tvFullSpeed.setText(String.valueOf(curSpeed));
        if (tvFullSpeedLimit != null) {
            if (limitedSpeed > 0) {
                tvFullSpeedLimit.setText("限速 " + limitedSpeed);
//                tvFullSpeedLimit.setVisibility(View.VISIBLE);
            } else {
                tvFullSpeedLimit.setText("没有限速，可以起飞");
            }
        }
        if (tvFullCurRoadName != null) {
            String name = (curRoadName != null && !curRoadName.isEmpty()) ? curRoadName : roadName;
            tvFullCurRoadName.setText(name != null ? name : "未知道路");
        }
        if (tvDistanceNumFull != null) tvDistanceNumFull.setText(disNum);
        if (tvDistanceUnitFull != null) tvDistanceUnitFull.setText(disNumIsNow(disNum) ? "进入" : disUnit);
        if (tvRoadNameMinFull != null) tvRoadNameMinFull.setText(roadName != null ? roadName : "");
        if (tvSummaryFull != null) tvSummaryFull.setText(summaryStr);
        if (tvEtaFull != null) tvEtaFull.setText(eta);
        if (tvFullEndPoiName != null) tvFullEndPoiName.setText(endPoiName != null ? endPoiName : "");
        if (tvFullCameraDist != null) {
            if (cameraDist > 0) {
                tvFullCameraDist.setText(cameraDist + "米");
            } else {
                tvFullCameraDist.setText("--");
            }
        }
        if (tvFullLightCount != null) {
            if (totalLightNum > 0) {
                tvFullLightCount.setText(totalLightNum + "个");
            } else {
                tvFullLightCount.setText("--");
            }
        }
        if (tvFullDirection != null) {
            if (carDirection >= 0) {
                tvFullDirection.setText("朝向 " + getDirectionText(carDirection));
            } else {
                tvFullDirection.setText("");
            }
        }
    }

    private String getDirectionText(int degrees) {
        String[] dirs = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int index = (int) Math.round(degrees / 45.0) % 8;
        if (index < 0) index += 8;
        return dirs[index];
    }

    // ======================== 红绿灯更新 ========================

    public void updateTrafficLight(int status, int dir, int countdown) {
        hasCachedData = true;
        cachedLightStatus = status;
        cachedLightDir = dir;
        cachedLightCountdown = countdown;
        if (!isShowing || floatingView == null || currentMode != MODE_NAVI) return;

        View lightGroup;
        ImageView lightIcon;
        ImageView lightArrow;
        TextView lightTime;

        if (styleMode == 2) {
            lightGroup = llTrafficLightGroupFull;
            lightIcon = ivLightIconFull;
            lightArrow = ivLightArrowFull;
            lightTime = tvLightTimeFull;
        } else if (styleMode == 1) {
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
        View view;
        if (styleMode == 2) view = llTrafficLightGroupFull;
        else if (styleMode == 1) view = llTrafficLightGroupMin;
        else view = llTrafficLightGroup;
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
}
