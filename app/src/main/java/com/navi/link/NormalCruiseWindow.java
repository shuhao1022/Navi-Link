package com.navi.link;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

public class NormalCruiseWindow extends BaseFloatingWindow {

    private TextView tvCnSpeed;
    private TextView tvCnRoadName;
    private LinearLayout llCnTrafficLightsContainer;
    private LaneLineView laneLineView;
    private CameraWarningView llCnCameraDist;
    private View llCruiseNormalFirstRow;

    private boolean isOverspeedBlinking = false;
    private int themeColor = Color.BLACK;

    public NormalCruiseWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        tvCnSpeed = floatingView.findViewById(R.id.tv_cn_speed);
        tvCnRoadName = floatingView.findViewById(R.id.tv_cn_road_name);
        llCnTrafficLightsContainer = floatingView.findViewById(R.id.ll_cn_traffic_lights_container);
        laneLineView = floatingView.findViewById(R.id.lane_line_view);
        llCnCameraDist = floatingView.findViewById(R.id.ll_cn_camera_dist);
        llCruiseNormalFirstRow = floatingView.findViewById(R.id.ll_cruise_normal_first_row);
    }

    @Override
    public void updateNaviInfo(
            int icon, String disNum, String disUnit, String actionStr,
            String roadName, String summaryStr, String eta,
            int progress, int curSpeed,
            int limitedSpeed, int cameraType, int cameraDist, int cameraSpeed,
            String endPoiName, int totalLightNum, int remainLightNum,
            String curRoadName, int carDirection
    ) {
        // 常规巡航窗口不处理导航数据
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        // 控制第一排文字信息显示隐藏
        boolean showInfo = sp.getBoolean("normal_cruise_info_enabled", true);
        if (llCruiseNormalFirstRow != null) {
            llCruiseNormalFirstRow.setVisibility(showInfo ? View.VISIBLE : View.GONE);
        }
        if (tvCnSpeed != null) {
            tvCnSpeed.setText(String.valueOf(speed));
            // 超速警告：限速>0 且 当前速度>限速 → 红色+闪烁 (受 overspeed_warning_enabled 开关控制)
            int threshold = sp.getInt("overspeed_threshold", 0);
            double factor = 1.0 + threshold / 100.0;
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && cameraSpeed > 0 && speed > Math.round(cameraSpeed * factor);
            if (overspeed) {
                tvCnSpeed.setTextColor(Color.RED);
                ObjectAnimator animator = (ObjectAnimator) tvCnSpeed.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(tvCnSpeed, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    tvCnSpeed.setTag(newAnimator);
                    isOverspeedBlinking = true;
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) tvCnSpeed.getTag();
                if (animator != null) {
                    animator.cancel();
                    tvCnSpeed.setTag(null);
                }
                tvCnSpeed.setAlpha(1f);
                isOverspeedBlinking = false;
                // 全透明 + 黑色主题：速度颜色跟随昼夜
                if (themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                    tvCnSpeed.setTextColor(isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
                } else {
                    // 恢复正常主题色
                    int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
                    tvCnSpeed.setTextColor(accentColor);
                }
            }
        }
        if (tvCnRoadName != null && roadName != null) {
            tvCnRoadName.setText(roadName);
        }
        updateCruiseCameraAndLimit(cameraType, cameraSpeed, cameraDist);
    }

    private void updateCruiseCameraAndLimit(int cameraType, int cameraSpeed, int cameraDist) {
        if (llCnCameraDist != null) {
            llCnCameraDist.updateCameraInfo(cameraType, cameraDist, cameraSpeed);
        }
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        // 巡航使用 updateCruiseTrafficLights 处理多灯倒计时
    }

    @Override
    public void updateCruiseTrafficLights(JSONArray lightsArray) {
        LinearLayout container = llCnTrafficLightsContainer;
        if (container == null) return;

        int count = lightsArray != null ? lightsArray.length() : 0;
        int childCount = container.getChildCount();

        if (count == 0) {
            container.setVisibility(View.GONE);
            if (childCount > 0) container.removeAllViews();
            return;
        }

        if (count != childCount) {
            container.removeAllViews();
            float scale = getWindowScale();
            for (int i = 0; i < count; i++) {
                try {
                    JSONObject lightObj = lightsArray.getJSONObject(i);
                    TrafficLightView lightView = new TrafficLightView(context);
                    lightView.setCompact(count >= 3);
                    LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    llLp.setMarginStart(dpToPx(5));
                    lightView.setLayoutParams(llLp);
                    if (scale != 1.0f) {
                        scaleViewRecursive(lightView, scale);
                    }
                    int status = lightObj.getInt("status");
                    int countdown = lightObj.getInt("countdown");
                    int dir = lightObj.getInt("dir");
                    if (countdown > 0) {
                        lightView.setData(status, dir, countdown, false);
                    } else {
                        lightView.setVisibility(View.GONE);
                    }
                    container.addView(lightView);
                } catch (Exception ignored) {
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                try {
                    TrafficLightView lightView = (TrafficLightView) container.getChildAt(i);
                    JSONObject lightObj = lightsArray.getJSONObject(i);
                    int status = lightObj.getInt("status");
                    int countdown = lightObj.getInt("countdown");
                    int dir = lightObj.getInt("dir");
                    if (countdown > 0) {
                        lightView.setVisibility(View.VISIBLE);
                        lightView.setData(status, dir, countdown, false);
                    } else {
                        lightView.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        container.setVisibility(View.VISIBLE);

        // 所有灯都倒计时为0时隐藏容器
        boolean allGone = true;
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i).getVisibility() == View.VISIBLE) {
                allGone = false;
                break;
            }
        }
        if (allGone) {
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateLaneLines(String driveWayJson) {
        if (laneLineView != null) {
            boolean laneEnabled = sp.getBoolean("normal_navi_lane_enabled", false);
            if (laneEnabled) {
                laneLineView.updateLanes(driveWayJson);
            } else {
                laneLineView.clear();
            }
        }
    }

    @Override
    public void updateExitInfo(String exitName, String exitDirection) {
        // 巡航无出口信息
    }

    @Override
    public void applyThemeColor(int themeColor) {
        this.themeColor = themeColor;
        int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
        if (tvCnSpeed != null && !isOverspeedBlinking) {
            // 全透明 + 黑色主题：速度颜色跟随昼夜
            if (themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                boolean isNight = sp.getBoolean("is_night_mode", true);
                tvCnSpeed.setTextColor(isNight ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
            } else {
                tvCnSpeed.setTextColor(accentColor);
            }
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary;
        int textSecondary;

        if (sp.getInt("background_mode", 0) == 2 && themeColor != 0xFF1A1A1A) {
            // 全透明 + 非默认黑色主题：文字颜色跟随主题
            int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
            if (accentColor == Color.WHITE) {
                textPrimary = TEXT_PRIMARY_DARK;
                textSecondary = TEXT_SECONDARY_DARK;
            } else {
                textPrimary = accentColor;
                textSecondary = accentColor;
            }
        } else {
            // 跟随高德昼夜
            textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
            textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;
        }

        if (tvCnRoadName != null) tvCnRoadName.setTextColor(textPrimary);
        if (llCnCameraDist != null) llCnCameraDist.setTextColor(textPrimary);
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvCnRoadName != null) tvCnRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (llCnCameraDist != null) llCnCameraDist.setTextColor(TEXT_PRIMARY_DARK);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llCnTrafficLightsContainer != null) {
            llCnTrafficLightsContainer.removeAllViews();
        }
        if (tvCnSpeed != null) {
            ObjectAnimator animator = (ObjectAnimator) tvCnSpeed.getTag();
            if (animator != null) {
                animator.cancel();
                tvCnSpeed.setTag(null);
            }
            tvCnSpeed.setAlpha(1f);
        }
        isOverspeedBlinking = false;
    }
}
