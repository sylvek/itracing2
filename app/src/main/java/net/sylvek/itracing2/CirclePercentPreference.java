package net.sylvek.itracing2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by sylvek on 15/01/2016.
 */
public class CirclePercentPreference extends android.preference.Preference {

    private float batteryPercent = 100f, rssiValue = 0f;
    private CircleDisplay mCircleDisplay;

    public CirclePercentPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent)
    {
        super.onCreateView(parent);
        final LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = li.inflate(R.layout.circle_percent_preference, parent, false);
        return view;
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
        this.mCircleDisplay = (CircleDisplay) view.findViewById(R.id.circleDisplay);

        this.mCircleDisplay.setValueWidthPercent(15f);
        this.mCircleDisplay.setFormatDigits(1);
        this.mCircleDisplay.setDimAlpha(80);
        this.mCircleDisplay.setTouchEnabled(false);
        this.mCircleDisplay.setStepSize(0.5f);
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
            this.mCircleDisplay.showValue(this.batteryPercent, 100f, false);
            this.mCircleDisplay.setColor(getCalculatedColor(this.batteryPercent));
        }
    }

    public void setRssiValue(float value)
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
}
