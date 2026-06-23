package com.navi.link;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.TextView;

public class NormalNaviWindow extends BaseFloatingWindow {

    private ImageView ivTurnIcon;
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
    private View vDivider;
    private LaneLineView laneLineView;

    private String mOriginalRoadName = "";
    private String mExitName = "";
    private String mExitDirection = "";

    private View llCameraDistGroup;
    private TextView tvCameraDist;
    private int mCameraDist = 0;
    private int mTrafficLightCountdown = 0;

    private View llTrafficLightGroup;
    private ImageView ivLightIcon;
    private ImageView ivLightArrow;
    private TextView tvLightTime;

    public NormalNaviWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        ivTurnIcon = floatingView.findViewById(R.id.iv_turn_icon);
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
        vDivider = floatingView.findViewById(R.id.v_divider);
        laneLineView = floatingView.findViewById(R.id.lane_line_view);
        llCameraDistGroup = floatingView.findViewById(R.id.ll_camera_dist_group);
        tvCameraDist = floatingView.findViewById(R.id.tv_camera_dist);

        llTrafficLightGroup = floatingView.findViewById(R.id.ll_traffic_light_group);
        if (llTrafficLightGroup != null) {
            ivLightIcon = llTrafficLightGroup.findViewById(R.id.iv_light_icon);
            ivLightArrow = llTrafficLightGroup.findViewById(R.id.iv_light_arrow);
            tvLightTime = llTrafficLightGroup.findViewById(R.id.tv_light_time);
        }

        if (tmcProgressBar != null) {
            boolean tmcEnabled = sp.getBoolean("normal_navi_tmc_enabled", true);
            tmcProgressBar.setVisibility(tmcEnabled ? View.VISIBLE : View.GONE);
        }
        if (layoutInfoBar != null) {
            boolean bottomInfoEnabled = sp.getBoolean("normal_navi_bottom_info_enabled", true);
            layoutInfoBar.setVisibility(bottomInfoEnabled ? View.VISIBLE : View.GONE);
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
            int limitedSpeed, int cameraDist, int cameraSpeed,
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
        updateCameraDistVisibility();
        if (tvSummary != null) {
            tvSummary.setText(summaryStr);
        }
        if (tvEta != null) {
            tvEta.setText(formatEta(eta));
        }
        if (tvNaviLightCount != null) {
            if (remainLightNum > 0) {
                tvNaviLightCount.setText("🚦" + remainLightNum + "个");
            } else {
                tvNaviLightCount.setText("🚦--");
            }
        }
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraSpeed, int cameraDist, int carDirection) {
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
            llTrafficLightGroup.setVisibility(View.GONE);
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroup.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroup.setTag(null);
            }
            llTrafficLightGroup.setAlpha(1f);
            updateCameraDistVisibility();
            return;
        }
        llTrafficLightGroup.setVisibility(View.VISIBLE);
        updateCameraDistVisibility();
        if (ivLightIcon != null) {
            ivLightIcon.setImageResource(getNaviLightIconRes(status));
        }
        if (ivLightArrow != null) {
            ivLightArrow.setImageResource(getNaviLightDirRes(dir));
        }
        if (tvLightTime != null) {
            tvLightTime.setText(String.valueOf(countdown));
        }

        if (countdown <= 5) {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroup.getTag();
            if (animator == null) {
                ObjectAnimator newAnimator = ObjectAnimator.ofFloat(llTrafficLightGroup, "alpha", 1f, 0.3f);
                newAnimator.setDuration(500);
                newAnimator.setRepeatCount(ValueAnimator.INFINITE);
                newAnimator.setRepeatMode(ValueAnimator.REVERSE);
                newAnimator.start();
                llTrafficLightGroup.setTag(newAnimator);
            }
        } else {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroup.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroup.setTag(null);
            }
            llTrafficLightGroup.setAlpha(1f);
        }
    }

    private void updateCameraDistVisibility() {
        if (llCameraDistGroup == null) return;
        if (mTrafficLightCountdown > 0) {
            llCameraDistGroup.setVisibility(View.GONE);
        } else {
            if (mCameraDist > 0) {
                if (tvCameraDist != null) {
                    tvCameraDist.setText(mCameraDist + "米");
                }
                llCameraDistGroup.setVisibility(View.VISIBLE);
            } else {
                llCameraDistGroup.setVisibility(View.GONE);
            }
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
        int backgroundMode = sp.getInt("background_mode", 0);
        boolean isDark = isDarkThemeColor(themeColor);
        int accentColor = isDark ? Color.WHITE : themeColor;

        if (layoutInfoBar != null) {
            int cornerPx = Math.round(dpToPx(12) * FloatingWindowManager.getInstance().getScale());
            if (backgroundMode == 2) {
                layoutInfoBar.setBackground(null);
            } else if (backgroundMode == 1) {
                GradientDrawable bgDrawable = new GradientDrawable();
                bgDrawable.setShape(GradientDrawable.RECTANGLE);
                int semiColor = (themeColor & 0x00FFFFFF) | 0x80000000;
                bgDrawable.setColor(semiColor);
                bgDrawable.setCornerRadii(new float[]{0, 0, 0, 0, cornerPx, cornerPx, cornerPx, cornerPx});
                layoutInfoBar.setBackground(bgDrawable);
            } else {
                int bgColor;
                if (isDark) {
                    bgColor = 0xFF242424;
                } else {
                    int r = (themeColor >> 16) & 0xFF;
                    int g = (themeColor >> 8) & 0xFF;
                    int b = themeColor & 0xFF;
                    bgColor = 0xFF000000
                            | ((int) (r * 0.20f) << 16)
                            | ((int) (g * 0.20f) << 8)
                            | (int) (b * 0.20f);
                }
                GradientDrawable bgDrawable = new GradientDrawable();
                bgDrawable.setShape(GradientDrawable.RECTANGLE);
                bgDrawable.setColor(bgColor);
                bgDrawable.setCornerRadii(new float[]{0, 0, 0, 0, cornerPx, cornerPx, cornerPx, cornerPx});
                layoutInfoBar.setBackground(bgDrawable);
            }
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        int textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        int textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;

        if (tvDistanceNum != null) tvDistanceNum.setTextColor(textPrimary);
        if (tvDistanceUnit != null) tvDistanceUnit.setTextColor(textPrimary);
        if (tvAction != null) tvAction.setTextColor(textSecondary);
        if (tvRoadName != null) tvRoadName.setTextColor(textPrimary);
        if (tvSummary != null) tvSummary.setTextColor(textSecondary);
        if (tvEta != null) tvEta.setTextColor(textSecondary);
        if (tvNaviLightCount != null) tvNaviLightCount.setTextColor(textPrimary);
        if (ivTurnIcon != null) ivTurnIcon.setColorFilter(textPrimary);
        if (vDivider != null) vDivider.setBackgroundColor(textPrimary);
        if (tvExitInfo != null) tvExitInfo.setTextColor(textSecondary);
        if (tvCameraDist != null) tvCameraDist.setTextColor(textPrimary);
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
        if (vDivider != null) vDivider.setBackgroundColor(TEXT_PRIMARY_DARK);
        if (tvExitInfo != null) tvExitInfo.setTextColor(TEXT_SECONDARY_DARK);
        if (tvCameraDist != null) tvCameraDist.setTextColor(TEXT_PRIMARY_DARK);
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
    public void onDestroy() {
        super.onDestroy();
        if (llTrafficLightGroup != null) {
            ObjectAnimator animator = (ObjectAnimator) llTrafficLightGroup.getTag();
            if (animator != null) {
                animator.cancel();
                llTrafficLightGroup.setTag(null);
            }
            llTrafficLightGroup.setAlpha(1f);
        }
        if (ivTurnIcon != null) {
            ObjectAnimator animator = (ObjectAnimator) ivTurnIcon.getTag();
            if (animator != null) {
                animator.cancel();
                ivTurnIcon.setTag(null);
            }
            ivTurnIcon.setAlpha(1f);
        }
    }
}
