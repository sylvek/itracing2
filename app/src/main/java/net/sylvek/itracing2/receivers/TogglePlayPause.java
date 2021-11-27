package net.sylvek.itracing2.receivers;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import net.sylvek.itracing2.BluetoothLEService;

/**
 * Created by antocuni on 27/11/2021, copied&adapted from ToggleRingPhone
 */
public class TogglePlayPause extends BroadcastReceiver {

    static final int NOTIFICATION_ID = 453438;
    public static final String TAG = TogglePlayPause.class.toString();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(BluetoothLEService.ACTION_PREFIX + "TOGGLE_PLAY_PAUSE")) {
            Log.d(TAG,"Toggle Play/Pause");
            AudioManager mgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            KeyEvent key_down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            mgr.dispatchMediaKeyEvent(key_down);

            KeyEvent key_up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            mgr.dispatchMediaKeyEvent(key_up);
            return;
        }

    }
}
