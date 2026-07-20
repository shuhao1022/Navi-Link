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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.View;
import com.navi.link.view.IntervalSpeedView;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class FullNaviWindow extends BaseFloatingWindow {

    private IntervalSpeedView intervalSpeedView;
    private TextView tvFullSpeed;
    private TextView tvFullSpeedLimit;
    private TextView tvFullCurRoadName;
    private TextView tvFullSpeedUnit;
    private TextView tvDistanceNumFull;
    private TextView tvDistanceUnitFull;
    private TextView tvRoadNameMinFull;
    private TextView tvSummaryFull;
    private TextView tvEtaFull;
    private CameraWarningView tvFullCameraDist;
    private TextView tvFullLightCount;
    private CardView cvFullMiddle;

    private TrafficLightView llTrafficLightGroupFull;

    private ImageView ivActionIconFull;
    private TextView tvFullDirection;
    private TmcProgressBar tmcProgressBarFull;
    private LaneLineView laneLineViewFull;

    // 底部容器与服务区
    private View layoutBottomContainerFull;
    private View layoutInfoBarFull;
    private View layoutSapaGroupFull;
    private TextView tvSapaName1Full;
    private TextView tvSapaDist1Full;
    private TextView tvSapaName2Full;
    private TextView tvSapaDist2Full;
    private ImageView ivSapaBadge1Full;
    private ImageView ivSapaBadge2Full;

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
        tvFullCameraDist = floatingView.findViewById(R.id.tv_full_camera_dist);
        if (tvFullCameraDist != null) {
            tvFullCameraDist.setAlwaysShow(true);
        }
        tvFullLightCount = floatingView.findViewById(R.id.tv_full_light_count);
        cvFullMiddle = floatingView.findViewById(R.id.cv_full_middle);

        llTrafficLightGroupFull = floatingView.findViewById(R.id.ll_traffic_light_group);

        ivActionIconFull = floatingView.findViewById(R.id.iv_action_icon_full);
        tvFullDirection = floatingView.findViewById(R.id.tv_full_direction);
        tmcProgressBarFull = floatingView.findViewById(R.id.tmc_progress_bar_full);
        laneLineViewFull = floatingView.findViewById(R.id.lane_line_view_full);
        intervalSpeedView = floatingView.findViewById(R.id.interval_speed_view);

        // 底部容器与服务区
        layoutBottomContainerFull = floatingView.findViewById(R.id.layout_bottom_container_full);
        layoutInfoBarFull = floatingView.findViewById(R.id.layout_info_bar_full);
        layoutSapaGroupFull = floatingView.findViewById(R.id.layout_sapa_group_full);
        tvSapaName1Full = floatingView.findViewById(R.id.tv_sapa_name_1_full);
        tvSapaDist1Full = floatingView.findViewById(R.id.tv_sapa_dist_1_full);
        tvSapaName2Full = floatingView.findViewById(R.id.tv_sapa_name_2_full);
        tvSapaDist2Full = floatingView.findViewById(R.id.tv_sapa_dist_2_full);
        ivSapaBadge1Full = floatingView.findViewById(R.id.iv_sapa_badge_1_full);
        ivSapaBadge2Full = floatingView.findViewById(R.id.iv_sapa_badge_2_full);

        if (tmcProgressBarFull != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            tmcProgressBarFull.setVisibility(tmcEnabled ? View.VISIBLE : View.GONE);
        }
        boolean bottomInfoEnabled = sp.getBoolean("normal_navi_bottom_info_enabled", true);
        if (layoutInfoBarFull != null) {
            layoutInfoBarFull.setVisibility(bottomInfoEnabled ? View.VISIBLE : View.GONE);
        }
        updateBottomContainerVisibility();
    }

    private int mCurSpeed = 0;

    @Override
    public void updateNaviInfo(
            int icon, String disNum, String disUnit, String actionStr,
            String roadName, String summaryStr, String eta,
            int progress, int curSpeed,
            int limitedSpeed, int cameraType, int cameraDist, int cameraSpeed,
            String endPoiName, int totalLightNum, int remainLightNum,
            String curRoadName, int carDirection
    ) {
        this.mCurSpeed = curSpeed;
        if (intervalSpeedView != null) {
            intervalSpeedView.updateCurrentSpeed(curSpeed);
        }
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
                // 恢复正常车速颜色，使用 XML 原生设定的蓝色 #FF0099FF
                tvFullSpeed.setTextColor(0xFF0099FF);
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
            tvEtaFull.setText(formatEta(eta));
        }

        if (tvFullCameraDist != null) {
            tvFullCameraDist.updateCameraInfo(cameraType, cameraDist, cameraSpeed);
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
                tvFullDirection.setText("车头 " + getDirectionText(carDirection));
            } else {
                tvFullDirection.setText("");
            }
        }
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        // 全数据导航不处理巡航数据
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        if (llTrafficLightGroupFull == null) return;
        if (countdown <= 0) {
            llTrafficLightGroupFull.clear();
            return;
        }
        llTrafficLightGroupFull.setVisibility(View.VISIBLE);
        llTrafficLightGroupFull.setData(status, dir, countdown, true);
    }

    @Override
    public void updateSapaInfo(String sapaName, String sapaDist, int sapaType, String nextSapaName, String nextSapaDist, int nextSapaType) {
        boolean hasFirst = sapaName != null && !sapaName.trim().isEmpty();
        if (!hasFirst) {
            if (layoutSapaGroupFull != null) {
                layoutSapaGroupFull.setVisibility(View.GONE);
            }
            updateBottomContainerVisibility();
            return;
        }

        if (layoutSapaGroupFull != null) {
            layoutSapaGroupFull.setVisibility(View.VISIBLE);
        }

        if (ivSapaBadge1Full != null) {
            ivSapaBadge1Full.setImageResource(sapaType == 0 ? R.drawable.spap_0 : R.drawable.spap_1);
            ivSapaBadge1Full.setBackgroundResource(sapaType == 0 ? R.drawable.bg_sapa_0 : R.drawable.bg_sapa_1);
        }
        if (tvSapaName1Full != null) {
            tvSapaName1Full.setText(sapaName);
        }
        if (tvSapaDist1Full != null) {
            tvSapaDist1Full.setText(sapaDist != null ? sapaDist : "");
        }

        boolean hasSecond = nextSapaName != null && !nextSapaName.trim().isEmpty();
        View row2Full = floatingView.findViewById(R.id.layout_sapa_row_2_full);
        if (row2Full != null) {
            row2Full.setVisibility(hasSecond ? View.VISIBLE : View.GONE);
        }
        if (hasSecond) {
            if (ivSapaBadge2Full != null) {
                ivSapaBadge2Full.setImageResource(nextSapaType == 0 ? R.drawable.spap_0 : R.drawable.spap_1);
                ivSapaBadge2Full.setBackgroundResource(nextSapaType == 0 ? R.drawable.bg_sapa_0 : R.drawable.bg_sapa_1);
            }
            if (tvSapaName2Full != null) {
                tvSapaName2Full.setText(nextSapaName);
            }
            if (tvSapaDist2Full != null) {
                tvSapaDist2Full.setText(nextSapaDist != null ? nextSapaDist : "");
            }
        }
        updateBottomContainerVisibility();
    }

    private void updateBottomContainerVisibility() {
        if (layoutBottomContainerFull == null) return;
        boolean bottomInfoVisible = layoutInfoBarFull != null && layoutInfoBarFull.getVisibility() == View.VISIBLE;
        boolean sapaVisible = layoutSapaGroupFull != null && layoutSapaGroupFull.getVisibility() == View.VISIBLE;
        if (bottomInfoVisible || sapaVisible) {
            layoutBottomContainerFull.setVisibility(View.VISIBLE);
        } else {
            layoutBottomContainerFull.setVisibility(View.GONE);
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
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary = getPrimaryTextColor(isNightMode);
        int textSecondary = getSecondaryTextColor(isNightMode);

        if (tvFullCurRoadName != null) tvFullCurRoadName.setTextColor(textPrimary);

        // 计算中间卡片（cvFullMiddle）的文字与图标颜色，使其与卡片背景色形成对比
        int middleBgColor = isNightMode ? sp.getInt("full_middle_bg_color_night", 0xFF0099FF) : sp.getInt("full_middle_bg_color_day", 0xFF0099FF);
        if (cvFullMiddle != null) {
            cvFullMiddle.setCardBackgroundColor(middleBgColor);
        }
        if (tvDistanceNumFull != null) tvDistanceNumFull.setTextColor(textPrimary);
        if (tvDistanceUnitFull != null) tvDistanceUnitFull.setTextColor(textSecondary);
        if (tvRoadNameMinFull != null) tvRoadNameMinFull.setTextColor(textPrimary);
        if (ivActionIconFull != null) {
            ivActionIconFull.setColorFilter(textPrimary);
        }

        if (tvSummaryFull != null) tvSummaryFull.setTextColor(textSecondary);
        if (tvEtaFull != null) tvEtaFull.setTextColor(textSecondary);
        if (tvFullCameraDist != null) tvFullCameraDist.setTextColor(textPrimary);
        if (tvFullDirection != null) tvFullDirection.setTextColor(textPrimary);
        if (tvFullLightCount != null) tvFullLightCount.setTextColor(textPrimary);
        if (tvSapaName1Full != null) tvSapaName1Full.setTextColor(textPrimary);
        if (tvSapaDist1Full != null) tvSapaDist1Full.setTextColor(textPrimary);
        if (tvSapaName2Full != null) tvSapaName2Full.setTextColor(textPrimary);
        if (tvSapaDist2Full != null) tvSapaDist2Full.setTextColor(textPrimary);
        View sapaDivider = floatingView.findViewById(R.id.v_sapa_top_divider_full);
        if (sapaDivider != null) {
            sapaDivider.setBackgroundColor(isNightMode ? 0x2AFFFFFF : 0x2A000000);
        }
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvFullCurRoadName != null) tvFullCurRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (cvFullMiddle != null) cvFullMiddle.setCardBackgroundColor(0xFF0099FF);
        if (tvDistanceNumFull != null) tvDistanceNumFull.setTextColor(TEXT_PRIMARY_DARK);
        if (tvDistanceUnitFull != null) tvDistanceUnitFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvRoadNameMinFull != null) tvRoadNameMinFull.setTextColor(TEXT_PRIMARY_DARK);
        if (ivActionIconFull != null) ivActionIconFull.clearColorFilter();
        if (tvSummaryFull != null) tvSummaryFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvEtaFull != null) tvEtaFull.setTextColor(TEXT_SECONDARY_DARK);
        if (tvFullCameraDist != null) tvFullCameraDist.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullDirection != null) tvFullDirection.setTextColor(TEXT_PRIMARY_DARK);
        if (tvFullLightCount != null) tvFullLightCount.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaName1Full != null) tvSapaName1Full.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaDist1Full != null) tvSapaDist1Full.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaName2Full != null) tvSapaName2Full.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaDist2Full != null) tvSapaDist2Full.setTextColor(TEXT_PRIMARY_DARK);
        View sapaDivider = floatingView.findViewById(R.id.v_sapa_top_divider_full);
        if (sapaDivider != null) {
            sapaDivider.setBackgroundColor(0x2AFFFFFF);
        }
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

    @Override
    public void updateIntervalSpeed(int startDist, String startDistText, int avgSpeed, String endDistText, int limitSpeed) {
        if (intervalSpeedView == null) return;

        if (startDist <= 0 && (startDistText == null || startDistText.isEmpty() || "0m".equals(startDistText) || "0.0m".equals(startDistText))
                && avgSpeed <= 0 && (endDistText == null || endDistText.isEmpty()) && limitSpeed <= 0) {
            intervalSpeedView.hide();
            return;
        }

        boolean isInside = (endDistText != null && !endDistText.trim().isEmpty()) || avgSpeed > 0;
        if (isInside) {
            intervalSpeedView.setInsideState(mCurSpeed, limitSpeed, avgSpeed, endDistText);
        } else {
            String distText = (startDistText != null && !startDistText.isEmpty()) ? startDistText : (startDist + "m");
            intervalSpeedView.setApproachingState(mCurSpeed, distText, limitSpeed);
        }
    }
}
