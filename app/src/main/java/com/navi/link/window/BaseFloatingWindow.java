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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONArray;

public abstract class BaseFloatingWindow {
    protected final Context context;
    protected final View floatingView;
    protected final SharedPreferences sp;

    // 透明主题文字颜色常量
    protected static final int TEXT_PRIMARY_LIGHT = 0xFF1a1a1a;
    protected static final int TEXT_SECONDARY_LIGHT = 0xFF333333;
    protected static final int TEXT_HINT_LIGHT = 0xFF999999;
    protected static final int TEXT_PRIMARY_DARK = 0xFFFFFFFF;
    protected static final int TEXT_SECONDARY_DARK = 0xBBFFFFFF;
    protected static final int TEXT_HINT_DARK = 0xFF888888;

    protected boolean isNightMode = true;
    protected boolean isClusterWindow = false;

    public BaseFloatingWindow(Context context, View floatingView) {
        this.context = context;
        this.floatingView = floatingView;
        this.sp = context.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        initViews();
    }

    protected abstract void initViews();

    public abstract void updateNaviInfo(
            int icon, String disNum, String disUnit, String actionStr,
            String roadName, String summaryStr, String eta,
            int progress, int curSpeed,
            int limitedSpeed, int cameraType, int cameraDist, int cameraSpeed,
            String endPoiName, int totalLightNum, int remainLightNum,
            String curRoadName, int carDirection
    );

    public abstract void updateCruiseInfo(int speed, String roadName, int cameraType, int cameraSpeed, int cameraDist, int carDirection);

    public abstract void updateTrafficLight(int status, int dir, int countdown);

    public abstract void updateLaneLines(String driveWayJson);

    public abstract void updateExitInfo(String exitName, String exitDirection);

    public abstract void applyThemeColor(int themeColor);

    public abstract void applyDayNightTextColors(boolean isNightMode);

    public abstract void resetToDefaultTextColors();

    // 销毁窗口时的清理工作
    public void onDestroy() {}

    // 更新TMC路况条
    public void updateTmcData(String tmcJson) {}

    // 更新最近的两个服务区信息
    public void updateSapaInfo(String sapaName, String sapaDist, int sapaType, String nextSapaName, String nextSapaDist, int nextSapaType) {}

    // ======================== 通用辅助方法 ========================

    protected boolean isDarkThemeColor(int color) {
        return ((color >> 16) & 0xFF) * 0.299
                + ((color >> 8) & 0xFF) * 0.587
                + (color & 0xFF) * 0.114 < 100;
    }

    protected int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    protected boolean disNumIsNow(String disNum) {
        return "现在".equals(disNum);
    }

    protected boolean shouldBlinkTurnIcon(String disNum, String disUnit) {
        if (disNum == null || disNum.isEmpty()) return false;
        if ("现在".equals(disNum)) return true;
        try {
            String cleanNum = disNum.replaceAll("[^0-9.]", "");
            if (cleanNum.isEmpty()) return false;
            float val = Float.parseFloat(cleanNum);
            if (disUnit != null && (disUnit.contains("公里") || disUnit.contains("km") || disUnit.contains("KM"))) {
                val = val * 1000;
            }
            return val < 500;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class EtaInfo {
        public final String time;
        public final int dayOffset;

        public EtaInfo(String time, int dayOffset) {
            this.time = time;
            this.dayOffset = dayOffset;
        }
    }

    public static EtaInfo parseEta(String etaText) {
        if (etaText == null || etaText.isEmpty()) {
            return new EtaInfo("", 0);
        }

        // 1. 正则提取时间 (如 "18:06")
        String time = "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{1,2}:\\d{2}").matcher(etaText);
        if (matcher.find()) {
            time = matcher.group();
        } else {
            return new EtaInfo(etaText.replace("预计", "").replace("到达", "").replace("到", "").trim(), 0);
        }

        // 2. 匹配跨天天数
        int dayOffset = 0;
        if (etaText.contains("明天") || etaText.contains("次日")) {
            dayOffset = 1;
        } else if (etaText.contains("后天")) {
            dayOffset = 2;
        } else if (etaText.contains("周") || etaText.contains("星期")) {
            int targetDay = getDayOfWeekNum(etaText);
            if (targetDay > 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int currentDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
                int currentDay = (currentDayOfWeek == java.util.Calendar.SUNDAY) ? 7 : (currentDayOfWeek - 1);
                dayOffset = targetDay - currentDay;
                if (dayOffset < 0) {
                    dayOffset += 7;
                }
            }
        }

        return new EtaInfo(time, dayOffset);
    }

    private static int getDayOfWeekNum(String text) {
        if (text.contains("一")) return 1;
        if (text.contains("二")) return 2;
        if (text.contains("三")) return 3;
        if (text.contains("四")) return 4;
        if (text.contains("五")) return 5;
        if (text.contains("六")) return 6;
        if (text.contains("日") || text.contains("天")) return 7;
        return 0;
    }

    protected CharSequence formatEtaSpannable(String etaText) {
        if (etaText == null || etaText.isEmpty()) return "";
        EtaInfo etaInfo = parseEta(etaText);
        if (etaInfo.dayOffset > 0) {
            String offsetStr = "+" + etaInfo.dayOffset;
            String rawText = etaInfo.time + offsetStr;
            android.text.SpannableString spannable = new android.text.SpannableString(rawText);
            int start = etaInfo.time.length();
            int end = start + offsetStr.length();
            spannable.setSpan(new android.text.style.SuperscriptSpan(), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new android.text.style.RelativeSizeSpan(0.6f), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        } else {
            String text = etaInfo.time;
            if (text.isEmpty()) {
                text = etaText.replace("预计", "").replace("到达", "").trim();
            }
            if (!text.isEmpty() && !text.endsWith("到")) {
                text = text + "到";
            }
            return text;
        }
    }

    protected String formatEta(String eta) {
        return formatEtaSpannable(eta).toString();
    }

    protected String getDirectionText(int degrees) {
        String[] dirs = {"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
        int index = (int) Math.round(degrees / 45.0) % 8;
        if (index < 0) index += 8;
        return dirs[index];
    }

    protected int getTurnIconRes(int icon) {
        switch (icon) {
            case 2: return R.mipmap.sou2_night_a530;
            case 3: return R.mipmap.sou3_night_a530;
            case 4: return R.mipmap.sou4_night_a530;
            case 5: return R.mipmap.sou5_night_a530;
            case 6: return R.mipmap.sou6_night_a530;
            case 7: return R.mipmap.sou7_night_a530;
            case 8: return R.mipmap.sou8_night_a530;
            case 9: return R.mipmap.sou9_night_a530;
            case 10: return R.mipmap.sou10_night_a530;
            case 11: return R.mipmap.sou11_night_a530;
            case 12: return R.mipmap.sou12_night_a530;
            case 13: return R.mipmap.sou13_night_a530;
            case 14: return R.mipmap.sou14_night_a530;
            case 15: return R.mipmap.sou15_night_a530;
            case 16: return R.mipmap.sou16_night_a530;
            case 17: return R.mipmap.sou17_night_a530;
            case 18: return R.mipmap.sou18_night_a530;
            case 19: return R.mipmap.sou19_night_a530;
            case 20: return R.mipmap.sou20_night_a530;
            default: return R.mipmap.sou20_night_a530;
        }
    }

    protected int getNaviLightIconRes(int status) {
        if (status == 4) return R.drawable.ic_traffic_light_green;
        if (status == 1) return R.drawable.ic_traffic_light_red;
        return R.drawable.ic_traffic_light_yellow;
    }

    protected int getNaviLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_right;
        if (dir == 3) return R.mipmap.light_u_turn;
        if (dir == 4) return R.mipmap.light_straight;
        return R.mipmap.light_straight;
    }

    protected int getCruiseLightIconRes(int status) {
        if (status == 1) return R.drawable.ic_traffic_light_green;
        if (status == 0) return R.drawable.ic_traffic_light_red;
        return R.drawable.ic_traffic_light_yellow;
    }

    protected int getCruiseLightDirRes(int dir) {
        if (dir == 1) return R.mipmap.light_left;
        if (dir == 2) return R.mipmap.light_straight;
        if (dir == 3) return R.mipmap.light_right;
        return R.mipmap.light_straight;
    }

    public void updateCruiseTrafficLights(JSONArray lightsArray) {}

    protected boolean isBlinkingAnimationEnabled() {
        return sp != null && sp.getBoolean("enable_blinking_animation", true);
    }

    protected void scaleViewRecursive(View view, float factor) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() * factor);
        }

        view.setPadding(
                Math.round(view.getPaddingLeft() * factor),
                Math.round(view.getPaddingTop() * factor),
                Math.round(view.getPaddingRight() * factor),
                Math.round(view.getPaddingBottom() * factor));

        PlatformCompat.scaleGradientCorners(view.getBackground(), factor);

        int minH = view.getMinimumHeight();
        if (minH > 0) view.setMinimumHeight(Math.round(minH * factor));

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            if (lp.width > 0) lp.width = Math.round(lp.width * factor);
            if (lp.height > 0) lp.height = Math.round(lp.height * factor);
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.leftMargin = Math.round(mlp.leftMargin * factor);
                mlp.topMargin = Math.round(mlp.topMargin * factor);
                mlp.rightMargin = Math.round(mlp.rightMargin * factor);
                mlp.bottomMargin = Math.round(mlp.bottomMargin * factor);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                scaleViewRecursive(vg.getChildAt(i), factor);
            }
        }
    }

    public void setClusterWindow(boolean clusterWindow) {
        this.isClusterWindow = clusterWindow;
        onScaleChanged();
    }

    protected void onScaleChanged() {
    }

    public float getWindowScale() {
        FloatingWindowManager fwm = FloatingWindowManager.getInstance();
        if (fwm == null) return 1.0f;
        return isClusterWindow ? fwm.getClusterScale() : fwm.getScale();
    }

    // --- Custom Color Helpers ---
    protected int getPrimaryTextColor(boolean isNightMode) {
        return isNightMode ? 
            sp.getInt("text_primary_night", TEXT_PRIMARY_DARK) : 
            sp.getInt("text_primary_day", TEXT_PRIMARY_LIGHT);
    }

    protected int getSecondaryTextColor(boolean isNightMode) {
        return isNightMode ? 
            sp.getInt("text_secondary_night", TEXT_SECONDARY_DARK) : 
            sp.getInt("text_secondary_day", TEXT_SECONDARY_LIGHT);
    }

    protected int getHintTextColor(boolean isNightMode) {
        return isNightMode ? 
            sp.getInt("text_hint_night", TEXT_HINT_DARK) : 
            sp.getInt("text_hint_day", TEXT_HINT_LIGHT);
    }

    protected int getWindowBgColor(boolean isNightMode) {
        return isNightMode ? 
            sp.getInt("bg_color_night", 0xCC121212) : 
            sp.getInt("bg_color_day", 0xE6F5F5F5);
    }

    public void updateIntervalSpeed(int startDist, String startDistText, int avgSpeed, String endDistText, int limitSpeed) {
        // To be overridden by subclasses
    }

    /**
     * 更新摄像头胶囊背景显隐（读取 hide_camera_capsule_bg 配置）
     * 全数据导航卡片（FullNaviWindow）不调用此方法
     */
    protected void updateCameraCapsuleBackground(View cameraView) {
        if (cameraView == null) return;
        boolean hideBg = sp.getBoolean("hide_camera_capsule_bg", false);
        if (hideBg) {
            cameraView.setBackground(null);
        } else {
            cameraView.setBackgroundResource(R.drawable.bg_traffic_light_capsule);
        }
    }
}
