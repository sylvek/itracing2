package net.sylvek.itracing2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sylvek on 04/03/2016.
 */
public class Database {
    public static final String DATABASE_NAME = "itracing2DB";
    public static final int DATABASE_VERSION = 3;

    private static final String EVENTS_CREATE = "create table events ( _id integer primary key, address text not null, event text not null, option text not null, created datetime default CURRENT_TIMESTAMP);";
    private static final String DEVICES_CREATE = "create table devices ( _id integer primary key, name text not null, address text not null, enabled boolean not null default 0);";

    public static final String SELECT_EVENTS = "select * from events where address = ? order by created desc";
    public static final String SELECT_DEVICES = "select * from devices";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper geDatabaseHelperInstance(Context context)
    {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context)
        {
            super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL(DEVICES_CREATE);
            sqLiteDatabase.execSQL(EVENTS_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
        {
            if (oldVersion == 1 && newVersion == 2) {
                sqLiteDatabase.execSQL("ALTER TABLE devices ADD COLUMN enabled boolean not null default 0");
            }

            if (oldVersion == 2 && newVersion == 3) {
                sqLiteDatabase.execSQL(EVENTS_CREATE);
            }
        }
    }
}
