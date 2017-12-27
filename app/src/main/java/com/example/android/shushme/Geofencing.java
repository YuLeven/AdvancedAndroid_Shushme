package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuri Levenhagen on 2017-12-27 as part
 * of the Udacity-Google Advanced Android App Development course.
 * <p>
 * The base example code belongs to The Android Open Source Project under the Apache 2.0 licence
 * All code further implemented as part of the course is under the same licence.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Geofencing implements ResultCallback {

    private static final String LOG_TAG = Geofence.class.getCanonicalName();
    // Limits the Geofence lifetime 10 hours (in miliseconds)
    private static final int GEOFENCE_TIMEOUT = 10 * 60 * 60 * 1000;
    // Determines the radius of the geofence (in meters)
    private static final int GEOFENCE_RADIUS = 15;

    private GoogleApiClient mApiClient;
    private Context mContext;
    private List<Geofence> mGeofences;
    private PendingIntent mGeofencePendingIntent;

    public Geofencing(Context context, GoogleApiClient apiClient) {
        mApiClient = apiClient;
        mContext = context;
        mGeofences = new ArrayList<>();
        mGeofencePendingIntent = null;
    }

    /**
     * Checks if the geofences can be set, and, if so, register them
     */
    public void registerGeofences() {
        // Returns early if the geofences can't be set
        if (!canRegisterGeofences()) return;

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException sException) {
            Log.e(LOG_TAG, sException.getLocalizedMessage());
        }
    }

    /**
     * Deregister the Geofences (the cient has to be set and connected)
     */
    public void unregisterGeofences() {
        // Returns early if the client isn't set or connected
        if (!isClientAvailable()) return;

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException sException) {
            Log.e(LOG_TAG, sException.getLocalizedMessage());
        }
    }

    /**
     * Register a set of places as Geofences on the Google Play Services API
     * @param places - The list of places to be registered as virtual fences
     */
    public void updateGeofencesList(PlaceBuffer places) {
        mGeofences.clear();

        // Return early if no places were passed
        if (places == null || places.getCount() == 0) return;

        for (Place place : places) {
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLong = place.getLatLng().longitude;

            // Build a new Geofence and adds it to the list
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLong, GEOFENCE_RADIUS)
                    // More bitflags can be found within the Geofence class
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofences.add(geofence);
        }
    }

    /**
     * This will be called as a callback to addGeofences()
     * @param result - The result passed to the callback
     */
    @Override
    public void onResult(@NonNull Result result) {
        if (result.getStatus().isSuccess()) {
            Log.d(LOG_TAG, "Successfully added new location");
        } else {
            Log.e(LOG_TAG, String.format("Error adding/removing geofence: %s",
                    result.getStatus().toString()));
        }
    }

    /**
     * Creates a GeofencingRequest object using its builder.
     * This will be used to actually request their inclusion on the API.
     * @return - A GeofencingRequest
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder geoBuilder = new GeofencingRequest.Builder();
        // This control behaviour in case the device is already inside a GeoFence.
        // The passed flag will cause it to fire an event immediately if it IS already inside one.
        geoBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        geoBuilder.addGeofences(mGeofences);
        return geoBuilder.build();
    }

    /**
     * Getter for the pending intent. It will try to reuse a currently set Pending Intent
     * and, only if it's null, build a new one before returning it.
     * @return - This class' mGeofencePendingIntent property
     */
    private PendingIntent getGeofencePendingIntent() {
        // If we already have a PendingIntent set, return it
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent;

        // Otherwise, build a new one and return it
        mGeofencePendingIntent = buildPendingIntent();
        return mGeofencePendingIntent;
    }

    /**
     * Builds a new PendingIntent
     * @return - A Pending Intent registered to the GeofenceBroadcastReceived class
     */
    private PendingIntent buildPendingIntent() {
        // Registers the GeofenceBroadcastReceived as the received of the intent
        Intent intent = new Intent(mContext, GeofenceBroadcastReceived.class);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Return wether the cient is set and connected
     * @return - A boolean stating the availability of the client
     */
    private boolean isClientAvailable() {
        return mApiClient != null && mApiClient.isConnected();
    }

    /**
     * Checks if the client is available and there are GeoFences in the list
     * @return - A boolean stating if Geofences can be registered
     */
    private boolean canRegisterGeofences() {
        return  isClientAvailable() && mGeofences != null && mGeofences.size() > 0;
    }
}
