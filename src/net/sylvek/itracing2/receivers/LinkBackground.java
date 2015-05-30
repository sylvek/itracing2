package net.sylvek.itracing2.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.sylvek.itracing2.BluetoothLEService;
import net.sylvek.itracing2.Preferences;

/**
 * Created by sylvek on 20/05/2015.
 */
public class LinkBackground extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        Log.d(BluetoothLEService.TAG, "bluetooth change state: " + bluetoothState);
        if (Preferences.getKeyringUUID(context) != null && Preferences.getLinkBackgroundEnabled(context) && bluetoothState == BluetoothAdapter.STATE_ON) {
            context.startService(new Intent(context, BluetoothLEService.class));
        }
    }
}
