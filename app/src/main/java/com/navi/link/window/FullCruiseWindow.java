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
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

public class FullCruiseWindow extends BaseFloatingWindow {

    private TextView tvFullCruiseLabel;
    private TextView tvFullCruiseSpeed;
    private TextView tvFullCruiseUnit;
    private TextView tvFullCruiseRoadName;
    private LinearLayout llFullCruiseTrafficLights;
    private LaneLineView laneLineViewFullCruise;
    private CameraWarningView llFullCruiseCamera;

    private boolean isOverspeedBlinking = false;
    private int themeColor = Color.BLACK;

    public FullCruiseWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        tvFullCruiseLabel = floatingView.findViewById(R.id.tv_full_cruise_label);
        tvFullCruiseSpeed = floatingView.findViewById(R.id.tv_full_cruise_speed);
        tvFullCruiseUnit = floatingView.findViewById(R.id.tv_full_cruise_unit);
        tvFullCruiseRoadName = floatingView.findViewById(R.id.tv_full_cruise_road_name);
        llFullCruiseTrafficLights = floatingView.findViewById(R.id.ll_full_cruise_traffic_lights);
        laneLineViewFullCruise = floatingView.findViewById(R.id.lane_line_view_full_cruise);
        llFullCruiseCamera = floatingView.findViewById(R.id.ll_full_cruise_camera);
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
        updateCameraCapsuleBackground(llFullCruiseCamera);
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
        // 巡航不处理导航数据
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        if (tvFullCruiseSpeed != null) {
            tvFullCruiseSpeed.setText(String.valueOf(speed));
            // 超速警告
            int threshold = sp.getInt("overspeed_threshold", 0);
            double factor = 1.0 + threshold / 100.0;
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && cameraSpeed > 0 && speed > Math.round(cameraSpeed * factor);
            if (overspeed) {
                tvFullCruiseSpeed.setTextColor(Color.RED);
                ObjectAnimator animator = (ObjectAnimator) tvFullCruiseSpeed.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(tvFullCruiseSpeed, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    tvFullCruiseSpeed.setTag(newAnimator);
                    isOverspeedBlinking = true;
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) tvFullCruiseSpeed.getTag();
                if (animator != null) {
                    animator.cancel();
                    tvFullCruiseSpeed.setTag(null);
                }
                tvFullCruiseSpeed.setAlpha(1f);
                isOverspeedBlinking = false;
                // 恢复正常车速颜色（跟随主文字自定义颜色）
                tvFullCruiseSpeed.setTextColor(getPrimaryTextColor(isNightMode));
            }
        }
        if (tvFullCruiseRoadName != null && roadName != null) {
            tvFullCruiseRoadName.setText(roadName);
        }
        updateCameraInfo(cameraType, cameraSpeed, cameraDist);
    }

    private void updateCameraInfo(int cameraType, int cameraSpeed, int cameraDist) {
        if (llFullCruiseCamera != null) {
            llFullCruiseCamera.updateCameraInfo(cameraType, cameraDist, cameraSpeed);
        }
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        // 巡航使用 updateCruiseTrafficLights 处理多灯
    }

    @Override
    public void updateCruiseTrafficLights(JSONArray lightsArray) {
        LinearLayout container = llFullCruiseTrafficLights;
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
        if (laneLineViewFullCruise != null) {
            boolean laneEnabled = sp.getBoolean("normal_navi_lane_enabled", false);
            if (laneEnabled) {
                laneLineViewFullCruise.updateLanes(driveWayJson);
            } else {
                laneLineViewFullCruise.clear();
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
        int customPrimaryColor = getPrimaryTextColor(isNightMode);
        if (tvFullCruiseSpeed != null && !isOverspeedBlinking) {
            tvFullCruiseSpeed.setTextColor(customPrimaryColor);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary = getPrimaryTextColor(isNightMode);
        int textSecondary = getSecondaryTextColor(isNightMode);
        int textHint = getHintTextColor(isNightMode);

        if (tvFullCruiseRoadName != null) tvFullCruiseRoadName.setTextColor(textPrimary);
        if (tvFullCruiseUnit != null) tvFullCruiseUnit.setTextColor(textSecondary);
        if (tvFullCruiseLabel != null) tvFullCruiseLabel.setTextColor(textHint);
        if (llFullCruiseCamera != null) llFullCruiseCamera.setTextColor(textPrimary);
        if (tvFullCruiseSpeed != null && !isOverspeedBlinking) {
            tvFullCruiseSpeed.setTextColor(textPrimary);
        }
        if (laneLineViewFullCruise != null) {
            int laneIconColor = isNightMode ? sp.getInt("lane_icon_color_night", 0xFFFFFFFF) : sp.getInt("lane_icon_color_day", 0xFFFFFFFF);
            laneLineViewFullCruise.setIconColor(laneIconColor);
        }
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvFullCruiseRoadName != null) tvFullCruiseRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullCruiseUnit != null) tvFullCruiseUnit.setTextColor(TEXT_SECONDARY_DARK);
        if (tvFullCruiseLabel != null) tvFullCruiseLabel.setTextColor(TEXT_HINT_DARK);
        if (llFullCruiseCamera != null) llFullCruiseCamera.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullCruiseSpeed != null) tvFullCruiseSpeed.setTextColor(TEXT_PRIMARY_DARK);
        if (laneLineViewFullCruise != null) laneLineViewFullCruise.setIconColor(0xFFFFFFFF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llFullCruiseTrafficLights != null) {
            llFullCruiseTrafficLights.removeAllViews();
        }
        if (tvFullCruiseSpeed != null) {
            ObjectAnimator animator = (ObjectAnimator) tvFullCruiseSpeed.getTag();
            if (animator != null) {
                animator.cancel();
                tvFullCruiseSpeed.setTag(null);
            }
            tvFullCruiseSpeed.setAlpha(1f);
        }
        isOverspeedBlinking = false;
    }
}
