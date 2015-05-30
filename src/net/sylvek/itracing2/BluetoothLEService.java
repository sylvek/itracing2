package net.sylvek.itracing2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by sylvek on 18/05/2015.
 */
public class BluetoothLEService extends Service {

    public static final int NO_ALERT = 0x00;
    public static final int MEDIUM_ALERT = 0x01;
    public static final int HIGH_ALERT = 0x02;

    public static final String IMMEDIATE_ALERT_PREFIX = "00001802";
    public static final String BATTERY_PREFIX = "0000180f";
    public static final String LINK_LOSS_PREFIX = "00001803";
    public static final String BUTTON_PREFIX = "0000ffe0";

    public static final String IMMEDIATE_ALERT_AVAILABLE = "IMMEDIATE_ALERT_AVAILABLE";
    public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String GATT_CONNECTED = "GATT_CONNECTED";
    public static final String SERVICES_DISCOVERED = "SERVICES_DISCOVERED";

    public static final String TAG = BluetoothLEService.class.toString();

    private BluetoothGatt bluetoothGatt = null;

    private BluetoothGattService immediateAlertService;

    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.d(TAG, "onConnectionStateChange()");
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG, "onConnectionStateChange => " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcaster.sendBroadcast(new Intent(GATT_CONNECTED));
                    gatt.discoverServices();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.d(TAG, "onServicesDiscovered()");
            broadcaster.sendBroadcast(new Intent(SERVICES_DISCOVERED));
            if (BluetoothGatt.GATT_SUCCESS == status) {
                for (final BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().startsWith(IMMEDIATE_ALERT_PREFIX)) {
                        immediateAlertService = service;
                        broadcaster.sendBroadcast(new Intent(IMMEDIATE_ALERT_AVAILABLE));
                    }

                    if (service.getUuid().toString().startsWith(BATTERY_PREFIX)) {
                        batteryCharacteristic = service.getCharacteristics().get(0);
                    }

                    if (service.getUuid().toString().startsWith(BUTTON_PREFIX)) {
                        final BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(0);
                        final BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.d(TAG, "onDescriptorWrite()");
            gatt.readCharacteristic(batteryCharacteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d(TAG, "onCharacteristicChanged()");
            String action = Preferences.getActionButton(getApplicationContext());
            sendBroadcast(new Intent("net.sylvek.itracing2.action." + action));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "onCharacteristicRead()");
            final Intent batteryLevel = new Intent(BATTERY_LEVEL);
            batteryLevel.putExtra(BATTERY_LEVEL, Integer.valueOf(characteristic.getValue()[0]) + "%");
            broadcaster.sendBroadcast(batteryLevel);
        }
    };

    public class BackgroundBluetoothLEBinder extends Binder {
        public BluetoothLEService service()
        {
            return BluetoothLEService.this;
        }
    }

    private BackgroundBluetoothLEBinder myBinder = new BackgroundBluetoothLEBinder();

    private LocalBroadcastManager broadcaster;

    @Override
    public IBinder onBind(Intent intent)
    {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        this.connect();
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public void immediateAlert()
    {
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(HIGH_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        this.bluetoothGatt.writeCharacteristic(characteristic);
    }

    public void connect()
    {
        if (this.bluetoothGatt == null) {
            Log.d(TAG, "connect() - connecting GATT");
            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(Preferences.getKeyringUUID(this));
            this.bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
        } else {
            Log.d(TAG,"connect() - discovering services");
            this.bluetoothGatt.discoverServices();
        }
    }

    public void disconnect()
    {
        Log.d(TAG, "disconnect()");
        this.bluetoothGatt.disconnect();
        this.bluetoothGatt.close();
        this.bluetoothGatt = null;
    }
}
