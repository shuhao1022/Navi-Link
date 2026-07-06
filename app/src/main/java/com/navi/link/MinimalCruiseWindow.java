package com.navi.link;

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
                // 全透明 + 黑色主题：速度颜色跟随昼夜
                if (themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                    tvCruiseSpeed.setTextColor(isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
                } else {
                    // 恢复正常主题色
                    int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
                    tvCruiseSpeed.setTextColor(accentColor);
                }
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
            container.removeAllViews();
            float scale = FloatingWindowManager.getInstance().getScale();
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
        int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
        if (tvCruiseSpeed != null && !isOverspeedBlinking) {
            // 全透明 + 黑色主题：速度颜色跟随昼夜
            if (themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                boolean isNight = sp.getBoolean("is_night_mode", true);
                tvCruiseSpeed.setTextColor(isNight ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
            } else {
                tvCruiseSpeed.setTextColor(accentColor);
            }
        }
        if (tvCruiseUnit != null && !isOverspeedBlinking) {
            if (themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                boolean isNight = sp.getBoolean("is_night_mode", true);
                tvCruiseUnit.setTextColor(isNight ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
            } else {
                tvCruiseUnit.setTextColor(accentColor);
            }
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
        this.isNightMode = isNightMode;
        int textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        int textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;
        if (tvCruiseRoadName != null) {
            tvCruiseRoadName.setTextColor(textPrimary);
        }
        if (tvCruiseDirection != null) {
            tvCruiseDirection.setTextColor(textPrimary);
        }
        if (tvCruiseUnit != null) {
            tvCruiseUnit.setTextColor(textPrimary);
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
        if (tvCruiseUnit != null) {
            tvCruiseUnit.setTextColor(TEXT_PRIMARY_DARK);
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
