package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.activity.ClusterPositionActivity;
import com.navi.link.window.FloatingWindowManager;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FeaturesPanelDelegate {
    private final MainActivity activity;

    private SwitchCompat cbAvoidForegroundEnabled;
    private TextView tvAvoidForegroundStatus;
    private MaterialCardView cardAvoidForegroundToggle;

    private SwitchCompat cbCrossMapHideEnabled;
    private TextView tvCrossMapHideStatus;
    private MaterialCardView cardCrossMapHideToggle;

    private SwitchCompat cbHideLaneLineBgEnabled;
    private TextView tvHideLaneLineBgStatus;
    private MaterialCardView cardHideLaneLineBgToggle;

    private SwitchCompat cbHideCameraCapsuleBgEnabled;
    private TextView tvHideCameraCapsuleBgStatus;
    private MaterialCardView cardHideCameraCapsuleBgToggle;

    private MaterialCardView cardClusterMirrorToggle;
    private SwitchCompat cbClusterMirrorEnabled;
    private TextView tvClusterMirrorStatus;

    private MaterialCardView cardClusterDisplaySelect;
    private TextView tvClusterDisplaySelectStatus;
    private TextView tvClusterDisplaySelectLabel;
    private TextView btnAdjustClusterPos;

    private MaterialCardView cardHideMainWhenClusterActive;
    private SwitchCompat cbHideMainWhenClusterActive;
    private TextView tvHideMainWhenClusterActiveStatus;

    public FeaturesPanelDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        cbAvoidForegroundEnabled = activity.findViewById(R.id.cb_avoid_foreground_enabled);
        tvAvoidForegroundStatus = activity.findViewById(R.id.tv_avoid_foreground_status);
        cardAvoidForegroundToggle = activity.findViewById(R.id.card_avoid_foreground_toggle);

        cbCrossMapHideEnabled = activity.findViewById(R.id.cb_cross_map_hide_enabled);
        tvCrossMapHideStatus = activity.findViewById(R.id.tv_cross_map_hide_status);
        cardCrossMapHideToggle = activity.findViewById(R.id.card_cross_map_hide_toggle);

        cbHideLaneLineBgEnabled = activity.findViewById(R.id.cb_hide_lane_line_bg_enabled);
        tvHideLaneLineBgStatus = activity.findViewById(R.id.tv_hide_lane_line_bg_status);
        cardHideLaneLineBgToggle = activity.findViewById(R.id.card_hide_lane_line_bg_toggle);

        cbHideCameraCapsuleBgEnabled = activity.findViewById(R.id.cb_hide_camera_capsule_bg_enabled);
        tvHideCameraCapsuleBgStatus = activity.findViewById(R.id.tv_hide_camera_capsule_bg_status);
        cardHideCameraCapsuleBgToggle = activity.findViewById(R.id.card_hide_camera_capsule_bg_toggle);

        cbClusterMirrorEnabled = activity.findViewById(R.id.cb_cluster_mirror_enabled);
        tvClusterMirrorStatus = activity.findViewById(R.id.tv_cluster_mirror_status);
        cardClusterMirrorToggle = activity.findViewById(R.id.card_cluster_mirror_toggle);

        cardClusterDisplaySelect = activity.findViewById(R.id.card_cluster_display_select);
        tvClusterDisplaySelectStatus = activity.findViewById(R.id.tv_cluster_display_select_status);
        tvClusterDisplaySelectLabel = activity.findViewById(R.id.tv_cluster_display_select_label);
        btnAdjustClusterPos = activity.findViewById(R.id.btn_adjust_cluster_pos);

        cardHideMainWhenClusterActive = activity.findViewById(R.id.card_hide_main_when_cluster_active);
        cbHideMainWhenClusterActive = activity.findViewById(R.id.cb_hide_main_when_cluster_active);
        tvHideMainWhenClusterActiveStatus = activity.findViewById(R.id.tv_hide_main_when_cluster_active_status);

        setupListeners();
    }

    public void setupListeners() {
        if (cbAvoidForegroundEnabled != null) {
            cbAvoidForegroundEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.avoidForegroundEnabled = isChecked;
                activity.savePreferences();
                if (tvAvoidForegroundStatus != null) {
                    tvAvoidForegroundStatus.setText(isChecked ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
                }
            });
        }
        if (cardAvoidForegroundToggle != null) {
            cardAvoidForegroundToggle.setOnClickListener(v -> {
                if (cbAvoidForegroundEnabled != null) cbAvoidForegroundEnabled.toggle();
            });
        }

        if (cbCrossMapHideEnabled != null) {
            cbCrossMapHideEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.crossMapHideEnabled = isChecked;
                activity.savePreferences();
                if (tvCrossMapHideStatus != null) {
                    tvCrossMapHideStatus.setText(isChecked ? "路口放大图时隐藏悬浮窗" : "路口放大图时正常显示浮窗");
                }
            });
        }
        if (cardCrossMapHideToggle != null) {
            cardCrossMapHideToggle.setOnClickListener(v -> {
                if (cbCrossMapHideEnabled != null) cbCrossMapHideEnabled.toggle();
            });
        }

        if (cbHideLaneLineBgEnabled != null) {
            cbHideLaneLineBgEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.hideLaneLineBg = isChecked;
                activity.savePreferences();
                if (tvHideLaneLineBgStatus != null) {
                    tvHideLaneLineBgStatus.setText(isChecked ? "背景已隐藏" : "背景已显示");
                }
                activity.refreshFloatingWindow();
            });
        }
        if (cardHideLaneLineBgToggle != null) {
            cardHideLaneLineBgToggle.setOnClickListener(v -> {
                if (cbHideLaneLineBgEnabled != null) cbHideLaneLineBgEnabled.toggle();
            });
        }

        if (cbHideCameraCapsuleBgEnabled != null) {
            cbHideCameraCapsuleBgEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.hideCameraCapsuleBg = isChecked;
                activity.savePreferences();
                if (tvHideCameraCapsuleBgStatus != null) {
                    tvHideCameraCapsuleBgStatus.setText(isChecked ? "背景已隐藏" : "背景默认显示");
                }
                activity.refreshFloatingWindow();
            });
        }
        if (cardHideCameraCapsuleBgToggle != null) {
            cardHideCameraCapsuleBgToggle.setOnClickListener(v -> {
                if (cbHideCameraCapsuleBgEnabled != null) cbHideCameraCapsuleBgEnabled.toggle();
            });
        }

        if (cbClusterMirrorEnabled != null) {
            cbClusterMirrorEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.clusterMirrorEnabled = isChecked;
                activity.savePreferences();
                if (tvClusterMirrorStatus != null) {
                    tvClusterMirrorStatus.setText(isChecked ? "仪表盘/副屏镜像已开启" : "未开启仪表盘/副屏镜像");
                }
                if (btnAdjustClusterPos != null) {
                    btnAdjustClusterPos.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.onClusterMirrorConfigChanged();
                }
            });
        }
        if (cardClusterMirrorToggle != null) {
            cardClusterMirrorToggle.setOnClickListener(v -> {
                if (cbClusterMirrorEnabled != null) cbClusterMirrorEnabled.toggle();
            });
        }

        if (cbHideMainWhenClusterActive != null) {
            cbHideMainWhenClusterActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.hideMainWhenClusterActive = isChecked;
                activity.savePreferences();
                if (tvHideMainWhenClusterActiveStatus != null) {
                    tvHideMainWhenClusterActiveStatus.setText(isChecked ? "副屏成功显示后自动隐藏主屏悬浮窗" : "已关闭该功能，主副屏同时显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.updateFloatingWindowVisibility();
                }
            });
        }
        if (cardHideMainWhenClusterActive != null) {
            cardHideMainWhenClusterActive.setOnClickListener(v -> {
                if (cbHideMainWhenClusterActive != null) cbHideMainWhenClusterActive.toggle();
            });
        }

        if (cardClusterDisplaySelect != null) {
            cardClusterDisplaySelect.setOnClickListener(v -> showClusterDisplaySelectionDialog());
        }

        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setOnClickListener(v -> {
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm == null || !fwm.isClusterMirrorActive()) {
                    Toast.makeText(activity, "副屏投屏未开启，请先开启投屏", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(activity, ClusterPositionActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    public void showClusterDisplaySelectionDialog() {
        DisplayManager dm = (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
        if (dm == null) return;
        Display[] displays = dm.getDisplays();
        List<String> displayNames = new ArrayList<>();
        List<Integer> displayIds = new ArrayList<>();

        for (Display d : displays) {
            displayNames.add("Display " + d.getDisplayId() + ": " + d.getName() + " (" + d.getWidth() + "x" + d.getHeight() + ")");
            displayIds.add(d.getDisplayId());
        }

        String[] items = displayNames.toArray(new String[0]);
        int selectedIndex = displayIds.indexOf(activity.clusterDisplayId);

        new android.app.AlertDialog.Builder(activity)
                .setTitle("选择仪表盘/副屏显示器")
                .setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
                    activity.clusterDisplayId = displayIds.get(which);
                    activity.savePreferences();
                    if (tvClusterDisplaySelectStatus != null) {
                        tvClusterDisplaySelectStatus.setText("已选择: ID " + activity.clusterDisplayId);
                    }
                    FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                    if (fwm != null && activity.clusterMirrorEnabled) {
                        fwm.onClusterMirrorConfigChanged();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void loadSettings() {
        if (cbAvoidForegroundEnabled != null) cbAvoidForegroundEnabled.setChecked(activity.avoidForegroundEnabled);
        if (tvAvoidForegroundStatus != null) {
            tvAvoidForegroundStatus.setText(activity.avoidForegroundEnabled ? "高德前台时隐藏悬浮窗" : "前台正常显示浮窗");
        }

        if (cbCrossMapHideEnabled != null) cbCrossMapHideEnabled.setChecked(activity.crossMapHideEnabled);
        if (tvCrossMapHideStatus != null) {
            tvCrossMapHideStatus.setText(activity.crossMapHideEnabled ? "路口放大图时隐藏悬浮窗" : "路口放大图时正常显示浮窗");
        }

        if (cbHideLaneLineBgEnabled != null) cbHideLaneLineBgEnabled.setChecked(activity.hideLaneLineBg);
        if (tvHideLaneLineBgStatus != null) {
            tvHideLaneLineBgStatus.setText(activity.hideLaneLineBg ? "背景已隐藏" : "背景已显示");
        }

        if (cbHideCameraCapsuleBgEnabled != null) cbHideCameraCapsuleBgEnabled.setChecked(activity.hideCameraCapsuleBg);
        if (tvHideCameraCapsuleBgStatus != null) {
            tvHideCameraCapsuleBgStatus.setText(activity.hideCameraCapsuleBg ? "背景已隐藏" : "背景默认显示");
        }

        if (cbClusterMirrorEnabled != null) cbClusterMirrorEnabled.setChecked(activity.clusterMirrorEnabled);
        if (tvClusterMirrorStatus != null) {
            tvClusterMirrorStatus.setText(activity.clusterMirrorEnabled ? "仪表盘/副屏镜像已开启" : "未开启仪表盘/副屏镜像");
        }
        if (btnAdjustClusterPos != null) {
            btnAdjustClusterPos.setVisibility(activity.clusterMirrorEnabled ? View.VISIBLE : View.GONE);
        }

        if (cbHideMainWhenClusterActive != null) cbHideMainWhenClusterActive.setChecked(activity.hideMainWhenClusterActive);
        if (tvHideMainWhenClusterActiveStatus != null) {
            tvHideMainWhenClusterActiveStatus.setText(activity.hideMainWhenClusterActive ? "副屏成功显示后自动隐藏主屏悬浮窗" : "已关闭该功能，主副屏同时显示");
        }

        if (tvClusterDisplaySelectStatus != null) {
            tvClusterDisplaySelectStatus.setText(activity.clusterDisplayId != -1 ? ("已选择: ID " + activity.clusterDisplayId) : "默认自动检测显示器");
        }
    }

    public void updateThemeColors() {
        int accentColor = activity.getAccentColor();
        activity.updateSwitchTheme(cbAvoidForegroundEnabled, accentColor);
        activity.updateSwitchTheme(cbCrossMapHideEnabled, accentColor);
        activity.updateSwitchTheme(cbHideLaneLineBgEnabled, accentColor);
        activity.updateSwitchTheme(cbHideCameraCapsuleBgEnabled, accentColor);
        activity.updateSwitchTheme(cbClusterMirrorEnabled, accentColor);
        if (tvClusterDisplaySelectLabel != null) {
            tvClusterDisplaySelectLabel.setTextColor(activity.getThemeColorAttr(R.attr.panelTextColorPrimary));
        }
        activity.updateSwitchTheme(cbHideMainWhenClusterActive, accentColor);
    }
}
