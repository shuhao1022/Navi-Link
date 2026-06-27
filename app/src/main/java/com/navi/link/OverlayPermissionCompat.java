package com.navi.link;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

/**
 * Compatibility wrapper for the overlay permission model introduced in Android 6.0.
 *
 * <p>On Android 4.2-5.1 the manifest permission is granted during installation,
 * and {@link Settings#canDrawOverlays(Context)} does not exist.</p>
 */
final class OverlayPermissionCompat {

    private OverlayPermissionCompat() {}

    static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Api23Impl.canDrawOverlays(context);
    }

    private static final class Api23Impl {
        private Api23Impl() {}

        @RequiresApi(Build.VERSION_CODES.M)
        static boolean canDrawOverlays(Context context) {
            return Settings.canDrawOverlays(context);
        }
    }
}
