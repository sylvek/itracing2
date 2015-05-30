package net.sylvek.itracing2.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 27/05/2015.
 */
public class StartRingPhone extends BroadcastReceiver {

    static final int NOTIFICATION_ID = 453435;
    static Ringtone currentRingtone;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (currentRingtone == null) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
            currentRingtone = RingtoneManager.getRingtone(context, sound);
        }
        currentRingtone.play();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification.Builder(context)
                .setContentText(context.getString(R.string.stop_ring))
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getBroadcast(context, 0, new Intent(context, StopRingPhone.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
