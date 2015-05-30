package net.sylvek.itracing2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements FirstTimeFragment.OnFirstTimeListener, DashboardFragment.OnDashboardListener {

    private final static int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private static final String DEFAULT_DEVICE_NAME = "Quintic PROXR";

    private final FirstTimeFragment firstTimeFragment = FirstTimeFragment.instance();
    private final DashboardFragment dashboardFragment = DashboardFragment.instance();

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver receiver;
    private BluetoothLEService service;
    private Runnable stopScan;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            final String uuid = device.getAddress();
            final String name = device.getName();

            if (DEFAULT_DEVICE_NAME.equals(name)) {
                Preferences.setKeyringUUID(WelcomeActivity.this, uuid);
                setRefreshing(false);
                showDashboard();
            }
        }
    };

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
    public void onImmediateAlert()
    {
        service.immediateAlert();
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
}
