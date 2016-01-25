package cz.uhk.fim.ringtonechanger;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private Button mBtnNoise;
    private Button mBtnRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnNoise = (Button) findViewById(R.id.btn_noise);
        mBtnRingtone = (Button) findViewById(R.id.btn_ringtone);




        mBtnNoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoiseCheckingActivity.class);
                startActivity(intent);
            }
        });

        mBtnRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RingtoneChangingActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
}
