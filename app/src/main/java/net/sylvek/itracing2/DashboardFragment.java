package net.sylvek.itracing2;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import net.sylvek.itracing2.database.Devices;

/**
 * Created by sylvek on 18/05/2015.
 */
public class DashboardFragment extends PreferenceFragment {

    private OnDashboardListener presenter;

    private boolean activated;

    public static DashboardFragment instance(final String address)
    {
        final DashboardFragment dashboardFragment = new DashboardFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Devices.ADDRESS, address);
        dashboardFragment.setArguments(arguments);
        return dashboardFragment;
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
        findPreference(Preferences.CIRCLE_PERCENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                activated = (activated) ? false : true;
                presenter.onImmediateAlert(address, activated);
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.dashboard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.delete) {
            this.presenter.onRemove();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnDashboardListener) {
            this.presenter = (OnDashboardListener) activity;
        } else {
            throw new ClassCastException("must implement OnDashboardListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.presenter.onDashboardStarted();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.presenter.onDashboardStopped();
    }

    public void setImmediateAlertEnabled(final boolean enabled)
    {
        findPreference(Preferences.CIRCLE_PERCENT).setEnabled(enabled);
    }

    public void setPercent(final int percent)
    {
        ((CirclePercentPreference) findPreference(Preferences.CIRCLE_PERCENT)).setBatteryPercent(Float.valueOf(percent));
    }

    public void setRssi(int rssi)
    {
        ((CirclePercentPreference) findPreference(Preferences.CIRCLE_PERCENT)).setRssiValue(Float.valueOf(rssi));
    }

    public interface OnDashboardListener {

        void onImmediateAlert(String address, boolean activated);

        void onDashboardStarted();

        void onDashboardStopped();

        void onRingStone();

        void onRemove();
    }
}
