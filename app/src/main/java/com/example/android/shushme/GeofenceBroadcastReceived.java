package com.example.android.shushme;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.android.shushme.util.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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

public class GeofenceBroadcastReceived extends BroadcastReceiver {

    public static final String LOG_TAG = GeofenceBroadcastReceived.class.getCanonicalName();
    public static final int RINGER_NOTIFICATION_ID = 100;

    /**
     * This will be invoked when a broadcast is made once the user crosses the boundary of the GeoFence
     * @param context - The context that caused the trigger
     * @param intent - The intent itself
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        switch (event.getGeofenceTransition()) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Util.setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
                Util.notifyUserOfRingerChange(context, true);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Util.setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
                Util.notifyUserOfRingerChange(context, true);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Util.setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
                Util.notifyUserOfRingerChange(context, false);
                break;
            default:
                Log.i(LOG_TAG, "Unknown geofence transition received");
        }
    }
}
