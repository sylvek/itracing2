package net.sylvek.itracing2.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.sylvek.itracing2.BluetoothLEService;
import net.sylvek.itracing2.R;
import net.sylvek.itracing2.database.Devices;


/**
 * Created by sylvek on 18/05/2015.
 */
public class DashboardFragment extends Fragment {

    private float batteryPercent = 100f, rssiValue = 0f;

    private CircleDisplay mCircleDisplay;

    private OnDashboardListener presenter;

    private BroadcastReceiver receiver;

    public static DashboardFragment instance(final String address)
    {
        final DashboardFragment dashboardFragment = new DashboardFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Devices.ADDRESS, address);
        dashboardFragment.setArguments(arguments);
        dashboardFragment.setRetainInstance(true);
        return dashboardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.circle_percent, container, false);
        this.mCircleDisplay = (CircleDisplay) view.findViewById(R.id.circleDisplay);
        this.mCircleDisplay.setValueWidthPercent(15f);
        this.mCircleDisplay.setFormatDigits(1);
        this.mCircleDisplay.setDimAlpha(80);
        this.mCircleDisplay.setTouchEnabled(false);
        this.mCircleDisplay.setStepSize(0.5f);
        this.mCircleDisplay.setAnimDuration(1000);
        this.mCircleDisplay.setTextSize(50f);
        this.mCircleDisplay.startInfiniteLoop();
        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnDashboardListener) {
            this.presenter = (OnDashboardListener) activity;
        } else {
            throw new ClassCastException("must implement OnDashboardListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent)
            {
                if (BluetoothLEService.IMMEDIATE_ALERT_AVAILABLE.equals(intent.getAction())) {
                    presenter.onImmediateAlertAvailable();
                }

                if (BluetoothLEService.BATTERY_LEVEL.equals(intent.getAction())) {
                    setBatteryPercent(intent.getIntExtra(BluetoothLEService.BATTERY_LEVEL, 0));
                }

                if (BluetoothLEService.SERVICES_DISCOVERED.equals(intent.getAction())) {
                    mCircleDisplay.stopInfiniteLoop();
                }

                if (BluetoothLEService.RSSI_RECEIVED.equals(intent.getAction())) {
                    setRssiValue(intent.getIntExtra(BluetoothLEService.RSSI_RECEIVED, 0));
                }
            }
        };
        // register events
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothLEService.IMMEDIATE_ALERT_AVAILABLE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothLEService.BATTERY_LEVEL));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothLEService.SERVICES_DISCOVERED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothLEService.RSSI_RECEIVED));

        this.presenter.onDashboardStarted();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        this.presenter.onDashboardStopped();
    }

    private int getCalculatedColor(float value)
    {
        final int abs = (int) Math.abs(value);

        final int red, green;
        if (abs > 50) {
            green = 255;
            red = 2 * abs;
        } else {
            red = 255;
            green = 255 - 2 * (abs - 50);
        }

        return Color.argb(Integer.MAX_VALUE, red, green, 0);
    }

    public void setBatteryPercent(float value)
    {
        this.batteryPercent = value;
        updateBatteryPercent();
    }

    private void updateBatteryPercent()
    {
        if (this.mCircleDisplay != null) {
            this.mCircleDisplay.showValue(this.batteryPercent, 100f, true);
            this.mCircleDisplay.setColor(getCalculatedColor(this.batteryPercent));
        }
    }

    private void setRssiValue(float value)
    {
        this.rssiValue = value;
        updateRssiColor();
    }

    private void updateRssiColor()
    {
        if (this.mCircleDisplay != null) {
            this.mCircleDisplay.setInnerColor(getCalculatedColor(this.rssiValue));
        }
    }

    public interface OnDashboardListener {

        void onDashboardStarted();

        void onDashboardStopped();

        void onImmediateAlertAvailable();
    }
}
