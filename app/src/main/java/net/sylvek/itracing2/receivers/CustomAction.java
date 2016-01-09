package net.sylvek.itracing2.receivers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import net.sylvek.itracing2.Preferences;
import net.sylvek.itracing2.database.Devices;

/**
 * Created by sylvek on 14/12/2015.
 */
public class CustomAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String address = intent.getStringExtra(Devices.ADDRESS);
        final String action = Preferences.getCustomAction(context, address);

        if (action.startsWith("http://")) {
            new CallUrl<>(action).start();
        }

        if (action.startsWith("https://")) {
            new CallUrl<HttpsURLConnection>(action).start();
        }

        if (!action.isEmpty()) {
            context.sendBroadcast(new Intent(action));
        }
    }

    private class CallUrl<T extends HttpURLConnection> extends Thread {

        final String url;

        public CallUrl(String url)
        {
            this.url = url;
        }

        @Override
        public void run()
        {
            try {
                T urlConnection = (T) new URL(url).openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    readStream(in);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                // nothing to do.
            }
        }
    }

    private void readStream(InputStream in) throws IOException
    {
        // nothing to do.
        in.close();
    }
}
