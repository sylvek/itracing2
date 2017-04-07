package net.sylvek.itracing2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.sylvek.itracing2.Preferences;
import net.sylvek.itracing2.database.Devices;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sylvek on 14/12/2015.
 */
public class CustomAction extends BroadcastReceiver {

    public static final String TAG = CustomAction.class.toString();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String address = intent.getStringExtra(Devices.ADDRESS);
        final String source = intent.getStringExtra(Devices.SOURCE);
        final String action = Preferences.getCustomAction(context, address, source);

        if (action.startsWith("http://")) {
            new CallUrl<>(action, "address=" + address + "&source=" + source).start();
        }

        if (action.startsWith("https://")) {
            new CallUrl<HttpsURLConnection>(action, "address=" + address + "&source=" + source).start();
        }

        if (action.startsWith("mqtt://")) {
            new PublishMQTT(action, address + "," + source).start();
        }

        if (!action.isEmpty()) {
            context.sendBroadcast(new Intent(action));
        }
    }

    private class PublishMQTT extends Thread {

        // mqtt://login:password@broker:1883/my/topic
        private static final String PATTERN = "^mqtt:\\/\\/(([a-zA-Z-0-9]+):([a-zA-Z-0-9]+))?@?([.a-z0-9]+):?(\\d+)?\\/([/a-zA-Z0-9]+)$";

        private final Pattern pattern = Pattern.compile(PATTERN);

        private final String payload;

        private final String domain;

        public PublishMQTT(String domain, String payload) {
            this.domain = domain;
            this.payload = payload;
        }

        @Override
        public void run() {
            final Matcher matcher = pattern.matcher(this.domain);
            if (matcher.matches()) {
                String login = matcher.group(2);
                String password = matcher.group(3);
                String host = matcher.group(4);
                String port = matcher.group(5);
                String topic = matcher.group(6);

                if (port == null) {
                    port = "1883";
                }

                try {
                    final MqttClient client = new MqttClient("tcp://" + host + ":" + port,
                            MqttClient.generateClientId(),
                            new MemoryPersistence()
                    );
                    final MqttConnectOptions options = new MqttConnectOptions();

                    if (login != null && password != null) {
                        options.setUserName(login);
                        options.setPassword(password.toCharArray());
                    }

                    client.connect(options);
                    if (client.isConnected()) {
                        client.publish(topic, payload.getBytes(), 0, false);
                        client.disconnect();

                    }
                } catch (MqttException e) {
                    Log.d(TAG, "exception", e);
                }
            }
        }
    }

    private class CallUrl<T extends HttpURLConnection> extends Thread {

        final String url;

        public CallUrl(String domain, String action) {
            this.url = domain + (domain.contains("?") ? "&" : "?") + action;
        }

        @Override
        public void run() {
            try {
                T urlConnection = (T) new URL(url).openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    readStream(in);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.d(TAG, "exception", e);
            }
        }
    }

    private void readStream(InputStream in) throws IOException {
        // nothing to do.
        in.close();
    }
}
