package io.lerk.vaporface.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import static android.content.Context.MODE_PRIVATE;


public final class VaporUtils {
    private static final String TAG = VaporUtils.class.getCanonicalName();

    public static final String PATH_WITH_FEATURE = "/watch_face_config/VaporFace";
    public static final String KEY_BACKGROUND_IMAGE = "background";
    public static final String KEY_ENABLE_ANIMATION = "animations_enabled";
    public static final String PREFERENCES_NAME = "vaporface";

    /**
     * Callback interface to perform an action with the current config {@link DataMap} for
     * {@link VaporFace}.
     */
    public interface FetchConfigDataMapCallback {
        /**
         * Callback invoked with the current config {@link DataMap} for
         * {@link VaporFace}.
         */
        void onConfigDataMapFetched(DataMap config);
    }

    /**
     * Asynchronously fetches the current config {@link DataMap} for {@link VaporFace}
     * and passes it to the given callback.
     * <p>
     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    public static void fetchConfigDataMap(final GoogleApiClient client,
                                          final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                getLocalNodeResult -> {
                    String localNode = getLocalNodeResult.getNode().getId();
                    Uri uri = new Uri.Builder()
                            .scheme("wear")
                            .path(PATH_WITH_FEATURE)
                            .authority(localNode)
                            .build();
                    Wearable.DataApi.getDataItem(client, uri)
                            .setResultCallback(new DataItemResultCallback(callback));
                }
        );
    }

    /**
     * Adds locally stored preferences or default values to the dataMap.
     *
     * @param dataMap the dataMap
     */
    public static void setDefaultValuesForMissingConfigKeys(Context context, DataMap dataMap) {
        SharedPreferences preferences = context.getSharedPreferences(VaporUtils.PREFERENCES_NAME, MODE_PRIVATE);

        if (!dataMap.containsKey(VaporUtils.KEY_BACKGROUND_IMAGE)) {
            dataMap.putString(VaporUtils.KEY_BACKGROUND_IMAGE, preferences.getString(VaporUtils.KEY_BACKGROUND_IMAGE, String.valueOf(0)));
        }

        if (!dataMap.containsKey(VaporUtils.KEY_ENABLE_ANIMATION)) {
            dataMap.putBoolean(VaporUtils.KEY_ENABLE_ANIMATION, preferences.getBoolean(VaporUtils.KEY_ENABLE_ANIMATION, false));
        }
    }

    /**
     * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
     * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
     * it's created.
     * <p>
     * It is allowed that only some of the keys used in the config DataItem appear in
     * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
     */
    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
                                                    final DataMap configKeysToOverwrite) {

        VaporUtils.fetchConfigDataMap(googleApiClient,
                currentConfig -> {
                    DataMap overwrittenConfig = new DataMap();
                    overwrittenConfig.putAll(currentConfig);
                    overwrittenConfig.putAll(configKeysToOverwrite);
                    VaporUtils.putConfigDataItem(googleApiClient, overwrittenConfig);
                }
        );
    }

    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        putDataMapRequest.setUrgent();
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(dataItemResult -> {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                    }
                });
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }

    private VaporUtils() {
    }
}
