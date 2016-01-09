package net.sylvek.itracing2;

import java.util.HashMap;
import java.util.UUID;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import net.sylvek.itracing2.database.Devices;

/**
 * Created by sylvek on 18/05/2015.
 */
public class BluetoothLEService extends Service {

    public static final int NO_ALERT = 0x00;
    public static final int MEDIUM_ALERT = 0x01;
    public static final int HIGH_ALERT = 0x02;

    public static final String IMMEDIATE_ALERT_AVAILABLE = "IMMEDIATE_ALERT_AVAILABLE";
    public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String GATT_CONNECTED = "GATT_CONNECTED";
    public static final String SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
    public static final String RSSI_RECEIVED = "RSSI_RECEIVED";

    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID LINK_LOSS_SERVICE = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static final String TAG = BluetoothLEService.class.toString();
    public static final String ACTION_PREFIX = "net.sylvek.itracing2.action.";
    public static final long TRACK_REMOTE_RSSI_DELAY_MILLIS = 5000L;
    public static final int FOREGROUND_ID = 1664;
    public static final String OUT_OF_BAND = "out_of_band";
    public static final String DOUBLE_CLICK = "double-click";
    public static final String SIMPLE_CLICK = "simple-click";
    public static final String BROADCAST_INTENT_ACTION = "BROADCAST_INTENT";

    private BluetoothDevice mDevice;

    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();

    private BluetoothGattService immediateAlertService;

    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCharacteristic buttonCharacteristic;

    private long lastChange;

    private Runnable r;

    private Handler handler = new Handler();

    private Runnable trackRemoteRssi = null;

    private class CustomBluetoothGattCallback extends BluetoothGattCallback {

        private final String address;

        CustomBluetoothGattCallback(final String address)
        {
            this.address = address;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            Log.d(TAG, "onConnectionStateChange() address: " + address + " status => " + status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcaster.sendBroadcast(new Intent(GATT_CONNECTED));
                    gatt.discoverServices();
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                }
            }

            final boolean actionOnPowerOff = Preferences.isActionOnPowerOff(BluetoothLEService.this, this.address);
            if (actionOnPowerOff || status == 8) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    String action = Preferences.getActionOutOfBand(getApplicationContext(), this.address);
                    sendAction(OUT_OF_BAND, action);
                    enablePeerDeviceNotifyMe(gatt, false);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            final Intent rssiIntent = new Intent(RSSI_RECEIVED);
            final int quality = 2 * (rssi + 100);
            rssiIntent.putExtra(RSSI_RECEIVED, quality + "%");
            broadcaster.sendBroadcast(rssiIntent);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status)
        {
            Log.d(TAG, "onServicesDiscovered()");

            launchTrackingRemoteRssi(gatt);

            broadcaster.sendBroadcast(new Intent(SERVICES_DISCOVERED));
            if (BluetoothGatt.GATT_SUCCESS == status) {

                for (BluetoothGattService service : gatt.getServices()) {
                    if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                        immediateAlertService = service;
                        broadcaster.sendBroadcast(new Intent(IMMEDIATE_ALERT_AVAILABLE));
                        gatt.readCharacteristic(getCharacteristic(gatt, IMMEDIATE_ALERT_SERVICE, ALERT_LEVEL_CHARACTERISTIC));
                    }

                    if (BATTERY_SERVICE.equals(service.getUuid())) {
                        batteryCharacteristic = service.getCharacteristics().get(0);
                        gatt.readCharacteristic(batteryCharacteristic);
                    }

                    if (FIND_ME_SERVICE.equals(service.getUuid())) {
                        if (!service.getCharacteristics().isEmpty()) {
                            buttonCharacteristic = service.getCharacteristics().get(0);
                            setCharacteristicNotification(gatt, buttonCharacteristic, true);
                        }
                    }
                }
                enablePeerDeviceNotifyMe(gatt, true);
            }
        }

        private void launchTrackingRemoteRssi(final BluetoothGatt gatt)
        {
            if (trackRemoteRssi != null) {
                handler.removeCallbacks(trackRemoteRssi);
            }

            trackRemoteRssi = new Runnable() {
                @Override
                public void run()
                {
                    gatt.readRemoteRssi();
                    handler.postDelayed(this, TRACK_REMOTE_RSSI_DELAY_MILLIS);
                }
            };
            handler.post(trackRemoteRssi);
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
            final long delayDoubleClick = Preferences.getDoubleButtonDelay(getApplicationContext(), this.address);

            final long now = SystemClock.elapsedRealtime();
            if (lastChange + delayDoubleClick > now) {
                Log.d(TAG, "onCharacteristicChanged() - double click");
                lastChange = 0;
                handler.removeCallbacks(r);
                String action = Preferences.getActionDoubleButton(getApplicationContext(), this.address);
                sendAction(DOUBLE_CLICK, action);
            } else {
                lastChange = now;
                r = new Runnable() {
                    @Override
                    public void run()
                    {
                        Log.d(TAG, "onCharacteristicChanged() - simple click");
                        String action = Preferences.getActionSimpleButton(getApplicationContext(), CustomBluetoothGattCallback.this.address);
                        sendAction(SIMPLE_CLICK, action);
                    }
                };
                handler.postDelayed(r, delayDoubleClick);
            }
        }

        private void sendAction(String source, String action)
        {
            final Intent intent = new Intent(BROADCAST_INTENT_ACTION.equals(action) ? ACTION_PREFIX + source : ACTION_PREFIX + action);
            intent.putExtra(Devices.ADDRESS, this.address);
            sendBroadcast(intent);
            Log.d(TAG, "onCharacteristicChanged() address: " + address + " - sendBroadcast action: " + intent.getAction() );
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "onCharacteristicRead()");

            final Intent batteryLevel = new Intent(BATTERY_LEVEL);
            batteryLevel.putExtra(BATTERY_LEVEL, Integer.valueOf(characteristic.getValue()[0]) + "%");
            broadcaster.sendBroadcast(batteryLevel);


        }
    }

    private void setCharacteristicNotification(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, boolean flag)
    {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        if (FIND_ME_CHARACTERISTIC.equals(bluetoothgattcharacteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothgatt.writeDescriptor(descriptor);
            }
        }
    }

    public void enablePeerDeviceNotifyMe(BluetoothGatt bluetoothgatt, boolean flag)
    {
        BluetoothGattCharacteristic bluetoothgattcharacteristic = getCharacteristic(bluetoothgatt, FIND_ME_SERVICE, FIND_ME_CHARACTERISTIC);
        if (bluetoothgattcharacteristic != null && (bluetoothgattcharacteristic.getProperties() | 0x10) > 0) {
            setCharacteristicNotification(bluetoothgatt, bluetoothgattcharacteristic, flag);
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothgatt, UUID serviceUuid, UUID characteristicUuid)
    {
        if (bluetoothgatt != null) {
            BluetoothGattService service = bluetoothgatt.getService(serviceUuid);
            if (service != null) {
                return service.getCharacteristic(characteristicUuid);
            }
        }

        return null;
    }

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
        this.setForegroundEnabled(Preferences.isForegroundEnabled(this));
        this.connect();
        return START_STICKY;
    }

    public void setForegroundEnabled(boolean enabled)
    {
        if (enabled) {
            final Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getText(R.string.app_name))
                    .setTicker(getText(R.string.foreground_started))
                    .setContentText(getText(R.string.foreground_started))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, DevicesActivity.class), 0))
                    .setShowWhen(false).build();
            this.startForeground(FOREGROUND_ID, notification);
        } else {
            this.stopForeground(true);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        this.broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy()
    {
        if (trackRemoteRssi != null) {
            handler.removeCallbacks(trackRemoteRssi);
        }

        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public void immediateAlert(String address, int alertType)
    {
        Log.d(TAG, "immediateAlert() - the device " + address);
        if (immediateAlertService == null || immediateAlertService.getCharacteristics() == null || immediateAlertService.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        this.bluetoothGatt.get(address).writeCharacteristic(characteristic);
    }

    private synchronized void somethingGoesWrong()
    {
        Toast.makeText(this, R.string.something_goes_wrong, Toast.LENGTH_LONG).show();
    }

    public synchronized void connect()
    {
        final Cursor cursor = Devices.findDevices(this);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String address = cursor.getString(0);
                if (Devices.isEnabled(this, address)) {
                    this.connect(address);
                }
            } while (cursor.moveToNext());
        }
    }

    public synchronized void connect(final String address)
    {
        if (!this.bluetoothGatt.containsKey(address)) {
            Log.d(TAG, "connect() - (new link) to device " + address);
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            this.bluetoothGatt.put(address, mDevice.connectGatt(this, true, new CustomBluetoothGattCallback(address)));
        } else {
            Log.d(TAG, "connect() - discovering services for " + address);
            this.bluetoothGatt.get(address).discoverServices();
        }
    }

    public synchronized void disconnect(final String address)
    {
        if (this.bluetoothGatt.containsKey(address)) {
            Log.d(TAG, "disconnect() - to device " + address);
            if (!Devices.isEnabled(this, address)) {
                Log.d(TAG, "disconnect() - no background linked for " + address);
                this.bluetoothGatt.get(address).disconnect();
                this.bluetoothGatt.remove(address);
            }
        }
    }

    public synchronized void remove(final String address)
    {
        if (this.bluetoothGatt.containsKey(address)) {
            Log.d(TAG, "remove() - to device " + address);
            this.bluetoothGatt.get(address).disconnect();
            this.bluetoothGatt.remove(address);
        }
    }
}
