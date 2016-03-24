package net.sylvek.itracing2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by sylvek on 04/03/2016.
 */
public class Events extends Database {

    public static final String _ID = "_id";
    public static final String NAME = "event";
    public static final String OPTION = "option";
    public static final String CREATED = "created";
    public static final String ADDRESS = "address";

    private static final String TABLE = "events";

    public static void insert(Context context, String name, String address, String option)
    {
        final ContentValues event = new ContentValues();
        event.put(Events.NAME, name);
        event.put(Events.ADDRESS, address);
        event.put(Events.OPTION, option);
        Database.getDatabaseHelperInstance(context).getWritableDatabase().insert(Events.TABLE, null, event);
    }

    public static boolean removeEvents(Context context, String address)
    {
        return 0 < Database.getDatabaseHelperInstance(context).getWritableDatabase().delete(Events.TABLE, "address = ?", new String[]{address});
    }

    public static String export(Context context, String address)
    {
        final Cursor cursor = Database.getDatabaseHelperInstance(context).getWritableDatabase().query(Events.TABLE, new String[]{NAME, OPTION, CREATED}, "address = ?", new String[]{address}, null, null, null);
        final StringBuilder result = new StringBuilder();
        while (cursor.moveToNext()) {
            result.append(cursor.getString(0)).append(",").append(cursor.getString(1)).append(",").append(cursor.getString(2)).append("\n");
        }
        return result.toString();
    }
}
