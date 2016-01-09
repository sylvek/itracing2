package net.sylvek.itracing2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by sylvek on 07/01/2016.
 */
public class PreferencesActivity extends CommonActivity implements PreferencesFragment.OnPreferencesListener {

    private BluetoothLEService service;
    private PreferencesFragment preferencesFragment;

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
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        showPreferences();
    }

    private void showPreferences()
    {
        this.preferencesFragment = PreferencesFragment.instance();
        getFragmentManager().beginTransaction().replace(R.id.container, preferencesFragment).commit();
    }

    @Override
    public void onPreferencesStarted()
    {
        bindService(new Intent(this, BluetoothLEService.class), serviceConnection, BIND_AUTO_CREATE);
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
