package com.example.bill.assignment_maps_billganley;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.bill.assignment_maps_billganley.R;

public class BroadcastReceiverMap extends BroadcastReceiver {
    private static final String MAP_TAG = "MAP_TAG";

    public static final String NEW_MAP_LOCATION_BROADCAST = "com.example.bill.assignment_maps_billganley.NEW_MAP_LOCATION_BROADCAST";
    public static final String EXTRA_LATITUDE = "LATITUDE";
    public static final String EXTRA_LONGITUDE = "LONGITUDE";
    public static final String MAP_LOCATION = "LOCATION";

    public static final int CHANNEL_ID = 1;
    public static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    public static final String CHANNEL_DESCRIPTION = "BROADCAST MAP CHANNEL";
    public static final String CHANNEL_NAME = "MAPS";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    @Override
    public void onReceive(Context context, Intent intent) {

        Double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN);
        Double longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN);
        String location = intent.getStringExtra(MAP_LOCATION);

        // It assumes the highest and lowest latitude on Earth as being, respectively, 90 and -90.
        // Any points between -23 and 23 is considered as CENTRAL HEMISPHERE, as it is close
        // to the Equator.
        String hemisphere = getHemisphere(latitude);


        if (hemisphere.equals("NORTH") || hemisphere.equals("SOUTH") || hemisphere.equals("SOUTH")){

            // Create an instance of the NotificationManager
            // It call the static function from(Context) to get a NotificationManagerCompat object, and then call
            // one of its methods to post or cancel notifications

            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // If you are using the Android API above then 26, you should use another constructor.
//            builder = new Notification.Builder(context, CHANNEL_NAME);
            //Create NotificationCompat.Builder with my API level 22
            builder = new NotificationCompat.Builder(context,CHANNEL_NAME);
            // Set up the notification Title and Text.
            builder.setSmallIcon(R.drawable.broadcast);
            builder.setContentTitle(location);
            builder.setContentText(Double.isNaN(latitude) || Double.isNaN(longitude)
                    ? "Location Unknown" :
                    "Located at the " + hemisphere +
                            ", with coordinates (lat, lng): "+
                            latitude+", "+longitude);

            // Set the notification channel
//            notificationManager.createNotificationChannel(getNotificationChannel());

            // Set the notification ID, and passing as parameter the Notification.Builder.
            notificationManager.notify(CHANNEL_ID, builder.build());

        }else
            Log.d(MAP_TAG, String.valueOf(R.string.location_out));

    }

//    private NotificationChannel getNotificationChannel(){
//
//        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_NAME, CHANNEL_DESCRIPTION, CHANNEL_IMPORTANCE);
//        notificationChannel.setDescription(CHANNEL_DESCRIPTION);
//        notificationChannel.enableLights(true);
//        notificationChannel.setLightColor(Color.BLUE);
//        notificationChannel.enableVibration(true);
//        notificationChannel.setShowBadge(true);

//        return notificationChannel;
//    }

    private String getHemisphere(Double latitude){

        String hemisphere = "";
        boolean isCentralHemisphere = (latitude < 23 && latitude > -23);
        boolean isNorthHemisphere = (latitude > 23 && latitude <= 90);
        boolean isSouthHemisphere = (latitude < -23 && latitude >= -90);

        if(isCentralHemisphere)
            hemisphere = "CENTRAL";
        else{
            if (isNorthHemisphere) hemisphere = "NORTH";
            else{
                if (isSouthHemisphere)
                    hemisphere = "SOUTH";
            }
        }

        return hemisphere;
    }
}
