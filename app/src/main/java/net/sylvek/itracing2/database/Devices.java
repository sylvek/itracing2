package net.sylvek.itracing2.database;

import android.content.ContentValues;
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

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE = "create table devices ( _id integer primary key, name text not null, address text not null, enabled boolean not null default 0);";
    public static final String SELECT_DEVICES = "select * from devices";
    public static final String TABLE = "devices";
    public static final String ENABLED = "enabled";

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
        return Devices.getDevicesHelperInstance(context).getWritableDatabase().query(true, Devices.TABLE, new String[]{Devices.ADDRESS, Devices.NAME}, null, null, null, null, null, null);
    }

    public static void removeDevice(Context context, String address)
    {
        Devices.getDevicesHelperInstance(context).getWritableDatabase().delete(Devices.TABLE, "address = ?", new String[]{address});
    }

    public static void updateDevice(Context context, String address, String name)
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Devices.NAME, name);
        Devices.getDevicesHelperInstance(context).getWritableDatabase().update(Devices.TABLE, contentValues, "address = ?", new String[]{address});
    }

    public static boolean isEnabled(Context context, String address)
    {
        final Cursor c = Devices.getDevicesHelperInstance(context).getWritableDatabase().query(true, Devices.TABLE, new String[]{Devices.ENABLED}, ADDRESS + " = ?", new String[]{address}, null, null, null, null);
        return c != null && c.moveToFirst() && c.getInt(0) == 1;
    }

    public static void setEnabled(Context context, String address, boolean enabled)
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Devices.ENABLED, (enabled) ? 1 : 0);
        Devices.getDevicesHelperInstance(context).getWritableDatabase().update(Devices.TABLE, contentValues, "address = ?", new String[]{address});
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
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
        {
            if (oldVersion == 1 && newVersion == 2) {
                sqLiteDatabase.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + ENABLED + " boolean not null default 0");
            }
        }
    }
}
