package com.navi.link;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 透明路由 Activity - 应用入口分发器。
 * 不展示任何 UI，根据用户配置的启动方式决定行为：
 * <ul>
 *   <li>「只启动服务」模式：有权限时静默启动/提示已运行；无权限时跳转配置页</li>
 *   <li>「正常打开」模式：直接跳转 MainActivity 配置页</li>
 * </ul>
 */
public class RouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化全局异常捕获
        CrashHandler.getInstance().init(this);

        SharedPreferences sp = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        boolean isServiceOnlyMode = sp.getBoolean("is_service_only", false);

        if (isServiceOnlyMode) {
            handleServiceOnlyMode();
        } else {
            // 正常打开 → 直接进配置页
            startMainActivity();
        }
    }

    /**
     * 「只启动服务」模式：有权限时根据服务状态启动或提示；无权限时跳转配置页
     */
    private void handleServiceOnlyMode() {
        if (!Settings.canDrawOverlays(this)) {
            startMainActivity();
            return;
        }

        FloatingWindowManager manager = FloatingWindowManager.getInstance();
        if (manager != null && manager.isShowing()) {
            Toast.makeText(this, "服务已在运行中", Toast.LENGTH_SHORT).show();
        } else {
            startAutoMapService();
            Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void startAutoMapService() {
        Intent intent = new Intent(this, AutoMapService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
