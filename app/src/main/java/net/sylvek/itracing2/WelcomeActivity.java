package net.sylvek.itracing2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements FirstTimeFragment.OnFirstTimeListener, DashboardFragment.OnDashboardListener {

    private final static int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private static final List<String> DEFAULT_DEVICE_NAME = new ArrayList<String>();

    static {
        DEFAULT_DEVICE_NAME.add("Quintic PROXR");
        DEFAULT_DEVICE_NAME.add("Cigii IT-02 Smart Bluetooth Tracker");
        DEFAULT_DEVICE_NAME.add("MLE-15");
        DEFAULT_DEVICE_NAME.add("iTAG");
    }

    private final FirstTimeFragment firstTimeFragment = FirstTimeFragment.instance();
    private final DashboardFragment dashboardFragment = DashboardFragment.instance();

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private BluetoothLEService service;
    private Runnable stopScan;

    private final Map<String, String> devices = new HashMap<String, String>();

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            final String uuid = device.getAddress();
            final String name = device.getName();

            devices.put((name == null) ? uuid : name, uuid);

            if (DEFAULT_DEVICE_NAME.contains(name)) {
                selectDevice(uuid);
            }
        }
    };

    private void selectDevice(String uuid)
    {
        devices.clear();
        Preferences.setKeyringUUID(WelcomeActivity.this, uuid);
        setRefreshing(false);
        showDashboard();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            if (iBinder instanceof BluetoothLEService.BackgroundBluetoothLEBinder) {
                service = ((BluetoothLEService.BackgroundBluetoothLEBinder) iBinder).service();
                service.connect();
                setRefreshing(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            service.disconnect();
            setRefreshing(false);
        }
    };

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().setDisplayShowHomeEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        // detect Bluetooth LE support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();

        // retrieve beacon uuid
        final String uuid = Preferences.getKeyringUUID(this);
        if (uuid == null) {
            showFirstTime();
        } else {
            showDashboard();
        }

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
                            dashboardFragment.setPercent(intent.getStringExtra(BluetoothLEService.BATTERY_LEVEL));
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
                            dashboardFragment.setRssi(intent.getStringExtra(BluetoothLEService.RSSI_RECEIVED));
                        }
                    });
                }
            }
        };
    }

    private void showFirstTime()
    {
        getFragmentManager().beginTransaction().replace(R.id.container, firstTimeFragment).commit();
    }

    private void showDashboard()
    {
        getFragmentManager().beginTransaction().replace(R.id.container, dashboardFragment).commit();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // detect Bluetooth enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onScanStart()
    {
        stopScan = new Runnable() {
            @Override
            public void run()
            {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                setRefreshing(false);
                Toast.makeText(WelcomeActivity.this, R.string.beacon_not_found, Toast.LENGTH_LONG).show();

                if (!devices.isEmpty()) {
                    displayListScannedDevices();
                }
            }
        };
        mHandler.postDelayed(stopScan, SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        setRefreshing(true);
    }

    @Override
    public void onFirstTimeStarted()
    {
    }

    @Override
    public void onFirstTimeStopped()
    {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mHandler.removeCallbacks(stopScan);
    }

    @Override
    public void onImmediateAlert(final boolean activate)
    {
        service.immediateAlert((activate) ? BluetoothLEService.HIGH_ALERT : BluetoothLEService.NO_ALERT);
    }

    @Override
    public void onLinkLoss(boolean checked)
    {
        final Intent service = new Intent(this, BluetoothLEService.class);
        if (checked) {
            startService(service);
        } else {
            stopService(service);
        }
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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                service.connect();
            }
        });
    }

    @Override
    public void onDashboardStopped()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unbindService(serviceConnection);
    }

    @Override
    public void onDonate()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/SylvainMaucourt"));
        startActivity(browserIntent);
    }

    @Override
    public void onFeedBack()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sylvek/itracing2/issues"));
        startActivity(browserIntent);
    }

    @Override
    public void onRingStone()
    {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ring_tone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(Preferences.getRingtone(this)));
        startActivityForResult(intent, 5);
    }

    private void setRefreshing(final boolean refreshing)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run()
            {
                mSwipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                Preferences.setRingtone(this, uri.toString());
            }
        }
    }

    private void displayListScannedDevices()
    {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(R.string.select_device);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(devices.keySet());
        builderSingle.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final String device = arrayAdapter.getItem(which);
                        selectDevice(devices.get(device));
                    }
                });
        builderSingle.show();
    }
}
