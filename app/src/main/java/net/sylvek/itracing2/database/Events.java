package net.sylvek.itracing2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        Database.geDatabaseHelperInstance(context).getWritableDatabase().insert(Events.TABLE, null, event);
    }

    public static void deleteAll(Context context, String address)
    {
        Database.geDatabaseHelperInstance(context).getWritableDatabase().delete(Events.TABLE, "address = ?", new String[]{address});
    }
}
