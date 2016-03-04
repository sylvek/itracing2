package net.sylvek.itracing2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sylvek on 28/12/2015.
 */
public class Devices extends Database {

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";

    public static final String TABLE = "devices";
    public static final String ENABLED = "enabled";
    public static final String SOURCE = "source";

    public static boolean containsDevice(Context context, String address)
    {
        final Cursor query = Database.geDatabaseHelperInstance(context).getWritableDatabase().query(true, TABLE, new String[]{ADDRESS}, ADDRESS + " = ?", new String[]{address}, null, null, null, null);
        return query != null && query.getCount() > 0;
    }

    public static Cursor findDevices(Context context)
    {
        return Database.geDatabaseHelperInstance(context).getWritableDatabase().query(true, Devices.TABLE, new String[]{Devices.ADDRESS, Devices.NAME}, null, null, null, null, null, null);
    }

    public static void removeDevice(Context context, String address)
    {
        Database.geDatabaseHelperInstance(context).getWritableDatabase().delete(Devices.TABLE, "address = ?", new String[]{address});
    }

    public static void updateDevice(Context context, String address, String name)
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Devices.NAME, name);
        Database.geDatabaseHelperInstance(context).getWritableDatabase().update(Devices.TABLE, contentValues, "address = ?", new String[]{address});
    }

    public static boolean isEnabled(Context context, String address)
    {
        final Cursor c = Devices.geDatabaseHelperInstance(context).getWritableDatabase().query(true, Devices.TABLE, new String[]{Devices.ENABLED}, ADDRESS + " = ?", new String[]{address}, null, null, null, null);
        return c != null && c.moveToFirst() && c.getInt(0) == 1;
    }

    public static void setEnabled(Context context, String address, boolean enabled)
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Devices.ENABLED, (enabled) ? 1 : 0);
        Database.geDatabaseHelperInstance(context).getWritableDatabase().update(Devices.TABLE, contentValues, "address = ?", new String[]{address});
    }

    public static void insert(Context context, String name, String address)
    {
        final ContentValues device = new ContentValues();
        device.put(Devices.NAME, name);
        device.put(Devices.ADDRESS, address);
        Database.geDatabaseHelperInstance(context).getWritableDatabase().insert(Devices.TABLE, null, device);
    }
}
