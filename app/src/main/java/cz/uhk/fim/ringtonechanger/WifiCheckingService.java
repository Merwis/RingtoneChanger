package cz.uhk.fim.ringtonechanger;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Petr on 16. 1. 2016.
 */
public class WifiCheckingService extends Service {

    private String wifiName;
    private Uri ringtone;
    private Uri defaultRingtone;
    private WifiManager wifiManager;
    BroadcastReceiver receiver;
    Timer t = new Timer();
    Handler mHandler = new Handler();

    AreasDatabaseHelper mHelper;
    SimpleCursorAdapter mAdapter;
    SQLiteDatabase mDatabase;

    private boolean connected = false;

    public static final String TAG = "WifiSevice";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHelper = new AreasDatabaseHelper(getApplicationContext());
        mDatabase = mHelper.getReadableDatabase();

/*        wifiName = intent.getStringExtra(AreasTable.COLUMN_NAME_WIFI);
        ringtone = Uri.parse(intent.getStringExtra(AreasTable.COLUMN_NAME_RINGTONE));*/
        defaultRingtone = getRingtone();
        Log.d(TAG, "start sluzby" + wifiName);
        checkConnection();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Neco se deje");
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkConnection();
                    }
                }, 2000);


            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(wifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilter);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        setDefaultRingtone();
        Log.d(TAG, "konec sluzby");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkConnection () {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Log.d(TAG, "SSID je " + "\"" + wifiManager.getConnectionInfo().getSSID() + "\"");
        Log.d(TAG, "Jestli je zapnuto: " + wifiManager.isWifiEnabled() );
        String SSID = "\"" + wifiManager.getConnectionInfo().getSSID() + "\"";
        wifiName = null;

        String[] projection = new String[] {
                AreasTable.COLUMN_NAME_WIFI, AreasTable.COLUMN_NAME_RINGTONE
        };

        Cursor c = mDatabase.query(AreasTable.TABLE_NAME, projection,
                AreasTable.COLUMN_NAME_WIFI + " LIKE ?", new String[] {SSID}, null, null, null);

        Log.d(TAG, "c je " + c.getCount() + "");

        if (c.getCount() > 0) {
            int colRingtone = c.getColumnIndex(AreasTable.COLUMN_NAME_RINGTONE);
            c.moveToFirst();
            ringtone = Uri.parse(c.getString(colRingtone));
            int colWifi = c.getColumnIndex(AreasTable.COLUMN_NAME_WIFI);
            wifiName = c.getString(colWifi);
        }
        c.close();

        if (wifiManager.isWifiEnabled() && SSID.equals(wifiName) && !connected) {
            setNewRingtone();
            sendNotification(getString(R.string.connected));
            connected = true;
        } else if (!wifiManager.isWifiEnabled() && !connected) {
            /*Log.d(TAG, "Toastik");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Wifi is disabled", Toast.LENGTH_SHORT).show();
                }
            });*/
        } else if (!wifiManager.isWifiEnabled() && connected) {
            sendNotification(getString(R.string.disconnected));
            connected = true;
        } else {
        } if (!SSID.equals(wifiName) && connected){
            Log.d(TAG, "pripojeno ale k jine");
            setDefaultRingtone();
            connected = false;
            sendNotification(getString(R.string.connected));
        }
    }

    private Uri getRingtone() {
        return RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
    }

    private void setNewRingtone() {
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, ringtone);
    }

    private void setDefaultRingtone() {
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, defaultRingtone);
    }

    private String getRingtoneInfo() {
        MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();
        Uri uri = getRingtone();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), uri);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) +
                ": " +mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }

    private void sendNotification(String status) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(status + " " + wifiName)
                .setContentText("Ringtone: " + getRingtoneInfo());

        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }
}
