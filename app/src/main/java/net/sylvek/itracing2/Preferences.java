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

    public static final String ACTION_SIMPLE_BUTTON_LIST = "action_simple_button_list";
    public static final String ACTION_DOUBLE_BUTTON_LIST = "action_double_button_list";
    public static final String ACTION_OUT_OF_BAND_LIST = "action_out_of_band_list";
    public static final String ACTION_ON_POWER_OFF = "action_on_power_off";
    public static final String RINGTONE = "ring_tone";
    public static final String FOREGROUND = "action_foreground";
    private static final String DOUBLE_BUTTON_DELAY = "double_button_delay";
    private static final String CUSTOM_ACTION = "custom_action";
    private static final String DONATED = "donated";

    private static SharedPreferences getSharedPreferences(Context context, String address)
    {
        return context.getSharedPreferences(address, Context.MODE_PRIVATE);
    }

    public static String getActionSimpleButton(Context context, String address)
    {
        return getSharedPreferences(context, address).getString(ACTION_SIMPLE_BUTTON_LIST, null);
    }

    public static String getActionDoubleButton(Context context, String address)
    {
        return getSharedPreferences(context, address).getString(ACTION_DOUBLE_BUTTON_LIST, null);
    }

    public static String getActionOutOfBand(Context context, String address)
    {
        return getSharedPreferences(context, address).getString(ACTION_OUT_OF_BAND_LIST, null);
    }

    public static boolean isActionOnPowerOff(Context context, String address)
    {
        return getSharedPreferences(context, address).getBoolean(ACTION_ON_POWER_OFF, false);
    }

    public static String getRingtone(Context context, String address)
    {
        final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        return getSharedPreferences(context, address).getString(RINGTONE, sound.toString());
    }

    public static void setRingtone(Context context, String address, String uri)
    {
        getSharedPreferences(context, address).edit().putString(RINGTONE, uri).commit();
    }

    public static long getDoubleButtonDelay(Context context, String address)
    {
        final String defaultDoubleButtonDelay = context.getString(R.string.default_double_button_delay);
        return Long.valueOf(getSharedPreferences(context, address).getString(DOUBLE_BUTTON_DELAY, defaultDoubleButtonDelay));
    }

    public static String getCustomAction(Context context, String address)
    {
        return getSharedPreferences(context, address).getString(CUSTOM_ACTION, "");
    }

    public static boolean clearAll(Context context, String address)
    {
        return getSharedPreferences(context, address).edit().clear().commit();
    }

    public static boolean isForegroundEnabled(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FOREGROUND, false);
    }

    public static boolean isDonated(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DONATED, false);
    }

    public static void setDonated(Context context, boolean donated)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(DONATED, donated).commit();
    }
}
