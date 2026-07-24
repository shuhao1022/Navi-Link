package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.window.FloatingWindowManager;

import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.card.MaterialCardView;

public class NormalPanelDelegate {
    private final MainActivity activity;

    private SwitchCompat cbNormalLaneEnabled;
    private TextView tvNormalLaneStatus;
    private MaterialCardView cardNormalLaneToggle;

    private SwitchCompat cbHideTurnIconBg;
    private TextView tvHideTurnIconBgStatus;
    private MaterialCardView cardHideTurnIconBgToggle;

    private MaterialCardView cardNormalTmcToggle;
    private SwitchCompat cbNormalTmcEnabled;
    private TextView tvNormalTmcStatus;

    private MaterialCardView cardNormalBottomInfoToggle;
    private SwitchCompat cbNormalBottomInfoEnabled;
    private TextView tvNormalBottomInfoStatus;

    private MaterialCardView cardNormalCruiseInfoToggle;
    private SwitchCompat cbNormalCruiseInfoEnabled;
    private TextView tvNormalCruiseInfoStatus;

    private MaterialCardView cardHideNormalCruiseSpeedToggle;
    private SwitchCompat cbHideNormalCruiseSpeedEnabled;
    private TextView tvHideNormalCruiseSpeedStatus;

    private TextView tvNormalWidthValue;
    private View btnNormalWidthDecrease;
    private View btnNormalWidthIncrease;

    private TextView tvCruiseWidthValue;
    private View btnCruiseWidthDecrease;
    private View btnCruiseWidthIncrease;

    private TextView tvFullNaviWidthValue;
    private View btnFullNaviWidthDecrease;
    private View btnFullNaviWidthIncrease;

    private TextView tvFullCruiseWidthValue;
    private View btnFullCruiseWidthDecrease;
    private View btnFullCruiseWidthIncrease;

    public NormalPanelDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        cbNormalLaneEnabled = activity.findViewById(R.id.cb_normal_lane_enabled);
        tvNormalLaneStatus = activity.findViewById(R.id.tv_normal_lane_status);
        cardNormalLaneToggle = activity.findViewById(R.id.card_normal_lane_toggle);

        cbHideTurnIconBg = activity.findViewById(R.id.cb_hide_turn_icon_bg);
        tvHideTurnIconBgStatus = activity.findViewById(R.id.tv_hide_turn_icon_bg_status);
        cardHideTurnIconBgToggle = activity.findViewById(R.id.card_hide_turn_icon_bg_toggle);

        cardNormalCruiseInfoToggle = activity.findViewById(R.id.card_normal_cruise_info_toggle);
        cbNormalCruiseInfoEnabled = activity.findViewById(R.id.cb_normal_cruise_info_enabled);
        tvNormalCruiseInfoStatus = activity.findViewById(R.id.tv_normal_cruise_info_status);

        cbHideNormalCruiseSpeedEnabled = activity.findViewById(R.id.cb_hide_normal_cruise_speed_enabled);
        tvHideNormalCruiseSpeedStatus = activity.findViewById(R.id.tv_hide_normal_cruise_speed_status);
        cardHideNormalCruiseSpeedToggle = activity.findViewById(R.id.card_hide_normal_cruise_speed_toggle);

        cbNormalTmcEnabled = activity.findViewById(R.id.cb_normal_tmc_enabled);
        tvNormalTmcStatus = activity.findViewById(R.id.tv_normal_tmc_status);
        cardNormalTmcToggle = activity.findViewById(R.id.card_normal_tmc_toggle);

        cbNormalBottomInfoEnabled = activity.findViewById(R.id.cb_normal_bottom_info_enabled);
        tvNormalBottomInfoStatus = activity.findViewById(R.id.tv_normal_bottom_info_status);
        cardNormalBottomInfoToggle = activity.findViewById(R.id.card_normal_bottom_info_toggle);

        tvNormalWidthValue = activity.findViewById(R.id.tv_normal_width_value);
        btnNormalWidthDecrease = activity.findViewById(R.id.btn_normal_width_decrease);
        btnNormalWidthIncrease = activity.findViewById(R.id.btn_normal_width_increase);

        tvCruiseWidthValue = activity.findViewById(R.id.tv_cruise_width_value);
        btnCruiseWidthDecrease = activity.findViewById(R.id.btn_cruise_width_decrease);
        btnCruiseWidthIncrease = activity.findViewById(R.id.btn_cruise_width_increase);

        tvFullNaviWidthValue = activity.findViewById(R.id.tv_full_navi_width_value);
        btnFullNaviWidthDecrease = activity.findViewById(R.id.btn_full_navi_width_decrease);
        btnFullNaviWidthIncrease = activity.findViewById(R.id.btn_full_navi_width_increase);

        tvFullCruiseWidthValue = activity.findViewById(R.id.tv_full_cruise_width_value);
        btnFullCruiseWidthDecrease = activity.findViewById(R.id.btn_full_cruise_width_decrease);
        btnFullCruiseWidthIncrease = activity.findViewById(R.id.btn_full_cruise_width_increase);

        setupListeners();
    }

    public void setupListeners() {
        if (cbNormalLaneEnabled != null) {
            cbNormalLaneEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.normalLaneEnabled = isChecked;
                activity.savePreferences();
                if (tvNormalLaneStatus != null) {
                    tvNormalLaneStatus.setText(isChecked ? "车道线已启用" : "车道线已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalLaneToggle != null) {
            cardNormalLaneToggle.setOnClickListener(v -> {
                if (cbNormalLaneEnabled != null) cbNormalLaneEnabled.toggle();
            });
        }

        if (cbHideTurnIconBg != null) {
            cbHideTurnIconBg.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.hideTurnIconBg = isChecked;
                activity.savePreferences();
                if (tvHideTurnIconBgStatus != null) {
                    tvHideTurnIconBgStatus.setText(isChecked ? "背景已隐藏" : "背景已显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardHideTurnIconBgToggle != null) {
            cardHideTurnIconBgToggle.setOnClickListener(v -> {
                if (cbHideTurnIconBg != null) cbHideTurnIconBg.toggle();
            });
        }

        if (cbNormalCruiseInfoEnabled != null) {
            cbNormalCruiseInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.normalCruiseInfoEnabled = isChecked;
                activity.savePreferences();
                if (tvNormalCruiseInfoStatus != null) {
                    tvNormalCruiseInfoStatus.setText(isChecked ? "第一排图文信息已启用" : "第一排图文信息已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalCruiseInfoToggle != null) {
            cardNormalCruiseInfoToggle.setOnClickListener(v -> {
                if (cbNormalCruiseInfoEnabled != null) cbNormalCruiseInfoEnabled.toggle();
            });
        }

        if (cbHideNormalCruiseSpeedEnabled != null) {
            cbHideNormalCruiseSpeedEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.hideNormalCruiseSpeed = isChecked;
                activity.savePreferences();
                if (tvHideNormalCruiseSpeedStatus != null) {
                    tvHideNormalCruiseSpeedStatus.setText(isChecked ? "已隐藏常规巡航车速" : "常规巡航时显示车速");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardHideNormalCruiseSpeedToggle != null) {
            cardHideNormalCruiseSpeedToggle.setOnClickListener(v -> {
                if (cbHideNormalCruiseSpeedEnabled != null) cbHideNormalCruiseSpeedEnabled.toggle();
            });
        }

        if (cbNormalTmcEnabled != null) {
            cbNormalTmcEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.normalTmcEnabled = isChecked;
                activity.savePreferences();
                if (tvNormalTmcStatus != null) {
                    tvNormalTmcStatus.setText(isChecked ? "TMC路况进度条已启用" : "TMC路况进度条已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalTmcToggle != null) {
            cardNormalTmcToggle.setOnClickListener(v -> {
                if (cbNormalTmcEnabled != null) cbNormalTmcEnabled.toggle();
            });
        }

        if (cbNormalBottomInfoEnabled != null) {
            cbNormalBottomInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.normalBottomInfoEnabled = isChecked;
                activity.savePreferences();
                if (tvNormalBottomInfoStatus != null) {
                    tvNormalBottomInfoStatus.setText(isChecked ? "底栏到达信息已启用" : "底栏到达信息已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardNormalBottomInfoToggle != null) {
            cardNormalBottomInfoToggle.setOnClickListener(v -> {
                if (cbNormalBottomInfoEnabled != null) cbNormalBottomInfoEnabled.toggle();
            });
        }

        // 常规导航窗口宽度加减号
        if (btnNormalWidthDecrease != null) {
            btnNormalWidthDecrease.setOnClickListener(v -> adjustWidth("normal_navi_window_width", -5, tvNormalWidthValue, "dp"));
        }
        if (btnNormalWidthIncrease != null) {
            btnNormalWidthIncrease.setOnClickListener(v -> adjustWidth("normal_navi_window_width", 5, tvNormalWidthValue, "dp"));
        }

        // 常规巡航窗口宽度加减号
        if (btnCruiseWidthDecrease != null) {
            btnCruiseWidthDecrease.setOnClickListener(v -> adjustWidth("normal_cruise_window_width", -5, tvCruiseWidthValue, "dp"));
        }
        if (btnCruiseWidthIncrease != null) {
            btnCruiseWidthIncrease.setOnClickListener(v -> adjustWidth("normal_cruise_window_width", 5, tvCruiseWidthValue, "dp"));
        }

        // 全数据导航窗口宽度加减号
        if (btnFullNaviWidthDecrease != null) {
            btnFullNaviWidthDecrease.setOnClickListener(v -> adjustWidth("full_navi_window_width", -5, tvFullNaviWidthValue, "dp"));
        }
        if (btnFullNaviWidthIncrease != null) {
            btnFullNaviWidthIncrease.setOnClickListener(v -> adjustWidth("full_navi_window_width", 5, tvFullNaviWidthValue, "dp"));
        }

        // 全数据巡航窗口宽度加减号
        if (btnFullCruiseWidthDecrease != null) {
            btnFullCruiseWidthDecrease.setOnClickListener(v -> adjustWidth("full_cruise_window_width", -5, tvFullCruiseWidthValue, "dp"));
        }
        if (btnFullCruiseWidthIncrease != null) {
            btnFullCruiseWidthIncrease.setOnClickListener(v -> adjustWidth("full_cruise_window_width", 5, tvFullCruiseWidthValue, "dp"));
        }
    }

    public void loadSettings() {
        if (cbNormalLaneEnabled != null) cbNormalLaneEnabled.setChecked(activity.normalLaneEnabled);
        if (tvNormalLaneStatus != null) {
            tvNormalLaneStatus.setText(activity.normalLaneEnabled ? "车道线已启用" : "车道线已禁用");
        }

        if (cbHideTurnIconBg != null) cbHideTurnIconBg.setChecked(activity.hideTurnIconBg);
        if (tvHideTurnIconBgStatus != null) {
            tvHideTurnIconBgStatus.setText(activity.hideTurnIconBg ? "背景已隐藏" : "背景已显示");
        }

        if (cbNormalCruiseInfoEnabled != null) cbNormalCruiseInfoEnabled.setChecked(activity.normalCruiseInfoEnabled);
        if (tvNormalCruiseInfoStatus != null) {
            tvNormalCruiseInfoStatus.setText(activity.normalCruiseInfoEnabled ? "第一排图文信息已启用" : "第一排图文信息已禁用");
        }

        if (cbHideNormalCruiseSpeedEnabled != null) cbHideNormalCruiseSpeedEnabled.setChecked(activity.hideNormalCruiseSpeed);
        if (tvHideNormalCruiseSpeedStatus != null) {
            tvHideNormalCruiseSpeedStatus.setText(activity.hideNormalCruiseSpeed ? "已隐藏常规巡航车速" : "常规巡航时显示车速");
        }

        if (cbNormalTmcEnabled != null) cbNormalTmcEnabled.setChecked(activity.normalTmcEnabled);
        if (tvNormalTmcStatus != null) {
            tvNormalTmcStatus.setText(activity.normalTmcEnabled ? "TMC路况进度条已启用" : "TMC路况进度条已禁用");
        }

        if (cbNormalBottomInfoEnabled != null) cbNormalBottomInfoEnabled.setChecked(activity.normalBottomInfoEnabled);
        if (tvNormalBottomInfoStatus != null) {
            tvNormalBottomInfoStatus.setText(activity.normalBottomInfoEnabled ? "底栏到达信息已启用" : "底栏到达信息已禁用");
        }

        updateWidthDisplay();
    }

    private void updateWidthDisplay() {
        if (tvNormalWidthValue != null) {
            tvNormalWidthValue.setText(activity.normalNaviWindowWidth + "dp");
        }
        if (tvCruiseWidthValue != null) {
            tvCruiseWidthValue.setText(activity.normalCruiseWindowWidth + "dp");
        }
        if (tvFullNaviWidthValue != null) {
            tvFullNaviWidthValue.setText(activity.fullNaviWindowWidth + "dp");
        }
        if (tvFullCruiseWidthValue != null) {
            tvFullCruiseWidthValue.setText(activity.fullCruiseWindowWidth + "dp");
        }
    }

    private void adjustWidth(String spKey, int delta, TextView valueView, String unit) {
        int current = activity.getWidthFromSP(spKey);
        int newWidth = Math.max(200, Math.min(500, current + delta));
        if (newWidth != current) {
            activity.setWidthToSP(spKey, newWidth);
            if (valueView != null) {
                valueView.setText(newWidth + unit);
            }
            updateWidthDisplay();
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        }
    }

    public void updateThemeColors() {
        updateThemeColors(activity.getAccentColor());
    }

    public void updateThemeColors(int accentColor) {
        activity.updateSwitchTheme(cbNormalLaneEnabled, accentColor);
        activity.updateSwitchTheme(cbHideTurnIconBg, accentColor);
        activity.updateSwitchTheme(cbNormalCruiseInfoEnabled, accentColor);
        activity.updateSwitchTheme(cbHideNormalCruiseSpeedEnabled, accentColor);
        activity.updateSwitchTheme(cbNormalTmcEnabled, accentColor);
        activity.updateSwitchTheme(cbNormalBottomInfoEnabled, accentColor);
    }
}
