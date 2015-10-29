package net.sylvek.itracing2.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 27/05/2015.
 */
public class StartVibratePhone extends BroadcastReceiver {

    static final int NOTIFICATION_ID = 453436;
    private static final long[] VIBRATE_PATTERN = new long[]{0, 1000, 100, 2000, 100, 3000, 100, 2000, 100};
    private static final int VIBRATE_REPEAT = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (!vibrator.hasVibrator()) {
            Toast.makeText(context, R.string.vibrator_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEAT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification.Builder(context)
                .setContentText(context.getString(R.string.stop_vibrate))
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getBroadcast(context, 0, new Intent(context, StopVibratePhone.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
