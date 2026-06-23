package com.navi.link;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

public class NormalCruiseWindow extends BaseFloatingWindow {

    private TextView tvCnSpeed;
    private TextView tvCnRoadName;
    private TextView tvCnSpeedLimit;
    private LinearLayout llCnTrafficLightsContainer;
    private LaneLineView laneLineView;
    private View llCnCameraDist;
    private TextView tvCnCameraDist;

    private boolean isOverspeedBlinking = false;
    private int themeColor = Color.BLACK;

    public NormalCruiseWindow(Context context, View floatingView) {
        super(context, floatingView);
    }

    @Override
    protected void initViews() {
        tvCnSpeed = floatingView.findViewById(R.id.tv_cn_speed);
        tvCnRoadName = floatingView.findViewById(R.id.tv_cn_road_name);
        tvCnSpeedLimit = floatingView.findViewById(R.id.tv_cn_speed_limit);
        llCnTrafficLightsContainer = floatingView.findViewById(R.id.ll_cn_traffic_lights_container);
        laneLineView = floatingView.findViewById(R.id.lane_line_view);
        llCnCameraDist = floatingView.findViewById(R.id.ll_cn_camera_dist);
        tvCnCameraDist = floatingView.findViewById(R.id.tv_cn_camera_dist);
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
        // 常规巡航窗口不处理导航数据
    }

    @Override
    public void updateCruiseInfo(int speed, String roadName, int cameraSpeed, int cameraDist, int carDirection) {
        if (tvCnSpeed != null) {
            tvCnSpeed.setText(String.valueOf(speed));
            // 超速警告：限速>0 且 当前速度>限速 → 红色+闪烁 (受 overspeed_warning_enabled 开关控制)
            boolean isOverspeedWarningEnabled = sp.getBoolean("overspeed_warning_enabled", true);
            boolean overspeed = isOverspeedWarningEnabled && cameraSpeed > 0 && speed > cameraSpeed;
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
                // 恢复正常主题色
                int accentColor = isDarkThemeColor(themeColor) ? Color.WHITE : themeColor;
                tvCnSpeed.setTextColor(accentColor);
            }
        }
        if (tvCnRoadName != null && roadName != null) {
            tvCnRoadName.setText(roadName);
        }
        updateCruiseCameraAndLimit(cameraSpeed, cameraDist);
    }

    private void updateCruiseCameraAndLimit(int cameraSpeed, int cameraDist) {
        if (cameraSpeed > 0) {
            if (tvCnSpeedLimit != null) {
                tvCnSpeedLimit.setText(String.valueOf(cameraSpeed));
                tvCnSpeedLimit.setVisibility(View.VISIBLE);
            }
            if (llCnCameraDist != null) {
                llCnCameraDist.setVisibility(View.GONE);
            }
        } else if (cameraDist > 0) {
            if (llCnCameraDist != null) {
                if (tvCnCameraDist != null) {
                    tvCnCameraDist.setText(cameraDist + "米");
                }
                llCnCameraDist.setVisibility(View.VISIBLE);
            }
            if (tvCnSpeedLimit != null) {
                tvCnSpeedLimit.setVisibility(View.GONE);
            }
        } else {
            if (tvCnSpeedLimit != null) {
                tvCnSpeedLimit.setText("0");
                tvCnSpeedLimit.setVisibility(View.VISIBLE);
            }
            if (llCnCameraDist != null) {
                llCnCameraDist.setVisibility(View.GONE);
            }
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
            int layoutRes = (count >= 3)
                    ? R.layout.item_cruise_traffic_light_small
                    : R.layout.item_cruise_traffic_light;
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
            tvCnSpeed.setTextColor(accentColor);
        }
    }

    @Override
    public void applyDayNightTextColors(boolean isNightMode) {
        int textPrimary = isNightMode ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
        int textSecondary = isNightMode ? TEXT_SECONDARY_DARK : TEXT_SECONDARY_LIGHT;

        if (tvCnRoadName != null) tvCnRoadName.setTextColor(textPrimary);
        if (tvCnCameraDist != null) tvCnCameraDist.setTextColor(textPrimary);
    }

    @Override
    public void resetToDefaultTextColors() {
        if (tvCnRoadName != null) tvCnRoadName.setTextColor(TEXT_PRIMARY_DARK);
        if (tvCnCameraDist != null) tvCnCameraDist.setTextColor(TEXT_PRIMARY_DARK);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llCnTrafficLightsContainer != null) {
            for (int i = 0; i < llCnTrafficLightsContainer.getChildCount(); i++) {
                View child = llCnTrafficLightsContainer.getChildAt(i);
                if (child != null) {
                    ObjectAnimator animator = (ObjectAnimator) child.getTag();
                    if (animator != null) {
                        animator.cancel();
                        child.setTag(null);
                    }
                }
            }
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
