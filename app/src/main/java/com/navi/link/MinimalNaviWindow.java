package com.navi.link;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.TextView;

public class MinimalNaviWindow extends BaseFloatingWindow {

    private ImageView ivActionIconMin;
    private TextView tvMinSpeed;
    private TextView tvMinSpeedUnit;
    private TextView tvDistanceNumMin;
    private TextView tvDistanceUnitMin;
    private TextView tvRoadNameMin;
    private TextView tvMinDirection;
    private TextView tvMinLightCount;
    private View minDividerMin;
    private View naviMinDivider;

    private TrafficLightView llTrafficLightGroupMin;

    private CameraWarningView llMinNaviCameraGroup;

    private int mCameraDist = 0;
    private int mCameraSpeed = 0;
    private int mCameraType = 0;
    private int mTrafficLightCountdown = 0;

    private int themeColor = 0xFF4FC3F7;
    private boolean isOverspeedBlinking = false;
    private LaneLineView laneLineViewMin;

    public MinimalNaviWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        ivActionIconMin = floatingView.findViewById(R.id.iv_action_icon_min);
        tvMinSpeed = floatingView.findViewById(R.id.tv_min_speed);
        tvMinSpeedUnit = floatingView.findViewById(R.id.tv_min_speed_unit);
        tvDistanceNumMin = floatingView.findViewById(R.id.tv_distance_num_min);
        tvDistanceUnitMin = floatingView.findViewById(R.id.tv_distance_unit_min);
        tvRoadNameMin = floatingView.findViewById(R.id.tv_road_name_min);
        tvMinDirection = floatingView.findViewById(R.id.tv_min_direction);
        tvMinLightCount = floatingView.findViewById(R.id.tv_min_light_count);
        minDividerMin = floatingView.findViewById(R.id.min_divider);
        naviMinDivider = floatingView.findViewById(R.id.navi_min_divider);

        llTrafficLightGroupMin = floatingView.findViewById(R.id.ll_traffic_light_group);

        llMinNaviCameraGroup = floatingView.findViewById(R.id.ll_min_navi_camera_group);

        laneLineViewMin = floatingView.findViewById(R.id.lane_line_view_min);
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
        if (ivActionIconMin != null) {
            int turnIconRes = getTurnIconRes(icon);
            if (turnIconRes != 0) {
                ivActionIconMin.setImageResource(turnIconRes);
            }
            boolean shouldBlink = shouldBlinkTurnIcon(disNum, disUnit);
            if (shouldBlink) {
                ObjectAnimator animator = (ObjectAnimator) ivActionIconMin.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(ivActionIconMin, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    ivActionIconMin.setTag(newAnimator);
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) ivActionIconMin.getTag();
                if (animator != null) {
                    animator.cancel();
                    ivActionIconMin.setTag(null);
                }
                ivActionIconMin.setAlpha(1f);
            }
        }
        boolean speedEnabled = sp.getBoolean("minimal_speed_enabled", true);
        boolean turnInfoEnabled = sp.getBoolean("minimal_turn_info_enabled", true);
        if (tvMinSpeed != null) {
            tvMinSpeed.setText(String.valueOf(curSpeed));
            // 超速警告：限速优先用cameraSpeed，为0则用limitedSpeed
            int limit = cameraSpeed > 0 ? cameraSpeed : limitedSpeed;
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && limit > 0 && curSpeed > limit;
            if (overspeed) {
                tvMinSpeed.setTextColor(Color.RED);
                ObjectAnimator animator = (ObjectAnimator) tvMinSpeed.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(tvMinSpeed, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    tvMinSpeed.setTag(newAnimator);
                    isOverspeedBlinking = true;
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) tvMinSpeed.getTag();
                if (animator != null) {
                    animator.cancel();
                    tvMinSpeed.setTag(null);
                }
                tvMinSpeed.setAlpha(1f);
                isOverspeedBlinking = false;
                // 全透明 + 黑色主题：速度颜色跟随昼夜（打开主题色调开关时则走主题色）
                boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
                if (!accentNaviInfo && themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                    tvMinSpeed.setTextColor(isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
                } else {
                    // 恢复正常主题色
                    int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
                    tvMinSpeed.setTextColor(accentColor);
                }
            }
            tvMinSpeed.setVisibility(speedEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvMinSpeedUnit != null) {
            tvMinSpeedUnit.setVisibility(speedEnabled ? View.VISIBLE : View.GONE);
        }
        if (minDividerMin != null) {
            minDividerMin.setVisibility(speedEnabled && turnInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (ivActionIconMin != null) {
            ivActionIconMin.setVisibility(turnInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvDistanceNumMin != null) {
            tvDistanceNumMin.setVisibility(turnInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvDistanceUnitMin != null) {
            tvDistanceUnitMin.setVisibility(turnInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (naviMinDivider != null) {
            naviMinDivider.setVisibility(turnInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvDistanceNumMin != null) {
            tvDistanceNumMin.setText(disNum);
        }
        if (tvDistanceUnitMin != null) {
            tvDistanceUnitMin.setText(disNumIsNow(disNum) ? "进入" : disUnit);
        }
        if (tvRoadNameMin != null) {
            boolean roadNameEnabled = sp.getBoolean("minimal_road_name_enabled", true);
            if (roadNameEnabled) {
                tvRoadNameMin.setText(roadName);
                tvRoadNameMin.setVisibility(View.VISIBLE);
            } else {
                tvRoadNameMin.setVisibility(View.GONE);
            }
        }
        if (tvMinDirection != null) {
            boolean directionEnabled = sp.getBoolean("minimal_direction_enabled", false);
            if (directionEnabled && carDirection >= 0) {
                tvMinDirection.setText(getDirectionText(carDirection));
                tvMinDirection.setVisibility(View.VISIBLE);
            } else {
                tvMinDirection.setVisibility(View.GONE);
            }
        }
        if (tvMinLightCount != null) {
            boolean lightCountEnabled = sp.getBoolean("minimal_light_count_enabled", false);
            if (lightCountEnabled && remainLightNum > 0) {
                tvMinLightCount.setText("🚦" + remainLightNum);
                tvMinLightCount.setVisibility(View.VISIBLE);
            } else {
                tvMinLightCount.setVisibility(View.GONE);
            }
        }
        mCameraType = cameraType;
        mCameraSpeed = cameraSpeed;
        mCameraDist = cameraDist;
        updateCameraDistVisibility();
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        // 灵动岛导航不处理巡航
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        mTrafficLightCountdown = countdown;
        if (llTrafficLightGroupMin == null) return;
        if (countdown <= 0) {
            llTrafficLightGroupMin.clear();
            return;
        }
        llTrafficLightGroupMin.setVisibility(View.VISIBLE);
        llTrafficLightGroupMin.setData(status, dir, countdown, true);
    }

    private void updateCameraDistVisibility() {
        if (llMinNaviCameraGroup == null) return;
        boolean minimalCameraEnabled = sp.getBoolean("minimal_camera_enabled", false);
        if (minimalCameraEnabled) {
            llMinNaviCameraGroup.updateCameraInfo(mCameraType, mCameraDist, mCameraSpeed);
        } else {
            llMinNaviCameraGroup.setVisibility(View.GONE);
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
        // 极简导航无出口信息
    }

    @Override
    public void applyThemeColor(int themeColor) {
        this.themeColor = themeColor;
        int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (tvMinSpeed != null && !isOverspeedBlinking) {
            // 全透明 + 黑色主题：速度颜色跟随昼夜（打开主题色调开关时则走主题色）
            if (!accentNaviInfo && themeColor == 0xFF1A1A1A && sp.getInt("background_mode", 0) == 2) {
                boolean isNight = sp.getBoolean("is_night_mode", true);
                tvMinSpeed.setTextColor(isNight ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT);
            } else {
                tvMinSpeed.setTextColor(accentColor);
            }
        }

        if (accentNaviInfo) {
            if (tvDistanceNumMin != null) tvDistanceNumMin.setTextColor(accentColor);
            if (tvDistanceUnitMin != null) tvDistanceUnitMin.setTextColor(accentColor);
            if (tvRoadNameMin != null) tvRoadNameMin.setTextColor(accentColor);
            if (ivActionIconMin != null) ivActionIconMin.setColorFilter(accentColor);
            if (tvMinSpeedUnit != null) tvMinSpeedUnit.setTextColor(accentColor);
            if (tvMinDirection != null) tvMinDirection.setTextColor(accentColor);
            if (tvMinLightCount != null) tvMinLightCount.setTextColor(accentColor);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        int textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;

        if (tvDistanceNumMin != null) tvDistanceNumMin.setTextColor(textPrimary);
        if (tvDistanceUnitMin != null) tvDistanceUnitMin.setTextColor(textSecondary);
        if (tvRoadNameMin != null) tvRoadNameMin.setTextColor(textSecondary);
        if (ivActionIconMin != null) ivActionIconMin.setColorFilter(textPrimary);
        if (tvMinSpeedUnit != null) tvMinSpeedUnit.setTextColor(textPrimary);
        if (tvMinDirection != null) tvMinDirection.setTextColor(textPrimary);
        if (tvMinLightCount != null) tvMinLightCount.setTextColor(textPrimary);

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (accentNaviInfo) {
            int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
            if (tvDistanceNumMin != null) tvDistanceNumMin.setTextColor(accentColor);
            if (tvDistanceUnitMin != null) tvDistanceUnitMin.setTextColor(accentColor);
            if (tvRoadNameMin != null) tvRoadNameMin.setTextColor(accentColor);
            if (ivActionIconMin != null) ivActionIconMin.setColorFilter(accentColor);
            if (tvMinSpeedUnit != null) tvMinSpeedUnit.setTextColor(accentColor);
            if (tvMinDirection != null) tvMinDirection.setTextColor(accentColor);
            if (tvMinLightCount != null) tvMinLightCount.setTextColor(accentColor);
        }
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvDistanceNumMin != null) tvDistanceNumMin.setTextColor(TEXT_PRIMARY_DARK);
        if (tvDistanceUnitMin != null) tvDistanceUnitMin.setTextColor(TEXT_SECONDARY_DARK);
        if (tvRoadNameMin != null) tvRoadNameMin.setTextColor(TEXT_SECONDARY_DARK);
        if (ivActionIconMin != null) ivActionIconMin.clearColorFilter();
        if (tvMinSpeedUnit != null) tvMinSpeedUnit.setTextColor(TEXT_PRIMARY_DARK);
        if (tvMinDirection != null) tvMinDirection.setTextColor(TEXT_PRIMARY_DARK);
        if (tvMinLightCount != null) tvMinLightCount.setTextColor(TEXT_PRIMARY_DARK);

        boolean accentNaviInfo = sp.getBoolean("minimal_accent_navi_info_enabled", false);
        if (accentNaviInfo) {
            int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
            if (tvDistanceNumMin != null) tvDistanceNumMin.setTextColor(accentColor);
            if (tvDistanceUnitMin != null) tvDistanceUnitMin.setTextColor(accentColor);
            if (tvRoadNameMin != null) tvRoadNameMin.setTextColor(accentColor);
            if (ivActionIconMin != null) ivActionIconMin.setColorFilter(accentColor);
            if (tvMinSpeedUnit != null) tvMinSpeedUnit.setTextColor(accentColor);
            if (tvMinDirection != null) tvMinDirection.setTextColor(accentColor);
            if (tvMinLightCount != null) tvMinLightCount.setTextColor(accentColor);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tvMinSpeed != null) {
            ObjectAnimator animator = (ObjectAnimator) tvMinSpeed.getTag();
            if (animator != null) {
                animator.cancel();
                tvMinSpeed.setTag(null);
            }
            tvMinSpeed.setAlpha(1f);
        }
        if (ivActionIconMin != null) {
            ObjectAnimator animator = (ObjectAnimator) ivActionIconMin.getTag();
            if (animator != null) {
                animator.cancel();
                ivActionIconMin.setTag(null);
            }
            ivActionIconMin.setAlpha(1f);
        }
        isOverspeedBlinking = false;
    }
}
