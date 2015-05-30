package net.sylvek.itracing2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by sylvek on 18/05/2015.
 */
public class Preferences {

    public static final String KEYRING_UUID = "keyring_uuid";
    public static final String LINK_OPTION = "link_background";
    public static final String ACTION_BUTTON_LIST = "action_button_list";
    public static final String ACTION_BUTTON = "action_button";
    public static final String BATTERY_INFO = "battery_info";
    public static final String DONATE = "donate";
    public static final String FEEDBACK = "feedback";

    public static String getKeyringUUID(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(KEYRING_UUID, null);
    }

    public static void setKeyringUUID(Context context, String uuid)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        defaultSharedPreferences.edit().putString(KEYRING_UUID, uuid).commit();
    }

    public static boolean getLinkBackgroundEnabled(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getBoolean(LINK_OPTION, false);
    }

    public static String getActionButton(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(ACTION_BUTTON_LIST, null);
    }
}
