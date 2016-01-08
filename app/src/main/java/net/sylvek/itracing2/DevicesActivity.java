package net.sylvek.itracing2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import net.sylvek.itracing2.database.Devices;

public class DevicesActivity extends CommonActivity implements DevicesFragment.OnDevicesListener, DeviceAlertDialogFragment.OnConfirmAlertDialogListener, ConfirmAlertDialogFragment.OnConfirmAlertDialogListener {

    public static final String TAG = DevicesActivity.class.toString();

    private BluetoothLEService service;

    private final static int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private static final List<String> DEFAULT_DEVICE_NAME = new ArrayList<>();

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

    private final Random random = new Random();

    static {
        DEFAULT_DEVICE_NAME.add("Quintic PROXR");
        DEFAULT_DEVICE_NAME.add("Cigii IT-02 Smart Bluetooth Tracker");
        DEFAULT_DEVICE_NAME.add("MLE-15");
        DEFAULT_DEVICE_NAME.add("iTAG");
    }

    private final DevicesFragment devicesFragment = DevicesFragment.instance();

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private Runnable stopScan;

    private final Map<String, String> devices = new HashMap<>();

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            final String address = device.getAddress();
            final String name = device.getName();

            Log.d(TAG, "device " + name + " with address " + address + " found");
            if (!Devices.containsDevice(DevicesActivity.this, address)) {
                devices.put((name == null) ? address : name, address);
            }
        }
    };

    private void selectDevice(String name, String address)
    {
        final ContentValues device = new ContentValues();
        device.put(Devices.NAME, name);
        device.put(Devices.ADDRESS, address);
        Devices.getDevicesHelperInstance(this).getWritableDatabase().insert(Devices.TABLE, null, device);
        devicesFragment.refresh();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // detect Bluetooth LE support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();

        showDevices();

        if (!isMyServiceRunning(BluetoothLEService.class)) {
            startService(new Intent(this, BluetoothLEService.class));
        }
    }

    private void showDevices()
    {
        getFragmentManager().beginTransaction().replace(R.id.container, devicesFragment).commit();
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
        devices.clear();
        stopScan = new Runnable() {
            @Override
            public void run()
            {
                mHandler.removeCallbacks(stopScan);
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                setRefreshing(false);

                if (!devices.isEmpty()) {
                    displayListScannedDevices();
                } else {
                    Toast.makeText(DevicesActivity.this, R.string.beacon_not_found, Toast.LENGTH_LONG).show();
                }
            }
        };
        mHandler.postDelayed(stopScan, SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        setRefreshing(true);
    }

    @Override
    public void onDevicesStarted()
    {
        if (random.nextInt(100) > 80 && !Preferences.isDonated(this)) { // displayed 20% time
            ConfirmAlertDialogFragment.instance(R.string.donate, R.string.donate_summary).show(getFragmentManager(), null);
        }

        // bind service
        bindService(new Intent(this, BluetoothLEService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDevicesStopped()
    {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mHandler.removeCallbacks(stopScan);

        unbindService(serviceConnection);
    }

    @Override
    public void onDevice(String address)
    {
        final Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(Devices.ADDRESS, address);
        startActivity(intent);
    }

    @Override
    public void onChangeDeviceName(String name, String address)
    {
        DeviceAlertDialogFragment.instance(name, address).show(getFragmentManager(), "dialog");
    }

    @Override
    public void onFeedback()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sylvek/itracing2/issues"));
        startActivity(browserIntent);
    }

    @Override
    public void onDonate()
    {
        Preferences.setDonated(this, true);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/SylvainMaucourt"));
        startActivity(browserIntent);
    }

    @Override
    public void onDeviceStateChanged(String address, boolean enabled)
    {
        if (service != null) {
            Devices.setEnabled(this, address, enabled);

            if (enabled) {
                service.connect(address);
            } else {
                AlertDialogFragment.instance(R.string.app_name, R.string.link_loss_disabled).show(getFragmentManager(), null);
                service.disconnect(address);
            }
        }
    }

    @Override
    public void onPreferences()
    {
        startActivity(new Intent(this, PreferencesActivity.class));
    }

    private void displayListScannedDevices()
    {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(R.string.select_device);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(devices.keySet());
        builderSingle.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int witch)
            {
                final String device = arrayAdapter.getItem(witch);
                selectDevice(device, devices.get(device));
                dialog.dismiss();
            }
        });
        builderSingle.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        builderSingle.show();
    }

    @Override
    public void doPositiveClick(String address, String name)
    {
        Devices.updateDevice(this, address, name);
        devicesFragment.refresh();
    }

    @Override
    public void doPositiveClick()
    {
        onDonate();
    }

    @Override
    public void doNegativeClick()
    {
    }

    @Override
    public void doNeutralClick(String address)
    {
        this.service.immediateAlert(address, BluetoothLEService.HIGH_ALERT);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
