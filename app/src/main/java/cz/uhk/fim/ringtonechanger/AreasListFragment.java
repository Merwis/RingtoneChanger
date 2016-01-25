package cz.uhk.fim.ringtonechanger;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class AreasListFragment extends ListFragment {

    AreasDatabaseHelper mHelper;
    SimpleCursorAdapter mAdapter;
    ListView mList;
    SQLiteDatabase mDatabase;
    boolean wifiServiceStarted = false;
    OnAreaActivatedListener mListener;
    private WifiManager wifiManager;
    //OnAreasTableelectedListener mCallback;

    SharedPreferences mSharedPreferences;

    private static final String TAG = "AreasKIstFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        mHelper = new AreasDatabaseHelper(getActivity());
        mDatabase = mHelper.getWritableDatabase();

        String[] projection = new String[] {
                AreasTable._ID, AreasTable.COLUMN_NAME_NAME, AreasTable.COLUMN_NAME_RADIUS, AreasTable.COLUMN_NAME_ACTIVE,
                AreasTable.COLUMN_NAME_WIFI};

        Cursor c = mDatabase.query(AreasTable.TABLE_NAME, projection, null, null, null, null, null);

        mAdapter = new MySimpleCursorAdapter(getActivity(),
                R.layout.list_item_1,
                c,
                new String[] {AreasTable.COLUMN_NAME_NAME, AreasTable.COLUMN_NAME_RADIUS},
                new int[] {android.R.id.text1, R.id.mujtext},
                SimpleCursorAdapter.NO_SELECTION);
        mAdapter.setViewBinder(new MyViewBinder());
        setListAdapter(mAdapter);

        c = mDatabase.query(AreasTable.TABLE_NAME, projection, AreasTable.COLUMN_NAME_ACTIVE + "=" + 1, null, null, null, null );

        if (c.getCount() > 0 && !wifiServiceStarted) {
            Intent intent = new Intent(getActivity(), WifiCheckingService.class);
            getActivity().startService(intent);
            wifiServiceStarted = true;
            Toast.makeText(getActivity(), getString(R.string.startingWifiChechking), Toast.LENGTH_SHORT).show();
        }

    }

    private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            String wifi = cursor.getString(cursor.getColumnIndex(AreasTable.COLUMN_NAME_WIFI));

            if (columnIndex == cursor.getColumnIndex(AreasTable.COLUMN_NAME_RADIUS)) {
                TextView textView = (TextView) view;
                if (wifi.equals("")) {
                    String radius = cursor.getString(columnIndex);
                    textView.setText("radius: " + radius + " km");
                } else {
                    textView.setText("Wifi: " + wifi);
                }
                return true;
            }
            return false;
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_AreasTable_list, container, false);
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mList = (ListView) v.findViewById(android.R.id.list);
        mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.context_menu_areas_list_fragment, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.delete_area:
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            if (mList.isItemChecked(i)) {
                                Long id = mAdapter.getItemId(i);
                                mDatabase.delete(AreasTable.TABLE_NAME,
                                        AreasTable._ID + " LIKE ?", new String[] {id+""});
                            }
                        }

                        Cursor c = mDatabase.query(AreasTable.TABLE_NAME,
                                null, null, null, null, null, null);
                        Cursor oldCursor = mAdapter.swapCursor(c);
                        if (oldCursor != null) {
                            oldCursor.close();
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });


        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        boolean wifiOn = wifiManager.isWifiEnabled();

        String[] select = new String[] {AreasTable._ID, AreasTable.COLUMN_NAME_ACTIVE,
                AreasTable.COLUMN_NAME_WIFI, AreasTable.COLUMN_NAME_RINGTONE, AreasTable.COLUMN_NAME_NAME,
                AreasTable.COLUMN_NAME_LONGITUDE, AreasTable.COLUMN_NAME_RADIUS, AreasTable.COLUMN_NAME_LATITUDE};

        Cursor c = mDatabase.query(AreasTable.TABLE_NAME, select, AreasTable._ID + "=" + id, null, null, null, null);
        Log.d("pred", "pred" + id);
        int col = c.getColumnIndex(AreasTable.COLUMN_NAME_ACTIVE);
        int colLongitude = c.getColumnIndex(AreasTable.COLUMN_NAME_LONGITUDE);
        int colLatitude = c.getColumnIndex(AreasTable.COLUMN_NAME_LATITUDE);
        int colRadius = c.getColumnIndex(AreasTable.COLUMN_NAME_RADIUS);
        int colName = c.getColumnIndex(AreasTable.COLUMN_NAME_NAME);
        int colWifi = c.getColumnIndex(AreasTable.COLUMN_NAME_WIFI);
        c.moveToFirst();
        Log.d("pred", "pred" + c.getCount() + " " + c.getColumnCount());

        String wifiIn = c.getString(colWifi);
        Long active = c.getLong(col);
        String longitude = c.getString(colLongitude);
        Log.d(TAG, "longituda:" + longitude + "");
        Area area = null;
        boolean gpsOn = mSharedPreferences.getBoolean(Strings.GPS_ACTIVE_ADDED_KEY, true);
        if (!longitude.equals("") && gpsOn ) {
            String latitude = c.getString(colLatitude);
            float radius = c.getFloat(colRadius);
            String name = c.getString(colName);
            area = new Area((int) id, name, longitude, latitude, radius);
        } else if (!longitude.equals("") && !gpsOn) {
            Toast.makeText(getActivity(), getString(R.string.gpsTrackingDisabled), Toast.LENGTH_SHORT).show();
        }

        ContentValues cv = new ContentValues();
        Intent intent = new Intent(getActivity(), WifiCheckingService.class);

        if (active == 0) {
            cv.put(AreasTable.COLUMN_NAME_ACTIVE, 1);
                if (area != null) {
                    mListener.onAreaActivated(area);
                    gpsOn = true;
                }
            Log.d("update", "1");
            v.setBackgroundColor(Color.CYAN);
        } else {
            cv.put(AreasTable.COLUMN_NAME_ACTIVE, 0);
            //getActivity().stopService(intent);
            if (area != null) {
                mListener.onAreaDeactivated(area);
                gpsOn = false;
            }
            Log.d("update", "0");
            v.setBackgroundColor(Color.TRANSPARENT);
        }

        mDatabase.update(AreasTable.TABLE_NAME, cv, AreasTable._ID + " = " + id, null);

        c = mDatabase.query(AreasTable.TABLE_NAME, select, AreasTable.COLUMN_NAME_ACTIVE + "=" + 1, null, null, null, null );

        if (c.getCount() > 0 && !wifiServiceStarted && !gpsOn && !wifiIn.equals("")) {
            if (wifiOn) {
                getActivity().startService(intent);
                wifiServiceStarted = true;
                Toast.makeText(getActivity(), getString(R.string.startingWifiChechking), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.wifiDisabled), Toast.LENGTH_SHORT).show();
            }
        } else if (c.getCount() < 1 && wifiServiceStarted) {
            getActivity().stopService(intent);
            wifiServiceStarted = false;
            Toast.makeText(getActivity(), getString(R.string.stoppingWifiChechking), Toast.LENGTH_SHORT).show();
        }


        c.close();
        //((MainActivity)getActivity()).setGeofenceChange(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_area:
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public interface OnAreaActivatedListener {
        public void onAreaActivated(Area area);
        public void onAreaDeactivated(Area area);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnAreaActivatedListener) activity;
        } catch (ClassCastException e) {

        }
    }
}
