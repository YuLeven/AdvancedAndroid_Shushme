package com.example.android.shushme.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.example.android.shushme.GeofenceBroadcastReceived;
import com.example.android.shushme.R;

/**
 * Created by Yuri Levenhagen on 2017-12-20 as part
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

public class Util {

    /**
     * Returns whether the passed permission is granted to the app
     * @param context - The context of the caller
     * @param permission - The permission to be checked (This is a string but can me retrieve from the static properties of Manifest.permissions)
     * @return - Whether the app has access or not
     */
    public static boolean checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Retrieves ringer permissions to the current APP
     * @param context - The context of the caller
     * @return - Whether the app has this permission or not
     */
    @RequiresApi(api = 23)
    public static boolean hasNotificationPermission(Context context) {
        NotificationManager nm = getNotificationManager(context);
        return nm != null && nm.isNotificationPolicyAccessGranted();
    }

    /**
     * Sets the ringer mode
     * @param context - The context of the caller
     * @param mode - The mode to set
     */
    public static void setRingerMode(Context context, int mode) {
        if (Build.VERSION.SDK_INT < 24 || (Build.VERSION.SDK_INT >= 24) && !hasNotificationPermission(context)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager != null)
                audioManager.setRingerMode(mode);
        }
    }

    public static void notifyUserOfRingerChange(Context context, boolean isDisabling) {

        // Gets the correct icons and phrases
        int smallIcon;
        int notificationPhrase;

        if (isDisabling) {
            smallIcon = R.drawable.ic_volume_off_white_24dp;
            notificationPhrase = R.string.silent_mode_activated;
        } else {
            smallIcon = R.drawable.ic_volume_up_white_24dp;
            notificationPhrase = R.string.silent_mode_deactivated;
        }


        // Builds the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIcon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), smallIcon))
                        .setContentTitle(context.getString(notificationPhrase));

        // Displays it
        NotificationManager nm = getNotificationManager(context);
        if (nm != null) {
            nm.notify(GeofenceBroadcastReceived.RINGER_NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Gets the Android notification manager
     * @param context - The context of the caller
     * @return - A NotificationManager (nullable)
     */
    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


}
