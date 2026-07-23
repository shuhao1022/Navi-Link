package com.navi.link.delegate;

import com.navi.link.R;
import com.navi.link.activity.MainActivity;
import com.navi.link.view.ColorWheelView;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

public class ColorSettingsDelegate {
    private final MainActivity activity;

    private View viewColorPreviewBgDay;
    private View viewColorPreviewBgNight;
    private View viewColorPreviewPrimaryDay;
    private View viewColorPreviewPrimaryNight;
    private View viewColorPreviewSecondaryDay;
    private View viewColorPreviewSecondaryNight;
    private View viewColorPreviewHintDay;
    private View viewColorPreviewHintNight;
    private View viewColorPreviewNormalTurnIconDay;
    private View viewColorPreviewNormalTurnIconNight;
    private View viewColorPreviewNormalTurnBgDay;
    private View viewColorPreviewNormalTurnBgNight;
    private View viewColorPreviewFullMiddleBgDay;
    private View viewColorPreviewFullMiddleBgNight;
    private View viewColorPreviewLaneIconDay;
    private View viewColorPreviewLaneIconNight;
    private MaterialCardView btnResetColors;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorSettingsDelegate(MainActivity activity) {
        this.activity = activity;
    }

    public void initViews() {
        viewColorPreviewBgDay = activity.findViewById(R.id.view_color_preview_bg_day);
        viewColorPreviewBgNight = activity.findViewById(R.id.view_color_preview_bg_night);
        viewColorPreviewPrimaryDay = activity.findViewById(R.id.view_color_preview_primary_day);
        viewColorPreviewPrimaryNight = activity.findViewById(R.id.view_color_preview_primary_night);
        viewColorPreviewSecondaryDay = activity.findViewById(R.id.view_color_preview_secondary_day);
        viewColorPreviewSecondaryNight = activity.findViewById(R.id.view_color_preview_secondary_night);
        viewColorPreviewHintDay = activity.findViewById(R.id.view_color_preview_hint_day);
        viewColorPreviewHintNight = activity.findViewById(R.id.view_color_preview_hint_night);
        viewColorPreviewNormalTurnIconDay = activity.findViewById(R.id.view_color_preview_normal_turn_icon_day);
        viewColorPreviewNormalTurnIconNight = activity.findViewById(R.id.view_color_preview_normal_turn_icon_night);
        viewColorPreviewNormalTurnBgDay = activity.findViewById(R.id.view_color_preview_normal_turn_bg_day);
        viewColorPreviewNormalTurnBgNight = activity.findViewById(R.id.view_color_preview_normal_turn_bg_night);
        viewColorPreviewFullMiddleBgDay = activity.findViewById(R.id.view_color_preview_full_middle_bg_day);
        viewColorPreviewFullMiddleBgNight = activity.findViewById(R.id.view_color_preview_full_middle_bg_night);
        viewColorPreviewLaneIconDay = activity.findViewById(R.id.view_color_preview_lane_icon_day);
        viewColorPreviewLaneIconNight = activity.findViewById(R.id.view_color_preview_lane_icon_night);
        btnResetColors = activity.findViewById(R.id.btn_reset_colors);

        setupListeners();
    }

    public void setupListeners() {
        if (viewColorPreviewBgDay != null) {
            viewColorPreviewBgDay.setOnClickListener(v -> showColorPickerDialog("白天背景色", activity.bgColorDay, color -> {
                activity.bgColorDay = color;
                updateColorPreview(viewColorPreviewBgDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewBgNight != null) {
            viewColorPreviewBgNight.setOnClickListener(v -> showColorPickerDialog("夜间背景色", activity.bgColorNight, color -> {
                activity.bgColorNight = color;
                updateColorPreview(viewColorPreviewBgNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewPrimaryDay != null) {
            viewColorPreviewPrimaryDay.setOnClickListener(v -> showColorPickerDialog("白天主文字颜色", activity.textPrimaryDay, color -> {
                activity.textPrimaryDay = color;
                updateColorPreview(viewColorPreviewPrimaryDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewPrimaryNight != null) {
            viewColorPreviewPrimaryNight.setOnClickListener(v -> showColorPickerDialog("夜间主文字颜色", activity.textPrimaryNight, color -> {
                activity.textPrimaryNight = color;
                updateColorPreview(viewColorPreviewPrimaryNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewSecondaryDay != null) {
            viewColorPreviewSecondaryDay.setOnClickListener(v -> showColorPickerDialog("白天次文字颜色", activity.textSecondaryDay, color -> {
                activity.textSecondaryDay = color;
                updateColorPreview(viewColorPreviewSecondaryDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewSecondaryNight != null) {
            viewColorPreviewSecondaryNight.setOnClickListener(v -> showColorPickerDialog("夜间次文字颜色", activity.textSecondaryNight, color -> {
                activity.textSecondaryNight = color;
                updateColorPreview(viewColorPreviewSecondaryNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewHintDay != null) {
            viewColorPreviewHintDay.setOnClickListener(v -> showColorPickerDialog("白天提示文字颜色", activity.textHintDay, color -> {
                activity.textHintDay = color;
                updateColorPreview(viewColorPreviewHintDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewHintNight != null) {
            viewColorPreviewHintNight.setOnClickListener(v -> showColorPickerDialog("夜间提示文字颜色", activity.textHintNight, color -> {
                activity.textHintNight = color;
                updateColorPreview(viewColorPreviewHintNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }

        if (viewColorPreviewNormalTurnIconDay != null) {
            viewColorPreviewNormalTurnIconDay.setOnClickListener(v -> showColorPickerDialog("常规转向图标白天颜色", activity.normalTurnIconColorDay, color -> {
                activity.normalTurnIconColorDay = color;
                updateColorPreview(viewColorPreviewNormalTurnIconDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnIconNight != null) {
            viewColorPreviewNormalTurnIconNight.setOnClickListener(v -> showColorPickerDialog("常规转向图标夜间颜色", activity.normalTurnIconColorNight, color -> {
                activity.normalTurnIconColorNight = color;
                updateColorPreview(viewColorPreviewNormalTurnIconNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnBgDay != null) {
            viewColorPreviewNormalTurnBgDay.setOnClickListener(v -> showColorPickerDialog("常规转向背景白天颜色", activity.normalTurnIconBgColorDay, color -> {
                activity.normalTurnIconBgColorDay = color;
                updateColorPreview(viewColorPreviewNormalTurnBgDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewNormalTurnBgNight != null) {
            viewColorPreviewNormalTurnBgNight.setOnClickListener(v -> showColorPickerDialog("常规转向背景夜间颜色", activity.normalTurnIconBgColorNight, color -> {
                activity.normalTurnIconBgColorNight = color;
                updateColorPreview(viewColorPreviewNormalTurnBgNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewFullMiddleBgDay != null) {
            viewColorPreviewFullMiddleBgDay.setOnClickListener(v -> showColorPickerDialog("全数据卡片白天背景", activity.fullMiddleBgColorDay, color -> {
                activity.fullMiddleBgColorDay = color;
                updateColorPreview(viewColorPreviewFullMiddleBgDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewFullMiddleBgNight != null) {
            viewColorPreviewFullMiddleBgNight.setOnClickListener(v -> showColorPickerDialog("全数据卡片夜间背景", activity.fullMiddleBgColorNight, color -> {
                activity.fullMiddleBgColorNight = color;
                updateColorPreview(viewColorPreviewFullMiddleBgNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }

        if (viewColorPreviewLaneIconDay != null) {
            viewColorPreviewLaneIconDay.setOnClickListener(v -> showColorPickerDialog("车道线图标白天颜色", activity.laneIconColorDay, color -> {
                activity.laneIconColorDay = color;
                updateColorPreview(viewColorPreviewLaneIconDay, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }
        if (viewColorPreviewLaneIconNight != null) {
            viewColorPreviewLaneIconNight.setOnClickListener(v -> showColorPickerDialog("车道线图标夜间颜色", activity.laneIconColorNight, color -> {
                activity.laneIconColorNight = color;
                updateColorPreview(viewColorPreviewLaneIconNight, color);
                activity.savePreferences();
                activity.refreshFloatingWindow();
            }));
        }

        if (btnResetColors != null) {
            btnResetColors.setOnClickListener(v -> new android.app.AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage("确定要将所有色调配置恢复为默认吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        resetToDefaultColors();
                        activity.savePreferences();
                        activity.refreshFloatingWindow();
                        Toast.makeText(activity, "已恢复默认颜色配置", Toast.LENGTH_SHORT).show();
                    }).show());
        }
    }

    public void loadSettings() {
        updateColorPreviews();
    }

    public void updateThemeColors() {
        updateColorPreviews();
    }

    public void resetToDefaultColors() {
        activity.bgColorDay = 0xE6F5F5F5;
        activity.bgColorNight = 0xCC121212;
        activity.textPrimaryDay = 0xFF1A1A1A;
        activity.textPrimaryNight = 0xFFFFFFFF;
        activity.textSecondaryDay = 0xFF333333;
        activity.textSecondaryNight = 0xBBFFFFFF;
        activity.textHintDay = 0xFF999999;
        activity.textHintNight = 0xFF888888;
        activity.normalTurnIconColorDay = 0xFFFFFFFF;
        activity.normalTurnIconColorNight = 0xFFFFFFFF;
        activity.normalTurnIconBgColorDay = 0xFF007D5E;
        activity.normalTurnIconBgColorNight = 0xFF007D5E;
        activity.fullMiddleBgColorDay = 0xFF0099FF;
        activity.fullMiddleBgColorNight = 0xFF0099FF;
        activity.laneIconColorDay = 0xFFFFFFFF;
        activity.laneIconColorNight = 0xFFFFFFFF;
        updateColorPreviews();
    }

    public void updateColorPreview(View view, int color) {
        if (view != null) {
            view.setBackground(createRoundedRectDrawable(color));
        }
    }

    /**
     * 创建带圆角和自适应边框的圆形矩形 Drawable
     */
    private GradientDrawable createRoundedRectDrawable(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        float luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        int strokeColor = luminance > 0.5f ? 0x66000000 : 0x66FFFFFF;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(activity.dpToPx(4));
        drawable.setStroke(activity.dpToPx(1), strokeColor);
        return drawable;
    }

    public void updateColorPreviews() {
        updateColorPreview(viewColorPreviewBgDay, activity.bgColorDay);
        updateColorPreview(viewColorPreviewBgNight, activity.bgColorNight);
        updateColorPreview(viewColorPreviewPrimaryDay, activity.textPrimaryDay);
        updateColorPreview(viewColorPreviewPrimaryNight, activity.textPrimaryNight);
        updateColorPreview(viewColorPreviewSecondaryDay, activity.textSecondaryDay);
        updateColorPreview(viewColorPreviewSecondaryNight, activity.textSecondaryNight);
        updateColorPreview(viewColorPreviewHintDay, activity.textHintDay);
        updateColorPreview(viewColorPreviewHintNight, activity.textHintNight);
        updateColorPreview(viewColorPreviewNormalTurnIconDay, activity.normalTurnIconColorDay);
        updateColorPreview(viewColorPreviewNormalTurnIconNight, activity.normalTurnIconColorNight);
        updateColorPreview(viewColorPreviewNormalTurnBgDay, activity.normalTurnIconBgColorDay);
        updateColorPreview(viewColorPreviewNormalTurnBgNight, activity.normalTurnIconBgColorNight);
        updateColorPreview(viewColorPreviewFullMiddleBgDay, activity.fullMiddleBgColorDay);
        updateColorPreview(viewColorPreviewFullMiddleBgNight, activity.fullMiddleBgColorNight);
        updateColorPreview(viewColorPreviewLaneIconDay, activity.laneIconColorDay);
        updateColorPreview(viewColorPreviewLaneIconNight, activity.laneIconColorNight);
    }

    public void showColorPickerDialog(String title, int initialColor, OnColorSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_color_picker, null);
        dialog.setContentView(dialogView);

        // 默认完全展开，禁止下滑关闭
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        });

        ColorWheelView colorWheel = dialogView.findViewById(R.id.color_wheel);
        SeekBar sbBrightness = dialogView.findViewById(R.id.sb_brightness);
        SeekBar sbAlpha = dialogView.findViewById(R.id.sb_alpha);
        View viewPreviewColor = dialogView.findViewById(R.id.view_preview_color);
        EditText etHexInput = dialogView.findViewById(R.id.et_hex_input);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_picker);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_picker);

        final float[] hsv = new float[3];
        Color.colorToHSV(initialColor, hsv);
        final int[] curAlpha = {Color.alpha(initialColor)};

        Runnable updateUI = new Runnable() {
            @Override
            public void run() {
                int rgbColor = Color.HSVToColor(hsv);
                int finalColor = (curAlpha[0] << 24) | (rgbColor & 0x00FFFFFF);

                if (colorWheel != null) {
                    colorWheel.setColor(hsv[0], hsv[1]);
                }

                if (viewPreviewColor != null) {
                    GradientDrawable previewDrawable = createRoundedRectDrawable(finalColor);
                    viewPreviewColor.setBackground(previewDrawable);
                }

                if (sbBrightness != null) {
                    int pureColor = Color.HSVToColor(new float[]{hsv[0], hsv[1], 1f});
                    GradientDrawable brightnessGrad = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{ 0xFF000000, pureColor }
                    );
                    brightnessGrad.setCornerRadius(activity.dpToPx(6));
                    sbBrightness.setBackground(brightnessGrad);
                    sbBrightness.setProgress((int) (hsv[2] * 100));
                }

                if (sbAlpha != null) {
                    int transparentColor = 0x00FFFFFF & rgbColor;
                    int opaqueColor = 0xFF000000 | rgbColor;
                    GradientDrawable alphaGrad = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{ transparentColor, opaqueColor }
                    );
                    alphaGrad.setCornerRadius(activity.dpToPx(6));
                    sbAlpha.setBackground(alphaGrad);
                    sbAlpha.setProgress(curAlpha[0]);
                }

                if (etHexInput != null && !etHexInput.hasFocus()) {
                    etHexInput.setText(String.format("#%08X", finalColor));
                }
            }
        };

        updateUI.run();

        if (colorWheel != null) {
            colorWheel.setOnColorSelectedListener((hue, saturation) -> {
                hsv[0] = hue;
                hsv[1] = saturation;
                updateUI.run();
            });
        }

        if (sbBrightness != null) {
            sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        hsv[2] = progress / 100f;
                        updateUI.run();
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (sbAlpha != null) {
            sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        curAlpha[0] = progress;
                        updateUI.run();
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (etHexInput != null) {
            etHexInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (etHexInput.hasFocus()) {
                        String input = s.toString().trim();
                        if (input.startsWith("#")) {
                            input = input.substring(1);
                        }
                        if (input.length() == 8) {
                            try {
                                int color = (int) Long.parseLong(input, 16);
                                Color.colorToHSV(color, hsv);
                                curAlpha[0] = Color.alpha(color);
                                updateUI.run();
                            } catch (NumberFormatException ignored) {}
                        } else if (input.length() == 6) {
                            try {
                                int color = 0xFF000000 | (int) Long.parseLong(input, 16);
                                Color.colorToHSV(color, hsv);
                                curAlpha[0] = 255;
                                updateUI.run();
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    int rgbColor = Color.HSVToColor(hsv);
                    int finalColor = (curAlpha[0] << 24) | (rgbColor & 0x00FFFFFF);
                    listener.onColorSelected(finalColor);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}
