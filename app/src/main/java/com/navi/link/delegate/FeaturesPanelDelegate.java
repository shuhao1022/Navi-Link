package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.activity.ClusterPositionActivity;
import com.navi.link.window.FloatingWindowManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.navi.link.view.SwitchButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FeaturesPanelDelegate {
    private final MainActivity activity;

    private SwitchButton cbAvoidForegroundEnabled;
    private TextView tvAvoidForegroundStatus;
    private MaterialCardView cardAvoidForegroundToggle;

    private SwitchButton cbCrossMapHideEnabled;
    private TextView tvCrossMapHideStatus;
    private MaterialCardView cardCrossMapHideToggle;

    private SwitchButton cbBlinkingAnimationEnabled;
    private TextView tvBlinkingAnimationStatus;
    private MaterialCardView cardBlinkingAnimationToggle;

    private SwitchButton cbHideLaneLineBgEnabled;
    private TextView tvHideLaneLineBgStatus;
    private MaterialCardView cardHideLaneLineBgToggle;

    private SwitchButton cbHideCameraCapsuleBgEnabled;
    private TextView tvHideCameraCapsuleBgStatus;
    private MaterialCardView cardHideCameraCapsuleBgToggle;

    private MaterialCardView cardClusterMirrorToggle;
    private SwitchButton cbClusterMirrorEnabled;
    private TextView tvClusterMirrorStatus;

    private MaterialCardView cardClusterDisplaySelect;
    private TextView tvClusterDisplaySelectStatus;
    private TextView tvClusterDisplaySelectLabel;
    private TextView btnAdjustClusterPos;

    private MaterialCardView cardHideMainWhenClusterActive;
    private SwitchButton cbHideMainWhenClusterActive;
    private TextView tvHideMainWhenClusterActiveStatus;

    private MaterialCardView cardClusterCrossMapHideToggle;
    private SwitchButton cbClusterCrossMapHideEnabled;
    private TextView tvClusterCrossMapHideStatus;

    private MaterialCardView cardClickAction;
    private TextView tvClickActionStatus;
    private MaterialCardView cardDoubleClickAction;
    private TextView tvDoubleClickActionStatus;

    private MaterialCardView cardDataTimeout;
    private TextView tvDataTimeoutStatus;

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

        cbBlinkingAnimationEnabled = activity.findViewById(R.id.cb_blinking_animation_enabled);
        tvBlinkingAnimationStatus = activity.findViewById(R.id.tv_blinking_animation_status);
        cardBlinkingAnimationToggle = activity.findViewById(R.id.card_blinking_animation_toggle);

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

        cardClusterCrossMapHideToggle = activity.findViewById(R.id.card_cluster_cross_map_hide_toggle);
        cbClusterCrossMapHideEnabled = activity.findViewById(R.id.cb_cluster_cross_map_hide_enabled);
        tvClusterCrossMapHideStatus = activity.findViewById(R.id.tv_cluster_cross_map_hide_status);

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

        if (cbBlinkingAnimationEnabled != null) {
            cbBlinkingAnimationEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.enableBlinkingAnimation = isChecked;
                activity.savePreferences();
                if (tvBlinkingAnimationStatus != null) {
                    tvBlinkingAnimationStatus.setText(isChecked ? "闪烁动画提示已开启" : "闪烁动画提示已关闭");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }
        if (cardBlinkingAnimationToggle != null) {
            cardBlinkingAnimationToggle.setOnClickListener(v -> {
                if (cbBlinkingAnimationEnabled != null) cbBlinkingAnimationEnabled.toggle();
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

        if (cbClusterCrossMapHideEnabled != null) {
            cbClusterCrossMapHideEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.clusterCrossMapHideEnabled = isChecked;
                activity.savePreferences();
                if (tvClusterCrossMapHideStatus != null) {
                    tvClusterCrossMapHideStatus.setText(isChecked ? "路口放大图时副屏隐藏" : "路口放大图时副屏正常显示");
                }
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.updateClusterFloatingWindowVisibility();
                }
            });
        }
        if (cardClusterCrossMapHideToggle != null) {
            cardClusterCrossMapHideToggle.setOnClickListener(v -> {
                if (cbClusterCrossMapHideEnabled != null) cbClusterCrossMapHideEnabled.toggle();
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

        cardClickAction = activity.findViewById(R.id.card_click_action);
        tvClickActionStatus = activity.findViewById(R.id.tv_click_action_status);
        cardDoubleClickAction = activity.findViewById(R.id.card_double_click_action);
        tvDoubleClickActionStatus = activity.findViewById(R.id.tv_double_click_action_status);

        if (cardClickAction != null) {
            cardClickAction.setOnClickListener(v -> showClickActionDialog(false));
        }
        if (cardDoubleClickAction != null) {
            cardDoubleClickAction.setOnClickListener(v -> showClickActionDialog(true));
        }

        cardDataTimeout = activity.findViewById(R.id.card_data_timeout);
        tvDataTimeoutStatus = activity.findViewById(R.id.tv_data_timeout_status);
        if (cardDataTimeout != null) {
            cardDataTimeout.setOnClickListener(v -> showDataTimeoutDialog());
        }
    }

    /** 单击/双击行为设置弹窗 */
    private void showClickActionDialog(final boolean isDouble) {
        final String title = isDouble ? "双击窗口行为" : "单击窗口行为";
        final int currentAction = isDouble ? activity.doubleClickAction : activity.clickAction;
        String[] items = {"打开设置页", "打开应用"};
        new android.app.AlertDialog.Builder(activity)
                .setTitle(title)
                .setSingleChoiceItems(items, currentAction, (dialog, which) -> {
                    if (which == 0) {
                        // 打开设置页
                        if (isDouble) {
                            activity.doubleClickAction = 0;
                            activity.doubleClickAppPackage = "";
                        } else {
                            activity.clickAction = 0;
                            activity.clickAppPackage = "";
                        }
                        activity.savePreferences();
                        dialog.dismiss();
                        updateClickActionStatusTexts();
                    } else {
                        // 打开应用 → 弹应用列表
                        dialog.dismiss();
                        activity.showAppPickerDialog("选择要打开的应用", pkg -> {
                            if (isDouble) {
                                activity.doubleClickAction = 1;
                                activity.doubleClickAppPackage = pkg;
                            } else {
                                activity.clickAction = 1;
                                activity.clickAppPackage = pkg;
                            }
                            activity.savePreferences();
                            updateClickActionStatusTexts();
                        });
                    }
                })
                .show();
    }

    private void updateClickActionStatusTexts() {
        if (tvClickActionStatus != null) {
            if (activity.clickAction == 1) {
                tvClickActionStatus.setText("单击打开应用: " + activity.getAppLabel(activity.clickAppPackage));
            } else {
                tvClickActionStatus.setText("单击打开设置页");
            }
        }
        if (tvDoubleClickActionStatus != null) {
            if (activity.doubleClickAction == 1) {
                tvDoubleClickActionStatus.setText("双击打开应用: " + activity.getAppLabel(activity.doubleClickAppPackage));
            } else {
                tvDoubleClickActionStatus.setText("双击打开设置页");
            }
        }
    }

    /** 数据超时隐藏窗口设置弹窗：自定义秒数，0=关闭 */
    private void showDataTimeoutDialog() {
        final SharedPreferences sp = activity.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        final int current = sp.getInt("data_timeout_seconds", 0);
        final android.widget.EditText input = new android.widget.EditText(activity);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("输入秒数，0 或留空 = 关闭");
        if (current > 0) {
            input.setText(String.valueOf(current));
            input.setSelection(input.getText().length());
        }
        int pad = Math.round(16 * activity.getResources().getDisplayMetrics().density);
        android.widget.LinearLayout container = new android.widget.LinearLayout(activity);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(pad, 0, pad, 0);
        container.addView(input);
        new android.app.AlertDialog.Builder(activity)
                .setTitle("数据超时隐藏窗口")
                .setMessage("无导航/巡航数据（10001广播）超过该秒数后自动隐藏窗口，用于高德被强杀等无结束广播的场景。")
                .setView(container)
                .setNegativeButton("关闭", (d, w) -> {
                    sp.edit().putInt("data_timeout_seconds", 0).apply();
                    updateDataTimeoutStatusText();
                })
                .setPositiveButton("确定", (d, w) -> {
                    String text = input.getText().toString().trim();
                    int seconds = 0;
                    if (!text.isEmpty()) {
                        try {
                            seconds = Integer.parseInt(text);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (seconds < 0) seconds = 0;
                    if (seconds > 9999) seconds = 9999;
                    sp.edit().putInt("data_timeout_seconds", seconds).apply();
                    updateDataTimeoutStatusText();
                })
                .show();
    }

    private void updateDataTimeoutStatusText() {
        if (tvDataTimeoutStatus == null) return;
        SharedPreferences sp = activity.getSharedPreferences("floating_config", Context.MODE_PRIVATE);
        int seconds = sp.getInt("data_timeout_seconds", 0);
        tvDataTimeoutStatus.setText(seconds > 0 ? "无数据 " + seconds + " 秒后隐藏窗口" : "关闭（默认）");
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

        if (cbBlinkingAnimationEnabled != null) cbBlinkingAnimationEnabled.setChecked(activity.enableBlinkingAnimation);
        if (tvBlinkingAnimationStatus != null) {
            tvBlinkingAnimationStatus.setText(activity.enableBlinkingAnimation ? "闪烁动画提示已开启" : "闪烁动画提示已关闭");
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

        if (cbClusterCrossMapHideEnabled != null) {
            cbClusterCrossMapHideEnabled.setChecked(activity.clusterCrossMapHideEnabled);
        }
        if (tvClusterCrossMapHideStatus != null) {
            tvClusterCrossMapHideStatus.setText(activity.clusterCrossMapHideEnabled ? "路口放大图时副屏隐藏" : "路口放大图时副屏正常显示");
        }

        if (tvClusterDisplaySelectStatus != null) {
            tvClusterDisplaySelectStatus.setText(activity.clusterDisplayId != -1 ? ("已选择: ID " + activity.clusterDisplayId) : "默认自动检测显示器");
        }

        updateClickActionStatusTexts();
        updateDataTimeoutStatusText();
    }

    public void updateThemeColors() {
        int accentColor = activity.getAccentColor();
        activity.updateSwitchTheme(cbAvoidForegroundEnabled, accentColor);
        activity.updateSwitchTheme(cbCrossMapHideEnabled, accentColor);
        activity.updateSwitchTheme(cbBlinkingAnimationEnabled, accentColor);
        activity.updateSwitchTheme(cbHideLaneLineBgEnabled, accentColor);
        activity.updateSwitchTheme(cbHideCameraCapsuleBgEnabled, accentColor);
        activity.updateSwitchTheme(cbClusterMirrorEnabled, accentColor);
        if (tvClusterDisplaySelectLabel != null) {
            tvClusterDisplaySelectLabel.setTextColor(activity.getThemeColorAttr(R.attr.panelTextColorPrimary));
        }
        activity.updateSwitchTheme(cbHideMainWhenClusterActive, accentColor);
    }
}
