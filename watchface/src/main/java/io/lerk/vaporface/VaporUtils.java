package io.lerk.vaporface;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class is used to retrieve the current background. Here for obvious debloating reasons.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class VaporUtils {

    static final String KEY_BACKGROUND_IMAGE = "background";
    static final String KEY_ENABLE_ANIMATION = "animations_enabled";
    static final String KEY_ENABLE_FULL_ANIMATION = "full_animations_enabled";
    static final String PREFERENCES_NAME = "vaporface";

    /**
     * Returns the currently configured background.
     *
     * @param context       the context (preferably {@link VaporFace}.this)
     * @param surfaceWidth  the surface width
     * @param surfaceHeight the surface height
     * @return the scaled and configured background drawable.
     */
    static int[] getBackgroundDrawables(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(VaporUtils.PREFERENCES_NAME, MODE_PRIVATE);
        String currentBackground = preferences.getString(VaporUtils.KEY_BACKGROUND_IMAGE, String.valueOf(0));
        boolean animationsEnabled = preferences.getBoolean(VaporUtils.KEY_ENABLE_ANIMATION, false);
        boolean fullAnimationsEnabled = preferences.getBoolean(VaporUtils.KEY_ENABLE_FULL_ANIMATION, false);

        int[] drawables;

        if ("1".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_04_01,
                    R.drawable.bg_04_02,
                    R.drawable.bg_04_03,
                    R.drawable.bg_04_04,
                    R.drawable.bg_04_05,
                    R.drawable.bg_04_06,
                    R.drawable.bg_04_07,
                    R.drawable.bg_04_08,
                    R.drawable.bg_04_09,
                    R.drawable.bg_04_10,
                    R.drawable.bg_04_11,
                    R.drawable.bg_04_12,
                    R.drawable.bg_04_13,
                    R.drawable.bg_04_14
            };
        } else if ("2".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_08_01,
                    R.drawable.bg_08_02,
                    R.drawable.bg_08_03,
                    R.drawable.bg_08_04,
                    R.drawable.bg_08_05,
                    R.drawable.bg_08_06,
                    R.drawable.bg_08_07,
                    R.drawable.bg_08_08,
                    R.drawable.bg_08_09,
                    R.drawable.bg_08_10,
                    R.drawable.bg_08_11,
                    R.drawable.bg_08_12,
                    R.drawable.bg_08_13,
                    R.drawable.bg_08_14,
                    R.drawable.bg_08_15,
                    R.drawable.bg_08_16,
                    R.drawable.bg_08_17,
                    R.drawable.bg_08_18,
                    R.drawable.bg_08_19,
                    R.drawable.bg_08_20,
                    R.drawable.bg_08_21,
                    R.drawable.bg_08_22,
                    R.drawable.bg_08_23,
                    R.drawable.bg_08_24
            };
        } else if ("3".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_10_01,
                    R.drawable.bg_10_02,
                    R.drawable.bg_10_03,
                    R.drawable.bg_10_04,
                    R.drawable.bg_10_05,
                    R.drawable.bg_10_06,
                    R.drawable.bg_10_07,
                    R.drawable.bg_10_08
            };
        } else if ("4".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_12_01,
                    R.drawable.bg_12_02,
                    R.drawable.bg_12_03,
                    R.drawable.bg_12_04,
                    R.drawable.bg_12_05,
                    R.drawable.bg_12_06,
                    R.drawable.bg_12_07,
                    R.drawable.bg_12_08,
                    R.drawable.bg_12_09,
                    R.drawable.bg_12_10,
                    R.drawable.bg_12_11,
                    R.drawable.bg_12_12,
                    R.drawable.bg_12_13,
                    R.drawable.bg_12_14,
                    R.drawable.bg_12_15,
                    R.drawable.bg_12_16,
                    R.drawable.bg_12_17,
                    R.drawable.bg_12_18,
                    R.drawable.bg_12_19,
                    R.drawable.bg_12_20,
                    R.drawable.bg_12_21,
                    R.drawable.bg_12_22
            };
        } else if ("5".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_15_01,
                    R.drawable.bg_15_02,
                    R.drawable.bg_15_03,
                    R.drawable.bg_15_04,
                    R.drawable.bg_15_05,
                    R.drawable.bg_15_06,
                    R.drawable.bg_15_07,
                    R.drawable.bg_15_08,
                    R.drawable.bg_15_09,
                    R.drawable.bg_15_10,
                    R.drawable.bg_15_11,
                    R.drawable.bg_15_12,
                    R.drawable.bg_15_13,
                    R.drawable.bg_15_14,
                    R.drawable.bg_15_15,
                    R.drawable.bg_15_16,
                    R.drawable.bg_15_17,
                    R.drawable.bg_15_18,
                    R.drawable.bg_15_19,
                    R.drawable.bg_15_20,
                    R.drawable.bg_15_21
            };
        } else if ("6".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_16_01,
                    R.drawable.bg_16_02,
                    R.drawable.bg_16_03,
                    R.drawable.bg_16_04,
                    R.drawable.bg_16_05,
                    R.drawable.bg_16_06,
                    R.drawable.bg_16_07,
                    R.drawable.bg_16_08,
                    R.drawable.bg_16_09,
                    R.drawable.bg_16_10,
                    R.drawable.bg_16_11,
                    R.drawable.bg_16_12,
                    R.drawable.bg_16_13,
                    R.drawable.bg_16_14,
                    R.drawable.bg_16_15,
                    R.drawable.bg_16_16,
                    R.drawable.bg_16_17,
                    R.drawable.bg_16_18,
                    R.drawable.bg_16_19,
                    R.drawable.bg_16_20,
                    R.drawable.bg_16_21,
                    R.drawable.bg_16_22,
                    R.drawable.bg_16_23,
                    R.drawable.bg_16_24
            };
        } else if ("7".equals(currentBackground)) {
            drawables = new int[]{
                    R.drawable.bg_20_01,
                    R.drawable.bg_20_02,
                    R.drawable.bg_20_03,
                    R.drawable.bg_20_04,
                    R.drawable.bg_20_05,
                    R.drawable.bg_20_06,
                    R.drawable.bg_20_07,
                    R.drawable.bg_20_08,
                    R.drawable.bg_20_09,
                    R.drawable.bg_20_10,
                    R.drawable.bg_20_11,
                    R.drawable.bg_20_12,
                    R.drawable.bg_20_13,
                    R.drawable.bg_20_14,
                    R.drawable.bg_20_15,
                    R.drawable.bg_20_16,
                    R.drawable.bg_20_17,
                    R.drawable.bg_20_18,
                    R.drawable.bg_20_19,
                    R.drawable.bg_20_20
            };
        } else {
            return new int[]{R.drawable.vaporwave_grid};
        }

        if (!animationsEnabled) {
            return new int[]{drawables[0]};
        }

        if (!fullAnimationsEnabled) {
            return new int[]{
                    drawables[0],
                    drawables[1],
                    drawables[2],
                    drawables[3]
            };
        }

        return drawables;
    }

    static Bitmap generateBitmap(Context context, int dstWidth, int dstHeight, @DrawableRes int drawable) {
        return Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(drawable)), dstWidth, dstHeight, false);
    }

    /**
     * https://stackoverflow.com/a/10600736/1979736
     *
     * @param drawable the drawable
     * @return a bitmap
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
