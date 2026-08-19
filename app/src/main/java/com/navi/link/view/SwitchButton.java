package com.navi.link.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * 满高全包裹胶囊 iOS / 车载风格高清开关组件 SwitchButton
 * 支持平滑平移动画、全高全包裹圆角轨道、完美的圆圈滑块。
 */
public class SwitchButton extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTrackRect = new RectF();
    private final RectF mThumbRect = new RectF();

    private boolean mChecked = false;
    private float mProgress = 0f; // 0f 为关闭，1f 为开启
    private ValueAnimator mAnimator;

    // 颜色配置
//    private int mColorOn = 0xFF1A73E8;    // 开启状态宝蓝
    private int mColorOn = 0xFF4FC3F7;    // 开启状态浅蓝
    private int mColorOff = 0xFF4A5056;   // 关闭状态深灰
    private int mThumbColor = 0xFFFFFFFF; // 纯白滑块圆圈

    private OnCheckedChangeListener mListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(SwitchButton buttonView, boolean isChecked);
    }

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth = dpToPx(52);
        int defaultHeight = dpToPx(28);

        int width = resolveSize(defaultWidth, widthMeasureSpec);
        int height = resolveSize(defaultHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) return;

        float padding = dpToPx(2);
        float radius = height / 2f;

        // 1. 绘制满高全包裹胶囊轨道 (Track)
        mTrackRect.set(0, 0, width, height);
        int currentColor = evaluateColor(mProgress, mColorOff, mColorOn);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mTrackRect, radius, radius, mPaint);

        // 2. 计算白色滑块 (Thumb) 位置
        float thumbDiameter = height - (padding * 2);
        float startX = padding;
        float endX = width - padding - thumbDiameter;
        float currentThumbLeft = startX + (endX - startX) * mProgress;

        mThumbRect.set(currentThumbLeft, padding, currentThumbLeft + thumbDiameter, padding + thumbDiameter);

        // 3. 绘制纯白滑块 (Thumb)
        mPaint.setColor(mThumbColor);
        canvas.drawRoundRect(mThumbRect, thumbDiameter / 2f, thumbDiameter / 2f, mPaint);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled() && event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            return true; // 消费事件，防止 super.onTouchEvent 再次触发 performClick
        }
        return super.onTouchEvent(event);
    }

    public void toggle() {
        setChecked(!mChecked, true);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (mChecked != checked) {
            mChecked = checked;
            if (mListener != null) {
                mListener.onCheckedChanged(this, mChecked);
            }
        }

        float targetProgress = mChecked ? 1.0f : 0.0f;
        if (animate) {
            startAnimation(targetProgress);
        } else {
            if (mAnimator != null) mAnimator.cancel();
            mProgress = targetProgress;
            invalidate();
        }
    }

    private void startAnimation(float targetProgress) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        mAnimator = ValueAnimator.ofFloat(mProgress, targetProgress);
        mAnimator.setDuration(220);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mListener = listener;
    }

    public void setColors(int colorOn, int colorOff) {
        this.mColorOn = colorOn;
        this.mColorOff = colorOff;
        invalidate();
    }

    private int evaluateColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24 |
                (startR + (int) (fraction * (endR - startR))) << 16 |
                (startG + (int) (fraction * (endG - startG))) << 8 |
                (startB + (int) (fraction * (endB - startB)));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }
}
