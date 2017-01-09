package net.sylvek.itracing2.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import net.sylvek.itracing2.BluetoothLEService;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 27/05/2015.
 */
public class ToggleVibratePhone extends BroadcastReceiver {

    static final int NOTIFICATION_ID = 453437;
    private static final long[] VIBRATE_PATTERN = new long[]{0, 1000, 100, 2000, 100, 3000, 100, 2000, 100};
    private static final int VIBRATE_REPEAT = 0;
    public static final String TAG = ToggleVibratePhone.class.toString();
    private static Vibrator vibrator = null;
    private static boolean vibrating=false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(vibrator==null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (!vibrator.hasVibrator()) {
            Toast.makeText(context, R.string.vibrator_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        // from notification
        if(intent.getAction() == null) {
            stopVibrate(context, intent);
            return;
        }

        if(intent.getAction().equals(BluetoothLEService.ACTION_PREFIX + "TOGGLE_VIBRATE_PHONE")) {
            Log.d(TAG,"Toggle");
            if (!vibrating) {
                startVibrate(context, intent);
            } else {
                stopVibrate(context, intent);
            }
            return;
        }
        if(intent.getAction().equals(BluetoothLEService.ACTION_PREFIX + "START_VIBRATE_PHONE")) {
            Log.d(TAG,"Start");
            startVibrate(context, intent);
            return;
        }
        if(intent.getAction().equals(BluetoothLEService.ACTION_PREFIX + "STOP_VIBRATE_PHONE")) {
            Log.d(TAG,"Stop");
            stopVibrate(context, intent);
            return;
        }
    }

    private void startVibrate(Context context, Intent intent) {

        vibrator.vibrate(VIBRATE_PATTERN, VIBRATE_REPEAT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification.Builder(context)
                .setContentText(context.getString(R.string.stop_vibrate))
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getBroadcast(context, 0, new Intent(context, ToggleVibratePhone.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
        vibrating=true;
    }


    private void stopVibrate(Context context, Intent intent) {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (!vibrator.hasVibrator()) {
            Toast.makeText(context, R.string.vibrator_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        vibrator.cancel();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ToggleVibratePhone.NOTIFICATION_ID);
        vibrating=false;
    }
}
