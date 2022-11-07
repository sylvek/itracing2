package net.sylvek.itracing2.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.net.Uri;

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

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

        if (action.startsWith("mqtt://") || action.startsWith("mqtts://")) {
            new PublishMQTT(action, address + "," + source).start();
        }

        if (action.startsWith("tel:")) {
            new Phone(context, action).start();
        }

        if (!action.isEmpty()) {
            context.sendBroadcast(new Intent(action));
        }
    }

    private class PublishMQTT extends Thread {

        // mqtt://login:password@broker:1883/my/topic
        // mqtts://login:password@broker:1883/my/topic
        private static final String PATTERN = "^(?<protocol>mqtt[s]*):\\/\\/((?<login>[a-zA-Z-0-9]+):(?<password>[a-zA-Z-0-9]+))?@?(?<host>[.a-z0-9-]+):?(?<port>\\d+)?\\/(?<topic>[/a-zA-Z0-9-]+)$";

        private final Pattern pattern = Pattern.compile(PATTERN);

        private final String payload;

        private final String domain;

        public PublishMQTT(String domain, String payload) {
            this.domain = domain;
            this.payload = payload;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            final Matcher matcher = pattern.matcher(this.domain);
            if (matcher.matches()) {
                String protocol = matcher.group("protocol");
                String login = matcher.group("login");
                String password = matcher.group("password");
                String host = matcher.group("host");
                String port = matcher.group("port");
                String topic = matcher.group("topic");

                if (port == null && protocol.equals("mqtt")) {
                    port = "1883";
                }
                else if (port == null && protocol.equals("mqtts")){
                    port = "8883";
                }

                if (protocol.equals("mqtts")) {
                    protocol = "ssl";
                }


                try {
                    final MqttClient client = new MqttClient(protocol + "://" + host + ":" + port,
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

    private class Phone extends Thread {

        private final Context context;
        private final String action;

        public Phone(Context context, String action) {
            this.context = context;
            this.action = action;
        }

        @Override
        public void run() {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            callIntent.setData(Uri.parse(action));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            context.startActivity(callIntent);
        }
    }
}
