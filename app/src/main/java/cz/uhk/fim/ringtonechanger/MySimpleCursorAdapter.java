package cz.uhk.fim.ringtonechanger;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Petr on 16. 1. 2016.
 */
public class MySimpleCursorAdapter extends SimpleCursorAdapter {

    private Context context;
    private int layout;

    public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int num) {
        super(context, layout, c, from, to, num);
        this.context = context;
        this.layout = layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Cursor c = getCursor();
        c.moveToPosition(position);
        int colActive = c.getColumnIndex(AreasTable.COLUMN_NAME_ACTIVE);
        int active = c.getInt(colActive);

        if (active == 1) {
            v.setBackgroundColor(Color.CYAN);
        }

        return v;
    }
}
