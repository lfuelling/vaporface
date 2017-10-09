package io.lerk.vaporface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.concurrent.Executors;

/**
 * Copied from the codelabs example, modified. So it's Apache2.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */

public class VaporFaceConfigActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ConfigActivity";

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    private SharedPreferences preferences;
    private ImageButton bgChangeRight;
    private ImageButton bgChangeLeft;
    private ImageButton animationToggle;

    public enum ComplicationLocation {
        BOTTOM
    }

    private int bottomComplicationId;

    // Selected complication id by user.
    private int selectedComplicationId;

    // ComponentName used to identify a specific service that renders the watch face.
    private ComponentName watchFaceComponentName;

    // Required to retrieve complication data from watch face for preview.
    private ProviderInfoRetriever providerInfoRetriever;

    private ImageView bottomComplicationBackground;

    private ImageButton bottomComplication;

    private Drawable defaultAddComplicationDrawable;

    private Integer currentBG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        defaultAddComplicationDrawable = getDrawable(R.drawable.add_complication);

        selectedComplicationId = -1;
        watchFaceComponentName = new ComponentName(getApplicationContext(), VaporFace.class);

        initBottomComplication();


        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever =
                new ProviderInfoRetriever(getApplicationContext(), Executors.newCachedThreadPool());
        providerInfoRetriever.init();

        preferences = getSharedPreferences("vaporface", MODE_PRIVATE);

        setBackgroundPreview();

        animationToggle = findViewById(R.id.animation_toggle);
        boolean animationsEnabled = preferences.getBoolean("animations_enabled", false);
        if(animationsEnabled) {
            animationToggle.setImageDrawable(getDrawable(R.drawable.ic_check_box_white_24dp));
        } else {
            animationToggle.setImageDrawable(getDrawable(R.drawable.ic_check_box_outline_blank_white_24dp));
        }
        animationToggle.setOnClickListener(v -> toggleAnimationToggle());

        bgChangeRight = findViewById(R.id.bgc_right);
        bgChangeRight.setOnClickListener(v -> toggleNextBg());

        bgChangeLeft = findViewById(R.id.bgc_left);
        bgChangeLeft.setOnClickListener(v -> togglePrevBg());

        retrieveInitialComplicationsData();
    }

    private void toggleAnimationToggle() {
        boolean previousState = preferences.getBoolean("animations_enabled", false);
        preferences.edit().putBoolean("animations_enabled", !previousState).apply();
        boolean animationsEnabled = preferences.getBoolean("animations_enabled", false);
        if(animationsEnabled) {
            animationToggle.setImageDrawable(getDrawable(R.drawable.ic_check_box_white_24dp));
        } else {
            animationToggle.setImageDrawable(getDrawable(R.drawable.ic_check_box_outline_blank_white_24dp));
        }
    }

    private void togglePrevBg() {
        currentBG--;
        if (currentBG < 0) {
            currentBG = 0;
            bgChangeLeft.setVisibility(View.GONE);
            bgChangeRight.setVisibility(View.VISIBLE);
        } else {
            bgChangeLeft.setVisibility(View.VISIBLE);
            bgChangeRight.setVisibility(View.VISIBLE);
        }
        preferences.edit().putString("background", String.valueOf(currentBG)).apply();
        setBackgroundPreview();
    }

    private void toggleNextBg() {
        currentBG++;
        if (currentBG > 7) {
            currentBG = 7;
            bgChangeRight.setVisibility(View.GONE);
            bgChangeLeft.setVisibility(View.VISIBLE);
        } else {
            bgChangeLeft.setVisibility(View.VISIBLE);
            bgChangeRight.setVisibility(View.VISIBLE);
        }

        preferences.edit().putString("background", String.valueOf(currentBG)).apply();
        setBackgroundPreview();
    }

    private void setBackgroundPreview() {
        String background = preferences.getString("background", String.valueOf(currentBG));

        View container = findViewById(R.id.config_view);
        switch (background) {
            case "1":
                container.setBackground(getDrawable(R.drawable.bg_04_01));
                break;
            case "2":
                container.setBackground(getDrawable(R.drawable.bg_08_01));
                break;
            case "3":
                container.setBackground(getDrawable(R.drawable.bg_10_01));
                break;
            case "4":
                container.setBackground(getDrawable(R.drawable.bg_12_01));
                break;
            case "5":
                container.setBackground(getDrawable(R.drawable.bg_15_01));
                break;
            case "6":
                container.setBackground(getDrawable(R.drawable.bg_16_01));
                break;
            case "7":
                container.setBackground(getDrawable(R.drawable.bg_20_01));
                break;
            case "0":
            default:
                container.setBackground(getDrawable(R.drawable.vaporwave_grid));
                break;
        }
        currentBG = Integer.parseInt(background);
    }

    private void initBottomComplication() {
        bottomComplicationId = VaporFace.getComplicationId(ComplicationLocation.BOTTOM);

        // Sets up bottom complication preview.
        bottomComplicationBackground = findViewById(R.id.bottom_complication_background);
        bottomComplication = findViewById(R.id.bottom_complication);
        bottomComplication.setOnClickListener(this);

        // Sets default as "Add Complication" icon.
        bottomComplication.setImageDrawable(defaultAddComplicationDrawable);
        bottomComplicationBackground.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VaporFace.updateBackground = true;
        providerInfoRetriever.release();
    }

    public void retrieveInitialComplicationsData() {

        final int[] complicationIds = VaporFace.getComplicationIds();

        providerInfoRetriever.retrieveProviderInfo(
                new ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                    @Override
                    public void onProviderInfoReceived(
                            int watchFaceComplicationId,
                            @Nullable ComplicationProviderInfo complicationProviderInfo) {

                        Log.d(TAG, "onProviderInfoReceived: " + complicationProviderInfo);

                        updateComplicationViews(watchFaceComplicationId, complicationProviderInfo);
                    }
                }, watchFaceComponentName, complicationIds);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(bottomComplication)) {
            launchComplicationHelperActivity(ComplicationLocation.BOTTOM);
        }
    }

    // Verifies the watch face supports the complication location, then launches the helper
    // class, so user can choose their complication data provider.
    private void launchComplicationHelperActivity(ComplicationLocation complicationLocation) {

        selectedComplicationId = VaporFace.getComplicationId(complicationLocation);

        if (selectedComplicationId >= 0) {

            int[] supportedTypes =
                    VaporFace.getSupportedComplicationTypes(complicationLocation);

            startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                            getApplicationContext(),
                            watchFaceComponentName,
                            selectedComplicationId,
                            supportedTypes),
                    VaporFaceConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

        } else {
            Log.d(TAG, "Complication not supported by watch face.");
        }
    }

    public void updateComplicationViews(
            int watchFaceComplicationId, ComplicationProviderInfo complicationProviderInfo) {
        Log.d(TAG, "updateComplicationViews(): id: " + watchFaceComplicationId);
        Log.d(TAG, "\tinfo: " + complicationProviderInfo);

        if (watchFaceComplicationId == bottomComplicationId) {
            if (complicationProviderInfo != null) {
                bottomComplication.setImageIcon(complicationProviderInfo.providerIcon);
                bottomComplicationBackground.setVisibility(View.VISIBLE);
            } else {
                bottomComplication.setImageDrawable(defaultAddComplicationDrawable);
                bottomComplicationBackground.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {

            // Retrieves information for selected Complication provider.
            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            if (selectedComplicationId >= 0) {
                updateComplicationViews(selectedComplicationId, complicationProviderInfo);
            }
        }

    }
}
