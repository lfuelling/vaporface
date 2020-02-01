package io.lerk.vaporface;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class is used to retrieve the current background. Here for obvious debloating reasons.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class VaporUtils {

    private static final String KEY_BACKGROUND_IMAGE = "background";
    private static final String KEY_ENABLE_ANIMATION = "animations_enabled";
    private static final String KEY_ENABLE_FULL_ANIMATION = "full_animations_enabled";
    private static final String PREFERENCES_NAME = "vaporface";

    /**
     * Returns the currently configured background.
     * @param context the context (preferably {@link VaporFace}.this)
     * @param surfaceWidth the surface width
     * @param surfaceHeight the surface width
     * @return the scaled and configured background drawable.
     */
    static Bitmap[] getBackgroundDrawable(Context context, Integer surfaceWidth, Integer surfaceHeight) {

        SharedPreferences preferences = context.getSharedPreferences(VaporUtils.PREFERENCES_NAME, MODE_PRIVATE);
        String currentBackground = preferences.getString(VaporUtils.KEY_BACKGROUND_IMAGE, String.valueOf(0));
        Boolean animationsEnabled = preferences.getBoolean(VaporUtils.KEY_ENABLE_ANIMATION, false);
        Boolean animationsFull = preferences.getBoolean(VaporUtils.KEY_ENABLE_FULL_ANIMATION, false);

        Bitmap[] drawable;
        int dstWidth = ((surfaceWidth != null)) ? surfaceWidth : 320;
        int dstHeight = ((surfaceHeight != null)) ? surfaceHeight : 320;

        if ("1".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_04_14)), dstWidth, dstHeight, false) */
            };
        } else if ("2".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_14)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_15)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_16)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_17)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_18)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_19)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_20)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_21)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_22)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_23)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_08_24)), dstWidth, dstHeight, false) */
            };
        } else if ("3".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_10_08)), dstWidth, dstHeight, false) */
            };
        } else if ("4".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_14)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_15)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_16)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_17)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_18)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_19)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_20)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_21)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_12_22)), dstWidth, dstHeight, false) */
            };
        } else if ("5".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_14)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_15)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_16)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_17)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_18)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_19)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_20)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_15_21)), dstWidth, dstHeight, false) */
            };
        } else if ("6".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_14)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_15)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_16)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_17)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_18)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_19)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_20)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_21)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_22)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_23)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_16_24)), dstWidth, dstHeight, false) */
            };
        } else if ("7".equals(currentBackground)) {
            drawable = new Bitmap[]{
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_01)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_02)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_03)), dstWidth, dstHeight, false),
                    Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_04)), dstWidth, dstHeight, false)  /*
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_05)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_06)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_07)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_08)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_09)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_10)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_11)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_12)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_13)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_14)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_15)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_16)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_17)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_18)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_19)), dstWidth, dstHeight, false),
                        Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.bg_20_20)), dstWidth, dstHeight, false) */
            };
        } else {
            return new Bitmap[]{Bitmap.createScaledBitmap(drawableToBitmap(context.getDrawable(R.drawable.vaporwave_grid)), dstWidth, dstHeight, false)};
        }

        if (!animationsEnabled) {
            return new Bitmap[]{drawable[0]};
        }

        return drawable;
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
