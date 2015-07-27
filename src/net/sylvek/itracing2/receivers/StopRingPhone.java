package net.sylvek.itracing2.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sylvek on 27/05/2015.
 */
public class StopRingPhone extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (StartRingPhone.currentRingtone != null) {
            StartRingPhone.currentRingtone.stop();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(StartRingPhone.NOTIFICATION_ID);
    }
}
