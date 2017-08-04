package io.lerk.vaporface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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

        retrieveInitialComplicationsData();
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
