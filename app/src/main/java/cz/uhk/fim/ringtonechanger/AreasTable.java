package cz.uhk.fim.ringtonechanger;

import android.provider.BaseColumns;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class AreasTable implements BaseColumns {
    public static final String TABLE_NAME = "areas";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_LONGITUDE = "longitude";
    public static final String COLUMN_NAME_LATITUDE = "latitude";
    public static final String COLUMN_NAME_RADIUS = "radius";
    public static final String COLUMN_NAME_ACTIVE = "active";
    public static final String COLUMN_NAME_RINGTONE = "ringtone";
    public static final String COLUMN_NAME_WIFI = "wifi";
}
