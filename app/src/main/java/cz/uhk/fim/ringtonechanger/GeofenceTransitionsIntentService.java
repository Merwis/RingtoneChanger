package cz.uhk.fim.ringtonechanger;

/**
 * Created by Petr on 16. 1. 2016.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/* Most of the code taken from an example code on github https://github.com/googlesamples/android-play-location/tree/master/Geofencing
*/
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "geofence-transitions-service";

    AreasDatabaseHelper mHelper;
    SQLiteDatabase mDatabase;
    private Uri ringtone;
    private Uri defaultRingtone;

    SharedPreferences mSharedPreferences;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "chyba1";
           // Log.d(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            //ringtoneDetails = getRingtoneInfo();
            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
           // Log.d(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            // Log.e(TAG, "chyba2");
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {



        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        changeRingtone(triggeringGeofencesIdsString);

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void changeRingtone(String name) {

        mHelper = new AreasDatabaseHelper(this);
        mDatabase = mHelper.getReadableDatabase();
        String[] projection = new String[] {
                AreasTable.COLUMN_NAME_NAME, AreasTable.COLUMN_NAME_RINGTONE, AreasTable.COLUMN_NAME_ACTIVE};
        Cursor c = mDatabase.query(AreasTable.TABLE_NAME, projection,
                AreasTable.COLUMN_NAME_NAME + " LIKE ?", new String[]{name}, null, null, null);
        int colRingtone = c.getColumnIndex(AreasTable.COLUMN_NAME_RINGTONE);
        int colName = c.getColumnIndex(AreasTable.COLUMN_NAME_NAME);
        c.moveToLast();
/*        Log.d(TAG, name + "name y geofencu");
          Log.d(TAG, "name y db = " + c.getString(colName));*/

        try {
            if (c.getCount() != 0) {
                ringtone = Uri.parse(c.getString(colRingtone));
                Log.d(TAG, ringtone + "ringtone z db");
                Log.d(TAG, name + "name z geofencu");
            }
        } catch (SecurityException e) {

        }

        c.close();
    }

    private Uri getRingtone() {
        return RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
    }

    private void setNewRingtone() {
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, ringtone);
    }

    private void setDefaultRingtone() {
        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);
        defaultRingtone = Uri.parse(mSharedPreferences.getString(Strings.DEFAULT_RINGTONE_ADDED_KEY, ""));
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, defaultRingtone);
    }

    private String getRingtoneInfo() {
        MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();
        Uri uri = ringtone;
        Log.d(TAG, ringtone + "");
        Log.d(TAG, defaultRingtone + " default");
        mediaMetadataRetriever.setDataSource(getApplicationContext(), uri);
        return "Ringtone: " + mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) +
                ": " +mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);


        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getRingtoneInfo())
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());

    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                setNewRingtone();
                return "You entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                setDefaultRingtone();
                return "You left";
            default:
                return "You are in desired area";
        }
    }
}

