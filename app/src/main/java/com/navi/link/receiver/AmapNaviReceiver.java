package com.navi.link.receiver;
import com.navi.link.R;
import com.navi.link.BuildConfig;
import com.navi.link.activity.*;
import com.navi.link.delegate.*;
import com.navi.link.window.*;
import com.navi.link.view.*;
import com.navi.link.receiver.*;
import com.navi.link.service.*;
import com.navi.link.utils.*;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;

public class AmapNaviReceiver extends BroadcastReceiver {

    private static final String TAG = "AmapNavi";
    private boolean isLog = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"AUTONAVI_STANDARD_BROADCAST_SEND".equals(intent.getAction())) return;

        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager == null || !manager.isShowing()) return;

        int keyType = intent.getIntExtra("KEY_TYPE", 0);


        Bundle extras = intent.getExtras();
        if (extras != null && isLog) {
            // 打印所有原始数据
            Log.d(TAG, "========== 🚥所有原始数据包==========");
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "Key: " + key + " | Value: " + value + " | Type: " + (value != null ? value.getClass().getSimpleName() : "null"));
            }
            Log.d(TAG, "==========================================================");
        }
        if (keyType == 12110) {
            manager.resetWatchdog();
            if (manager.getCurrentMode() == FloatingWindowManager.MODE_NAVI) {
                manager.resetNaviTimeout();
            }
            int startDist = getIntSafe(intent, "START_DISTANCE", -1);
            String startDistText = intent.getStringExtra("START_DISTANCE_TEXT");
            int avgSpeed = getIntSafe(intent, "AVERAGE_SPEED", 0);
            String endDistText = intent.getStringExtra("END_DISTANCE_TEXT");
            int limitSpeed = getIntSafe(intent, "LIMITED_SPEED", 0);
            manager.updateIntervalSpeed(startDist, startDistText, avgSpeed, endDistText, limitSpeed);
            return;
        }

        if (keyType == 60073) {
            // 红绿灯数据也视为有活动数据，重置 5秒 看门狗
            manager.resetWatchdog();
            // 红绿灯数据
            handleTrafficLight(intent, manager);
            if (manager.getCurrentMode() == FloatingWindowManager.MODE_NAVI) {
                manager.resetNaviTimeout();
            }
            return;
        }

        if (keyType == 13011) {
            // TMC 路况数据
            String tmcSegment = intent.getStringExtra("EXTRA_TMC_SEGMENT");
            if (tmcSegment != null) {
                manager.updateTmcData(tmcSegment);
            }
            return;
        }

        if (keyType == 13012) {
            // 车道线数据
            String driveWay = intent.getStringExtra("EXTRA_DRIVE_WAY");
            if (driveWay != null) {
                manager.updateLaneLines(driveWay);
            }
            return;
        }

        // 昼夜模式切换及前后台、结束状态广播
        if (keyType == 10019) {
            int extraState = getIntSafe(intent, "EXTRA_STATE", -1);
            if (extraState == 37 || extraState == 38) {
                boolean isNight = (extraState == 38);
                manager.onDayNightChanged(isNight);
            } else if (extraState == 3 || extraState == 4) {
                boolean isForeground = (extraState == 3);
                manager.onAmapForegroundChanged(isForeground);
            } else if (extraState == 9) {
                manager.onNavigationEnded();
            } else if (extraState == 25) {
                manager.onCruiseEnded();
            } else if (extraState == 40) {
                if (manager.isActive() && !manager.isNavigationJustEnded() && !manager.isCruiseJustEnded()) {
                    manager.resetWatchdog();
                }
            }

            // 路口放大图状态（EXTRA_CROSS_MAP = 1 表示有路口放大图）
            if (intent.hasExtra("EXTRA_CROSS_MAP")) {
                int crossMap = getIntSafe(intent, "EXTRA_CROSS_MAP", 0);
                manager.updateCrossMapStatus(crossMap);
            }

            return;
        }

        if (keyType == 10001) {
            // 导航或巡航信息
            if (manager.isNavigationJustEnded() || manager.isCruiseJustEnded()) {
                return;
            }
            manager.resetWatchdog();

            int icon = getIntSafe(intent, "NEW_ICON", 0);
            if (icon == 0) {
                icon = getIntSafe(intent, "ICON", 0);
            }

            if (icon != 0) {
                // 有转向图标，说明在导航模式
                manager.switchToNaviMode();
                handleNaviInfo(intent, manager);
            } else {
                if (manager.getCurrentMode() == FloatingWindowManager.MODE_NAVI || manager.isNaviWindowActive()) {
                    // 导航模式但无新icon，或当前依然是导航窗口，立即切换到巡航模式
                    manager.switchToCruiseMode();
                }
                // 巡航模式：只有巡航启用时才处理数据
                if (manager.isCruiseEnabled()) {
                    handleCruiseInfo(intent, manager);
                }
            }
        }
    }

    private void handleTrafficLight(Intent intent, FloatingWindowManager manager) {
        if (manager.getCurrentMode() == FloatingWindowManager.MODE_NAVI) {
            int status = getIntSafe(intent, "trafficLightStatus", 0);
            int dir = getIntSafe(intent, "dir", 4);
            int countdown = getIntSafe(intent, "redLightCountDownSeconds", 0);
            manager.updateTrafficLight(status, dir, countdown);
            return;
        }
        // 巡航模式红绿灯数据
        String lightsData = intent.getStringExtra("lightsData");
        if (lightsData != null) {
            try {
                manager.updateCruiseTrafficLights(new JSONArray(lightsData));
            } catch (Exception e) {
                Log.e(TAG, "解析巡航红绿灯数据失败", e);
            }
        }
    }

    private void handleNaviInfo(Intent intent, FloatingWindowManager manager) {
        String segRemainDis = intent.getStringExtra("SEG_REMAIN_DIS_AUTO");
        String routeRemainDis = intent.getStringExtra("ROUTE_REMAIN_DIS_AUTO");
        String routeRemainTime = intent.getStringExtra("ROUTE_REMAIN_TIME_AUTO");
        String etaText = intent.getStringExtra("ETA_TEXT");
        String nextRoadName = intent.getStringExtra("NEXT_ROAD_NAME");
        String curRoadName = intent.getStringExtra("CUR_ROAD_NAME");

        int icon = getIntSafe(intent, "NEW_ICON", 0);
        if (icon == 0) {
            icon = getIntSafe(intent, "ICON", 0);
        }

        // 安全兜底防空指针
        if (segRemainDis == null) segRemainDis = "0米";
        if (routeRemainDis == null) routeRemainDis = "0公里";
        if (routeRemainTime == null) routeRemainTime = "0分钟";
        if (nextRoadName == null) nextRoadName = curRoadName;
        if (nextRoadName == null) nextRoadName = "未知道路";
        String roadName = nextRoadName;
        String eta = etaText != null ? etaText : "";

        // 智能拆分距离与单位
        String disUnit = "公里";
        if (segRemainDis.endsWith("公里")) {
            segRemainDis = segRemainDis.replace("公里", "");
        } else {
            disUnit = "米";
            if (segRemainDis.endsWith("米")) {
                segRemainDis = segRemainDis.replace("米", "");
            }
        }
        String disNum = segRemainDis;

        // 拼装底部 Summary 文本
        String summaryStr = routeRemainDis + " · " + routeRemainTime;

        // 进度条计算
        int routeRemainDisInt = getIntSafe(intent, "ROUTE_REMAIN_DIS", 0);
        int routeAllDis = getIntSafe(intent, "ROUTE_ALL_DIS", 1);
        int progressPercentage = routeAllDis > 0
                ? (int) ((1.0f - (float) routeRemainDisInt / routeAllDis) * 100)
                : 0;

        int curSpeed = getIntSafe(intent, "CUR_SPEED", 0);
        int limitedSpeed = getIntSafe(intent, "LIMITED_SPEED", 0);
        int cameraDist = getIntSafe(intent, "CAMERA_DIST", 0);
        int cameraSpeed = getIntSafe(intent, "CAMERA_SPEED", 0);
        int cameraType = getIntSafe(intent, "CAMERA_TYPE", 0);
        String endPoiName = intent.getStringExtra("endPOIName");
        int totalLightNum = getIntSafe(intent, "TRAFFIC_LIGHT_NUM", 0);
        int remainLightNum = getIntSafe(intent, "routeRemainTrafficLightNum", 0);
        int carDirection = getIntSafe(intent, "CAR_DIRECTION", -1);

        manager.updateNaviInfo(icon, disNum, disUnit, "进入", roadName,
                summaryStr, eta, progressPercentage, curSpeed,
                limitedSpeed, cameraType, cameraDist, cameraSpeed,
                endPoiName, totalLightNum, remainLightNum, curRoadName, carDirection);

        // 出口信息
        String exitName = intent.getStringExtra("EXIT_NAME_INFO");
        String exitDirection = intent.getStringExtra("EXIT_DIRECTION_INFO");
        manager.updateExitInfo(exitName, exitDirection);

        // 服务区信息
        String sapaName = intent.getStringExtra("SAPA_NAME");
        String sapaDist = intent.getStringExtra("SAPA_DIST_AUTO");
        int sapaType = getIntSafe(intent, "SAPA_TYPE", 0);
        String nextSapaName = intent.getStringExtra("NEXT_SAPA_NAME");
        String nextSapaDist = intent.getStringExtra("NEXT_SAPA_DIST_AUTO");
        int nextSapaType = getIntSafe(intent, "NEXT_SAPA_TYPE", 0);
        manager.updateSapaInfo(sapaName, sapaDist, sapaType, nextSapaName, nextSapaDist, nextSapaType);
    }

    private void handleCruiseInfo(Intent intent, FloatingWindowManager manager) {
        int curSpeed = getIntSafe(intent, "CUR_SPEED", 0);
        String curRoadName = intent.getStringExtra("CUR_ROAD_NAME");
        int cameraSpeed = getIntSafe(intent, "CAMERA_SPEED", 0);
        int cameraDist = getIntSafe(intent, "CAMERA_DIST", 0);
        int cameraType = getIntSafe(intent, "CAMERA_TYPE", 0);
        int carDirection = getIntSafe(intent, "CAR_DIRECTION", -1);
        if (curRoadName == null) curRoadName = "未知道路";
        manager.updateCruiseInfo(curSpeed, curRoadName, cameraType, cameraSpeed, cameraDist, carDirection);
    }

    private int getIntSafe(Intent intent, String key, int defaultValue) {
        if (intent == null) return defaultValue;
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(key)) return defaultValue;
        Object val = extras.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return (int) Float.parseFloat((String) val);
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }
}
