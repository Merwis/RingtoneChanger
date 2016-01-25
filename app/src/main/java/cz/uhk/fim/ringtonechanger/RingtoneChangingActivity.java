package cz.uhk.fim.ringtonechanger;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class RingtoneChangingActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, AreasListFragment.OnAreaActivatedListener  {

    GoogleApiClient mGoogleApiClient;
    Area activeArea;

    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private boolean mGeofenceAdded;
    private SharedPreferences mSharedPreferences;
    private Uri defaultRingtone;

    private LocationRequest mLocationRequest;


    Button mBtnOpenAddArea;

    public static final String TAG = "RingtoneChangingActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_changing);

        mBtnOpenAddArea = (Button) findViewById(R.id.btn_open_add_area);

        mBtnOpenAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneChangingActivity.this, AddAreaActivity.class);
                startActivity(intent);
            }
        });

        mGeofenceList = new ArrayList<Geofence>();

        mGeofencePendingIntent = null;

        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);

        mGeofenceAdded = mSharedPreferences.getBoolean(Strings.GEOFENCES_ADDED_KEY, false);

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_area:
                Intent intentDestination = new Intent(this, AddAreaActivity.class);
                startActivity(intentDestination);
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to GAC");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Strings.LAST_KNOWN_LONGITUDE_ADDED_KEY, mLastLocation.getLongitude() + "");
            editor.putString(Strings.LAST_KNOWN_LATITUDE_ADDED_KEY, mLastLocation.getLatitude() + "");
            editor.commit();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        defaultRingtone  = getRingtone();
        mGoogleApiClient.connect();
        Log.d(TAG, "connect");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);
        Log.d(TAG, "geof req");
        return builder.build();
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "try");
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()
            ).setResultCallback(this);

            mGeofenceAdded = true;

            Toast.makeText(
                    this,
                    ("Geoalarm activated"),
                    Toast.LENGTH_SHORT
            ).show();
        } catch (SecurityException e) {

        }
    }

    public void removeGeofences(List<String> deletedGeofence) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient, deletedGeofence
            ).setResultCallback(this);
            Log.d(TAG, "Geofence deactivated");
            mGeofenceAdded = false;

            Toast.makeText(
                    this,
                    ("Geoalarm deactivated"),
                    Toast.LENGTH_SHORT
            ).show();
        } catch (SecurityException e) {

        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Strings.GEOFENCES_ADDED_KEY, mGeofenceAdded);
            editor.commit();

        } else {
            Log.d(TAG, "Nekde je chyba");
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onAreaActivated(Area area) {
        startGpsUpdates();
        saveDefaultRingtone();
        activeArea = area;
        Log.d(TAG, "created " + activeArea.getName());

        mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(String.valueOf(activeArea.getName()))
                        .setCircularRegion(
                                Double.parseDouble(activeArea.getLatitude()),
                                Double.parseDouble(activeArea.getLongitude()),
                                activeArea.getRadius() * 1000
                        )
                        .setExpirationDuration(12 * 60 * 60 * 1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()
        );
        Log.d(TAG, "" + activeArea.getLatitude() + " " + activeArea.getLongitude() + " " + activeArea.getRadius());
        addGeofences();
    }

    private void stopGpsUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getGeofencePendingIntent());
    }

    private void startGpsUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, getGeofencePendingIntent());
    }

    @Override
    public void onAreaDeactivated(Area area) {
        List<String> list = new ArrayList<>();
        list.add(0, area.getName());
        stopGpsUpdates();
        setDefaultRingtone();
        removeGeofences(list);
    }

    private Uri getRingtone() {
        return RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
    }

    private void setDefaultRingtone() {
        Log.d(TAG, defaultRingtone + "");
        defaultRingtone = Uri.parse(mSharedPreferences.getString(Strings.DEFAULT_RINGTONE_ADDED_KEY, ""));
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, defaultRingtone);
    }

    private void saveDefaultRingtone() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Strings.DEFAULT_RINGTONE_ADDED_KEY, defaultRingtone + "");
        editor.commit();
    }

    
}
