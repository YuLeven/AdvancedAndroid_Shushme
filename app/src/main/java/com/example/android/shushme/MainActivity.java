package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.android.shushme.provider.PlaceContract;
import com.example.android.shushme.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener {

    // Constants
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private static final int FINE_LOCATION_PERMISSION = 111;
    private static final int PLACE_PICKER_REQUEST = 112;

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Bind this activity to the Google Api Client
        bindToGoogleApiClient();
    }

    /**
     * This will be called every time the app comes to the foreground.
     * We're checking the permissions and setting the checkboxes here
     * to avoid issues should the user reset permission outside of the app
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Check if we were granted notifications permissions and, if so, lock the checkbox
        CheckBox locationPermission = (CheckBox) findViewById(R.id.location_permission_checkbox);
        boolean hasNotificationPermission = Util.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        locationPermission.setEnabled(!hasNotificationPermission);
        locationPermission.setChecked(hasNotificationPermission);
    }

    /**
     * This is called when the permission checkbox is toggled
     * @param view - The caller. We won't use it, but it is necessary so Android can find and call this method
     */
    public void onLocationPermissionClicked(View view) {
        ActivityCompat.requestPermissions(
            this,
            new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
            FINE_LOCATION_PERMISSION
        );
    }

    public void onAddNewLocationClicked(View view) {
        if (!Util.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, R.string.location_permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }

        // Starts the place picker (Google provided activity that allows the user to search for places)
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            // Since this is mock app, we will do nothing
            Log.e(LOG_TAG, e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            // Since this is mock app, we will do nothing
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);

            // Return early if no place was picked
            if (place == null) {
                Log.d(LOG_TAG, "No place was selected");
                return;
            }

            // Gets the needed place information
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            // Persists the retrieved information to the database
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "Google API client connection established");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Google API client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google API client connection failed");
    }

    /**
     * Uses GoogleApiClient.Builder to tie this activity to the API client
     */
    private void bindToGoogleApiClient() {
        new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
    }
}
