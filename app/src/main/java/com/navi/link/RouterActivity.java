package com.navi.link;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 透明路由 Activity - 应用入口分发器。
 * 不展示任何 UI，根据权限和服务状态决定跳转目标：
 * <ul>
 *   <li>有悬浮窗权限且服务未运行 → 静默启动服务 + Toast 提示 → 关闭自身</li>
 *   <li>否则 → 跳转 MainActivity 配置页</li>
 * </ul>
 */
public class RouterActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 有悬浮窗权限
        if (Settings.canDrawOverlays(this)) {
            FloatingWindowManager manager = FloatingWindowManager.getInstance();
            if (manager == null || !manager.isShowing()) {
                // 服务未运行 → 静默启动
                startAutoMapService();
                Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // 无权限或服务已运行 → 打开配置页
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
