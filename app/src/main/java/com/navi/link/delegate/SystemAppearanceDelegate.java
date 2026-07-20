package com.navi.link.delegate;
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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.card.MaterialCardView;

public class SystemAppearanceDelegate {
    private final MainActivity activity;

    private MaterialCardView cardNormal, cardMinimal, cardFull;
    private RadioButton rbNormal, rbMinimal, rbFull;
    
    private MaterialCardView cardCruiseNormal, cardCruiseMinimal, cardCruiseFull;
    private RadioButton rbCruiseNormal, rbCruiseMinimal, rbCruiseFull;
    
    private MaterialCardView cardNormalStart, cardServiceOnly, cardStartAmap;
    private RadioButton rbNormalStart, rbServiceOnly, rbStartAmap;
    private TextView tvStartAmapDesc;
    
    private SeekBar sbScale, sbClusterScale;
    private TextView tvScaleValue, tvClusterScaleValue;
    
    private SwitchCompat cbCruiseEnabled;
    private TextView tvCruiseStatus;
    private MaterialCardView cardCruiseToggle;
    
    private SwitchCompat cbAutoStartEnabled;
    private TextView tvAutoStartStatus;
    private MaterialCardView cardAutoStartToggle;
    
    private SwitchCompat cbOverspeedWarningEnabled;
    private TextView tvOverspeedWarningStatus;
    private MaterialCardView cardOverspeedWarningToggle;
    private LinearLayout llOverspeedThresholdRow;
    private View[] overspeedThresholdChips;
    


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
        
        
        setupListeners();
    }
    
    private void setupListeners() {
        cardServiceOnly.setOnClickListener(v -> activity.selectStartupMode(1));
        cardNormalStart.setOnClickListener(v -> activity.selectStartupMode(0));
        if (cardStartAmap != null) cardStartAmap.setOnClickListener(v -> activity.selectStartupMode(2));
        
        cardNormal.setOnClickListener(v -> activity.selectStyle(0));
        cardMinimal.setOnClickListener(v -> activity.selectStyle(1));
        cardFull.setOnClickListener(v -> activity.selectStyle(2));
        
        cardCruiseNormal.setOnClickListener(v -> activity.selectCruiseStyle(0));
        cardCruiseMinimal.setOnClickListener(v -> activity.selectCruiseStyle(1));
        cardCruiseFull.setOnClickListener(v -> activity.selectCruiseStyle(2));
        
        cbCruiseEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.cruiseEnabled = isChecked;
            activity.savePreferences();
            if (tvCruiseStatus != null) tvCruiseStatus.setText(isChecked ? "巡航窗已启用" : "巡航窗已禁用");
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                if (isChecked) {
                    fwm.show();
                } else if (fwm.getCurrentMode() == FloatingWindowManager.MODE_CRUISE) {
                    fwm.hide();
                }
            }
        });
        if (cardCruiseToggle != null) {
            cardCruiseToggle.setOnClickListener(v -> cbCruiseEnabled.toggle());
        }
        
        cbAutoStartEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.autoStartEnabled = isChecked;
            activity.savePreferences();
            if (tvAutoStartStatus != null) {
                tvAutoStartStatus.setText(isChecked ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
            }
        });
        if (cardAutoStartToggle != null) {
            cardAutoStartToggle.setOnClickListener(v -> cbAutoStartEnabled.toggle());
        }
        
        cbOverspeedWarningEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.overspeedWarningEnabled = isChecked;
            activity.savePreferences();
            if (tvOverspeedWarningStatus != null) {
                tvOverspeedWarningStatus.setText(isChecked ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
            }
            if (llOverspeedThresholdRow != null) {
                llOverspeedThresholdRow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        });
        if (cardOverspeedWarningToggle != null) {
            cardOverspeedWarningToggle.setOnClickListener(v -> cbOverspeedWarningEnabled.toggle());
        }
        
        int[] thresholdValues = {0, 10, 20, 30, 50};
        for (int i = 0; i < overspeedThresholdChips.length; i++) {
            final int value = thresholdValues[i];
            chipClickListener(overspeedThresholdChips[i], value);
        }
        
        sbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float currentScale = (progress / 15.0f) * 1.5f + 0.5f;
                tvScaleValue.setText(String.format("%.1fx", currentScale));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float newScale = (seekBar.getProgress() / 15.0f) * 1.5f + 0.5f;
                FloatingWindowManager manager = FloatingWindowManager.getInstance();
                if (manager != null) {
                    manager.updateScale(newScale);
                }
            }
        });

        if (sbClusterScale != null) {
            sbClusterScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float currentScale = (progress / 15.0f) * 1.5f + 0.5f;
                    if (tvClusterScaleValue != null) {
                        tvClusterScaleValue.setText(String.format("%.1fx", currentScale));
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    float newScale = (seekBar.getProgress() / 15.0f) * 1.5f + 0.5f;
                    FloatingWindowManager manager = FloatingWindowManager.getInstance();
                    if (manager != null) {
                        manager.setClusterScale(newScale);
                    }
                }
            });
        }
    }
    
    private void chipClickListener(View chip, final int value) {
        chip.setOnClickListener(v -> {
            activity.overspeedThreshold = value;
            activity.savePreferences();
            activity.updateThresholdChips();
            FloatingWindowManager fwm = FloatingWindowManager.getInstance();
            if (fwm != null) {
                fwm.refreshWindow();
            }
        });
    }
    
    public void loadSettings() {
        cbCruiseEnabled.setChecked(activity.cruiseEnabled);
        if (tvCruiseStatus != null) tvCruiseStatus.setText(activity.cruiseEnabled ? "巡航窗已启用" : "巡航窗已禁用");
        
        cbAutoStartEnabled.setChecked(activity.autoStartEnabled);
        if (tvAutoStartStatus != null) {
            tvAutoStartStatus.setText(activity.autoStartEnabled ? "已启用开机自启（如未生效，请在车机设置中允许本应用的自启动权限）" : "已关闭开机自启功能");
        }
        
        cbOverspeedWarningEnabled.setChecked(activity.overspeedWarningEnabled);
        if (tvOverspeedWarningStatus != null) {
            tvOverspeedWarningStatus.setText(activity.overspeedWarningEnabled ? "超速时车速红色报警并闪烁" : "已关闭超速红色提醒");
        }
        if (llOverspeedThresholdRow != null) {
            llOverspeedThresholdRow.setVisibility(activity.overspeedWarningEnabled ? View.VISIBLE : View.GONE);
        }
        
        updateStartupSelection();
        updateStyleSelection();
        updateCruiseStyleSelection();
        updateSeekBarToCurrentScale();
        activity.updateThresholdChips();
    }
    
    public void updateStartupSelection() {
        rbNormalStart.setChecked(activity.startupMode == 0);
        rbServiceOnly.setChecked(activity.startupMode == 1);
        if (rbStartAmap != null) rbStartAmap.setChecked(activity.startupMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(com.navi.link.R.attr.panelCardStrokeColor);
        cardNormalStart.setStrokeColor(activity.startupMode == 0 ? accentColor : defaultStroke);
        cardServiceOnly.setStrokeColor(activity.startupMode == 1 ? accentColor : defaultStroke);
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
        rbNormal.setChecked(activity.styleMode == 0);
        rbMinimal.setChecked(activity.styleMode == 1);
        rbFull.setChecked(activity.styleMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(com.navi.link.R.attr.panelCardStrokeColor);
        cardNormal.setStrokeColor(activity.styleMode == 0 ? accentColor : defaultStroke);
        cardMinimal.setStrokeColor(activity.styleMode == 1 ? accentColor : defaultStroke);
        cardFull.setStrokeColor(activity.styleMode == 2 ? accentColor : defaultStroke);
    }
    
    public void updateCruiseStyleSelection() {
        rbCruiseNormal.setChecked(activity.cruiseStyleMode == 0);
        rbCruiseMinimal.setChecked(activity.cruiseStyleMode == 1);
        rbCruiseFull.setChecked(activity.cruiseStyleMode == 2);
        
        int accentColor = activity.getAccentColor();
        int defaultStroke = activity.getThemeColorAttr(com.navi.link.R.attr.panelCardStrokeColor);
        cardCruiseNormal.setStrokeColor(activity.cruiseStyleMode == 0 ? accentColor : defaultStroke);
        cardCruiseMinimal.setStrokeColor(activity.cruiseStyleMode == 1 ? accentColor : defaultStroke);
        cardCruiseFull.setStrokeColor(activity.cruiseStyleMode == 2 ? accentColor : defaultStroke);
    }
    
    public void updateSeekBarToCurrentScale() {
        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        float currentScale = (manager != null) ? manager.getScale() : 1.0f;
        int progress = Math.round(((currentScale - 0.5f) / 1.5f) * 15.0f);
        if (progress < 0) progress = 0;
        if (progress > 15) progress = 15;
        sbScale.setProgress(progress);
        tvScaleValue.setText(String.format("%.1fx", currentScale));

        float clusterScale = (manager != null) ? manager.getClusterScale() : 1.0f;
        int progressCluster = Math.round(((clusterScale - 0.5f) / 1.5f) * 15.0f);
        if (progressCluster < 0) progressCluster = 0;
        if (progressCluster > 15) progressCluster = 15;
        if (sbClusterScale != null) {
            sbClusterScale.setProgress(progressCluster);
        }
        if (tvClusterScaleValue != null) {
            tvClusterScaleValue.setText(String.format("%.1fx", clusterScale));
        }
    }
    

    public void updateThemeColors(int accentColor, android.content.res.ColorStateList accentColorStateList) {
        activity.updateSwitchTheme(cbCruiseEnabled, accentColor);
        activity.updateSwitchTheme(cbAutoStartEnabled, accentColor);
        activity.updateSwitchTheme(cbOverspeedWarningEnabled, accentColor);
        
        if (sbScale.getProgressDrawable() != null) {
            Drawable progressDrawable = DrawableCompat.wrap(sbScale.getProgressDrawable().mutate());
            DrawableCompat.setTint(progressDrawable, accentColor);
            sbScale.setProgressDrawable(progressDrawable);
        }
        if (sbScale.getThumb() != null) {
            Drawable thumbDrawable = DrawableCompat.wrap(sbScale.getThumb().mutate());
            DrawableCompat.setTintList(thumbDrawable, accentColorStateList);
            sbScale.setThumb(thumbDrawable);
        }
        tvScaleValue.setTextColor(accentColor);

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
        cardNormalStart.setStrokeColor(mode == 0 ? accentColor : Color.parseColor("#444444"));
        cardServiceOnly.setStrokeColor(mode == 1 ? accentColor : Color.parseColor("#444444"));
        if (cardStartAmap != null) cardStartAmap.setStrokeColor(mode == 2 ? accentColor : Color.parseColor("#444444"));

        int sMode = activity.styleMode;
        cardNormal.setStrokeColor(sMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardMinimal.setStrokeColor(sMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardFull.setStrokeColor(sMode == 2 ? accentColor : Color.parseColor("#444444"));

        int cMode = activity.cruiseStyleMode;
        cardCruiseNormal.setStrokeColor(cMode == 0 ? accentColor : Color.parseColor("#444444"));
        cardCruiseMinimal.setStrokeColor(cMode == 1 ? accentColor : Color.parseColor("#444444"));
        cardCruiseFull.setStrokeColor(cMode == 2 ? accentColor : Color.parseColor("#444444"));
        
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
    }
}
