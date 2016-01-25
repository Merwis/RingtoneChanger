package cz.uhk.fim.ringtonechanger;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class AreasDatabaseHelper extends SQLiteOpenHelper {

    public static final String TAG = AreasDatabaseHelper.class.getCanonicalName();
    public static final String DATABASE_NAME = "areasdatabase.db";
    public static final int VERSION = 1;

    public static final String SQL_CREATE_AREAS_TABLE =
            "CREATE TABLE " + AreasTable.TABLE_NAME + " (" +
                    AreasTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AreasTable.COLUMN_NAME_NAME + " TEXT, " +
                    AreasTable.COLUMN_NAME_LONGITUDE + " TEXT, " +
                    AreasTable.COLUMN_NAME_LATITUDE + " TEXT, " +
                    AreasTable.COLUMN_NAME_RADIUS + " INTEGER, " +
                    AreasTable.COLUMN_NAME_ACTIVE + " INTEGER, " +
                    AreasTable.COLUMN_NAME_RINGTONE + " TEXT, " +
                    AreasTable.COLUMN_NAME_WIFI + " TEXT)";

    public static final String SQL_DROP_AREAS_TABLE =
            "DROP TABLE IF EXISTS " + AreasTable.TABLE_NAME;

    public AreasDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating db");
        db.execSQL(SQL_CREATE_AREAS_TABLE);
        fillDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Deleting db");
        db.execSQL(SQL_DROP_AREAS_TABLE);
        Log.d(TAG, "Creating db");
        db.execSQL(SQL_CREATE_AREAS_TABLE);
        fillDatabase(db);
    }

    public void fillDatabase(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AreasTable.COLUMN_NAME_NAME, "Praha - demo");
        contentValues.put(AreasTable.COLUMN_NAME_LONGITUDE, "14.438338838517666");
        contentValues.put(AreasTable.COLUMN_NAME_LATITUDE, "50.07663649442885");
        contentValues.put(AreasTable.COLUMN_NAME_RADIUS, 15);
        contentValues.put(AreasTable.COLUMN_NAME_ACTIVE, 0);
        contentValues.put(AreasTable.COLUMN_NAME_WIFI, "Prazska wifi");
        db.insert(AreasTable.TABLE_NAME, null, contentValues);

        contentValues.put(AreasTable.COLUMN_NAME_NAME, "Nove Mesto - demo");
        contentValues.put(AreasTable.COLUMN_NAME_LONGITUDE, "16.1516458");
        contentValues.put(AreasTable.COLUMN_NAME_LATITUDE, "50.3603444");
        contentValues.put(AreasTable.COLUMN_NAME_RADIUS, 1);
        contentValues.put(AreasTable.COLUMN_NAME_ACTIVE, 0);
        contentValues.put(AreasTable.COLUMN_NAME_WIFI, "Domaci wifi");
        db.insert(AreasTable.TABLE_NAME, null, contentValues);

        //db.close();
        //zavreno v AreasListFragment
    }
}
