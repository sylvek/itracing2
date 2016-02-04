package net.sylvek.itracing2.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import net.sylvek.itracing2.Preferences;
import net.sylvek.itracing2.R;
import net.sylvek.itracing2.database.Devices;

/**
 * Created by sylvek on 01/02/2016.
 */
public class DevicePreferencesFragment extends PreferenceFragment {

    private OnDevicePreferencesListener presenter;

    public static DevicePreferencesFragment instance(final String address)
    {
        final DevicePreferencesFragment devicePreferencesFragment = new DevicePreferencesFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Devices.ADDRESS, address);
        devicePreferencesFragment.setArguments(arguments);
        devicePreferencesFragment.setRetainInstance(true);
        return devicePreferencesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        final String address = getArguments().getString(Devices.ADDRESS);
        this.getPreferenceManager().setSharedPreferencesName(address);
        this.addPreferencesFromResource(R.xml.device_preferences);
        findPreference(Preferences.RINGTONE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                presenter.onRingStone();
                return true;
            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnDevicePreferencesListener) {
            this.presenter = (OnDevicePreferencesListener) activity;
        } else {
            throw new ClassCastException("must implement OnDevicePreferencesListener");
        }
    }


    public interface OnDevicePreferencesListener {

        void onRingStone();
    }
}
