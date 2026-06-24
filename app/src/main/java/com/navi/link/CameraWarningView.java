package com.navi.link;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CameraWarningView extends LinearLayout {

    private ImageView ivCameraIcon;
    private TextView tvCameraSpeed;
    private TextView tvCameraDist;

    public CameraWarningView(Context context) {
        this(context, null);
    }

    public CameraWarningView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraWarningView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.layout_camera_warning, this, true);

        ivCameraIcon = findViewById(R.id.iv_camera_icon);
        tvCameraSpeed = findViewById(R.id.tv_camera_speed);
        tvCameraDist = findViewById(R.id.tv_camera_dist);
    }

    /**
     * 更新摄像头信息
     *
     * @param cameraType  摄像头类型
     * @param cameraDist  距离 (米)
     * @param cameraSpeed 限速值 (0表示无)
     */
    public void updateCameraInfo(int cameraType, int cameraDist, int cameraSpeed) {
        if (cameraDist <= 0) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        tvCameraDist.setText(cameraDist + "米");

        if (cameraSpeed > 0) {
            // 有限速值，隐藏图标，显示红色圆圈限速
            ivCameraIcon.setVisibility(GONE);
            tvCameraSpeed.setVisibility(VISIBLE);
            tvCameraSpeed.setText(String.valueOf(cameraSpeed));
        } else {
            // 无限速值，显示对应类型的图标
            ivCameraIcon.setVisibility(VISIBLE);
            tvCameraSpeed.setVisibility(GONE);
            ivCameraIcon.setImageResource(getIconRes(cameraType));
        }
    }

    private int getIconRes(int cameraType) {
        switch (cameraType) {
            case 6:
            case 20:
                return R.drawable.camera_bicycle;
            case 4:
            case 16:
                return R.drawable.camera_bus;
            case 13:
            case 1015:
                return R.drawable.camera_byfoot;
            case 11:
            case 1099:
                return R.drawable.camera_etc;
            case 29:
            case 1029:
                return R.drawable.camera_hov;
            case 22:
            case 1001:
                return R.drawable.camera_lamp;
            case 2:
            case 15:
                return R.drawable.camera_light;
            case 21:
            case 1017:
                return R.drawable.camera_park;
            case 19:
            case 1005:
                return R.drawable.camera_phone;
            case 12:
            case 1030:
                return R.drawable.camera_press;
            case 26:
            case 1024:
                return R.drawable.camera_railway;
            case 30:
            case 1012:
                return R.drawable.camera_recycle;
            case 25:
            case 1016:
                return R.drawable.camera_reverse;
            case 18:
            case 1002:
                return R.drawable.camera_safe;
            case 24:
            case 1021:
                return R.drawable.camera_sonar;
            case 28:
            case 1028:
                return R.drawable.camera_space;
            case 5:
                return R.drawable.camera_urgen;
            case 27:
            case 1011:
                return R.drawable.camera_tail;
            default:
                return R.drawable.camera_default;
        }
    }

    public void setTextColor(int color) {
        if (tvCameraDist != null) {
            tvCameraDist.setTextColor(color);
        }
    }
}
