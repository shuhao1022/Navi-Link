package com.navi.link;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.widget.ImageView;
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
        if (laneLineViewMin != null) {
            laneLineViewMin.setSimpleMode(true);
        }
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
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
                // 恢复正常主题色
                int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
                tvCruiseSpeed.setTextColor(accentColor);
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
            for (int i = 0; i < childCount; i++) {
                View child = container.getChildAt(i);
                if (child != null) {
                    ObjectAnimator animator = (ObjectAnimator) child.getTag();
                    if (animator != null) {
                        animator.cancel();
                        child.setTag(null);
                    }
                }
            }
            container.removeAllViews();
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(context);
//            int layoutRes = (count >= 3)
//                    ? R.layout.item_cruise_traffic_light_small
//                    : R.layout.item_cruise_traffic_light;
            int layoutRes =  R.layout.item_cruise_traffic_light;
            for (int i = 0; i < count; i++) {
                try {
                    JSONObject lightObj = lightsArray.getJSONObject(i);
                    View lightView = inflater.inflate(layoutRes, container, false);
                    float scale = FloatingWindowManager.getInstance().getScale();
                    if (scale != 1.0f) {
                        scaleViewRecursive(lightView, scale);
                    }
                    updateSingleLightView(lightView, lightObj);
                    container.addView(lightView);
                } catch (Exception ignored) {
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                try {
                    updateSingleLightView(container.getChildAt(i), lightsArray.getJSONObject(i));
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

        if (countdown > 0) {
            view.setVisibility(View.VISIBLE);
            if (countdown <= 5) {
                ObjectAnimator animator = (ObjectAnimator) view.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    view.setTag(newAnimator);
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) view.getTag();
                if (animator != null) {
                    animator.cancel();
                    view.setTag(null);
                }
                view.setAlpha(1f);
            }
        } else {
            view.setVisibility(View.GONE);
            ObjectAnimator animator = (ObjectAnimator) view.getTag();
            if (animator != null) {
                animator.cancel();
                view.setTag(null);
            }
            view.setAlpha(1f);
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
        int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
        if (tvCruiseSpeed != null && !isOverspeedBlinking) {
            tvCruiseSpeed.setTextColor(accentColor);
        }

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (accentNaviInfo) {
            if (tvCruiseRoadName != null) tvCruiseRoadName.setTextColor(accentColor);
            if (tvCruiseUnit != null) tvCruiseUnit.setTextColor(accentColor);
            if (tvCruiseDirection != null) tvCruiseDirection.setTextColor(accentColor);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        int textSecondary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        if (tvCruiseRoadName != null) {
            tvCruiseRoadName.setTextColor(textSecondary);
        }
        if (tvCruiseDirection != null) {
            tvCruiseDirection.setTextColor(isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
        }

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (accentNaviInfo) {
            int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
            if (tvCruiseRoadName != null) tvCruiseRoadName.setTextColor(accentColor);
            if (tvCruiseUnit != null) tvCruiseUnit.setTextColor(accentColor);
            if (tvCruiseDirection != null) tvCruiseDirection.setTextColor(accentColor);
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

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (accentNaviInfo) {
            int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
            if (tvCruiseRoadName != null) tvCruiseRoadName.setTextColor(accentColor);
            if (tvCruiseUnit != null) tvCruiseUnit.setTextColor(accentColor);
            if (tvCruiseDirection != null) tvCruiseDirection.setTextColor(accentColor);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llTrafficLightsContainer != null) {
            for (int i = 0; i < llTrafficLightsContainer.getChildCount(); i++) {
                View child = llTrafficLightsContainer.getChildAt(i);
                if (child != null) {
                    ObjectAnimator animator = (ObjectAnimator) child.getTag();
                    if (animator != null) {
                        animator.cancel();
                        child.setTag(null);
                    }
                }
            }
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
