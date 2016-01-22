package net.sylvek.itracing2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import net.sylvek.itracing2.database.Devices;

/**
 * Created by sylvek on 28/12/2015.
 */
public class DashboardActivity extends CommonActivity implements DashboardFragment.OnDashboardListener, ConfirmAlertDialogFragment.OnConfirmAlertDialogListener {

    public static final int REQUEST_CODE_RING_STONE = 5;

    private BluetoothLEService service;
    private BroadcastReceiver receiver;

    private DashboardFragment dashboardFragment;

    private String address;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            if (iBinder instanceof BluetoothLEService.BackgroundBluetoothLEBinder) {
                service = ((BluetoothLEService.BackgroundBluetoothLEBinder) iBinder).service();
                service.connect(DashboardActivity.this.address);
                setRefreshing(true);
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

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent)
            {
                if (BluetoothLEService.IMMEDIATE_ALERT_AVAILABLE.equals(intent.getAction())) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            dashboardFragment.setImmediateAlertEnabled(true);
                        }
                    });
                }

                if (BluetoothLEService.BATTERY_LEVEL.equals(intent.getAction())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            dashboardFragment.setPercent(intent.getIntExtra(BluetoothLEService.BATTERY_LEVEL, 0));
                        }
                    });
                }

                if (BluetoothLEService.SERVICES_DISCOVERED.equals(intent.getAction())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            setRefreshing(false);
                        }
                    });
                }

                if (BluetoothLEService.RSSI_RECEIVED.equals(intent.getAction())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            dashboardFragment.setRssi(intent.getIntExtra(BluetoothLEService.RSSI_RECEIVED, 0));
                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        address = getIntent().getStringExtra(Devices.ADDRESS);
        setTitle(address);
        showDashboard();
    }

    private void showDashboard()
    {
        this.dashboardFragment = DashboardFragment.instance(address);
        getFragmentManager().beginTransaction().replace(R.id.container, dashboardFragment).commit();
    }

    @Override
    public void onImmediateAlert(final String address, final boolean activate)
    {
        service.immediateAlert(address, (activate) ? BluetoothLEService.HIGH_ALERT : BluetoothLEService.NO_ALERT);
    }

    @Override
    public void onDashboardStarted()
    {
        // register events
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BluetoothLEService.IMMEDIATE_ALERT_AVAILABLE));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BluetoothLEService.BATTERY_LEVEL));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BluetoothLEService.SERVICES_DISCOVERED));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BluetoothLEService.RSSI_RECEIVED));

        // bind service
        bindService(new Intent(this, BluetoothLEService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDashboardStopped()
    {
        if (this.service != null) {
            this.service.disconnect(this.address);
        }

        this.setRefreshing(false);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unbindService(serviceConnection);
    }

    @Override
    public void onRingStone()
    {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ring_tone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(Preferences.getRingtone(this, address)));
        startActivityForResult(intent, REQUEST_CODE_RING_STONE);
    }

    @Override
    public void onRemove()
    {
        ConfirmAlertDialogFragment.instance(R.string.confirm_remove_keyring).show(getFragmentManager(), "dialog");
    }

    @Override
    public void doPositiveClick()
    {
        if (Preferences.clearAll(this, address)) {
            this.setRefreshing(false);
            this.service.remove(address);
            Devices.removeDevice(this, address);
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public void doNegativeClick()
    {
        // nothing to do.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RING_STONE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Preferences.setRingtone(this, address, uri.toString());
            }
        }
    }
}
