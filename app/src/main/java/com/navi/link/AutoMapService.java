package com.navi.link;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class AutoMapService extends Service {

    private static final String CHANNEL_ID = "shadow_map_channel";
    private static final int NOTIFICATION_ID = 1001;

    private AmapNaviReceiver amapNaviReceiver;
    private FloatingWindowManager floatingWindowManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        floatingWindowManager = FloatingWindowManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());

        if (!floatingWindowManager.isShowing()) {
            floatingWindowManager.show();
            floatingWindowManager.setVisible(false);
        }

        if (amapNaviReceiver != null) {
            return START_STICKY;
        }

        amapNaviReceiver = new AmapNaviReceiver();
        IntentFilter filter = new IntentFilter("AUTONAVI_STANDARD_BROADCAST_SEND");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(amapNaviReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(amapNaviReceiver, filter);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingWindowManager != null) {
            floatingWindowManager.hide();
        }
        if (amapNaviReceiver != null) {
            try {
                unregisterReceiver(amapNaviReceiver);
            } catch (Exception ignored) {
            }
            amapNaviReceiver = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "悬浮窗导航", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("悬浮窗导航服务运行中");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ShadowMap 导航")
                .setContentText("悬浮窗导航运行中")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
