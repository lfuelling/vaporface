package io.lerk.vaporface;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import io.lerk.vaporface.utils.VaporUtils;

/**
 * The phone-side config activity for {@code DigitalWatchFaceService}. Like the watch-side config
 * activity ({@code DigitalWatchFaceWearableConfigActivity}), allows for setting the background
 * color. Additionally, enables setting the color for hour, minute and second digits.
 */
public class ConfigActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = ConfigActivity.class.getCanonicalName();

    private GoogleApiClient googleApiClient;
    private String peerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        peerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        DataMap dummyMap = new DataMap();
        VaporUtils.setDefaultValuesForMissingConfigKeys(ConfigActivity.this, dummyMap);
        initViews(dummyMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connected: " + connectionHint);
        }

        if (peerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(VaporUtils.PATH_WITH_FEATURE).authority(peerId).build();
            Wearable.DataApi.getDataItem(googleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            initViews(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            initViews(null);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection suspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(@NonNull ConnectionResult result) {

        Toast.makeText(getApplicationContext(), R.string.companion_connection_failed, Toast.LENGTH_LONG).show();

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection failed: " + result);
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.okay);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, (dialog, id) -> {});
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initViews(DataMap config) {
        ImageView backgroundPreview = findViewById(R.id.companion_config_preview);
        SeekBar backgroundSelector = findViewById(R.id.companion_config_background_selector);
        Switch animationToggle = findViewById(R.id.companion_config_animations_toggle);

        backgroundSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setBackgroundPreview(i, backgroundPreview);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "SeekBar: tracking started");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "SeekBar: tracking stopped.");
                sendBackgroundUpdate(String.valueOf(seekBar.getProgress()));
                Log.d(TAG, "SeekBar: Message sent.");
            }
        });

        animationToggle.setOnCheckedChangeListener((compoundButton, b) -> sendAnimationUpdate(b));

        if(config != null) {
            animationToggle.setChecked(config.getBoolean(VaporUtils.KEY_ENABLE_ANIMATION));
            setBackgroundPreview(Integer.parseInt(config.getString(VaporUtils.KEY_BACKGROUND_IMAGE)), backgroundPreview);
        }
    }

    private void setBackgroundPreview(int background, ImageView view) {
        switch (background) {
            case 1:
                view.setImageDrawable(getDrawable(R.drawable.bg_04_01));
                break;
            case 2:
                view.setImageDrawable(getDrawable(R.drawable.bg_08_01));
                break;
            case 3:
                view.setImageDrawable(getDrawable(R.drawable.bg_10_01));
                break;
            case 4:
                view.setImageDrawable(getDrawable(R.drawable.bg_12_01));
                break;
            case 5:
                view.setImageDrawable(getDrawable(R.drawable.bg_15_01));
                break;
            case 6:
                view.setImageDrawable(getDrawable(R.drawable.bg_16_01));
                break;
            case 7:
                view.setImageDrawable(getDrawable(R.drawable.bg_20_01));
                break;
            case 0:
            default:
                view.setImageDrawable(getDrawable(R.drawable.vaporwave_grid));
                break;
        }
    }

    private void sendBackgroundUpdate(String value) {
        if (peerId != null) {
            DataMap config = new DataMap();
            config.putString(VaporUtils.KEY_BACKGROUND_IMAGE, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(googleApiClient, peerId, VaporUtils.PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "config message: " + VaporUtils.KEY_BACKGROUND_IMAGE + " -> " + value);
            }
        }
    }

    private void sendAnimationUpdate(Boolean value) {
        if (peerId != null) {
            DataMap config = new DataMap();
            config.putBoolean(VaporUtils.KEY_ENABLE_ANIMATION, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(googleApiClient, peerId, VaporUtils.PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "config message: " + VaporUtils.KEY_ENABLE_ANIMATION + " -> " + value);
            }
        }
    }
}

