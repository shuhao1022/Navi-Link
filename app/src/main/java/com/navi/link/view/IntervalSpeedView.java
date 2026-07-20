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
    private TextView tvTitle;
    private TextView tvInfo;
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
        tvTitle = findViewById(R.id.tv_interval_title);
        tvInfo = findViewById(R.id.tv_interval_info);
        tvLimit = findViewById(R.id.tv_interval_limit);
        
        setVisibility(View.GONE);
    }

    /**
     * 进入区间测速前：前方 100m 有区间测速
     */
    public void setApproachingState(String distanceToStart, int limitSpeed) {
        setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);
        
        tvTitle.setText("前方区间测速");
        tvTitle.setTextColor(Color.parseColor("#FFD700")); // Yellow-ish
        
        tvInfo.setText(distanceToStart != null ? distanceToStart : "");
        tvInfo.setTextColor(Color.WHITE);
        
        tvLimit.setText(limitSpeed > 0 ? String.valueOf(limitSpeed) : "--");
    }

    /**
     * 已进入区间测速：显示均速和剩余距离
     */
    public void setInsideState(int limitSpeed, int avgSpeed, String distanceToEnd) {
        setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);
        
        tvTitle.setText("区间测速中");
        tvTitle.setTextColor(Color.parseColor("#00FF00")); // Green-ish
        
        StringBuilder info = new StringBuilder();
        info.append("均速 ").append(avgSpeed);
        if (distanceToEnd != null && !distanceToEnd.isEmpty()) {
            info.append(" | 剩余 ").append(distanceToEnd);
        }
        tvInfo.setText(info.toString());
        
        // 超速标红均速部分
        if (limitSpeed > 0 && avgSpeed > limitSpeed) {
            tvInfo.setTextColor(Color.parseColor("#FF4444")); // Red
        } else {
            tvInfo.setTextColor(Color.WHITE);
        }
        
        tvLimit.setText(limitSpeed > 0 ? String.valueOf(limitSpeed) : "--");
    }

    public void hide() {
        setVisibility(View.GONE);
        container.setVisibility(View.GONE);
    }
}
