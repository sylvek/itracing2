package net.sylvek.itracing2.preferences;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import net.sylvek.itracing2.BluetoothLEService;
import net.sylvek.itracing2.CommonActivity;
import net.sylvek.itracing2.Preferences;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 07/01/2016.
 */
public class PreferencesActivity extends CommonActivity implements PreferencesFragment.OnPreferencesListener {

    public static final String TAG = "PREFERENCES_FRAGMENT_TAG";

    private BluetoothLEService service;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            if (iBinder instanceof BluetoothLEService.BackgroundBluetoothLEBinder) {
                service = ((BluetoothLEService.BackgroundBluetoothLEBinder) iBinder).service();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            Log.d(BluetoothLEService.TAG, "onServiceDisconnected()");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        showPreferences();
    }

    private void showPreferences()
    {
        getFragmentManager().beginTransaction().replace(R.id.container, PreferencesFragment.instance(), TAG).commit();
    }

    @Override
    public void onPreferencesStarted()
    {
        bindService(new Intent(this, BluetoothLEService.class), serviceConnection, BIND_AUTO_CREATE);

        if (Preferences.isSamsung()) {
            final PreferencesFragment preferencesFragment = (PreferencesFragment) getFragmentManager().findFragmentByTag(TAG);
            preferencesFragment.setForegroundBackground(true);
        }
    }

    @Override
    public void onPreferencesStopped()
    {
        unbindService(serviceConnection);
    }

    @Override
    public void onForegroundChecked(boolean checked)
    {
        this.service.setForegroundEnabled(checked);
    }

}
