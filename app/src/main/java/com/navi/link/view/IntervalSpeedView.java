package com.navi.link.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

import com.navi.link.R;

public class IntervalSpeedView extends LinearLayout {

    private View container;
    private TextView tvCurSpeed;
    private TextView tvAvgVal;
    private TextView tvAvgLabel;
    private TextView tvDistVal;
    private TextView tvDistLabel;
    private TextView tvLimit;

    public IntervalSpeedView(Context context) {
        super(context);
        init();
    }

    public IntervalSpeedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IntervalSpeedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_interval_speed, this, true);
        container = findViewById(R.id.ll_interval_container);
        tvCurSpeed = findViewById(R.id.tv_interval_cur_speed);
        tvAvgVal = findViewById(R.id.tv_interval_avg_val);
        tvAvgLabel = findViewById(R.id.tv_interval_avg_label);
        tvDistVal = findViewById(R.id.tv_interval_dist_val);
        tvDistLabel = findViewById(R.id.tv_interval_dist_label);
        tvLimit = findViewById(R.id.tv_interval_limit);
        
        setVisibility(View.GONE);
    }

    /**
     * 实时更新当前车速
     */
    public void updateCurrentSpeed(int curSpeed) {
        if (tvCurSpeed != null) {
            tvCurSpeed.setText(String.valueOf(curSpeed));
        }
    }

    /**
     * 进入区间测速前：前方 452m 有区间测速
     */
    public void setApproachingState(int curSpeed, String distanceToStart, int limitSpeed) {
        setVisibility(View.VISIBLE);
        if (container != null) container.setVisibility(View.VISIBLE);
        
        if (tvCurSpeed != null) {
            tvCurSpeed.setText(String.valueOf(curSpeed));
        }
        if (tvAvgVal != null) {
            tvAvgVal.setText("--");
            tvAvgVal.setTextColor(Color.WHITE);
        }
        if (tvAvgLabel != null) {
            tvAvgLabel.setText("即将进入");
        }
        if (tvDistVal != null) {
            tvDistVal.setText(distanceToStart != null ? distanceToStart : "--");
            tvDistVal.setTextColor(Color.WHITE);
        }
        if (tvDistLabel != null) {
            tvDistLabel.setText("距离起点");
        }
        if (tvLimit != null) {
            tvLimit.setText(limitSpeed > 0 ? String.valueOf(limitSpeed) : "--");
        }
    }

    /**
     * 已进入区间测速：显示均速和剩余距离
     */
    public void setInsideState(int curSpeed, int limitSpeed, int avgSpeed, String distanceToEnd) {
        setVisibility(View.VISIBLE);
        if (container != null) container.setVisibility(View.VISIBLE);
        
        if (tvCurSpeed != null) {
            tvCurSpeed.setText(String.valueOf(curSpeed));
        }
        if (tvAvgVal != null) {
            tvAvgVal.setText(String.valueOf(avgSpeed));
            // 超速警示红
            if (limitSpeed > 0 && avgSpeed > limitSpeed) {
                tvAvgVal.setTextColor(Color.parseColor("#FF4444"));
            } else {
                tvAvgVal.setTextColor(Color.WHITE);
            }
        }
        if (tvAvgLabel != null) {
            tvAvgLabel.setText("平均车速");
        }
        if (tvDistVal != null) {
            String distStr = (distanceToEnd != null && !distanceToEnd.isEmpty()) ? distanceToEnd : "--";
            tvDistVal.setText(distStr);
            tvDistVal.setTextColor(Color.WHITE);
        }
        if (tvDistLabel != null) {
            tvDistLabel.setText("剩余里程");
        }
        if (tvLimit != null) {
            tvLimit.setText(limitSpeed > 0 ? String.valueOf(limitSpeed) : "--");
        }
    }

    public void hide() {
        setVisibility(View.GONE);
        if (container != null) container.setVisibility(View.GONE);
    }
}
