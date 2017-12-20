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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;

import com.example.android.shushme.util.Util;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private static final int FINE_LOCATION_PERMISSION = 111;

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

}
