package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.window.FloatingWindowManager;

import android.widget.TextView;
import com.navi.link.view.SwitchButton;
import com.google.android.material.card.MaterialCardView;

public class MinimalPanelDelegate {
    private final MainActivity activity;

    private MaterialCardView cardMinimalCameraToggle;
    private SwitchButton cbMinimalCameraEnabled;
    private TextView tvMinimalCameraStatus;

    private MaterialCardView cardMinimalAutocenterToggle;
    private SwitchButton cbMinimalAutocenterEnabled;

    private MaterialCardView cardMinimalLaneToggle;
    private SwitchButton cbMinimalLaneEnabled;
    private TextView tvMinimalLaneStatus;

    private MaterialCardView cardMinimalRoadNameToggle;
    private SwitchButton cbMinimalRoadNameEnabled;
    private TextView tvMinimalRoadNameStatus;

    private MaterialCardView cardMinimalDirectionToggle;
    private SwitchButton cbMinimalDirectionEnabled;
    private TextView tvMinimalDirectionStatus;

    private MaterialCardView cardMinimalTurnInfoToggle;
    private SwitchButton cbMinimalTurnInfoEnabled;
    private TextView tvMinimalTurnInfoStatus;

    private MaterialCardView cardMinimalSpeedToggle;
    private SwitchButton cbMinimalSpeedEnabled;
    private TextView tvMinimalSpeedStatus;

    private MaterialCardView cardMinimalLightCountToggle;
    private SwitchButton cbMinimalLightCountEnabled;
    private TextView tvMinimalLightCountStatus;

    private MaterialCardView cardMinimalSpeedLimitToggle;
    private SwitchButton cbMinimalSpeedLimitEnabled;
    private TextView tvMinimalSpeedLimitStatus;

    private MaterialCardView cardMinimalArrivalInfoToggle;
    private SwitchButton cbMinimalArrivalInfoEnabled;
    private TextView tvMinimalArrivalInfoStatus;

    public MinimalPanelDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        cardMinimalCameraToggle = activity.findViewById(R.id.card_minimal_camera_toggle);
        cbMinimalCameraEnabled = activity.findViewById(R.id.cb_minimal_camera_enabled);
        tvMinimalCameraStatus = activity.findViewById(R.id.tv_minimal_camera_status);

        cardMinimalAutocenterToggle = activity.findViewById(R.id.card_minimal_autocenter_toggle);
        cbMinimalAutocenterEnabled = activity.findViewById(R.id.cb_minimal_autocenter_enabled);

        cardMinimalLaneToggle = activity.findViewById(R.id.card_minimal_lane_toggle);
        cbMinimalLaneEnabled = activity.findViewById(R.id.cb_minimal_lane_enabled);
        tvMinimalLaneStatus = activity.findViewById(R.id.tv_minimal_lane_status);

        cardMinimalRoadNameToggle = activity.findViewById(R.id.card_minimal_road_name_toggle);
        cbMinimalRoadNameEnabled = activity.findViewById(R.id.cb_minimal_road_name_enabled);
        tvMinimalRoadNameStatus = activity.findViewById(R.id.tv_minimal_road_name_status);

        cardMinimalDirectionToggle = activity.findViewById(R.id.card_minimal_direction_toggle);
        cbMinimalDirectionEnabled = activity.findViewById(R.id.cb_minimal_direction_enabled);
        tvMinimalDirectionStatus = activity.findViewById(R.id.tv_minimal_direction_status);

        cardMinimalTurnInfoToggle = activity.findViewById(R.id.card_minimal_turn_info_toggle);
        cbMinimalTurnInfoEnabled = activity.findViewById(R.id.cb_minimal_turn_info_enabled);
        tvMinimalTurnInfoStatus = activity.findViewById(R.id.tv_minimal_turn_info_status);

        cardMinimalSpeedToggle = activity.findViewById(R.id.card_minimal_speed_toggle);
        cbMinimalSpeedEnabled = activity.findViewById(R.id.cb_minimal_speed_enabled);
        tvMinimalSpeedStatus = activity.findViewById(R.id.tv_minimal_speed_status);

        cardMinimalLightCountToggle = activity.findViewById(R.id.card_minimal_light_count_toggle);
        cbMinimalLightCountEnabled = activity.findViewById(R.id.cb_minimal_light_count_enabled);
        tvMinimalLightCountStatus = activity.findViewById(R.id.tv_minimal_light_count_status);

        cardMinimalSpeedLimitToggle = activity.findViewById(R.id.card_minimal_speed_limit_toggle);
        cbMinimalSpeedLimitEnabled = activity.findViewById(R.id.cb_minimal_speed_limit_enabled);
        tvMinimalSpeedLimitStatus = activity.findViewById(R.id.tv_minimal_speed_limit_status);

        cardMinimalArrivalInfoToggle = activity.findViewById(R.id.card_minimal_arrival_info_toggle);
        cbMinimalArrivalInfoEnabled = activity.findViewById(R.id.cb_minimal_arrival_info_enabled);
        tvMinimalArrivalInfoStatus = activity.findViewById(R.id.tv_minimal_arrival_info_status);

        setupListeners();
    }

    public void setupListeners() {
        if (cbMinimalAutocenterEnabled != null) {
            cbMinimalAutocenterEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalAutocenterEnabled = isChecked;
                activity.savePreferences();
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.setAutoCenteringEnabled(isChecked);
                }
            });
        }
        if (cardMinimalAutocenterToggle != null) {
            cardMinimalAutocenterToggle.setOnClickListener(v -> {
                if (cbMinimalAutocenterEnabled != null) cbMinimalAutocenterEnabled.toggle();
            });
        }

        if (cbMinimalCameraEnabled != null) {
            cbMinimalCameraEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalCameraEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalCameraStatus != null) {
                    tvMinimalCameraStatus.setText(isChecked ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalCameraToggle != null) {
            cardMinimalCameraToggle.setOnClickListener(v -> {
                if (cbMinimalCameraEnabled != null) cbMinimalCameraEnabled.toggle();
            });
        }

        if (cbMinimalLaneEnabled != null) {
            cbMinimalLaneEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.minimalLaneEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalLaneStatus != null) {
                    tvMinimalLaneStatus.setText(isChecked ? "灵动岛导航显示车道线" : "已关闭车道线显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalLaneToggle != null) {
            cardMinimalLaneToggle.setOnClickListener(v -> {
                if (cbMinimalLaneEnabled != null) cbMinimalLaneEnabled.toggle();
            });
        }

        if (cbMinimalRoadNameEnabled != null) {
            cbMinimalRoadNameEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalRoadNameEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalRoadNameStatus != null) {
                    tvMinimalRoadNameStatus.setText(isChecked ? "道路名显示已启用" : "道路名显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalRoadNameToggle != null) {
            cardMinimalRoadNameToggle.setOnClickListener(v -> {
                if (cbMinimalRoadNameEnabled != null) cbMinimalRoadNameEnabled.toggle();
            });
        }

        if (cbMinimalDirectionEnabled != null) {
            cbMinimalDirectionEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalDirectionEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalDirectionStatus != null) {
                    tvMinimalDirectionStatus.setText(isChecked ? "行驶方向显示已启用" : "行驶方向显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalDirectionToggle != null) {
            cardMinimalDirectionToggle.setOnClickListener(v -> {
                if (cbMinimalDirectionEnabled != null) cbMinimalDirectionEnabled.toggle();
            });
        }

        if (cbMinimalTurnInfoEnabled != null) {
            cbMinimalTurnInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalTurnInfoEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalTurnInfoStatus != null) {
                    tvMinimalTurnInfoStatus.setText(isChecked ? "转向图标及距离已启用" : "转向图标及距离已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalTurnInfoToggle != null) {
            cardMinimalTurnInfoToggle.setOnClickListener(v -> {
                if (cbMinimalTurnInfoEnabled != null) cbMinimalTurnInfoEnabled.toggle();
            });
        }

        if (cbMinimalSpeedEnabled != null) {
            cbMinimalSpeedEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalSpeedEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalSpeedStatus != null) {
                    tvMinimalSpeedStatus.setText(isChecked ? "车速显示已启用" : "车速显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalSpeedToggle != null) {
            cardMinimalSpeedToggle.setOnClickListener(v -> {
                if (cbMinimalSpeedEnabled != null) cbMinimalSpeedEnabled.toggle();
            });
        }

        if (cbMinimalLightCountEnabled != null) {
            cbMinimalLightCountEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalLightCountEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalLightCountStatus != null) {
                    tvMinimalLightCountStatus.setText(isChecked ? "红绿灯计数已启用" : "红绿灯计数已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalLightCountToggle != null) {
            cardMinimalLightCountToggle.setOnClickListener(v -> {
                if (cbMinimalLightCountEnabled != null) cbMinimalLightCountEnabled.toggle();
            });
        }

        if (cbMinimalSpeedLimitEnabled != null) {
            cbMinimalSpeedLimitEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalSpeedLimitEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalSpeedLimitStatus != null) {
                    tvMinimalSpeedLimitStatus.setText(isChecked ? "限速显示已启用" : "限速显示已禁用");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalSpeedLimitToggle != null) {
            cardMinimalSpeedLimitToggle.setOnClickListener(v -> {
                if (cbMinimalSpeedLimitEnabled != null) cbMinimalSpeedLimitEnabled.toggle();
            });
        }

        if (cbMinimalArrivalInfoEnabled != null) {
            cbMinimalArrivalInfoEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.isMinimalArrivalInfoEnabled = isChecked;
                activity.savePreferences();
                if (tvMinimalArrivalInfoStatus != null) {
                    tvMinimalArrivalInfoStatus.setText(isChecked ? "到达信息显示已启用" : "已关闭到达信息显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardMinimalArrivalInfoToggle != null) {
            cardMinimalArrivalInfoToggle.setOnClickListener(v -> {
                if (cbMinimalArrivalInfoEnabled != null) cbMinimalArrivalInfoEnabled.toggle();
            });
        }
    }

    public void loadSettings() {
        if (cbMinimalAutocenterEnabled != null) cbMinimalAutocenterEnabled.setChecked(activity.isMinimalAutocenterEnabled);

        if (cbMinimalCameraEnabled != null) cbMinimalCameraEnabled.setChecked(activity.isMinimalCameraEnabled);
        if (tvMinimalCameraStatus != null) {
            tvMinimalCameraStatus.setText(activity.isMinimalCameraEnabled ? "灵动岛布局显示摄像头距离" : "已关闭灵动岛摄像头显示");
        }

        if (cbMinimalLaneEnabled != null) cbMinimalLaneEnabled.setChecked(activity.minimalLaneEnabled);
        if (tvMinimalLaneStatus != null) {
            tvMinimalLaneStatus.setText(activity.minimalLaneEnabled ? "灵动岛导航显示车道线" : "已关闭车道线显示");
        }

        if (cbMinimalRoadNameEnabled != null) cbMinimalRoadNameEnabled.setChecked(activity.isMinimalRoadNameEnabled);
        if (tvMinimalRoadNameStatus != null) {
            tvMinimalRoadNameStatus.setText(activity.isMinimalRoadNameEnabled ? "道路名显示已启用" : "道路名显示已禁用");
        }

        if (cbMinimalDirectionEnabled != null) cbMinimalDirectionEnabled.setChecked(activity.isMinimalDirectionEnabled);
        if (tvMinimalDirectionStatus != null) {
            tvMinimalDirectionStatus.setText(activity.isMinimalDirectionEnabled ? "行驶方向显示已启用" : "行驶方向显示已禁用");
        }

        if (cbMinimalTurnInfoEnabled != null) cbMinimalTurnInfoEnabled.setChecked(activity.isMinimalTurnInfoEnabled);
        if (tvMinimalTurnInfoStatus != null) {
            tvMinimalTurnInfoStatus.setText(activity.isMinimalTurnInfoEnabled ? "转向图标及距离已启用" : "转向图标及距离已禁用");
        }

        if (cbMinimalSpeedEnabled != null) cbMinimalSpeedEnabled.setChecked(activity.isMinimalSpeedEnabled);
        if (tvMinimalSpeedStatus != null) {
            tvMinimalSpeedStatus.setText(activity.isMinimalSpeedEnabled ? "车速显示已启用" : "车速显示已禁用");
        }

        if (cbMinimalLightCountEnabled != null) cbMinimalLightCountEnabled.setChecked(activity.isMinimalLightCountEnabled);
        if (tvMinimalLightCountStatus != null) {
            tvMinimalLightCountStatus.setText(activity.isMinimalLightCountEnabled ? "红绿灯计数已启用" : "红绿灯计数已禁用");
        }

        if (cbMinimalSpeedLimitEnabled != null) cbMinimalSpeedLimitEnabled.setChecked(activity.isMinimalSpeedLimitEnabled);
        if (tvMinimalSpeedLimitStatus != null) {
            tvMinimalSpeedLimitStatus.setText(activity.isMinimalSpeedLimitEnabled ? "限速显示已启用" : "限速显示已禁用");
        }

        if (cbMinimalArrivalInfoEnabled != null) cbMinimalArrivalInfoEnabled.setChecked(activity.isMinimalArrivalInfoEnabled);
        if (tvMinimalArrivalInfoStatus != null) {
            tvMinimalArrivalInfoStatus.setText(activity.isMinimalArrivalInfoEnabled ? "到达信息显示已启用" : "已关闭到达信息显示");
        }
    }

    public void updateThemeColors() {
        int accentColor = activity.getAccentColor();
        updateThemeColors(accentColor, android.content.res.ColorStateList.valueOf(accentColor));
    }

    public void updateThemeColors(int accentColor, android.content.res.ColorStateList accentColorStateList) {
        activity.updateSwitchTheme(cbMinimalAutocenterEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalCameraEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalLaneEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalRoadNameEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalDirectionEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalTurnInfoEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalSpeedEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalLightCountEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalSpeedLimitEnabled, accentColor);
        activity.updateSwitchTheme(cbMinimalArrivalInfoEnabled, accentColor);
    }
}
