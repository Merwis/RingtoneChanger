package cz.uhk.fim.ringtonechanger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class NoiseCheckingActivity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnCalibrate;
    private boolean noiseChecking;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noise_checking);

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnCalibrate = (Button) findViewById(R.id.btn_calibrate);

        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);

        noiseChecking = mSharedPreferences.getBoolean(Strings.NOISE_CHECKING_RUNNING_ADDED_KEY, false);

        switchButtons();

        final Intent mServiceIntent = new Intent(this, NoiseCheckingService.class);



        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(mServiceIntent);
                noiseChecking = true;
                switchButtons();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(mServiceIntent);
                noiseChecking = false;
                switchButtons();
            }
        });



        mBtnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoiseChecking n = new NoiseChecking(getApplicationContext());
                n.calibrate();
                Toast.makeText(getApplicationContext(), getString(R.string.calibrationInProgress), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void switchButtons() {
        if (!noiseChecking) {
            mBtnStop.setEnabled(false);
            mBtnStart.setEnabled(true);
        } else {
            mBtnStop.setEnabled(true);
            mBtnStart.setEnabled(false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
