package com.navi.link;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 车道线组件：根据高德广播 EXTRA_DRIVE_WAY 数据动态显示车道线图标。
 * <p>
 * 数据结构：
 * - drive_way_size: 车道线数量
 * - drive_way_info[]: 每条车道信息
 *   - drive_way_number: 车道序号（0-based）
 *   - drive_way_lane_Back_icon: 图标编号（对应 drawable/lane_pdf_{N}.png）
 */
public class LaneLineView extends LinearLayout {

    private static final String TAG = "LaneLineView";
    private static final int LANE_ICON_BASE_DP = 50;
    private static final int LANE_MARGIN_DP = 0;
    private static final int DIVIDER_WIDTH_DP = 1;
    private static final int DIVIDER_HEIGHT_DP = 34;

    private String cachedDriveWayJson = null;
    private boolean isCompactMode = false; // true=wrap_content(≤3条), false=match_parent(>3条)
    private boolean isSimpleMode = false;

    public void setSimpleMode(boolean simpleMode) {
        this.isSimpleMode = simpleMode;
        if (simpleMode) {
            setBackgroundResource(R.drawable.bg_mini_capsule);
            setPadding(0, 0, 0, 0);
        } else {
            setBackgroundResource(R.drawable.bg_lane_line);
            setPadding(0, 0, 0, 0);
        }
    }

    public LaneLineView(Context context) {
        super(context);
        init();
    }

    public LaneLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LaneLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.bg_lane_line);
        setPadding(0, 0, 0, 0);
    }

    /**
     * 更新车道线数据
     *
     * @param driveWayJson EXTRA_DRIVE_WAY 的原始 JSON 字符串
     */
    public void updateLanes(String driveWayJson) {
        cachedDriveWayJson = driveWayJson;
        if (driveWayJson == null || driveWayJson.isEmpty()) {
            clear();
            return;
        }
        try {
            JSONObject root = new JSONObject(driveWayJson);
            boolean enabled = root.optBoolean("drive_way_enabled", false);
            if (!enabled) {
                if (getVisibility() == View.VISIBLE) {
                    return; // 保持上一包状态，避免闪烁
                }
                clear();
                return;
            }
            int size = root.optInt("drive_way_size", 0);
            JSONArray infoArray = root.optJSONArray("drive_way_info");
            if (size <= 0 || infoArray == null || infoArray.length() == 0) {
                if (getVisibility() == View.VISIBLE) {
                    return; // 忽略此空包，保持上一包状态，避免闪烁
                }
                clear();
                return;
            }

            // 收集并按 drive_way_number 排序
            ArrayList<JSONObject> lanes = new ArrayList<>();
            for (int i = 0; i < infoArray.length(); i++) {
                lanes.add(infoArray.getJSONObject(i));
            }
            Collections.sort(lanes, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    return a.optInt("drive_way_number", 0) - b.optInt("drive_way_number", 0);
                }
            });

            // 复用或重建子 View
            rebuildLanes(lanes);

            setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "解析车道线数据失败", e);
            clear();
        }
    }

    /**
     * 恢复缓存的车道线数据
     */
    public void restoreCached() {
        if (cachedDriveWayJson != null) {
            updateLanes(cachedDriveWayJson);
        }
    }

    public String getCachedDriveWayJson() {
        return cachedDriveWayJson;
    }

    /**
     * 清空并隐藏
     */
    public void clear() {
        removeAllViews();
        setVisibility(View.GONE);
    }

    private void rebuildLanes(ArrayList<JSONObject> lanes) {
        int targetCount = lanes.size();
        if (isSimpleMode) {
            removeAllViews();
            float scale = FloatingWindowManager.getInstance().getScale();
            int iconPx = Math.round(dpToPx(50) * scale);
            int marginPx = Math.round(dpToPx(1) * scale);
            for (int i = 0; i < targetCount; i++) {
                ImageView iv = new ImageView(getContext());
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                LayoutParams ivLp = new LayoutParams(iconPx, iconPx);
                ivLp.gravity = Gravity.CENTER_VERTICAL;
                ivLp.setMargins(marginPx, 0, marginPx, 0);
                addView(iv, ivLp);
            }
            updateLaneIcons(lanes);
            return;
        }

        float scale = FloatingWindowManager.getInstance().getScale();
        int baseIconPx = Math.round(dpToPx(LANE_ICON_BASE_DP) * scale);

        boolean compact = targetCount <= 3;
        int totalViews = targetCount + (targetCount - 1);

        // 布局策略变化或子 View 数量不匹配时需要重建
        if (getChildCount() == totalViews && compact == isCompactMode) {
            updateLaneIcons(lanes);
            return;
        }

        isCompactMode = compact;
        removeAllViews();

        // 动态切换宽度策略
        ViewGroup.LayoutParams selfLp = getLayoutParams();
        if (selfLp != null) {
            selfLp.width = compact ? ViewGroup.LayoutParams.WRAP_CONTENT
                    : ViewGroup.LayoutParams.MATCH_PARENT;
            setLayoutParams(selfLp);
        }

        int marginPx = dpToPx(LANE_MARGIN_DP);

        for (int i = 0; i < targetCount; i++) {
            // 分割线（非首个）
            if (i > 0) {
                View divider = new View(getContext());
                divider.setBackgroundColor(0x44FFFFFF);
                LayoutParams divLp = new LayoutParams(dpToPx(DIVIDER_WIDTH_DP), Math.round(dpToPx(DIVIDER_HEIGHT_DP) * scale));
                divLp.gravity = Gravity.CENTER_VERTICAL;
                divLp.setMargins(marginPx, 0, marginPx, 0);
                addView(divider, divLp);
            }

            // 车道图标
            ImageView iv = new ImageView(getContext());
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            LayoutParams ivLp;
            if (compact) {
                // ≤3条：固定图标尺寸，wrap_content 整体包裹
                ivLp = new LayoutParams(baseIconPx, baseIconPx);
                ivLp.setMargins(marginPx, 0, marginPx, 0);
            } else {
                // >3条：weight 均分，match_parent 整体拉满
                ivLp = new LayoutParams(0, baseIconPx, 1.0f);
                ivLp.setMargins(marginPx, 0, marginPx, 0);
            }
            ivLp.gravity = Gravity.CENTER_VERTICAL;
            addView(iv, ivLp);
        }

        updateLaneIcons(lanes);
    }

    private void updateLaneIcons(ArrayList<JSONObject> lanes) {
        int iconIndex = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                if (iconIndex < lanes.size()) {
                    String iconStr = lanes.get(iconIndex).optString("drive_way_lane_Back_icon", "");
                    int resId = getLaneDrawableRes(iconStr);
                    ((ImageView) child).setImageResource(resId);
                    child.setVisibility(View.VISIBLE);
                }
                iconIndex++;
            }
        }
    }

    private int getLaneDrawableRes(String iconNumber) {
        if (iconNumber == null || iconNumber.isEmpty()) return getFallbackRes();
        String resName = "lane_pdf_" + iconNumber;
        int resId = getContext().getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
        return resId != 0 ? resId : getFallbackRes();
    }

    private int getFallbackRes() {
        return getContext().getResources().getIdentifier(
                "lane_special_unknown", "drawable", getContext().getPackageName());
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getContext().getResources().getDisplayMetrics().density);
    }
}
