package com.navi.link;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.widget.Toast;

/**
 * PackageInstaller 会话提交后的结果回调接收器。
 *
 * <p>系统需要用户确认安装时会返回 {@link PackageInstaller#STATUS_PENDING_USER_ACTION}，
 * 此时携带一个确认 Intent，需要由本接收器拉起。</p>
 */
public class InstallResultReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);

        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                // 需要用户确认：拉起系统安装确认界面
                Intent confirm = getConfirmIntent(intent);
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(confirm);
                    } catch (Exception e) {
                        toast(context, "无法打开安装确认界面");
                    }
                }
                break;

            case PackageInstaller.STATUS_SUCCESS:
                toast(context, "安装成功");
                break;

            default:
                String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                toast(context, "安装未完成" + (msg != null ? "：" + msg : ""));
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private static Intent getConfirmIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
        }
        return intent.getParcelableExtra(Intent.EXTRA_INTENT);
    }

    private static void toast(Context context, String msg) {
        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
