package net.sylvek.itracing2;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by sylvek on 18/05/2015.
 */
public class Preferences {

    public static final String KEYRING_UUID = "keyring_uuid";
    public static final String LINK_OPTION = "link_background";
    public static final String ACTION_SIMPLE_BUTTON_LIST = "action_simple_button_list";
    public static final String ACTION_DOUBLE_BUTTON_LIST = "action_double_button_list";
    public static final String ACTION_OUT_OF_BAND_LIST = "action_out_of_band_list";
    public static final String ACTION_ON_POWER_OFF = "action_on_power_off";
    public static final String ACTION_BUTTON = "action_button";
    public static final String BATTERY_INFO = "battery_info";
    public static final String RSSI_INFO = "rssi_info";
    public static final String DONATE = "donate";
    public static final String FEEDBACK = "feedback";
    public static final String RINGTONE = "ring_tone";
    private static final String DOUBLE_BUTTON_DELAY = "double_button_delay";
    private static final String CUSTOM_ACTION = "custom_action";

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

    public static String getActionSimpleButton(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(ACTION_SIMPLE_BUTTON_LIST, null);
    }

    public static String getActionDoubleButton(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(ACTION_DOUBLE_BUTTON_LIST, null);
    }

    public static String getActionOutOfBand(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(ACTION_OUT_OF_BAND_LIST, null);
    }

    public static boolean isActionOnPowerOff(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getBoolean(ACTION_ON_POWER_OFF, false);
    }

    public static String getRingtone(Context context)
    {
        final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(RINGTONE, sound.toString());
    }

    public static void setRingtone(Context context, String uri)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        defaultSharedPreferences.edit().putString(RINGTONE, uri).commit();
    }

    public static long getDoubleButtonDelay(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final String defaultDoubleButtonDelay = context.getString(R.string.default_double_button_delay);
        return Long.valueOf(defaultSharedPreferences.getString(DOUBLE_BUTTON_DELAY, defaultDoubleButtonDelay));
    }

    public static String getCustomAction(Context context)
    {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return defaultSharedPreferences.getString(CUSTOM_ACTION, "");
    }
}
