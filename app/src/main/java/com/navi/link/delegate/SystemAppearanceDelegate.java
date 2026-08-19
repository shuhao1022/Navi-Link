package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.window.FloatingWindowManager;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.navi.link.view.SwitchButton;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.card.MaterialCardView;

public class SystemAppearanceDelegate {
    private final MainActivity activity;

    private MaterialCardView cardNormal, cardMinimal, cardFull;
    private RadioButton rbNormal, rbMinimal, rbFull;
    
    private MaterialCardView cardCruiseNormal, cardCruiseMinimal, cardCruiseFull;
    private RadioButton rbCruiseNormal, rbCruiseMinimal, rbCruiseFull;
    
    private MaterialCardView cardDayNightFollow, cardDayNightDay, cardDayNightNight;
    private RadioButton rbDayNightFollow, rbDayNightDay, rbDayNightNight;
    
    private MaterialCardView cardNormalStart, cardServiceOnly, cardStartAmap;
    private RadioButton rbNormalStart, rbServiceOnly, rbStartAmap;
    private TextView tvStartAmapDesc;
    
    private SeekBar sbScale, sbClusterScale;
    private TextView tvScaleValue, tvClusterScaleValue;
    
    private SwitchButton cbCruiseEnabled;
    private TextView tvCruiseStatus;
    private MaterialCardView cardCruiseToggle;
    
    private SwitchButton cbAutoStartEnabled;
    private TextView tvAutoStartStatus;
    private MaterialCardView cardAutoStartToggle;
    
    private SwitchButton cbOverspeedWarningEnabled;
    private TextView tvOverspeedWarningStatus;
    private MaterialCardView cardOverspeedWarningToggle;
    private LinearLayout llOverspeedThresholdRow;
    private View[] overspeedThresholdChips;
    private View btnExportConfig, btnImportConfig, btnResetConfig;

    public SystemAppearanceDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        cardNormal = activity.findViewById(R.id.card_normal);
        cardMinimal = activity.findViewById(R.id.card_minimal);
        cardFull = activity.findViewById(R.id.card_full);
        
        rbNormal = activity.findViewById(R.id.rb_normal);
        rbMinimal = activity.findViewById(R.id.rb_minimal);
        rbFull = activity.findViewById(R.id.rb_full);
        
        cardCruiseNormal = activity.findViewById(R.id.card_cruise_normal);
        cardCruiseMinimal = activity.findViewById(R.id.card_cruise_minimal);
        cardCruiseFull = activity.findViewById(R.id.card_cruise_full);
        
        rbCruiseNormal = activity.findViewById(R.id.rb_cruise_normal);
        rbCruiseMinimal = activity.findViewById(R.id.rb_cruise_minimal);
        rbCruiseFull = activity.findViewById(R.id.rb_cruise_full);

        cardDayNightFollow = activity.findViewById(R.id.card_day_night_follow);
        cardDayNightDay = activity.findViewById(R.id.card_day_night_day);
        cardDayNightNight = activity.findViewById(R.id.card_day_night_night);

        rbDayNightFollow = activity.findViewById(R.id.rb_day_night_follow);
        rbDayNightDay = activity.findViewById(R.id.rb_day_night_day);
        rbDayNightNight = activity.findViewById(R.id.rb_day_night_night);
        
        cardNormalStart = activity.findViewById(R.id.card_normal_start);
        cardServiceOnly = activity.findViewById(R.id.card_service_only);
        cardStartAmap = activity.findViewById(R.id.card_start_amap);
        
        rbNormalStart = activity.findViewById(R.id.rb_normal_start);
        rbServiceOnly = activity.findViewById(R.id.rb_service_only);
        rbStartAmap = activity.findViewById(R.id.rb_start_amap);
        tvStartAmapDesc = activity.findViewById(R.id.tv_start_amap_desc);
        
        sbScale = activity.findViewById(R.id.sb_scale);
        sbClusterScale = activity.findViewById(R.id.sb_cluster_scale);
        tvScaleValue = activity.findViewById(R.id.tv_scale_value);
        tvClusterScaleValue = activity.findViewById(R.id.tv_cluster_scale_value);
        
        cbCruiseEnabled = activity.findViewById(R.id.cb_cruise_enabled);
        tvCruiseStatus = activity.findViewById(R.id.tv_cruise_status);
        cardCruiseToggle = activity.findViewById(R.id.card_cruise_toggle);
        
        cardAutoStartToggle = activity.findViewById(R.id.card_auto_start_toggle);
        cbAutoStartEnabled = activity.findViewById(R.id.cb_auto_start_enabled);
        tvAutoStartStatus = activity.findViewById(R.id.tv_auto_start_status);
        
        cbOverspeedWarningEnabled = activity.findViewById(R.id.cb_overspeed_warning_enabled);
        tvOverspeedWarningStatus = activity.findViewById(R.id.tv_overspeed_warning_status);
        cardOverspeedWarningToggle = activity.findViewById(R.id.card_overspeed_warning_toggle);
        llOverspeedThresholdRow = activity.findViewById(R.id.ll_overspeed_threshold_row);
        
        overspeedThresholdChips = new View[]{
                activity.findViewById(R.id.chip_overspeed_0),
                activity.findViewById(R.id.chip_overspeed_10),
                activity.findViewById(R.id.chip_overspeed_20),
                activity.findViewById(R.id.chip_overspeed_30),
                activity.findViewById(R.id.chip_overspeed_50)
        };

        btnExportConfig = activity.findViewById(R.id.btn_export_config);
        btnImportConfig = activity.findViewById(R.id.btn_import_config);
        btnResetConfig = activity.findViewById(R.id.btn_reset_config);
        
        setupListeners();
    }
    
    public void setupListeners() {
        if (cardServiceOnly != null) cardServiceOnly.setOnClickListener(v -> activity.selectStartupMode(1));
        if (cardNormalStart != null) cardNormalStart.setOnClickListener(v -> activity.selectStartupMode(0));
        if (cardStartAmap != null) cardStartAmap.setOnClickListener(v -> activity.selectStartupMode(2));

        if (btnExportConfig != null) btnExportConfig.setOnClickListener(v -> activity.exportConfig());
        if (btnImportConfig != null) btnImportConfig.setOnClickListener(v -> activity.importConfig());
        if (btnResetConfig != null) btnResetConfig.setOnClickListener(v -> activity.resetDefaultConfig());
        
        if (cardNormal != null) cardNormal.setOnClickListener(v -> activity.selectStyle(0));
        if (cardMinimal != null) cardMinimal.setOnClickListener(v -> activity.selectStyle(1));
        if (cardFull != null) cardFull.setOnClickListener(v -> activity.selectStyle(2));
        
        if (cardCruiseNormal != null) cardCruiseNormal.setOnClickListener(v -> activity.selectCruiseStyle(0));
        if (cardCruiseMinimal != null) cardCruiseMinimal.setOnClickListener(v -> activity.selectCruiseStyle(1));
        if (cardCruiseFull != null) cardCruiseFull.setOnClickListener(v -> activity.selectCruiseStyle(2));

        if (cardDayNightFollow != null) cardDayNightFollow.setOnClickListener(v -> activity.selectDayNightOption(0));
        if (cardDayNightDay != null) cardDayNightDay.setOnClickListener(v -> activity.selectDayNightOption(1));
        if (cardDayNightNight != null) cardDayNightNight.setOnClickListener(v -> activity.selectDayNightOption(2));
        
        if (cbCruiseEnabled != null) {
            cbCruiseEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.cruiseEnabled = isChecked;
                activity.savePreferences();
                if (tvCruiseStatus != null) tvCruiseStatus.setText(isChecked ? "巡航窗已启用" : "巡航窗已禁用");
                FloatingWindowManager fwm = FloatingWindowManager.getInstance();
                if (fwm != null) {
                    fwm.refreshWindow();
                }
            });
        }

        if (cardCruiseToggle != null) {
            cardCruiseToggle.setOnClickListener(v -> {
                if (cbCruiseEnabled != null) cbCruiseEnabled.toggle();
            });
        }
        
        if (cbAutoStartEnabled != null) {
            cbAutoStartEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.autoStartEnabled = isChecked;
                activity.savePreferences();
                if (tvAutoStartStatus != null) {
                    tvAutoStartStatus.setText(isChecked ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
                }
            });
        }
        
        if (cardAutoStartToggle != null) {
            cardAutoStartToggle.setOnClickListener(v -> {
                if (cbAutoStartEnabled != null) cbAutoStartEnabled.toggle();
            });
        }
        
        if (cbOverspeedWarningEnabled != null) {
            cbOverspeedWarningEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.overspeedWarningEnabled = isChecked;
                activity.savePreferences();
                if (tvOverspeedWarningStatus != null) {
                    tvOverspeedWarningStatus.setText(isChecked ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
                }
                if (llOverspeedThresholdRow != null) {
                    llOverspeedThresholdRow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                activity.refreshFloatingWindow();
            });
        }
        
        if (cardOverspeedWarningToggle != null) {
            cardOverspeedWarningToggle.setOnClickListener(v -> {
                if (cbOverspeedWarningEnabled != null) cbOverspeedWarningEnabled.toggle();
            });
        }
        
        if (overspeedThresholdChips != null) {
            int[] thresholds = {0, 10, 20, 30, 50};
            for (int i = 0; i < overspeedThresholdChips.length; i++) {
                final int th = thresholds[i];
                if (overspeedThresholdChips[i] != null) {
                    overspeedThresholdChips[i].setOnClickListener(v -> {
                        activity.overspeedThreshold = th;
                        activity.savePreferences();
                        updateThresholdChips();
                        activity.refreshFloatingWindow();
                    });
                }
            }
        }
        
        if (sbScale != null) {
            sbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        float scale = 0.5f + (progress / 15.0f) * 1.5f;
                        if (tvScaleValue != null) tvScaleValue.setText(String.format("%.1fx", scale));
                        FloatingWindowManager manager = FloatingWindowManager.getInstance();
                        if (manager != null) {
                            manager.updateScale(scale);
                        }
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (sbClusterScale != null) {
            sbClusterScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        float scale = 0.1f + (progress / 19.0f) * 1.9f;
                        if (tvClusterScaleValue != null) {
                            tvClusterScaleValue.setText(String.format("%.1fx", scale));
                        }
                        FloatingWindowManager manager = FloatingWindowManager.getInstance();
                        if (manager != null) {
                            manager.setClusterScale(scale);
                        }
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }
    
    public void loadSettings() {
        if (cbCruiseEnabled != null) cbCruiseEnabled.setChecked(activity.cruiseEnabled);
        if (tvCruiseStatus != null) tvCruiseStatus.setText(activity.cruiseEnabled ? "巡航窗已启用" : "巡航窗已禁用");
        
        if (cbAutoStartEnabled != null) cbAutoStartEnabled.setChecked(activity.autoStartEnabled);
        if (tvAutoStartStatus != null) {
            tvAutoStartStatus.setText(activity.autoStartEnabled ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
        }
        
        if (cbOverspeedWarningEnabled != null) cbOverspeedWarningEnabled.setChecked(activity.overspeedWarningEnabled);
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(activity.overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        if (llOverspeedThresholdRow != null) {
            llOverspeedThresholdRow.setVisibility(activity.overspeedWarningEnabled ? View.VISIBLE : View.GONE);
        }
        
        updateStartupSelection();
        updateStyleSelection();
        updateCruiseStyleSelection();
        updateDayNightSelection();
        updateSeekBarToCurrentScale();
        updateThresholdChips();
    }
    
    public void updateStartupSelection() {
        if (rbNormalStart != null) rbNormalStart.setChecked(activity.startupMode == 0);
        if (rbServiceOnly != null) rbServiceOnly.setChecked(activity.startupMode == 1);
        if (rbStartAmap != null) rbStartAmap.setChecked(activity.startupMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(R.attr.panelCardStrokeColor);
        if (cardNormalStart != null) cardNormalStart.setStrokeColor(activity.startupMode == 0 ? accentColor : defaultStroke);
        if (cardServiceOnly != null) cardServiceOnly.setStrokeColor(activity.startupMode == 1 ? accentColor : defaultStroke);
        if (cardStartAmap != null) cardStartAmap.setStrokeColor(activity.startupMode == 2 ? accentColor : defaultStroke);
        
        if (tvStartAmapDesc != null) {
            if (activity.targetAmapPackage != null && !activity.targetAmapPackage.isEmpty()) {
                tvStartAmapDesc.setText("已选: " + activity.targetAmapPackage);
            } else {
                tvStartAmapDesc.setText("点击选择高德地图");
            }
        }
    }
    
    public void updateStyleSelection() {
        if (rbNormal != null) rbNormal.setChecked(activity.styleMode == 0);
        if (rbMinimal != null) rbMinimal.setChecked(activity.styleMode == 1);
        if (rbFull != null) rbFull.setChecked(activity.styleMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(R.attr.panelCardStrokeColor);
        if (cardNormal != null) cardNormal.setStrokeColor(activity.styleMode == 0 ? accentColor : defaultStroke);
        if (cardMinimal != null) cardMinimal.setStrokeColor(activity.styleMode == 1 ? accentColor : defaultStroke);
        if (cardFull != null) cardFull.setStrokeColor(activity.styleMode == 2 ? accentColor : defaultStroke);
    }
    
    public void updateCruiseStyleSelection() {
        if (rbCruiseNormal != null) rbCruiseNormal.setChecked(activity.cruiseStyleMode == 0);
        if (rbCruiseMinimal != null) rbCruiseMinimal.setChecked(activity.cruiseStyleMode == 1);
        if (rbCruiseFull != null) rbCruiseFull.setChecked(activity.cruiseStyleMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(R.attr.panelCardStrokeColor);
        if (cardCruiseNormal != null) cardCruiseNormal.setStrokeColor(activity.cruiseStyleMode == 0 ? accentColor : defaultStroke);
        if (cardCruiseMinimal != null) cardCruiseMinimal.setStrokeColor(activity.cruiseStyleMode == 1 ? accentColor : defaultStroke);
        if (cardCruiseFull != null) cardCruiseFull.setStrokeColor(activity.cruiseStyleMode == 2 ? accentColor : defaultStroke);
    }

    public void updateDayNightSelection() {
        if (rbDayNightFollow != null) rbDayNightFollow.setChecked(activity.dayNightOption == 0);
        if (rbDayNightDay != null) rbDayNightDay.setChecked(activity.dayNightOption == 1);
        if (rbDayNightNight != null) rbDayNightNight.setChecked(activity.dayNightOption == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(R.attr.panelCardStrokeColor);
        if (cardDayNightFollow != null) cardDayNightFollow.setStrokeColor(activity.dayNightOption == 0 ? accentColor : defaultStroke);
        if (cardDayNightDay != null) cardDayNightDay.setStrokeColor(activity.dayNightOption == 1 ? accentColor : defaultStroke);
        if (cardDayNightNight != null) cardDayNightNight.setStrokeColor(activity.dayNightOption == 2 ? accentColor : defaultStroke);
    }

    public void updateThresholdChips() {
        if (overspeedThresholdChips == null) return;
        int accentColor = activity.getAccentColor();
        int[] thresholds = {0, 10, 20, 30, 50};
        for (int i = 0; i < overspeedThresholdChips.length; i++) {
            View chip = overspeedThresholdChips[i];
            if (chip instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) chip;
                boolean selected = (activity.overspeedThreshold == thresholds[i]);
                card.setCardBackgroundColor(selected ? accentColor : Color.TRANSPARENT);
            }
        }
    }
    
    public void updateSeekBarToCurrentScale() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        float currentScale = (manager != null) ? manager.getScale() : 1.0f;
        int progress = Math.round(((currentScale - 0.5f) / 1.5f) * 15.0f);
        if (progress < 0) progress = 0;
        if (progress > 15) progress = 15;
        if (sbScale != null) sbScale.setProgress(progress);
        if (tvScaleValue != null) tvScaleValue.setText(String.format("%.1fx", currentScale));

        float clusterScale = (manager != null) ? manager.getClusterScale() : 1.0f;
        int progressCluster = Math.round(((clusterScale - 0.1f) / 1.9f) * 19.0f);
        if (progressCluster < 0) progressCluster = 0;
        if (progressCluster > 19) progressCluster = 19;
        if (sbClusterScale != null) {
            sbClusterScale.setMax(19);
            sbClusterScale.setProgress(progressCluster);
        }
        if (tvClusterScaleValue != null) {
            tvClusterScaleValue.setText(String.format("%.1fx", clusterScale));
        }
    }
    
    public void updateThemeColors() {
        int accentColor = activity.getAccentColor();
        updateThemeColors(accentColor, android.content.res.ColorStateList.valueOf(accentColor));
    }

    public void updateThemeColors(int accentColor, android.content.res.ColorStateList accentColorStateList) {
        activity.updateSwitchTheme(cbCruiseEnabled, accentColor);
        activity.updateSwitchTheme(cbAutoStartEnabled, accentColor);
        activity.updateSwitchTheme(cbOverspeedWarningEnabled, accentColor);
        
        if (sbScale != null && sbScale.getProgressDrawable() != null) {
            Drawable progressDrawable = DrawableCompat.wrap(sbScale.getProgressDrawable().mutate());
            DrawableCompat.setTint(progressDrawable, accentColor);
            sbScale.setProgressDrawable(progressDrawable);
        }
        if (sbScale != null && sbScale.getThumb() != null) {
            Drawable thumbDrawable = DrawableCompat.wrap(sbScale.getThumb().mutate());
            DrawableCompat.setTintList(thumbDrawable, accentColorStateList);
            sbScale.setThumb(thumbDrawable);
        }
        if (tvScaleValue != null) tvScaleValue.setTextColor(accentColor);

        if (sbClusterScale != null) {
            if (sbClusterScale.getProgressDrawable() != null) {
                Drawable progressDrawable = DrawableCompat.wrap(sbClusterScale.getProgressDrawable().mutate());
                DrawableCompat.setTint(progressDrawable, accentColor);
                sbClusterScale.setProgressDrawable(progressDrawable);
            }
            if (sbClusterScale.getThumb() != null) {
                Drawable thumbDrawable = DrawableCompat.wrap(sbClusterScale.getThumb().mutate());
                DrawableCompat.setTintList(thumbDrawable, accentColorStateList);
                sbClusterScale.setThumb(thumbDrawable);
            }
        }
        if (tvClusterScaleValue != null) {
            tvClusterScaleValue.setTextColor(accentColor);
        }
        
        int mode = activity.startupMode;
        if (cardNormalStart != null) cardNormalStart.setStrokeColor(mode == 0 ? accentColor : Color.parseColor("#444444"));
        if (cardServiceOnly != null) cardServiceOnly.setStrokeColor(mode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardStartAmap != null) cardStartAmap.setStrokeColor(mode == 2 ? accentColor : Color.parseColor("#444444"));

        int sMode = activity.styleMode;
        if (cardNormal != null) cardNormal.setStrokeColor(sMode == 0 ? accentColor : Color.parseColor("#444444"));
        if (cardMinimal != null) cardMinimal.setStrokeColor(sMode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardFull != null) cardFull.setStrokeColor(sMode == 2 ? accentColor : Color.parseColor("#444444"));

        int cMode = activity.cruiseStyleMode;
        if (cardCruiseNormal != null) cardCruiseNormal.setStrokeColor(cMode == 0 ? accentColor : Color.parseColor("#444444"));
        if (cardCruiseMinimal != null) cardCruiseMinimal.setStrokeColor(cMode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardCruiseFull != null) cardCruiseFull.setStrokeColor(cMode == 2 ? accentColor : Color.parseColor("#444444"));
        
        RadioButton rbServiceOnlyView = activity.findViewById(R.id.rb_service_only);
        RadioButton rbNormalStartView = activity.findViewById(R.id.rb_normal_start);
        RadioButton rbStartAmapView = activity.findViewById(R.id.rb_start_amap);
        if (rbServiceOnlyView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbServiceOnlyView, accentColorStateList);
        if (rbNormalStartView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbNormalStartView, accentColorStateList);
        if (rbStartAmapView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbStartAmapView, accentColorStateList);

        RadioButton rbNormalView = activity.findViewById(R.id.rb_normal);
        RadioButton rbMinimalView = activity.findViewById(R.id.rb_minimal);
        RadioButton rbFullView = activity.findViewById(R.id.rb_full);
        if (rbNormalView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbNormalView, accentColorStateList);
        if (rbMinimalView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbMinimalView, accentColorStateList);
        if (rbFullView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbFullView, accentColorStateList);

        RadioButton rbCruiseNormalView = activity.findViewById(R.id.rb_cruise_normal);
        RadioButton rbCruiseMinimalView = activity.findViewById(R.id.rb_cruise_minimal);
        RadioButton rbCruiseFullView = activity.findViewById(R.id.rb_cruise_full);
        if (rbCruiseNormalView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbCruiseNormalView, accentColorStateList);
        if (rbCruiseMinimalView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbCruiseMinimalView, accentColorStateList);
        if (rbCruiseFullView != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbCruiseFullView, accentColorStateList);

        if (rbDayNightFollow != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbDayNightFollow, accentColorStateList);
        if (rbDayNightDay != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbDayNightDay, accentColorStateList);
        if (rbDayNightNight != null) androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbDayNightNight, accentColorStateList);
    }
}
