package com.navi.link;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TMC 路况进度条
 * 分段绘制不同颜色 + 当前位置三角标记
 */
public class TmcProgressBar extends View {

    private static final int COLOR_PASSED = 0xFF666666;       // 已驶过 - 灰色
    private static final int COLOR_SMOOTH = 0xFF1abf54;       // 畅通 - 绿色
    private static final int COLOR_SLOW = 0xFFFFD600;         // 缓行 - 黄色
    private static final int COLOR_CONGESTED = 0xFFFF1744;    // 拥堵 - 红色
    private static final int COLOR_SEVERE = 0xFFB71C1C;       // 严重拥堵 - 深红
    private static final int COLOR_BLUE = 0xFF2196F3;           // 未知/特殊 - 蓝色
    private static final int COLOR_CYAN = 0xFF007d5d;           // 状态5 - 青蓝
    private static final int COLOR_BACKGROUND = 0xFF333333;   // 背景 - 深灰

    private int totalDistance = 0;
    private int finishDistance = 0;
    private int[] segmentStatuses;  // 每段状态码
    private int[] segmentDistances; // 每段距离
    private int segmentCount = 0;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float barHeight;
    private float markerSize;
    private Bitmap markerIcon;

    public TmcProgressBar(Context context) {
        super(context);
        init();
    }

    public TmcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TmcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        markerPaint.setColor(Color.WHITE);
        markerPaint.setStyle(Paint.Style.FILL);
        barHeight = dpToPx(4);
        markerSize = dpToPx(10);
        markerIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.navigation_icon);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // 图标与进度条垂直居中重叠，总高度取两者较大值
        int height = (int) Math.max(markerSize, barHeight);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float centerY = height / 2f;
        float barTop = centerY - barHeight / 2;
        float barBottom = centerY + barHeight / 2;

        if (totalDistance <= 0 || segmentCount == 0) {
            // 无数据，画灰色底条
            barPaint.setColor(COLOR_BACKGROUND);
            canvas.drawRoundRect(0, barTop, width, barBottom, barHeight / 2, barHeight / 2, barPaint);
            return;
        }

        // 裁剪为圆角矩形，确保左右都有圆角
        Path clipPath = new Path();
        clipPath.addRoundRect(0, barTop, width, barBottom, barHeight / 2, barHeight / 2, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);

        // 绘制分段色条
        float x = 0;
        for (int i = 0; i < segmentCount; i++) {
            float segWidth = (segmentDistances[i] / (float) totalDistance) * width;
            // 最后一段强制画到边缘，避免浮点误差导致空隙
            float right = (i == segmentCount - 1) ? width : x + segWidth;
            if (right - x < 0.5f && i != segmentCount - 1) {
                x += segWidth;
                continue;
            }
            barPaint.setColor(getStatusColor(segmentStatuses[i]));
            canvas.drawRect(x, barTop, right, barBottom, barPaint);
            x += segWidth;
        }

        canvas.restore();

        // 绘制当前位置图标标记
        if (finishDistance >= 0 && finishDistance <= totalDistance) {
            float markerX = (finishDistance / (float) totalDistance) * width;
            // 钳制在 [margin, width-margin] 防止图标被裁切
            float margin = markerSize / 2;
            markerX = Math.max(margin, Math.min(markerX, width - margin));

            if (markerIcon != null && !markerIcon.isRecycled()) {
                float iconSize = markerSize;
                float left = markerX - iconSize / 2;
                float top = centerY - iconSize / 2;
                markerPaint.setColor(0xFFFFFFFF);
                canvas.drawBitmap(markerIcon, null,
                        new RectF(left, top, left + iconSize, top + iconSize),
                        markerPaint);
            }
        }
    }

    private int getStatusColor(int status) {
        switch (status) {
            case 10: return COLOR_PASSED;      // 已驶过 - 灰
            case 0:  return COLOR_BLUE;        // 特殊 - 蓝
            case 1:  return COLOR_SMOOTH;      // 畅通 - 绿
            case 2:  return COLOR_SLOW;        // 缓行 - 黄
            case 3:  return COLOR_CONGESTED;   // 拥堵 - 红
            case 4:  return COLOR_SEVERE;      // 严重拥堵 - 深红
            case 5:  return COLOR_CYAN;        // 状态5 - 青蓝
            default: return COLOR_BACKGROUND;  // 未知 - 深灰
        }
    }

    /**
     * 更新 TMC 数据
     * @param tmcJson EXTRA_TMC_SEGMENT 的 JSON 字符串
     */
    public void updateTmcData(String tmcJson) {
        try {
            JSONObject root = new JSONObject(tmcJson);
            totalDistance = root.optInt("total_distance", 0);
            finishDistance = root.optInt("finish_distance", 0);

            JSONArray tmcInfo = root.optJSONArray("tmc_info");
            if (tmcInfo != null && tmcInfo.length() > 0) {
                segmentCount = tmcInfo.length();
                segmentStatuses = new int[segmentCount];
                segmentDistances = new int[segmentCount];

                for (int i = 0; i < segmentCount; i++) {
                    JSONObject seg = tmcInfo.getJSONObject(i);
                    segmentStatuses[i] = seg.optInt("tmc_status", 0);
                    segmentDistances[i] = seg.optInt("tmc_segment_distance", 0);
                }
            }
            invalidate();
        } catch (Exception e) {
            // JSON 解析失败，忽略
        }
    }

    /**
     * 清除 TMC 数据，重置进度条
     */
    public void clear() {
        totalDistance = 0;
        finishDistance = 0;
        segmentStatuses = null;
        segmentDistances = null;
        segmentCount = 0;
        invalidate();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
