package com.navi.link.window;
import com.navi.link.R;
import com.navi.link.BuildConfig;
import com.navi.link.activity.*;
import com.navi.link.delegate.*;
import com.navi.link.window.*;
import com.navi.link.view.*;
import com.navi.link.receiver.*;
import com.navi.link.service.*;
import com.navi.link.utils.*;


import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

public class MinimalCruiseWindow extends BaseFloatingWindow {

    private TextView tvCruiseSpeed;
    private TextView tvCruiseUnit;
    private TextView tvCruiseDirection;
    private TextView tvCruiseRoadName;
    private LinearLayout llTrafficLightsContainer;
    private CameraWarningView llMinCruiseCameraGroup;
    private LaneLineView laneLineViewMin;
    private View cruiseDividerMin;
    private TextView tvMinSpeedLimit;

    private int themeColor = 0xFF4FC3F7;
    private boolean isOverspeedBlinking = false;

    public MinimalCruiseWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        tvCruiseSpeed = floatingView.findViewById(R.id.tv_cruise_speed);
        tvCruiseUnit = floatingView.findViewById(R.id.tv_cruise_unit);
        tvCruiseDirection = floatingView.findViewById(R.id.tv_cruise_direction);
        tvCruiseRoadName = floatingView.findViewById(R.id.tv_cruise_road_name);
        llTrafficLightsContainer = floatingView.findViewById(R.id.ll_traffic_lights_container);
        llMinCruiseCameraGroup = floatingView.findViewById(R.id.ll_min_cruise_camera_group);
        laneLineViewMin = floatingView.findViewById(R.id.lane_line_view_min);
        cruiseDividerMin = floatingView.findViewById(R.id.cruise_divider);
        tvMinSpeedLimit = floatingView.findViewById(R.id.tv_min_speed_limit);
        if (laneLineViewMin != null) {
            laneLineViewMin.setSimpleMode(true);
        }
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
        updateCameraCapsuleBackground(llMinCruiseCameraGroup);
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
        // 极简巡航不处理导航数据
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        boolean speedEnabled = sp.getBoolean("minimal_speed_enabled", true);
        if (tvCruiseSpeed != null) {
            tvCruiseSpeed.setText(String.valueOf(speed));
            // 超速警告：限速优先用cameraSpeed
            int limit = cameraSpeed > 0 ? cameraSpeed : 0;
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && limit > 0 && speed > limit;
            if (overspeed) {
                tvCruiseSpeed.setTextColor(Color.RED);
                ObjectAnimator animator = (ObjectAnimator) tvCruiseSpeed.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(tvCruiseSpeed, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    tvCruiseSpeed.setTag(newAnimator);
                    isOverspeedBlinking = true;
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) tvCruiseSpeed.getTag();
                if (animator != null) {
                    animator.cancel();
                    tvCruiseSpeed.setTag(null);
                }
                tvCruiseSpeed.setAlpha(1f);
                isOverspeedBlinking = false;
                // 恢复正常车速颜色（跟随主文字自定义颜色）
                tvCruiseSpeed.setTextColor(getPrimaryTextColor(isNightMode));
            }
            tvCruiseSpeed.setVisibility(speedEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvCruiseUnit != null) {
            tvCruiseUnit.setVisibility(speedEnabled ? View.VISIBLE : View.GONE);
        }
        if (cruiseDividerMin != null) {
            cruiseDividerMin.setVisibility(speedEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvCruiseRoadName != null) {
            boolean roadNameEnabled = sp.getBoolean("minimal_road_name_enabled", true);
            if (roadNameEnabled) {
                tvCruiseRoadName.setText(roadName);
                tvCruiseRoadName.setVisibility(View.VISIBLE);
            } else {
                tvCruiseRoadName.setVisibility(View.GONE);
            }
        }
        if (tvCruiseDirection != null) {
            boolean directionEnabled = sp.getBoolean("minimal_direction_enabled", false);
            if (directionEnabled && carDirection >= 0) {
                tvCruiseDirection.setText(getDirectionText(carDirection));
                tvCruiseDirection.setVisibility(View.VISIBLE);
            } else {
                tvCruiseDirection.setVisibility(View.GONE);
            }
        }
        if (llMinCruiseCameraGroup != null) {
            boolean minimalCameraEnabled = sp.getBoolean("minimal_camera_enabled", false);
            if (minimalCameraEnabled) {
                llMinCruiseCameraGroup.updateCameraInfo(cameraType, cameraDist, cameraSpeed);
            } else {
                llMinCruiseCameraGroup.setVisibility(View.GONE);
            }
        }
        if (tvMinSpeedLimit != null) {
            boolean speedLimitEnabled = sp.getBoolean("minimal_speed_limit_enabled", false);
            if (speedLimitEnabled) {
                if (cameraSpeed > 0) {
                    tvMinSpeedLimit.setText(String.valueOf(cameraSpeed));
                    tvMinSpeedLimit.setVisibility(View.VISIBLE);
                } else {
                    tvMinSpeedLimit.setVisibility(View.GONE);
                }
            } else {
                tvMinSpeedLimit.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        // 巡航使用 updateCruiseTrafficLights
    }

    @Override
    public void updateCruiseTrafficLights(JSONArray lightsArray) {
        LinearLayout container = llTrafficLightsContainer;
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
                    // 极简巡航不使用紧凑模式
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
        if (laneLineViewMin != null) {
            boolean laneEnabled = sp.getBoolean("minimal_navi_lane_enabled", false);
            if (laneEnabled) {
                laneLineViewMin.updateLanes(driveWayJson);
            } else {
                laneLineViewMin.clear();
            }
        }
    }

    @Override
    public void updateExitInfo(String exitName, String exitDirection) {
        // 极简巡航无出口信息
    }

    @Override
    public void applyThemeColor(int themeColor) {
        this.themeColor = themeColor;
        int customPrimaryColor = getPrimaryTextColor(isNightMode);
        if (tvCruiseSpeed != null && !isOverspeedBlinking) {
            tvCruiseSpeed.setTextColor(customPrimaryColor);
        }
        if (tvCruiseUnit != null && !isOverspeedBlinking) {
            tvCruiseUnit.setTextColor(customPrimaryColor);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary = getPrimaryTextColor(isNightMode);

        if (tvCruiseRoadName != null) {
            tvCruiseRoadName.setTextColor(textPrimary);
        }
        if (tvCruiseDirection != null) {
            tvCruiseDirection.setTextColor(textPrimary);
        }
        if (tvCruiseUnit != null) {
            tvCruiseUnit.setTextColor(textPrimary);
        }
        if (tvCruiseSpeed != null && !isOverspeedBlinking) {
            tvCruiseSpeed.setTextColor(textPrimary);
        }
        if (llMinCruiseCameraGroup != null) llMinCruiseCameraGroup.setTextColor(textPrimary);
        if (laneLineViewMin != null) {
            int laneIconColor = isNightMode ? sp.getInt("lane_icon_color_night", 0xFFFFFFFF) : sp.getInt("lane_icon_color_day", 0xFFFFFFFF);
            laneLineViewMin.setIconColor(laneIconColor);
        }
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvCruiseRoadName != null) {
            tvCruiseRoadName.setTextColor(TEXT_PRIMARY_DARK);
        }
        if (tvCruiseDirection != null) {
            tvCruiseDirection.setTextColor(TEXT_PRIMARY_DARK);
        }
        if (tvCruiseUnit != null) {
            tvCruiseUnit.setTextColor(TEXT_PRIMARY_DARK);
        }
        if (laneLineViewMin != null) laneLineViewMin.setIconColor(0xFFFFFFFF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llTrafficLightsContainer != null) {
            llTrafficLightsContainer.removeAllViews();
        }
        if (tvCruiseSpeed != null) {
            ObjectAnimator animator = (ObjectAnimator) tvCruiseSpeed.getTag();
            if (animator != null) {
                animator.cancel();
                tvCruiseSpeed.setTag(null);
            }
            tvCruiseSpeed.setAlpha(1f);
        }
        isOverspeedBlinking = false;
    }
}
