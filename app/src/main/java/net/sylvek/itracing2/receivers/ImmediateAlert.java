package net.sylvek.itracing2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import net.sylvek.itracing2.BluetoothLEService;

/**
 * Created by sylvek on 25/06/2017.
 */
public class ImmediateAlert extends BroadcastReceiver {

    private BluetoothLEService service;

    @Override
    public void onReceive(Context context, Intent intent) {

        // itag://mac_address
        final Uri data = intent.getData();
        if (data != null) {
            final Intent startService = new Intent(context, BluetoothLEService.class);
            startService.setData(data);
            context.startService(intent);
        }
    }
}
