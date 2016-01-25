package cz.uhk.fim.ringtonechanger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by Petr on 16. 1. 2016.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Settings activity";

    private boolean gpsOn;
    private Switch mSwGpsOn;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);

        mSwGpsOn = (Switch) findViewById(R.id.swGpsActive);
        gpsOn = mSharedPreferences.getBoolean(Strings.GPS_ACTIVE_ADDED_KEY, false);
        mSwGpsOn.setChecked(gpsOn);
        Log.d(TAG, gpsOn + " prvni");

        mSwGpsOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                gpsOn = isChecked;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(Strings.GPS_ACTIVE_ADDED_KEY, gpsOn);
                editor.commit();
                Log.d(TAG, gpsOn + " po zmene");
            }
        });
    }



    public boolean getGpsOn() {
        return gpsOn;
    }
}
