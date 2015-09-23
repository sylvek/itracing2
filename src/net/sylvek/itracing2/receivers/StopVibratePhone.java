package net.sylvek.itracing2.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;
import net.sylvek.itracing2.R;

/**
 * Created by sylvek on 27/05/2015.
 */
public class StopVibratePhone extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (!vibrator.hasVibrator()) {
            Toast.makeText(context, R.string.vibrator_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        vibrator.cancel();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(StartVibratePhone.NOTIFICATION_ID);
    }
}
