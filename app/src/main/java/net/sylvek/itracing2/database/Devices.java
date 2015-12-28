package net.sylvek.itracing2.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sylvek on 28/12/2015.
 */
public class Devices {

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";

    private static final String DATABASE_NAME = "itracing2DB";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table devices ( _id integer primary key, name text not null, address text not null);";
    public static final String SELECT_DEVICES = "select * from devices";
    public static final String TABLE = "devices";

    private static DevicesHelper instance;

    public static synchronized DevicesHelper getDevicesHelperInstance(Context context)
    {
        if (instance == null) {
            instance = new DevicesHelper(context);
        }
        return instance;
    }

    public static boolean containsDevice(Context context, String address)
    {
        final Cursor query = getDevicesHelperInstance(context).getWritableDatabase().query(true, TABLE, new String[]{ADDRESS}, ADDRESS + " = ?", new String[]{address}, null, null, null, null);
        return query != null && query.getCount() > 0;
    }

    public static Cursor findDevices(Context context)
    {
        return Devices.getDevicesHelperInstance(context).getWritableDatabase().query(true, Devices.TABLE, new String[]{Devices.ADDRESS}, null, null, null, null, null, null);
    }

    public static void removeDevice(Context context, String address)
    {
        Devices.getDevicesHelperInstance(context).getWritableDatabase().delete(Devices.TABLE, "address = ?", new String[]{address});
    }

    public static class DevicesHelper extends SQLiteOpenHelper {

        private DevicesHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {
        }
    }
}
