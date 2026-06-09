package com.navi.link;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 仪表盘页面
 * 全屏显示速度仪表盘，数据来自高德广播
 */
public class DashboardActivity extends AppCompatActivity {

    private ImageView ivSpeedometer;
    private ImageView ivCar;
    private ImageView ivBg;
    private TextView tvSpeed;
    private TextView tvSpeedUnit;
    private TextView tvTime;
    private TextView tvEta;
    private TextView tvDistance;
    private View rootDashboard;

    // 导航信息
    private ImageView ivTurnIcon;
    private TextView tvDistanceNum;
    private TextView tvDistanceUnit;
    private TextView tvAction;
    private TextView tvRoadName;
    // 红绿灯
    private ImageView ivLightIcon;
    private ImageView ivLightArrow;
    private TextView tvLightTime;
    private View llTrafficLightGroup;

    private boolean isNight = true;
    private int currentSpeed = 0;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private BroadcastReceiver amapReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏沉浸
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_dashboard);
        initViews();

        // 从 FWM 获取当前昼夜状态
        FloatingWindowManager fwm = FloatingWindowManager.getInstance();
        if (fwm != null) {
            isNight = fwm.isNightMode();
        }
        applyDayNight();

        // 启动时钟刷新
        updateTime();
        timeHandler.postDelayed(timeRunnable, 1000);

        // 注册广播接收器
        registerAmapReceiver();
    }

    private void initViews() {
        ivSpeedometer = findViewById(R.id.iv_speedometer);
        ivCar = findViewById(R.id.iv_car);
        ivBg = findViewById(R.id.iv_bg);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSpeedUnit = findViewById(R.id.tv_speed_unit);
        tvTime = findViewById(R.id.tv_time);
        tvEta = findViewById(R.id.tv_eta);
        tvDistance = findViewById(R.id.tv_distance);
        rootDashboard = findViewById(R.id.root_dashboard);

        // 导航信息
        ivTurnIcon = findViewById(R.id.iv_turn_icon);
        tvDistanceNum = findViewById(R.id.tv_distance_num);
        tvDistanceUnit = findViewById(R.id.tv_distance_unit);
        tvAction = findViewById(R.id.tv_action);
        tvRoadName = findViewById(R.id.tv_road_name);
        // 红绿灯
        ivLightIcon = findViewById(R.id.iv_light_icon);
        ivLightArrow = findViewById(R.id.iv_light_arrow);
        tvLightTime = findViewById(R.id.tv_light_time);
        llTrafficLightGroup = findViewById(R.id.ll_traffic_light_group);
        if (llTrafficLightGroup != null) llTrafficLightGroup.setVisibility(View.GONE);
    }

    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            timeHandler.postDelayed(this, 10000); // 每10秒刷新一次
        }
    };

    private void updateTime() {
        tvTime.setText(timeFormat.format(new Date()));
    }

    // ==================== 广播接收 ====================

    private void registerAmapReceiver() {
        amapReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!"AUTONAVI_STANDARD_BROADCAST_SEND".equals(intent.getAction())) return;

                int keyType = intent.getIntExtra("KEY_TYPE", 0);

                if (keyType == 60073) {
                    // 红绿灯数据
                    handleTrafficLight(intent);
                } else if (keyType == 10019) {
                    // 昼夜模式切换
                    int extraState = intent.getIntExtra("EXTRA_STATE", -1);
                    if (extraState == 37 || extraState == 38) {
                        isNight = (extraState == 38);
                        applyDayNight();
                    }
                } else if (keyType == 10001) {
                    // 导航/巡航信息
                    int icon = intent.getIntExtra("NEW_ICON", 0);
                    if (icon == 0) icon = intent.getIntExtra("ICON", 0);

                    // 速度
                    int speed = intent.getIntExtra("CUR_SPEED", -1);
                    if (speed < 0) speed = 0;
                    updateSpeed(speed);

                    // 导航信息
                    if (icon != 0) {
                        int turnRes = getTurnIconRes(icon);
                        if (ivTurnIcon != null && turnRes != 0) ivTurnIcon.setImageResource(turnRes);

                        // 距离拆分
                        String segRemainDis = intent.getStringExtra("SEG_REMAIN_DIS_AUTO");
                        if (segRemainDis == null) segRemainDis = "0米";
                        String disUnit = "公里";
                        if (segRemainDis.endsWith("公里")) {
                            segRemainDis = segRemainDis.replace("公里", "");
                        } else {
                            disUnit = "米";
                            if (segRemainDis.endsWith("米")) segRemainDis = segRemainDis.replace("米", "");
                        }
                        if (tvDistanceNum != null) tvDistanceNum.setText(segRemainDis);
                        if (tvDistanceUnit != null) tvDistanceUnit.setText(disUnit);

                        // 路名
                        String nextRoadName = intent.getStringExtra("NEXT_ROAD_NAME");
                        if (nextRoadName == null) nextRoadName = intent.getStringExtra("CUR_ROAD_NAME");
                        if (tvRoadName != null && nextRoadName != null) tvRoadName.setText(nextRoadName);
                    }

                    // ETA
                    String etaText = intent.getStringExtra("ETA_TEXT");
                    if (etaText != null && !etaText.isEmpty()) {
                        tvEta.setText(etaText);
                    }

                    // 剩余距离
                    String routeDis = intent.getStringExtra("ROUTE_REMAIN_DIS_AUTO");
                    if (routeDis != null && !routeDis.isEmpty()) {
                        tvDistance.setText(routeDis);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter("AUTONAVI_STANDARD_BROADCAST_SEND");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(amapReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(amapReceiver, filter);
        }
    }

    // ==================== 速度更新 ====================

    private void updateSpeed(int speed) {
        if (speed == currentSpeed) return;
        currentSpeed = speed;
        tvSpeed.setText(String.valueOf(speed));

        // 映射速度到表盘图片
        int clampedSpeed = Math.max(1, Math.min(180, speed));
        String prefix = isNight ? "sd_" : "sd_";
        String suffix = isNight ? "_drak" : "";
        String resName = prefix + clampedSpeed + suffix;

        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
        if (resId != 0) {
            ivSpeedometer.setImageResource(resId);
        }
    }

    // ==================== 转向图标映射 ====================

    private int getTurnIconRes(int icon) {
        switch (icon) {
            case 2: return R.mipmap.ic_navi_left;
            case 3: return R.mipmap.ic_navi_right;
            case 4: return R.mipmap.ic_navi_left_d;
            case 5: return R.mipmap.ic_navi_right_d;
            case 8: return R.mipmap.ic_navi_u_turn;
            case 9: return R.mipmap.ic_navi_straight;
            case 10: return R.mipmap.ic_navi_mid;
            case 11: return R.mipmap.ic_navi_in_dao;
            case 12: return R.mipmap.ic_navi_en_dao;
            case 15: return R.mipmap.ic_navi_end;
            default: return R.mipmap.ic_navi_straight;
        }
    }

    // ==================== 红绿灯 ====================

    private void handleTrafficLight(Intent intent) {
        int status = intent.getIntExtra("trafficLightStatus", 0);
        int dir = intent.getIntExtra("dir", 4);
        int countdown = intent.getIntExtra("redLightCountDownSeconds", 0);

        if (countdown <= 0) {
            if (llTrafficLightGroup != null) llTrafficLightGroup.setVisibility(View.GONE);
            return;
        }

        if (llTrafficLightGroup != null) llTrafficLightGroup.setVisibility(View.VISIBLE);
        if (ivLightIcon != null) {
            if (status == 4) ivLightIcon.setImageResource(R.drawable.ic_traffic_light_green);
            else if (status == 1) ivLightIcon.setImageResource(R.drawable.ic_traffic_light_red);
            else ivLightIcon.setImageResource(R.drawable.ic_traffic_light_yellow);
        }
        if (ivLightArrow != null) {
            if (dir == 1) ivLightArrow.setImageResource(R.mipmap.light_left);
            else if (dir == 2) ivLightArrow.setImageResource(R.mipmap.light_right);
            else if (dir == 3) ivLightArrow.setImageResource(R.mipmap.light_u_turn);
            else ivLightArrow.setImageResource(R.mipmap.light_straight);
        }
        if (tvLightTime != null) tvLightTime.setText(String.valueOf(countdown));
    }

    // ==================== 昼夜切换 ====================

    private void applyDayNight() {
        if (isNight) {
            // 夜间模式
            rootDashboard.setBackgroundColor(0xFF1A1A2E);
            tvSpeed.setTextColor(0xFFFFFFFF);
            tvSpeedUnit.setTextColor(0xAAFFFFFF);
            tvTime.setTextColor(0xFFFFFFFF);
            tvEta.setTextColor(0xAAFFFFFF);
            tvDistance.setTextColor(0xAAFFFFFF);
            // 导航信息颜色
            if (tvDistanceNum != null) tvDistanceNum.setTextColor(0xFFFFFFFF);
            if (tvDistanceUnit != null) tvDistanceUnit.setTextColor(0xFFFFFFFF);
            if (tvAction != null) tvAction.setTextColor(0xFF888888);
            if (tvRoadName != null) tvRoadName.setTextColor(0xFFFFFFFF);
            if (ivTurnIcon != null) ivTurnIcon.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
            ivCar.setImageResource(R.drawable.iv_car_drak);
            ivBg.setImageResource(R.drawable.iv_rev_bg_drak);
        } else {
            // 日间模式
            rootDashboard.setBackgroundColor(0xFFF0F0F5);
            tvSpeed.setTextColor(0xFF222222);
            tvSpeedUnit.setTextColor(0xFF888888);
            tvTime.setTextColor(0xFF222222);
            tvEta.setTextColor(0xFF888888);
            tvDistance.setTextColor(0xFF888888);
            // 导航信息颜色
            if (tvDistanceNum != null) tvDistanceNum.setTextColor(0xFF222222);
            if (tvDistanceUnit != null) tvDistanceUnit.setTextColor(0xFF222222);
            if (tvAction != null) tvAction.setTextColor(0xFF999999);
            if (tvRoadName != null) tvRoadName.setTextColor(0xFF222222);
            if (ivTurnIcon != null) ivTurnIcon.setImageTintList(ColorStateList.valueOf(0xFF222222));
            ivCar.setImageResource(R.drawable.iv_car);
            ivBg.setImageResource(R.drawable.iv_rev_bg);
        }

        // 刷新表盘图片
        updateSpeed(currentSpeed);
    }

    // ==================== 生命周期 ====================

    @Override
    protected void onResume() {
        super.onResume();
        updateTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeRunnable);
        if (amapReceiver != null) {
            try {
                unregisterReceiver(amapReceiver);
            } catch (Exception ignored) {
            }
        }
    }
}
