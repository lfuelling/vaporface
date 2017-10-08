/*
 * Copyright (C) 2017 The Android Open Source Project (Only boilerplate code)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lerk.vaporface;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class VaporFace extends CanvasWatchFaceService {

    private static int bgaCount = 0;
    private static Bitmap[] backgroundDrawable;

    private static final int BOTTOM_COMPLICATION_ID = 0;


    public static final int[] COMPLICATION_IDS = {BOTTOM_COMPLICATION_ID};

    // Left and right dial supported types.
    public static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            }
    };

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private Typeface VAPOR_FONT;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<VaporFace.Engine> mWeakReference;

        public EngineHandler(VaporFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            VaporFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    static int getComplicationId(
            VaporFaceConfigActivity.ComplicationLocation complicationLocation) {
        // Add any other supported locations here you would like to support. In our case, we are
        // only supporting a left and right complication.
        switch (complicationLocation) {
            case BOTTOM:
                return BOTTOM_COMPLICATION_ID;
            default:
                return -1;
        }
    }

    static int[] getComplicationIds() {
        return COMPLICATION_IDS;
    }

    static int[] getSupportedComplicationTypes(
            VaporFaceConfigActivity.ComplicationLocation complicationLocation) {
        // Add any other supported locations here.
        switch (complicationLocation) {
            case BOTTOM:
                return COMPLICATION_SUPPORTED_TYPES[0];
            default:
                return new int[]{};
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private final String TAG = Engine.class.getCanonicalName();
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        private Paint complicationPaint;
        private SparseArray<ComplicationData> complicationDataSparseArray;
        private SparseArray<ComplicationDrawable> complicationDrawableSparseArray;

        Paint textPaint;
        boolean ambient;
        Calendar cal;

        final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                cal.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean lowBitAmbient;
        private boolean burnInProtection;
        private boolean isRound;
        private Paint ambientTextPaint;


        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            // For most Wear devices, width and height are the same, so we just chose one (width).
            int sizeOfComplication = width / 4;
            int midpointOfScreen = width / 2;

            int horizontalOffset = midpointOfScreen - (sizeOfComplication / 2);
            int verticalOffset = height - (sizeOfComplication + 18);

            Rect bottomBounds =
                    // Left, Top, Right, Bottom
                    new Rect(horizontalOffset,
                            verticalOffset,
                            (horizontalOffset + sizeOfComplication),
                            (verticalOffset + sizeOfComplication));

            ComplicationDrawable bottomComplicationDrawable = complicationDrawableSparseArray.get(BOTTOM_COMPLICATION_ID);
            bottomComplicationDrawable.setBounds(bottomBounds);

        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(VaporFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = VaporFace.this.getResources();

            VAPOR_FONT = Typeface.createFromAsset(getAssets(), "Monomod.ttf");

            textPaint = createTextPaint(resources.getColor(R.color.digital_text));
            ambientTextPaint = createTextPaint(resources.getColor(R.color.ambient_mode_text_secondary));
            ambientTextPaint.setTextSize(48F);
            ambientTextPaint.setAntiAlias(lowBitAmbient);

            initComplications();

            backgroundDrawable = getBackgroundDrawable();
            cal = Calendar.getInstance();
        }

        private Bitmap[] getBackgroundDrawable() {
            String currentBackground = getSharedPreferences("vaporface", MODE_PRIVATE).getString("background", String.valueOf(0));

            AnimationDrawable drawable;

            switch (currentBackground) {
                case "1":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_04_anim);
                    break;
                case "2":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_08_anim);
                    break;
                case "3":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_10_anim);
                    break;
                case "4":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_12_anim);
                    break;
                case "5":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_15_anim);
                    break;
                case "6":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_16_anim);
                    break;
                case "7":
                    drawable = (AnimationDrawable) getDrawable(R.drawable.bg_20_anim);
                    break;
                case "0":
                default:
                    drawable = null;
            }

            if (drawable == null) {
                return new Bitmap[]{Bitmap.createScaledBitmap(drawableToBitmap(getDrawable(R.drawable.vaporwave_grid)), 320, 320, false)};
            } else {
                ArrayList<Bitmap> bl = new ArrayList<>();
                for (int i = 0; i >= drawable.getNumberOfFrames(); i++) {
                    bl.add(Bitmap.createScaledBitmap(drawableToBitmap(drawable.getFrame(i)), 320, 320, false));
                }
                return bl.toArray(new Bitmap[bl.size()]);
            }


        }

        private void initComplications() {
            complicationDataSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            complicationPaint = new Paint();
            complicationPaint.setColor(Color.WHITE);
            complicationPaint.setTextSize(18F);
            complicationPaint.setTypeface(VAPOR_FONT);

            ComplicationDrawable bottomComplicationDrawable = (ComplicationDrawable) getDrawable(R.drawable.custom_complication_styles);
            bottomComplicationDrawable.setContext(getApplicationContext());


            complicationDrawableSparseArray = new SparseArray<>(COMPLICATION_IDS.length);
            complicationDrawableSparseArray.put(BOTTOM_COMPLICATION_ID, bottomComplicationDrawable);


            setActiveComplications(COMPLICATION_IDS);
        }

        @Override
        public void onComplicationDataUpdate(int complicationId, ComplicationData complicationData) {
            // Adds/updates active complication data in the array.
            complicationDataSparseArray.put(complicationId, complicationData);

            // Updates correct ComplicationDrawable with updated data.
            ComplicationDrawable complicationDrawable =
                    complicationDrawableSparseArray.get(complicationId);
            complicationDrawable.setComplicationData(complicationData);

            invalidate();
        }


        /*
         * Determines if tap inside a complication area or returns -1.
         */
        private int getTappedComplicationId(int x, int y) {

            int complicationId;
            ComplicationData complicationData;
            ComplicationDrawable complicationDrawable;

            long currentTimeMillis = System.currentTimeMillis();

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationId = COMPLICATION_IDS[i];
                complicationData = complicationDataSparseArray.get(complicationId);

                if ((complicationData != null)
                        && (complicationData.isActive(currentTimeMillis))
                        && (complicationData.getType() != ComplicationData.TYPE_NOT_CONFIGURED)
                        && (complicationData.getType() != ComplicationData.TYPE_EMPTY)) {

                    complicationDrawable = complicationDrawableSparseArray.get(complicationId);
                    Rect complicationBoundingRect = complicationDrawable.getBounds();

                    if (complicationBoundingRect.width() > 0) {
                        if (complicationBoundingRect.contains(x, y)) {
                            return complicationId;
                        }
                    } else {
                        Log.e(TAG, "Not a recognized complication id.");
                    }
                }
            }
            return -1;
        }

        // Fires PendingIntent associated with complication (if it has one).
        private void onComplicationTap(int complicationId) {
            Log.d(TAG, "onComplicationTap()");

            ComplicationData complicationData =
                    complicationDataSparseArray.get(complicationId);

            if (complicationData != null) {

                if (complicationData.getTapAction() != null) {
                    try {
                        complicationData.getTapAction().send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "onComplicationTap() tap action error: " + e);
                    }

                } else if (complicationData.getType() == ComplicationData.TYPE_NO_PERMISSION) {

                    // Watch face does not have permission to receive complication data, so launch
                    // permission request.
                    ComponentName componentName =
                            new ComponentName(
                                    getApplicationContext(), VaporFace.class);

                    Intent permissionRequestIntent =
                            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                    getApplicationContext(), componentName);

                    startActivity(permissionRequestIntent);
                }

            } else {
                Log.d(TAG, "No PendingIntent for complication " + complicationId + ".");
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(VAPOR_FONT);
            paint.setAntiAlias(true);
            paint.setLetterSpacing(-0.08F);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                cal.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            VaporFace.this.registerReceiver(timeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            VaporFace.this.unregisterReceiver(timeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = VaporFace.this.getResources();
            isRound = insets.isRound();
            float textSize = resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            textPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            ComplicationDrawable complicationDrawable;

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = complicationDrawableSparseArray.get(COMPLICATION_IDS[i]);

                if (complicationDrawable != null) {
                    complicationDrawable.setLowBitAmbient(lowBitAmbient);
                    complicationDrawable.setBurnInProtection(burnInProtection);
                }
            }
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (ambient != inAmbientMode) {
                ambient = inAmbientMode;
                if (lowBitAmbient) {
                    textPaint.setAntiAlias(!inAmbientMode);
                    complicationPaint.setAntiAlias(!isInAmbientMode());
                }

                ComplicationDrawable complicationDrawable;

                for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                    complicationDrawable = complicationDrawableSparseArray.get(COMPLICATION_IDS[i]);
                    complicationDrawable.setInAmbientMode(inAmbientMode);
                }
                invalidate();
            }

            backgroundDrawable = getBackgroundDrawable();

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    int tappedComplicationId = getTappedComplicationId(x, y);
                    if (tappedComplicationId != -1) {
                        onComplicationTap(tappedComplicationId);
                    }
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
                drawAesthetic(canvas);
            } else {
                if(bgaCount >= backgroundDrawable.length) {
                    bgaCount = 0;
                }
                canvas.drawBitmap(backgroundDrawable[bgaCount], 0F, 0F, null);
                bgaCount++;
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            cal.setTimeInMillis(now);

            String text = "";
            if (!DateFormat.is24HourFormat(getApplicationContext())) {
                Calendar mCalendar = Calendar.getInstance();
                int hourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay >= 12) {
                    //TODO: pm
                } else {
                    //TODO: am
                }
                if (ambient) {
                    text = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR),
                            cal.get(Calendar.MINUTE));
                } else {
                    text = String.format(Locale.getDefault(), "%02d:%02d:%02d", cal.get(Calendar.HOUR),
                            cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                }
            } else {
                if (ambient) {
                    text = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR),
                            cal.get(Calendar.MINUTE));
                } else {
                    text = String.format(Locale.getDefault(), "%02d:%02d:%02d", cal.get(Calendar.HOUR),
                            cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                }
            }


            drawText(canvas, textPaint, text);

            drawComplications(canvas, now);
        }

        private void drawComplications(Canvas canvas, long currentTimeMillis) {
            int complicationId;
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationId = COMPLICATION_IDS[i];
                complicationDrawable = complicationDrawableSparseArray.get(complicationId);
                complicationDrawable.draw(canvas, currentTimeMillis);
            }
        }

        /**
         * Text that gets drawn in ambient mode
         *
         * @param canvas
         */
        private void drawAesthetic(Canvas canvas) {
            canvas.save();
            canvas.rotate(45f);
            Rect bounds = new Rect();
            String text = "aesthetic";
            ambientTextPaint.getTextBounds(text, 0, text.length(), bounds);
            int x = (canvas.getWidth() / 2) - (bounds.width() / 3);
            int y = bounds.height() * 2;
            canvas.drawText(text, x, y, ambientTextPaint);
            canvas.restore();
        }

        /**
         * From: https://stackoverflow.com/a/20900551/1979736
         *
         * @param canvas
         * @param paint
         * @param text
         */
        public void drawText(Canvas canvas, Paint paint, String text) {
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
            int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
            canvas.drawText(text, x, y, paint);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    /**
     * https://stackoverflow.com/a/10600736/1979736
     *
     * @param drawable the drawable
     * @return a bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
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
