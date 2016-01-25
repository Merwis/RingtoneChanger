package cz.uhk.fim.ringtonechanger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Petr on 15. 1. 2016.
 */
public class AddAreaActivity extends AppCompatActivity {

    private static final int SELECT_WIFI_INTENT_NUMBER = 1;
    private static final int SELECT_RINGTONE_INTENT_NUMBER = 2;
    private static final int SELECT_COORDINATES_INTENT_NUMBER = 3;

    EditText mEtAreaName, mEtAreaLongitude, mEtAreaLatitude, mEtAreaRadius,
             mEtAreaWifi, mEtAreaRingtone;
    Button mBtnSave, mBtnOpenMap, mBtnSelectRingtone, mBtnSelectWifi;

    Uri audioUri;

    AreasDatabaseHelper mHelper;
    SQLiteDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_area);

        mEtAreaName = (EditText) findViewById(R.id.addAreaName);
        mEtAreaLongitude = (EditText) findViewById(R.id.addAreaLongitude);
        mEtAreaLatitude = (EditText) findViewById(R.id.addAreaLatitude);
        mEtAreaRadius = (EditText) findViewById(R.id.addAreaRadius);
        mEtAreaWifi = (EditText) findViewById(R.id.addAreaWifi);
        mEtAreaRingtone = (EditText) findViewById(R.id.addAreaRingtone);

        mEtAreaLongitude.setEnabled(false);
        mEtAreaLatitude.setEnabled(false);
        mEtAreaWifi.setEnabled(false);
        mEtAreaRingtone.setEnabled(false);

        mEtAreaRadius.setText("0.3");

        mBtnSelectWifi = (Button) findViewById(R.id.openWifiList);
        mBtnSave = (Button) findViewById(R.id.addAreaSave);
        mBtnOpenMap = (Button) findViewById(R.id.openMap);
        mBtnSelectRingtone = (Button) findViewById(R.id.openRingtoneList);

        mHelper = new AreasDatabaseHelper(this);
        mDatabase = mHelper.getWritableDatabase();

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtAreaName.getText().toString().trim().equals("")) {
                    mEtAreaName.setError(getString(R.string.areaNameIsRequired));
                } else if (mEtAreaRingtone.getText().toString().trim().equals("")) {
                    mEtAreaRingtone.setError(getString(R.string.ringtoneIsRequired));
                } else {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AreasTable.COLUMN_NAME_NAME, mEtAreaName.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_LONGITUDE, mEtAreaLongitude.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_LATITUDE, mEtAreaLatitude.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_RADIUS, mEtAreaRadius.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_WIFI, mEtAreaWifi.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_RINGTONE, mEtAreaRingtone.getText().toString());
                    contentValues.put(AreasTable.COLUMN_NAME_ACTIVE, "0");

                    mDatabase.insert(AreasTable.TABLE_NAME, null, contentValues);

                    mDatabase.close();

                    getBack();
                }
            }
        });

        mBtnSelectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAreaActivity.this, WifiListActivity.class);
                startActivityForResult(intent, SELECT_WIFI_INTENT_NUMBER);
            }
        });

        mBtnSelectRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_RINGTONE_INTENT_NUMBER);
            }
        });

        mBtnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddAreaActivity.this, MapActivity.class);
                Bundle b = new Bundle();
                b.putString(AreasTable.COLUMN_NAME_NAME, mEtAreaName.getText().toString());
                b.putString(AreasTable.COLUMN_NAME_RADIUS, mEtAreaRadius.getText().toString());
                b.putString(AreasTable.COLUMN_NAME_LONGITUDE, mEtAreaLongitude.getText().toString());
                b.putString(AreasTable.COLUMN_NAME_LATITUDE, mEtAreaLatitude.getText().toString());
                intent.putExtras(b);
                startActivityForResult(intent, SELECT_COORDINATES_INTENT_NUMBER);
            }
        });


    }

    private void getBack() {
        Intent intent = new Intent(this, RingtoneChangingActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_areas_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_area:
                Intent intentDestination = new Intent(this, AddAreaActivity.class);
                startActivity(intentDestination);
                return true;
            /*case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);*/
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SELECT_WIFI_INTENT_NUMBER) :
                if (resultCode == Activity.RESULT_OK) {
                    mEtAreaWifi.setText(data.getStringExtra(AreasTable.COLUMN_NAME_WIFI));
                }
                break;
            case (SELECT_RINGTONE_INTENT_NUMBER) :
                if (resultCode == Activity.RESULT_OK) {
                    audioUri = data.getData();
                    mEtAreaRingtone.setText(audioUri.toString());
                }
                break;
            case (SELECT_COORDINATES_INTENT_NUMBER) :
                if (resultCode == Activity.RESULT_OK) {
                    mEtAreaLongitude.setText(data.getStringExtra(AreasTable.COLUMN_NAME_LONGITUDE));
                    mEtAreaLatitude.setText(data.getStringExtra(AreasTable.COLUMN_NAME_LATITUDE));
                }

        }
    }
}
