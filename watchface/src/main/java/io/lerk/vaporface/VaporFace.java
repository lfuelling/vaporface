/*
 * Copyright (C) 2017 The Android Open Source Project (Only boilerplate code)
 * Copyright (C) 2017 Lukas Fülling (Remaining code)
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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/*
  __     __                                          ______
 /  |   /  |                                        /      \
 ## |   ## | ______    ______    ______    ______  /######  |______    _______   ______
 ## |   ## |/      \  /      \  /      \  /      \ ## |_ ##//      \  /       | /      \
 ##  \ /##/ ######  |/######  |/######  |/######  |##   |   ######  |/#######/ /######  |
  ##  /##/  /    ## |## |  ## |## |  ## |## |  ##/ ####/    /    ## |## |      ##    ## |
   ## ##/  /####### |## |__## |## \__## |## |      ## |    /####### |## \_____ ########/
    ###/   ##    ## |##    ##/ ##    ##/ ## |      ## |    ##    ## |##       |##       |
     #/     #######/ #######/   ######/  ##/       ##/      #######/  #######/  #######/
                     ## |
                     ## |
                     ##/
 */

/**
 * Digital vaporwave watch face with seconds.
 * In ambient mode, the seconds aren't displayed.
 * On devices with low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class VaporFace extends CanvasWatchFaceService {

    /**
     * Background animation step count.
     * This is actually needed and incremented in {@link Engine#onDraw(Canvas, Rect)}.
     */
    private static int bgaCount = 0;

    /**
     * The background to use.
     *
     * @see VaporUtils
     */
    private static int[] backgroundDrawables;

    /**
     * The only available complication.
     */
    private static final int BOTTOM_COMPLICATION_ID = 0;

    /**
     * The ids of all available complications.
     */
    public static final int[] COMPLICATION_IDS = {BOTTOM_COMPLICATION_ID};

    /**
     * Supported complication types.
     *
     * @see ComplicationData#TYPE_RANGED_VALUE
     * @see ComplicationData#TYPE_ICON
     * @see ComplicationData#TYPE_SHORT_TEXT
     * @see ComplicationData#TYPE_SMALL_IMAGE
     */
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

    /**
     * If true, the {@link #backgroundDrawables} will be polled again.
     */
    public static boolean updateBackground = false;

    /**
     * The {@link Typeface} to use.
     */
    private Typeface VAPOR_FONT;

    /**
     * {@inheritDoc}
     */
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    /**
     * @see android.os.Handler
     */
    private static class EngineHandler extends Handler {

        /**
         * @see WeakReference
         */
        private final WeakReference<VaporFace.Engine> weakReference;

        /**
         * Constructor.
         *
         * @param reference the reference.
         */
        EngineHandler(VaporFace.Engine reference) {
            weakReference = new WeakReference<>(reference);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(Message msg) {
            VaporFace.Engine engine = weakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    /**
     * Get the id belonging to a {@link VaporFaceConfigActivity.ComplicationLocation}.
     *
     * @param complicationLocation the {@link VaporFaceConfigActivity.ComplicationLocation}
     * @return the id of the complication.
     */
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

    /**
     * Getter for {@link #COMPLICATION_IDS}
     *
     * @return {@link #COMPLICATION_IDS}
     */
    static int[] getComplicationIds() {
        return COMPLICATION_IDS;
    }

    /**
     * See return.
     *
     * @param complicationLocation the {@link VaporFaceConfigActivity.ComplicationLocation}
     * @return the supported complication types.
     */
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

    /**
     * {@inheritDoc}
     */
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
        private Paint ambientTextPaint;
        private Integer surfaceWidth = null, surfaceHeight = null;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            surfaceWidth = width;
            surfaceHeight = height;
            backgroundDrawables = VaporUtils.getBackgroundDrawables(VaporFace.this);
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

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("deprecation")
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(VaporFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = VaporFace.this.getResources();

            //noinspection SpellCheckingInspection
            VAPOR_FONT = Typeface.createFromAsset(getAssets(), "Monomod.ttf");

            textPaint = createTextPaint(resources.getColor(R.color.digital_text));
            ambientTextPaint = createTextPaint(resources.getColor(R.color.ambient_mode_text_secondary));
            ambientTextPaint.setTextSize(48F);
            ambientTextPaint.setAntiAlias(lowBitAmbient);

            initComplications();

            cal = Calendar.getInstance();

            updateBackground = true;
            invalidate();
        }

        /**
         * Initializes the complications.
         */
        private void initComplications() {
            complicationDataSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            complicationPaint = new Paint();
            complicationPaint.setColor(Color.WHITE);
            complicationPaint.setTextSize(18F);
            complicationPaint.setTypeface(VAPOR_FONT);

            ComplicationDrawable bottomComplicationDrawable = (ComplicationDrawable) getDrawable(R.drawable.custom_complication_styles);
            //noinspection ConstantConditions
            bottomComplicationDrawable.setContext(getApplicationContext());


            complicationDrawableSparseArray = new SparseArray<>(COMPLICATION_IDS.length);
            complicationDrawableSparseArray.put(BOTTOM_COMPLICATION_ID, bottomComplicationDrawable);


            setActiveComplications(COMPLICATION_IDS);
        }

        /**
         * {@inheritDoc}
         */
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


        /**
         * Determines if tap inside a complication area or returns <pre>-1</pre>.
         *
         * @param x x coordinates
         * @param y x coordinates
         * @return the id of the tapped complication or <pre>-1</pre>.
         */
        @SuppressWarnings("ForLoopReplaceableByForEach")
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

        /**
         * Fires PendingIntent associated with complication (if it has one).
         *
         * @param complicationId the complication id.
         */
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

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        /**
         * Generates a {@link android.text.TextPaint} for the watchface.
         *
         * @param textColor the text color to use
         * @return the finished {@link android.text.TextPaint}
         */
        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(VAPOR_FONT);
            paint.setAntiAlias(true);
            paint.setLetterSpacing(-0.08F);
            return paint;
        }

        /**
         * {@inheritDoc}
         */
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

        /**
         * Registers the TimeZoneReceiver.
         */
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            VaporFace.this.registerReceiver(timeZoneReceiver, filter);
        }

        /**
         * Unregisters the TimeZoneReceiver.
         */
        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            VaporFace.this.unregisterReceiver(timeZoneReceiver);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = VaporFace.this.getResources();
            boolean isRound = insets.isRound();
            float textSize = resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            textPaint.setTextSize(textSize);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

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


        /**
         * Tick, Tack, Tick, Tack...
         *
         * @see android.support.wearable.watchface.CanvasWatchFaceService.Engine#invalidate
         */
        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("ForLoopReplaceableByForEach")
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

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         *
         * @param tapType   see the 'see'
         * @param x         x coordinates
         * @param y         y coordinates
         * @param eventTime time of the event
         * @see android.support.wearable.watchface.WatchFaceService#TAP_TYPE_TOUCH
         * @see android.support.wearable.watchface.WatchFaceService#TAP_TYPE_TAP
         * @see android.support.wearable.watchface.WatchFaceService#TAP_TYPE_TOUCH_CANCEL
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

        /**
         * Draws the whole thing.
         *
         * @param canvas the canvas on which to draw
         * @param bounds bounds of the screen i guess... I don't need to call this manually so I don't really care.
         * @see android.support.wearable.watchface.CanvasWatchFaceService.Engine#onDraw(Canvas, Rect)
         */
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
                drawAesthetic(canvas);
            } else {
                int dstWidth = ((surfaceWidth != null)) ? surfaceWidth : 320;
                int dstHeight = ((surfaceHeight != null)) ? surfaceHeight : 320;
                if (updateBackground) {
                    backgroundDrawables = VaporUtils.getBackgroundDrawables(VaporFace.this);
                    updateBackground = false;
                }
                if (backgroundDrawables.length > 1) {
                    if (bgaCount >= backgroundDrawables.length) {
                        bgaCount = 0;
                    }
                    Bitmap bitmap = VaporUtils.generateBitmap(VaporFace.this, dstWidth, dstHeight, backgroundDrawables[bgaCount]);
                    canvas.drawBitmap(bitmap, 0F, 0F, null);
                    bgaCount++;
                } else {
                    Bitmap bitmap = VaporUtils.generateBitmap(VaporFace.this, dstWidth, dstHeight, backgroundDrawables[0]);
                    canvas.drawBitmap(bitmap, 0F, 0F, null);
                }
            }

            long now = System.currentTimeMillis();
            cal.setTimeInMillis(now);
            drawText(canvas, textPaint, getFormattedTimeString());
            drawComplications(canvas, now);
        }

        /**
         * See return.
         *
         * @return Returns the formatted time string. eg: 00:00 in ambient mode and 00:00:00 in !ambient mode.
         */
        private String getFormattedTimeString() {
            String text;
            if (!DateFormat.is24HourFormat(getApplicationContext())) {
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
            return text;
        }

        /**
         * Draws the complications.
         *
         * @param canvas            the canvas
         * @param currentTimeMillis the current time in MILLISECONDS!
         */
        @SuppressWarnings("ForLoopReplaceableByForEach")
        private void drawComplications(Canvas canvas, long currentTimeMillis) {
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = complicationDrawableSparseArray.get(COMPLICATION_IDS[i]);
                complicationDrawable.draw(canvas, currentTimeMillis);
            }
        }

        /**
         * Text that gets drawn in ambient mode.
         *
         * @param canvas the canvas
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
         * @param canvas the canvas
         * @param paint  the paint
         * @param text   the text
         */
        void drawText(Canvas canvas, Paint paint, String text) {
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


}
