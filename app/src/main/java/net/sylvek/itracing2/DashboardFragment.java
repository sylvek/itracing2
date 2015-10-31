package net.sylvek.itracing2;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by sylvek on 18/05/2015.
 */
public class DashboardFragment extends PreferenceFragment {

    private OnDashboardListener presenter;

    public static DashboardFragment instance()
    {
        return new DashboardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        this.addPreferencesFromResource(R.xml.preferences);
        findPreference(Preferences.LINK_OPTION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                if (newValue instanceof Boolean) {
                    presenter.onLinkLoss((Boolean) newValue);
                }
                return true;
            }
        });
        findPreference(Preferences.ACTION_BUTTON).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final boolean activate = preference.getTitle().equals(R.string.start_immediate_alert);
                preference.setTitle((activate) ? R.string.stop_immediate_alert : R.string.start_immediate_alert);
                presenter.onImmediateAlert(activate);
                return true;
            }
        });
        findPreference(Preferences.DONATE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                presenter.onDonate();
                return true;
            }
        });
        findPreference(Preferences.FEEDBACK).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                presenter.onFeedBack();
                return true;
            }
        });
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.fragment_dashboard, menu);
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
        findPreference(Preferences.ACTION_BUTTON).setEnabled(enabled);
    }

    public void setPercent(final String percent)
    {
        findPreference(Preferences.BATTERY_INFO).setSummary(percent);
    }

    public void setRssi(String rssi)
    {
        findPreference(Preferences.RSSI_INFO).setSummary(rssi);
    }

    public interface OnDashboardListener {

        void onImmediateAlert(boolean activate);

        void onLinkLoss(boolean checked);

        void onDashboardStarted();

        void onDashboardStopped();

        void onDonate();

        void onFeedBack();

        void onRingStone();
    }
}
