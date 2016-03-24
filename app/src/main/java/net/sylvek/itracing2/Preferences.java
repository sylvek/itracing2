package net.sylvek.itracing2;

import java.util.Collections;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by sylvek on 18/05/2015.
 */
public class Preferences {

    private static final String SAMSUNG_DEVICE = "samsung";

    public enum Source {
        single_click, double_click, out_of_range, connected
    }

    public static final String ACTION_SIMPLE_BUTTON_LIST = "action_single_click_list";
    public static final String ACTION_DOUBLE_BUTTON_LIST = "action_double_click_list";
    public static final String ACTION_OUT_OF_RANGE_LIST = "action_out_of_range_list";
    public static final String ACTION_CONNECTED_LIST = "action_connected_list";
    public static final String ACTION_ON_POWER_OFF = "action_on_power_off";
    public static final String RINGTONE = "ring_tone";
    public static final String FOREGROUND = "action_foreground";
    private static final String DOUBLE_BUTTON_DELAY = "double_button_delay";
    private static final String CUSTOM_ACTION = "custom_action";
    private static final String DONATED = "donated";

    public static long getDoubleButtonDelay(Context context)
    {
        final String defaultDoubleButtonDelay = context.getString(R.string.default_double_button_delay);
        return Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(DOUBLE_BUTTON_DELAY, defaultDoubleButtonDelay));
    }

    private static SharedPreferences getSharedPreferences(Context context, String address)
    {
        return context.getSharedPreferences(address, Context.MODE_PRIVATE);
    }

    public static Set<String> getActionSimpleButton(Context context, String address)
    {
        return getSharedPreferences(context, address).getStringSet(ACTION_SIMPLE_BUTTON_LIST, Collections.<String>emptySet());
    }

    public static Set<String> getActionDoubleButton(Context context, String address)
    {
        return getSharedPreferences(context, address).getStringSet(ACTION_DOUBLE_BUTTON_LIST, Collections.<String>emptySet());
    }

    public static Set<String> getActionOutOfBand(Context context, String address)
    {
        return getSharedPreferences(context, address).getStringSet(ACTION_OUT_OF_RANGE_LIST, Collections.<String>emptySet());
    }

    public static Set<String> getActionConnected(Context context, String address)
    {
        return getSharedPreferences(context, address).getStringSet(ACTION_CONNECTED_LIST, Collections.<String>emptySet());
    }

    public static boolean isActionOnPowerOff(Context context, String address)
    {
        return getSharedPreferences(context, address).getBoolean(ACTION_ON_POWER_OFF, false);
    }

    public static String getRingtone(Context context, String address, String source)
    {
        final Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        return getSharedPreferences(context, address).getString(RINGTONE + "_" + source, sound.toString());
    }

    public static void setRingtone(Context context, String address, String source, String uri)
    {
        getSharedPreferences(context, address).edit().putString(RINGTONE + "_" + source, uri).commit();
    }

    public static String getCustomAction(Context context, String address, String source)
    {
        return getSharedPreferences(context, address).getString(CUSTOM_ACTION + "_" + source, "");
    }

    public static boolean clearAll(Context context, String address)
    {
        return getSharedPreferences(context, address).edit().clear().commit();
    }

    public static boolean isForegroundEnabled(Context context)
    {
        return isSamsung() || PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FOREGROUND, false);
    }

    public static boolean isSamsung()
    {
        final String manufacturer = android.os.Build.MANUFACTURER;
        return SAMSUNG_DEVICE.equals(manufacturer.toLowerCase());
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
