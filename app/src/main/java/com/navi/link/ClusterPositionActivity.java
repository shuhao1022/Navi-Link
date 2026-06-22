package com.navi.link;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class ClusterPositionActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvPosInfo;
    private FrameLayout trackpad;
    private FrameLayout indicator;

    private View btnCenter;
    private View btnUp;
    private View btnDown;
    private View btnLeft;
    private View btnRight;
    private MaterialButton btnDone;

    private int themeColor = 0xFF4FC3F7;
    private int accentColor = 0xFF4FC3F7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cluster_position);

        android.view.ViewGroup contentView = findViewById(android.R.id.content);
        View root = contentView.getChildAt(0);
        if (root != null) {
            final int paddingLeft = root.getPaddingLeft();
            final int paddingTop = root.getPaddingTop();
            final int paddingRight = root.getPaddingRight();
            final int paddingBottom = root.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsetsCompat) -> {
                Insets insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(
                        insets.left + paddingLeft,
                        insets.top + paddingTop,
                        insets.right + paddingRight,
                        insets.bottom + paddingBottom
                );
                return windowInsetsCompat;
            });
        }

        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        FloatingWindowManager fwm = FloatingWindowManager.getInstance();
        if (fwm == null || !fwm.isClusterMirrorActive()) {
            Toast.makeText(this, "副屏投屏未开启，请先开启投屏", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 读取主题色
        SharedPreferences sp = getSharedPreferences("floating_config", MODE_PRIVATE);
        themeColor = sp.getInt("theme_color", 0xFF4FC3F7);
        accentColor = isDarkColor(themeColor) ? Color.WHITE : themeColor;

        // 绑定视图
        tvTitle = findViewById(R.id.tv_dialog_title);
        tvPosInfo = findViewById(R.id.tv_cluster_pos_info);
        trackpad = findViewById(R.id.cluster_trackpad);
        indicator = findViewById(R.id.cluster_trackpad_indicator);

        btnCenter = findViewById(R.id.btn_pos_center);
        btnUp = findViewById(R.id.btn_pos_up);
        btnDown = findViewById(R.id.btn_pos_down);
        btnLeft = findViewById(R.id.btn_pos_left);
        btnRight = findViewById(R.id.btn_pos_right);
        btnDone = findViewById(R.id.btn_pos_done);

        // 动态应用主题色与样式
        if (tvTitle != null) {
            tvTitle.setTextColor(accentColor);
        }
        if (btnDone != null) {
            btnDone.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            btnDone.setOnClickListener(v -> finish());
        }

        // 圆角背景
        GradientDrawable trackpadBg = new GradientDrawable();
        trackpadBg.setColor(0xFF2C2C2E);
        trackpadBg.setCornerRadius(dpToPx(12));
        trackpadBg.setStroke(dpToPx(1), 0x33FFFFFF);
        if (trackpad != null) {
            trackpad.setBackground(trackpadBg);
        }

        GradientDrawable indicatorBg = new GradientDrawable();
        indicatorBg.setColor((accentColor & 0x00FFFFFF) | 0x80000000); // 50% 不透明度的主题色
        indicatorBg.setCornerRadius(dpToPx(6));
        indicatorBg.setStroke(dpToPx(1), Color.WHITE);
        if (indicator != null) {
            indicator.setBackground(indicatorBg);
        }

        int sw = fwm.getClusterScreenWidth();
        int sh = fwm.getClusterScreenHeight();
        int w = fwm.getClusterNaturalWidth();
        int h = fwm.getClusterNaturalHeight();

        if (sw <= 0 || sh <= 0) {
            sw = 1920;
            sh = 720;
        }
        if (w <= 0 || h <= 0) {
            w = dpToPx(160);
            h = dpToPx(120);
        }

        final int finalSw = sw;
        final int finalSh = sh;
        final int finalW = w;
        final int finalH = h;

        final int[] currentPos = new int[]{ fwm.getClusterSavedPosX(), fwm.getClusterSavedPosY() };

        Runnable refreshIndicator = new Runnable() {
            @Override
            public void run() {
                if (trackpad == null || indicator == null || tvPosInfo == null) return;
                int tw = trackpad.getWidth();
                int th = trackpad.getHeight();
                if (tw <= 0 || th <= 0) return;

                int iw = Math.max(20, Math.round(((float) finalW / finalSw) * tw));
                int ih = Math.max(15, Math.round(((float) finalH / finalSh) * th));

                ViewGroup.LayoutParams ilp = indicator.getLayoutParams();
                if (ilp != null) {
                    ilp.width = iw;
                    ilp.height = ih;
                    indicator.setLayoutParams(ilp);
                }

                int left = Math.round(((float) currentPos[0] / finalSw) * tw);
                int top = Math.round(((float) currentPos[1] / finalSh) * th);

                left = Math.max(0, Math.min(left, tw - iw));
                top = Math.max(0, Math.min(top, th - ih));

                indicator.setX(left);
                indicator.setY(top);

                tvPosInfo.setText(String.format("当前位置: X = %d px, Y = %d px", currentPos[0], currentPos[1]));
            }
        };

        if (trackpad != null) {
            // 比例自适应父容器
            trackpad.post(new Runnable() {
                @Override
                public void run() {
                    View parent = (View) trackpad.getParent();
                    if (parent == null) return;
                    int parentWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
                    int parentHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
                    if (parentWidth <= 0 || parentHeight <= 0) return;

                    float targetRatio = (float) finalSw / finalSh;
                    float parentRatio = (float) parentWidth / parentHeight;

                    int finalWidth;
                    int finalHeight;

                    if (targetRatio > parentRatio) {
                        finalWidth = parentWidth;
                        finalHeight = Math.round(parentWidth / targetRatio);
                    } else {
                        finalHeight = parentHeight;
                        finalWidth = Math.round(parentHeight * targetRatio);
                    }

                    ViewGroup.LayoutParams lp = trackpad.getLayoutParams();
                    if (lp != null) {
                        lp.width = finalWidth;
                        lp.height = finalHeight;
                        trackpad.setLayoutParams(lp);
                    }
                    trackpad.post(refreshIndicator);
                }
            });

            trackpad.setOnTouchListener(new View.OnTouchListener() {
                private boolean isDragging = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int tw = trackpad.getWidth();
                    int th = trackpad.getHeight();
                    if (tw <= 0 || th <= 0 || indicator == null) return false;

                    int iw = indicator.getWidth();
                    int ih = indicator.getHeight();

                    float x = event.getX();
                    float y = event.getY();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isDragging = true;
                            int targetX = Math.round(((x - iw / 2f) / tw) * finalSw);
                            int targetY = Math.round(((y - ih / 2f) / th) * finalSh);

                            targetX = Math.max(0, Math.min(targetX, finalSw - finalW));
                            targetY = Math.max(0, Math.min(targetY, finalSh - finalH));

                            currentPos[0] = targetX;
                            currentPos[1] = targetY;

                            fwm.updateClusterPosition(targetX, targetY);
                            refreshIndicator.run();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            if (isDragging) {
                                int moveX = Math.round(((x - iw / 2f) / tw) * finalSw);
                                int moveY = Math.round(((y - ih / 2f) / th) * finalSh);

                                moveX = Math.max(0, Math.min(moveX, finalSw - finalW));
                                moveY = Math.max(0, Math.min(moveY, finalSh - finalH));

                                currentPos[0] = moveX;
                                currentPos[1] = moveY;

                                fwm.updateClusterPosition(moveX, moveY);
                                refreshIndicator.run();
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            isDragging = false;
                            return true;
                    }
                    return false;
                }
            });
        }

        if (btnUp != null) {
            btnUp.setOnClickListener(v -> {
                currentPos[1] = Math.max(0, currentPos[1] - 5);
                fwm.updateClusterPosition(currentPos[0], currentPos[1]);
                refreshIndicator.run();
            });
        }
        if (btnDown != null) {
            btnDown.setOnClickListener(v -> {
                currentPos[1] = Math.max(0, Math.min(currentPos[1] + 5, finalSh - finalH));
                fwm.updateClusterPosition(currentPos[0], currentPos[1]);
                refreshIndicator.run();
            });
        }
        if (btnLeft != null) {
            btnLeft.setOnClickListener(v -> {
                currentPos[0] = Math.max(0, currentPos[0] - 5);
                fwm.updateClusterPosition(currentPos[0], currentPos[1]);
                refreshIndicator.run();
            });
        }
        if (btnRight != null) {
            btnRight.setOnClickListener(v -> {
                currentPos[0] = Math.max(0, Math.min(currentPos[0] + 5, finalSw - finalW));
                fwm.updateClusterPosition(currentPos[0], currentPos[1]);
                refreshIndicator.run();
            });
        }
        if (btnCenter != null) {
            btnCenter.setOnClickListener(v -> {
                currentPos[0] = (finalSw - finalW) / 2;
                currentPos[1] = (finalSh - finalH) / 2;
                fwm.updateClusterPosition(currentPos[0], currentPos[1]);
                refreshIndicator.run();
            });
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean isDarkColor(int color) {
        return ((color >> 16) & 0xFF) * 0.299
                + ((color >> 8) & 0xFF) * 0.587
                + (color & 0xFF) * 0.114 < 100;
    }
}
