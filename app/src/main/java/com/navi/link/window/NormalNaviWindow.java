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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import com.navi.link.view.IntervalSpeedView;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.TextView;

public class NormalNaviWindow extends BaseFloatingWindow {

    private IntervalSpeedView intervalSpeedView;
    private int themeColor = Color.BLACK;
    private ImageView ivTurnIcon;
    private View llTurnIconContainer;
    private TextView tvDistanceNum;
    private TextView tvDistanceUnit;
    private TextView tvAction;
    private TextView tvRoadName;
    private TmcProgressBar tmcProgressBar;
    private TextView tvSummary;
    private TextView tvEta;
    private View layoutInfoBar;
    private TextView tvExitInfo;
    private TextView tvNaviLightCount;
    private ImageView ivNaviLightIcon;
    private View vDivider;
    private LaneLineView laneLineView;
    private View layoutBottomContainer;
    private View layoutSapaGroup;
    private TextView tvSapaName1;
    private TextView tvSapaDist1;
    private TextView tvSapaName2;
    private TextView tvSapaDist2;
    private ImageView ivSapaBadge1;
    private ImageView ivSapaBadge2;

    private String mOriginalRoadName = "";
    private String mExitName = "";
    private String mExitDirection = "";

    private CameraWarningView cameraWarningView;
    private int mCameraDist = 0;
    private int mCameraSpeed = 0;
    private int mCameraType = 0;
    private int mTrafficLightCountdown = 0;

    private TrafficLightView llTrafficLightGroup;

    public NormalNaviWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        ivTurnIcon = floatingView.findViewById(R.id.iv_turn_icon);
        llTurnIconContainer = floatingView.findViewById(R.id.ll_turn_icon_container);
        tvDistanceNum = floatingView.findViewById(R.id.tv_distance_num);
        tvDistanceUnit = floatingView.findViewById(R.id.tv_distance_unit);
        tvAction = floatingView.findViewById(R.id.tv_action);
        tvRoadName = floatingView.findViewById(R.id.tv_road_name);
        tmcProgressBar = floatingView.findViewById(R.id.tmc_progress_bar);
        tvSummary = floatingView.findViewById(R.id.tv_summary);
        tvEta = floatingView.findViewById(R.id.tv_eta);
        layoutInfoBar = floatingView.findViewById(R.id.layout_info_bar);
        tvExitInfo = floatingView.findViewById(R.id.tv_exit_info);
        tvNaviLightCount = floatingView.findViewById(R.id.tv_navi_light_count);
        ivNaviLightIcon = floatingView.findViewById(R.id.iv_navi_light_icon);
        vDivider = floatingView.findViewById(R.id.v_divider);
        laneLineView = floatingView.findViewById(R.id.lane_line_view);
        cameraWarningView = floatingView.findViewById(R.id.ll_camera_dist_group);
        intervalSpeedView = floatingView.findViewById(R.id.interval_speed_view);

        layoutBottomContainer = floatingView.findViewById(R.id.layout_bottom_container);
        layoutSapaGroup = floatingView.findViewById(R.id.layout_sapa_group);
        tvSapaName1 = floatingView.findViewById(R.id.tv_sapa_name_1);
        tvSapaDist1 = floatingView.findViewById(R.id.tv_sapa_dist_1);
        tvSapaName2 = floatingView.findViewById(R.id.tv_sapa_name_2);
        tvSapaDist2 = floatingView.findViewById(R.id.tv_sapa_dist_2);
        ivSapaBadge1 = floatingView.findViewById(R.id.iv_sapa_badge_1);
        ivSapaBadge2 = floatingView.findViewById(R.id.iv_sapa_badge_2);

        llTrafficLightGroup = floatingView.findViewById(R.id.ll_traffic_light_group);

        if (tmcProgressBar != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            tmcProgressBar.setVisibility(tmcEnabled ? View.VISIBLE : View.GONE);
        }
        if (layoutInfoBar != null) {
            boolean bottomInfoEnabled = sp.getBoolean("normal_navi_bottom_info_enabled", true);
            layoutInfoBar.setVisibility(bottomInfoEnabled ? View.VISIBLE : View.GONE);
        }
        updateTurnIconBackground();
        updateBottomContainerVisibility();
    }

    private void updateTurnIconBackground() {
        if (ivTurnIcon != null) {
            View parent = (View) ivTurnIcon.getParent();
            if (parent != null) {
                boolean hideBg = sp.getBoolean("hide_turn_icon_bg", false);
                if (hideBg) {
                    parent.setBackground(null);
                } else {
                    parent.setBackgroundResource(R.drawable.bg_exit_shape);
                }
            }
        }
    }

    private boolean isNormalNaviLaneEnabled() {
        return sp.getBoolean("normal_navi_lane_enabled", false);
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
        if (ivTurnIcon != null) {
            int turnIconRes = getTurnIconRes(icon);
            if (turnIconRes != 0) {
                ivTurnIcon.setImageResource(turnIconRes);
            }
            boolean shouldBlink = shouldBlinkTurnIcon(disNum, disUnit);
            if (shouldBlink) {
                ObjectAnimator animator = (ObjectAnimator) ivTurnIcon.getTag();
                if (animator == null) {
                    ObjectAnimator newAnimator = ObjectAnimator.ofFloat(ivTurnIcon, "alpha", 1f, 0.3f);
                    newAnimator.setDuration(500);
                    newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    newAnimator.start();
                    ivTurnIcon.setTag(newAnimator);
                }
            } else {
                ObjectAnimator animator = (ObjectAnimator) ivTurnIcon.getTag();
                if (animator != null) {
                    animator.cancel();
                    ivTurnIcon.setTag(null);
                }
                ivTurnIcon.setAlpha(1f);
            }
        }
        if (tvDistanceNum != null) {
            tvDistanceNum.setText(disNum);
        }
        if (tvDistanceUnit != null) {
            tvDistanceUnit.setText(disNumIsNow(disNum) ? "" : disUnit);
        }
        if (tvAction != null) {
            tvAction.setText(actionStr);
        }
        mOriginalRoadName = roadName;
        updateRoadAndExitViews();
        mCameraDist = cameraDist;
        mCameraSpeed = cameraSpeed;
        mCameraType = cameraType;
        updateCameraDistVisibility();
        if (tvSummary != null) {
            tvSummary.setText(summaryStr);
        }
        if (tvEta != null) {
            tvEta.setText(formatEta(eta));
        }
        if (tvNaviLightCount != null) {
            if (remainLightNum > 0) {
                tvNaviLightCount.setText(remainLightNum + "个");
            } else {
                tvNaviLightCount.setText("--");
            }
        }
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection) {
        // 常规导航窗口不处理巡航数据
    }

    @Override
    public void updateTrafficLight(int status, int dir, int countdown) {
        mTrafficLightCountdown = countdown;
        if (llTrafficLightGroup == null) {
            updateCameraDistVisibility();
            return;
        }
        if (countdown <= 0) {
            llTrafficLightGroup.clear();
            updateCameraDistVisibility();
            return;
        }
        llTrafficLightGroup.setVisibility(View.VISIBLE);
        llTrafficLightGroup.setData(status, dir, countdown, true);
        updateCameraDistVisibility();
    }

    private void updateCameraDistVisibility() {
        if (cameraWarningView == null) return;
        if (mTrafficLightCountdown > 0) {
            cameraWarningView.setVisibility(View.GONE);
        } else {
            cameraWarningView.updateCameraInfo(mCameraType, mCameraDist, mCameraSpeed);
        }
    }

    @Override
    public void updateLaneLines(String driveWayJson) {
        if (laneLineView != null) {
            if (isNormalNaviLaneEnabled()) {
                laneLineView.updateLanes(driveWayJson);
            } else {
                laneLineView.clear();
            }
        }
    }

    @Override
    public void updateExitInfo(String exitName, String exitDirection) {
        mExitName = exitName;
        mExitDirection = exitDirection;
        updateRoadAndExitViews();
    }

    private void updateRoadAndExitViews() {
        if (tvExitInfo != null) {
            String name = mExitName != null ? mExitName.trim() : "";
            if (name.isEmpty()) {
                tvExitInfo.setVisibility(View.GONE);
            } else {
                tvExitInfo.setText(name);
                tvExitInfo.setVisibility(View.VISIBLE);
            }
        }
        if (tvRoadName != null) {
            String dir = mExitDirection != null ? mExitDirection.trim() : "";
            if (!dir.isEmpty()) {
                tvRoadName.setText(dir);
            } else {
                tvRoadName.setText(mOriginalRoadName != null ? mOriginalRoadName : "");
            }
        }
    }

    @Override
    public void applyThemeColor(int themeColor) {
        this.themeColor = themeColor;
        updateBottomContainerBackground();
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        this.isNightMode = isNightMode;
        int textPrimary = getPrimaryTextColor(isNightMode);
        int textSecondary = getSecondaryTextColor(isNightMode);

        if (tvDistanceNum != null) tvDistanceNum.setTextColor(textPrimary);
        if (tvDistanceUnit != null) tvDistanceUnit.setTextColor(textPrimary);
        if (tvAction != null) tvAction.setTextColor(textSecondary);
        if (tvRoadName != null) tvRoadName.setTextColor(textPrimary);
        if (tvSummary != null) tvSummary.setTextColor(textSecondary);
        if (tvEta != null) tvEta.setTextColor(textSecondary);
        if (tvNaviLightCount != null) tvNaviLightCount.setTextColor(textPrimary);
//        if (ivNaviLightIcon != null) ivNaviLightIcon.setColorFilter(textPrimary);
        if (ivTurnIcon != null) {
            int turnIconColor = isNightMode ? sp.getInt("normal_turn_icon_color_night", 0xFFFFFFFF) : sp.getInt("normal_turn_icon_color_day", 0xFFFFFFFF);
            ivTurnIcon.setColorFilter(turnIconColor);
        }
        if (llTurnIconContainer != null) {
            int turnBgColor = isNightMode ? sp.getInt("normal_turn_icon_bg_color_night", 0xFF007D5E) : sp.getInt("normal_turn_icon_bg_color_day", 0xFF007D5E);
            Drawable bg = llTurnIconContainer.getBackground();
            if (bg instanceof GradientDrawable) {
                ((GradientDrawable) bg.mutate()).setColor(turnBgColor);
            }
        }
        if (vDivider != null) vDivider.setBackgroundColor(textSecondary);
        if (tvExitInfo != null) {
            tvExitInfo.setTextColor(textSecondary);
            Drawable exitBg = tvExitInfo.getBackground();
            if (exitBg instanceof GradientDrawable) {
                ((GradientDrawable) exitBg.mutate()).setColor(isNightMode ? 0x33FFFFFF : 0x1A000000);
            }
        }
        if (cameraWarningView != null) cameraWarningView.setTextColor(textPrimary);

        if (tvSapaName1 != null) tvSapaName1.setTextColor(textPrimary);
        if (tvSapaDist1 != null) tvSapaDist1.setTextColor(textPrimary);
        if (tvSapaName2 != null) tvSapaName2.setTextColor(textPrimary);
        if (tvSapaDist2 != null) tvSapaDist2.setTextColor(textPrimary);
        View sapaDivider = floatingView.findViewById(R.id.v_sapa_top_divider);
        if (sapaDivider != null) {
            sapaDivider.setBackgroundColor(isNightMode ? 0x2AFFFFFF : 0x2A000000);
        }
        updateBottomContainerBackground();
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvDistanceNum != null) tvDistanceNum.setTextColor(TEXT_PRIMARY_DARK);
        if (tvDistanceUnit != null) tvDistanceUnit.setTextColor(TEXT_PRIMARY_DARK);
        if (tvAction != null) tvAction.setTextColor(TEXT_SECONDARY_DARK);
        if (tvRoadName != null) tvRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSummary != null) tvSummary.setTextColor(TEXT_SECONDARY_DARK);
        if (tvEta != null) tvEta.setTextColor(TEXT_SECONDARY_DARK);
        if (tvNaviLightCount != null) tvNaviLightCount.setTextColor(TEXT_PRIMARY_DARK);
        if (ivTurnIcon != null) ivTurnIcon.clearColorFilter();
        if (llTurnIconContainer != null) {
            Drawable bg = llTurnIconContainer.getBackground();
            if (bg instanceof GradientDrawable) {
                ((GradientDrawable) bg.mutate()).setColor(0xFF007D5E);
            }
        }
        if (vDivider != null) vDivider.setBackgroundColor(TEXT_SECONDARY_DARK);
        if (tvExitInfo != null) {
            tvExitInfo.setTextColor(TEXT_SECONDARY_DARK);
            Drawable exitBg = tvExitInfo.getBackground();
            if (exitBg instanceof GradientDrawable) {
                ((GradientDrawable) exitBg.mutate()).setColor(0x33FFFFFF);
            }
        }
        if (cameraWarningView != null) cameraWarningView.setTextColor(TEXT_PRIMARY_DARK);

        if (tvSapaName1 != null) tvSapaName1.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaDist1 != null) tvSapaDist1.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaName2 != null) tvSapaName2.setTextColor(TEXT_PRIMARY_DARK);
        if (tvSapaDist2 != null) tvSapaDist2.setTextColor(TEXT_PRIMARY_DARK);
        View sapaDivider = floatingView.findViewById(R.id.v_sapa_top_divider);
        if (sapaDivider != null) {
            sapaDivider.setBackgroundColor(0x2AFFFFFF);
        }
    }

    @Override
    public void updateTmcData(String tmcJson) {
        if (tmcProgressBar != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            if (tmcEnabled) {
                tmcProgressBar.setVisibility(View.VISIBLE);
                tmcProgressBar.updateTmcData(tmcJson);
            } else {
                tmcProgressBar.setVisibility(View.GONE);
                tmcProgressBar.clear();
            }
        }
    }

    @Override
    public void updateSapaInfo(String sapaName, String sapaDist, int sapaType, String nextSapaName, String nextSapaDist, int nextSapaType) {
        boolean hasFirst = sapaName != null && !sapaName.trim().isEmpty();
        if (!hasFirst) {
            if (layoutSapaGroup != null) {
                layoutSapaGroup.setVisibility(View.GONE);
            }
            updateBottomContainerVisibility();
            return;
        }

        if (layoutSapaGroup != null) {
            layoutSapaGroup.setVisibility(View.VISIBLE);
        }

        if (ivSapaBadge1 != null) {
            ivSapaBadge1.setImageResource(sapaType == 0 ? R.drawable.spap_0 : R.drawable.spap_1);
            ivSapaBadge1.setBackgroundResource(sapaType == 0 ? R.drawable.bg_sapa_0 : R.drawable.bg_sapa_1);
        }
        if (tvSapaName1 != null) {
            tvSapaName1.setText(sapaName);
        }
        if (tvSapaDist1 != null) {
            tvSapaDist1.setText(sapaDist != null ? sapaDist : "");
        }

        boolean hasSecond = nextSapaName != null && !nextSapaName.trim().isEmpty();
        View row2 = floatingView.findViewById(R.id.layout_sapa_row_2);
        if (row2 != null) {
            row2.setVisibility(hasSecond ? View.VISIBLE : View.GONE);
        }
        if (hasSecond) {
            if (ivSapaBadge2 != null) {
                ivSapaBadge2.setImageResource(nextSapaType == 0 ? R.drawable.spap_0 : R.drawable.spap_1);
                ivSapaBadge2.setBackgroundResource(nextSapaType == 0 ? R.drawable.bg_sapa_0 : R.drawable.bg_sapa_1);
            }
            if (tvSapaName2 != null) {
                tvSapaName2.setText(nextSapaName);
            }
            if (tvSapaDist2 != null) {
                tvSapaDist2.setText(nextSapaDist != null ? nextSapaDist : "");
            }
        }
        updateBottomContainerVisibility();
    }

    private void updateBottomContainerVisibility() {
        if (layoutBottomContainer == null) return;
        boolean bottomInfoVisible = layoutInfoBar != null && layoutInfoBar.getVisibility() == View.VISIBLE;
        boolean sapaVisible = layoutSapaGroup != null && layoutSapaGroup.getVisibility() == View.VISIBLE;
        if (bottomInfoVisible || sapaVisible) {
            layoutBottomContainer.setVisibility(View.VISIBLE);
        } else {
            layoutBottomContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ivTurnIcon != null) {
            ObjectAnimator animator = (ObjectAnimator) ivTurnIcon.getTag();
            if (animator != null) {
                animator.cancel();
                ivTurnIcon.setTag(null);
            }
            ivTurnIcon.setAlpha(1f);
        }
    }

    private void updateBottomContainerBackground() {
        if (layoutBottomContainer != null) {
            int bgColor = getWindowBgColor(isNightMode);
            int bottomBgColor = getBottomBgColor(bgColor, isNightMode);

            GradientDrawable bottomGrad = new GradientDrawable();
            bottomGrad.setShape(GradientDrawable.RECTANGLE);
            bottomGrad.setColor(bottomBgColor);
            
            float r = dpToPx(16);
            bottomGrad.setCornerRadii(new float[]{0, 0, 0, 0, r, r, r, r});
            layoutBottomContainer.setBackground(bottomGrad);
        }
    }

    private int getBottomBgColor(int bgColor, boolean isNight) {
        int a = (bgColor >> 24) & 0xFF;
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;

        if (isNight) {
            int avg = (r + g + b) / 3;
            if (avg < 40) {
                r = Math.min(255, r + 15);
                g = Math.min(255, g + 15);
                b = Math.min(255, b + 15);
            } else {
                r = Math.max(0, r - 15);
                g = Math.max(0, g - 15);
                b = Math.max(0, b - 15);
            }
        } else {
            int avg = (r + g + b) / 3;
            if (avg < 100) {
                r = Math.min(255, r + 20);
                g = Math.min(255, g + 20);
                b = Math.min(255, b + 20);
            } else {
                r = Math.max(0, r - 20);
                g = Math.max(0, g - 20);
                b = Math.max(0, b - 20);
            }
        }
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
