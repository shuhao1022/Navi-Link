package com.navi.link;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class FullNaviWindow extends BaseFloatingWindow {

    private TextView tvFullSpeed;
    private TextView tvFullSpeedLimit;
    private TextView tvFullCurRoadName;
    private TextView tvFullSpeedUnit;
    private TextView tvDistanceNumFull;
    private TextView tvDistanceUnitFull;
    private TextView tvRoadNameMinFull;
    private TextView tvSummaryFull;
    private TextView tvEtaFull;
    private TextView tvFullEndPoiName;
    private TextView tvFullCameraDist;
    private TextView tvFullLightCount;
    private CardView cvFullMiddle;

    private View llTrafficLightGroupFull;
    private ImageView ivLightIconFull;
    private ImageView ivLightArrowFull;
    private TextView tvLightTimeFull;

    private ImageView ivActionIconFull;
    private TextView tvFullLabelCurrent;
    private TextView tvFullLabelEnd;
    private TextView tvFullDirection;
    private TmcProgressBar tmcProgressBarFull;
    private LaneLineView laneLineViewFull;

    private boolean isOverspeedBlinking = false;
    private int themeColor = 0xFF4FC3F7;

    public FullNaviWindow(Context context, View floatingView) {
        super(context, floatingView);
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
    }

    @Override
    protected void initViews() {
        tvFullSpeed = floatingView.findViewById(R.id.tv_full_speed);
        tvFullSpeedLimit = floatingView.findViewById(R.id.tv_full_speed_limit);
        tvFullCurRoadName = floatingView.findViewById(R.id.tv_full_cur_road_name);
        tvFullSpeedUnit = floatingView.findViewById(R.id.tv_full_speed_unit);
        tvDistanceNumFull = floatingView.findViewById(R.id.tv_distance_num_full);
        tvDistanceUnitFull = floatingView.findViewById(R.id.tv_distance_unit_full);
        tvRoadNameMinFull = floatingView.findViewById(R.id.tv_road_name_min);
        tvSummaryFull = floatingView.findViewById(R.id.tv_summary_full);
        tvEtaFull = floatingView.findViewById(R.id.tv_eta_full);
        tvFullEndPoiName = floatingView.findViewById(R.id.tv_full_end_poi_name);
        tvFullCameraDist = floatingView.findViewById(R.id.tv_full_camera_dist);
        tvFullLightCount = floatingView.findViewById(R.id.tv_full_light_count);
        cvFullMiddle = floatingView.findViewById(R.id.cv_full_middle);

        llTrafficLightGroupFull = floatingView.findViewById(R.id.ll_traffic_light_group);
        if (llTrafficLightGroupFull != null) {
            ivLightIconFull = llTrafficLightGroupFull.findViewById(R.id.iv_light_icon);
            ivLightArrowFull = llTrafficLightGroupFull.findViewById(R.id.iv_light_arrow);
            tvLightTimeFull = llTrafficLightGroupFull.findViewById(R.id.tv_light_time);
        }

        ivActionIconFull = floatingView.findViewById(R.id.iv_action_icon_full);
        tvFullLabelCurrent = floatingView.findViewById(R.id.tv_full_label_current);
        tvFullLabelEnd = floatingView.findViewById(R.id.tv_full_label_end);
        tvFullDirection = floatingView.findViewById(R.id.tv_full_direction);
        tmcProgressBarFull = floatingView.findViewById(R.id.tmc_progress_bar_full);
        laneLineViewFull = floatingView.findViewById(R.id.lane_line_view_full);

        if (tmcProgressBarFull != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            tmcProgressBarFull.setVisibility(tmcEnabled ? View.VISIBLE : View.GONE);
        }
        boolean bottomInfoEnabled = sp.getBoolean("normal_navi_bottom_info_enabled", true);
        if (tvSummaryFull != null) {
            tvSummaryFull.setVisibility(bottomInfoEnabled ? View.VISIBLE : View.GONE);
        }
        if (tvEtaFull != null) {
            tvEtaFull.setVisibility(bottomInfoEnabled ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void updateNaviInfo(
            int icon, String disNum, String disUnit, String actionStr,
            String roadName, String summaryStr, String eta,
            int progress, int curSpeed,
            int limitedSpeed, int cameraDist, int cameraSpeed,
            String endPoiName, int totalLightNum, int remainLightNum,
            String curRoadName, int carDirection
    ) {
        int turnIconRes = getTurnIconRes(icon);
        if (ivActionIconFull != null && turnIconRes != 0) {
            ivActionIconFull.setImageResource(turnIconRes);
        }

        // 转向图标闪烁逻辑
        if (ivActionIconFull != null) {
            boolean shouldBlink = shouldBlinkTurnIcon(disNum, disUnit);
            if (shouldBlink) {
                ObjectAnimator animator = (ObjectAnimator) ivActionIconFull.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(ivActionIconFull, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    ivActionIconFull.setTag(newAnimator);
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) ivActionIconFull.getTag();
                if (animator != null) {
                    animator.cancel();
                    ivActionIconFull.setTag(null);
                }
                ivActionIconFull.setAlpha(1f);
            }
        }

        if (tvFullSpeed != null) {
            tvFullSpeed.setText(String.valueOf(curSpeed));
            // 超速警告：限速优先用cameraSpeed，为0则用limitedSpeed
            int limit = cameraSpeed > 0 ? cameraSpeed : limitedSpeed;
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && limit > 0 && curSpeed > limit;
            if (overspeed) {
                tvFullSpeed.setTextColor(Color.RED);
                ObjectAnimator animator = (ObjectAnimator) tvFullSpeed.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(tvFullSpeed, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    tvFullSpeed.setTag(newAnimator);
                    isOverspeedBlinking = true;
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) tvFullSpeed.getTag();
                if (animator != null) {
                    animator.cancel();
                    tvFullSpeed.setTag(null);
                }
                tvFullSpeed.setAlpha(1f);
                isOverspeedBlinking = false;
                // 恢复正常主题色（黑色主题用蓝色）
                int fullCardAccent = isDarkThemeColor(themeColor) ? 0xFF0099FF : themeColor;
                tvFullSpeed.setTextColor(fullCardAccent);
            }
        }

        if (tvFullSpeedLimit != null) {
            if (limitedSpeed > 0) {
                tvFullSpeedLimit.setText(String.valueOf(limitedSpeed));
            } else {
                tvFullSpeedLimit.setText("0");
            }
        }

        if (tvFullCurRoadName != null) {
            String name = (curRoadName != null && !curRoadName.isEmpty()) ? curRoadName : roadName;
            tvFullCurRoadName.setText(name != null ? name : "未知道路");
        }

        if (tvDistanceNumFull != null) {
            tvDistanceNumFull.setText(disNum);
        }

        if (tvDistanceUnitFull != null) {
            tvDistanceUnitFull.setText(disNumIsNow(disNum) ? "进入" : disUnit);
        }

        if (tvRoadNameMinFull != null) {
            tvRoadNameMinFull.setText(roadName != null ? roadName : "");
        }

        if (tvSummaryFull != null) {
            tvSummaryFull.setText(summaryStr);
        }

        if (tvEtaFull != null) {
            tvEtaFull.setText(eta);
        }

        if (tvFullEndPoiName != null) {
            tvFullEndPoiName.setText(endPoiName != null ? endPoiName : "");
        }

        if (tvFullCameraDist != null) {
            if (cameraDist > 0) {
                tvFullCameraDist.setText(cameraDist + "米");
            } else {
                tvFullCameraDist.setText("--");
            }
        }

        if (tvFullLightCount != null) {
            if (remainLightNum > 0) {
                tvFullLightCount.setText(remainLightNum + "个");
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

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraSpeed, int cameraDist, int carDirection) {
        // 全数据导航不处理巡航
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        if (llTrafficLightGroupFull == null) return;
        if (countdown <= 0) {
            llTrafficLightGroupFull.setVisibility(View.GONE);
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroupFull.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroupFull.setTag(null);
            }
            llTrafficLightGroupFull.setAlpha(1f);
            return;
        }
        llTrafficLightGroupFull.setVisibility(View.VISIBLE);
        if (ivLightIconFull != null) {
            ivLightIconFull.setImageResource(getNaviLightIconRes(status));
        }
        if (ivLightArrowFull != null) {
            ivLightArrowFull.setImageResource(getNaviLightDirRes(dir));
        }
        if (tvLightTimeFull != null) {
            tvLightTimeFull.setText(String.valueOf(countdown));
        }

        if (countdown <= 5) {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroupFull.getTag();
            if (animator == null) {
                ObjectAnimator newAnimator = ObjectAnimator.ofFloat(llTrafficLightGroupFull, "alpha", 1f, 0.3f);
                newAnimator.setDuration(500);
                newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                newAnimator.start();
                llTrafficLightGroupFull.setTag(newAnimator);
            }
        } else {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroupFull.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroupFull.setTag(null);
            }
            llTrafficLightGroupFull.setAlpha(1f);
        }
    }

    @Override
    public void updateLaneLines(String driveWayJson) {
        if (laneLineViewFull != null) {
            boolean laneEnabled = sp.getBoolean("normal_navi_lane_enabled", false);
            if (laneEnabled) {
                laneLineViewFull.updateLanes(driveWayJson);
            } else {
                laneLineViewFull.clear();
            }
        }
    }

    @Override
    public void updateExitInfo(String exitName, String exitDirection) {
        // 全数据导航无出口信息
    }

    @Override
    public void updateTmcData(String tmcJson) {
        if (tmcProgressBarFull != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            if (tmcEnabled) {
                tmcProgressBarFull.setVisibility(View.VISIBLE);
                tmcProgressBarFull.updateTmcData(tmcJson);
            } else {
                tmcProgressBarFull.setVisibility(View.GONE);
                tmcProgressBarFull.clear();
            }
        }
    }

    @Override
    public void applyThemeColor(int themeColor) {
        this.themeColor = themeColor;
        boolean isDark = isDarkThemeColor(themeColor);
        int accentColor = isDark ? Color.WHITE : themeColor;
        int fullCardAccent = isDark ? 0xFF0099FF : themeColor;
        int backgroundMode = sp.getInt("background_mode", 0);

        int labelColor = accentColor;
        if (backgroundMode == 2) {
            boolean isNight = sp.getBoolean("is_night_mode", true);
            labelColor = isNight ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;
        }

        if (tvFullSpeed != null && !isOverspeedBlinking) {
            tvFullSpeed.setTextColor(fullCardAccent);
        }
        if (tvFullSpeedUnit != null) {
            tvFullSpeedUnit.setTextColor(fullCardAccent);
        }
        if (tvFullLabelCurrent != null) {
            tvFullLabelCurrent.setTextColor(labelColor);
        }
        if (tvFullLabelEnd != null) {
            tvFullLabelEnd.setTextColor(labelColor);
        }
        if (tvFullDirection != null) {
            tvFullDirection.setTextColor(fullCardAccent);
        }

        if (cvFullMiddle != null) {
            cvFullMiddle.setCardBackgroundColor(fullCardAccent);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        int textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        int textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;

        if (tvFullCurRoadName != null) tvFullCurRoadName.setTextColor(textPrimary);
        if (tvDistanceNumFull != null) tvDistanceNumFull.setTextColor(textPrimary);
        if (tvDistanceUnitFull != null) tvDistanceUnitFull.setTextColor(textSecondary);
        if (tvRoadNameMinFull != null) tvRoadNameMinFull.setTextColor(textSecondary);
        if (ivActionIconFull != null) ivActionIconFull.setColorFilter(textPrimary);
        if (tvSummaryFull != null) tvSummaryFull.setTextColor(textSecondary);
        if (tvEtaFull != null) tvEtaFull.setTextColor(textSecondary);
        if (tvFullEndPoiName != null) tvFullEndPoiName.setTextColor(textPrimary);
        if (tvFullCameraDist != null) tvFullCameraDist.setTextColor(textPrimary);
        if (tvFullLightCount != null) tvFullLightCount.setTextColor(textPrimary);
        if (tvFullSpeedUnit != null) tvFullSpeedUnit.setTextColor(textPrimary);
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvFullCurRoadName != null) tvFullCurRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (tvDistanceNumFull != null) tvDistanceNumFull.setTextColor(TEXT_PRIMARY_DARK);
        if (tvDistanceUnitFull != null) tvDistanceUnitFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvRoadNameMinFull != null) tvRoadNameMinFull.setTextColor(TEXT_SECONDARY_DARK);
        if (ivActionIconFull != null) ivActionIconFull.clearColorFilter();
        if (tvSummaryFull != null) tvSummaryFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvEtaFull != null) tvEtaFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvFullEndPoiName != null) tvFullEndPoiName.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullCameraDist != null) tvFullCameraDist.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullLightCount != null) tvFullLightCount.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullSpeedUnit != null) tvFullSpeedUnit.setTextColor(TEXT_PRIMARY_DARK);
    }

    @Override
    public void onDestroy() {
        if (tvFullSpeed != null) {
            ObjectAnimator animator = (ObjectAnimator) tvFullSpeed.getTag();
            if (animator != null) {
                animator.cancel();
                tvFullSpeed.setTag(null);
            }
            tvFullSpeed.setAlpha(1f);
        }
        if (llTrafficLightGroupFull != null) {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroupFull.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroupFull.setTag(null);
            }
            llTrafficLightGroupFull.setAlpha(1f);
        }
        if (ivActionIconFull != null) {
            ObjectAnimator animator = (ObjectAnimator) ivActionIconFull.getTag();
            if (animator != null) {
                animator.cancel();
                ivActionIconFull.setTag(null);
            }
            ivActionIconFull.setAlpha(1f);
        }
        isOverspeedBlinking = false;
    }
}
