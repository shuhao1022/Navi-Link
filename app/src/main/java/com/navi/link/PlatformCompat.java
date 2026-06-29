package com.navi.link;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.StatFs;

import androidx.annotation.RequiresApi;

/**
 * Isolates newer framework calls in nested classes so Android 4.x Dalvik does
 * not try to resolve unavailable methods while verifying common app classes.
 */
final class PlatformCompat {

    private PlatformCompat() {}

    static void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Api26Impl.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
    }

    @SuppressWarnings("deprecation")
    static long[] getStorageStats(StatFs stat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return Api18Impl.getStorageStats(stat);
        }
        return new long[]{
                stat.getBlockSize(),
                stat.getBlockCount(),
                stat.getAvailableBlocks()
        };
    }

    static void scaleGradientCorners(Drawable background, float factor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && background instanceof GradientDrawable) {
            Api24Impl.scaleGradientCorners((GradientDrawable) background, factor);
        }
    }

    private static final class Api18Impl {
        private Api18Impl() {}

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        static long[] getStorageStats(StatFs stat) {
            return new long[]{
                    stat.getBlockSizeLong(),
                    stat.getBlockCountLong(),
                    stat.getAvailableBlocksLong()
            };
        }
    }

    private static final class Api24Impl {
        private Api24Impl() {}

        @RequiresApi(Build.VERSION_CODES.N)
        static void scaleGradientCorners(GradientDrawable drawable, float factor) {
            GradientDrawable mutable = (GradientDrawable) drawable.mutate();
            float radius = mutable.getCornerRadius();
            if (radius > 0) {
                mutable.setCornerRadius(radius * factor);
                return;
            }
            try {
                float[] radii = mutable.getCornerRadii();
                if (radii != null) {
                    for (int i = 0; i < radii.length; i++) {
                        radii[i] *= factor;
                    }
                    mutable.setCornerRadii(radii);
                }
            } catch (Exception ignored) {
                // Some vendor GradientDrawable implementations do not expose radii.
            }
        }
    }

    private static final class Api26Impl {
        private Api26Impl() {}

        @RequiresApi(Build.VERSION_CODES.O)
        static void startForegroundService(Context context, Intent intent) {
            context.startForegroundService(intent);
        }
    }
}
