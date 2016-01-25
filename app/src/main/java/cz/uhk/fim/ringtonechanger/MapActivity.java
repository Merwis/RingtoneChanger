package cz.uhk.fim.ringtonechanger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Petr on 16. 1. 2016.
 */
public class MapActivity extends Activity implements OnMapReadyCallback {

    MapFragment mMapFragment;
    MarkerOptions mMarker;
    Button mAddCoordinatesButton;
    LatLng mLatLng, mOldLatLng;
    String mName, mLongitude, mLatitude, mRadius, mLastKnownLongitude, mLastKnownLatitude;
    SharedPreferences mSharedPreferences;


    public static final String TAG = "MapActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            mName = b.getString(AreasTable.COLUMN_NAME_NAME);
            mLongitude = (b.getString(AreasTable.COLUMN_NAME_LONGITUDE));
            mLatitude = (b.getString(AreasTable.COLUMN_NAME_LATITUDE));
            mRadius = (b.getString(AreasTable.COLUMN_NAME_RADIUS));
        }

        if (savedInstanceState != null) {
            mLongitude = savedInstanceState.getString(AreasTable.COLUMN_NAME_LONGITUDE);
            mLatitude = savedInstanceState.getString(AreasTable.COLUMN_NAME_LATITUDE);
        }



        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mAddCoordinatesButton = (Button) findViewById(R.id.addGpsCoordinates);
        mAddCoordinatesButton.setEnabled(false);
        mAddCoordinatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AreasTable.COLUMN_NAME_LONGITUDE, mLatLng.longitude + "");
                resultIntent.putExtra(AreasTable.COLUMN_NAME_LATITUDE, mLatLng.latitude + "");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);
        mLastKnownLongitude = mSharedPreferences.getString(Strings.LAST_KNOWN_LONGITUDE_ADDED_KEY, "");
        mLastKnownLatitude = mSharedPreferences.getString(Strings.LAST_KNOWN_LATITUDE_ADDED_KEY, "");
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        if (!mLongitude.isEmpty() && !mLatitude.isEmpty()) {
            mOldLatLng = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
            mMarker = new MarkerOptions().position(mOldLatLng).title(mName);
            mLatLng = mOldLatLng;
            googleMap.addMarker(mMarker);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 8f));
            mAddCoordinatesButton.setEnabled(true);
        } else if (!mLastKnownLongitude.isEmpty() && !mLastKnownLatitude.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Double.parseDouble(mLastKnownLatitude),
                            Double.parseDouble(mLastKnownLongitude)),
                    8f));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mAddCoordinatesButton.setEnabled(true);
                mLatLng = latLng;
                if (mMarker == null) {
                    mMarker = new MarkerOptions().position(latLng).title(mName);
                    googleMap.addMarker(mMarker);
                } else {
                    googleMap.clear();
                    mMarker.position(latLng);
                    googleMap.addMarker(mMarker);
                }
                drawCircle(googleMap);
                Log.d(TAG, "" + latLng.latitude + " " + latLng.longitude);
            }
        });

        if (mOldLatLng != null) {
            if (mOldLatLng.latitude == mLatLng.latitude) {
                drawCircle(googleMap);
            }
        }


    }

    public void drawCircle(GoogleMap googleMap) {
        if (mMarker != null && !mRadius.isEmpty()) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(mLatLng)
                    .radius(Double.parseDouble(mRadius) * 1000)
                    .visible(true);
            googleMap.addCircle(circleOptions);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AreasTable.COLUMN_NAME_LONGITUDE, mLatLng.longitude + "");
        outState.putString(AreasTable.COLUMN_NAME_LATITUDE, mLatLng.latitude + "");
    }
}

