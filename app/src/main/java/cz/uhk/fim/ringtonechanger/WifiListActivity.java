package cz.uhk.fim.ringtonechanger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 15. 1. 2016.
 */
public class WifiListActivity extends Activity {

    ListView mWifiList;
    List<WifiConfiguration> mWifiConfigurations;
    List<String> mWifiNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        mWifiList= (ListView) findViewById(R.id.listViewWifi);
        mWifiConfigurations = new ArrayList<WifiConfiguration>();
        mWifiNames = new ArrayList<String>();


        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiConfigurations = wifiManager.getConfiguredNetworks();

        for (int i = 0; i < mWifiConfigurations.size(); i++) {
            mWifiNames.add(mWifiConfigurations.get(i).SSID);
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.list_item_1, android.R.id.text1, mWifiNames);

        mWifiList.setAdapter(arrayAdapter);

        mWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedWifi = mWifiNames.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AreasTable.COLUMN_NAME_WIFI, selectedWifi);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });


    }
}
