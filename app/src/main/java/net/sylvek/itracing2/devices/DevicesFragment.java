package net.sylvek.itracing2.devices;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import net.sylvek.itracing2.R;
import net.sylvek.itracing2.database.Devices;
import net.sylvek.itracing2.database.SQLiteCursorLoader;

/**
 * Created by sylvek on 18/05/2015.
 */
public class DevicesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DevicesFragment.class.toString();

    private OnDevicesListener presenter;
    private DevicesCursorAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;

    public static DevicesFragment instance()
    {
        return new DevicesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(0, null, this);
        final View view = inflater.inflate(R.layout.devices, container, false);
        this.coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                presenter.onScanStart();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnDevicesListener) {
            this.presenter = (OnDevicesListener) activity;
        } else {
            throw new ClassCastException("must implement OnDevicesListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new DevicesCursorAdapter(getActivity());
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                final TextView name = (TextView) view.findViewById(android.R.id.text1);
                final TextView address = (TextView) view.findViewById(android.R.id.text2);
                presenter.onDevice(name.getText().toString(), address.getText().toString());
            }
        });
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                final TextView name = (TextView) view.findViewById(android.R.id.text1);
                final TextView address = (TextView) view.findViewById(android.R.id.text2);
                final CheckBox enabled = (CheckBox) view.findViewById(android.R.id.selectedIcon);
                presenter.onChangeDeviceName(name.getText().toString(), address.getText().toString(), enabled.isChecked());
                return true;
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.presenter.onDevicesStarted();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.presenter.onDevicesStopped();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new SQLiteCursorLoader(
                getActivity(),
                Devices.getDevicesHelperInstance(getActivity()),
                Devices.SELECT_DEVICES,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mAdapter.swapCursor(null);
    }

    public void refresh()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void snack(String message)
    {
        Snackbar.make(this.coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public interface OnDevicesListener {

        void onDevicesStarted();

        void onDevicesStopped();

        void onDevice(String name, String address);

        void onChangeDeviceName(String name, String address, boolean checked);

        void onDeviceStateChanged(String address, boolean enabled);

        void onScanStart();
    }

    class DevicesCursorAdapter extends SimpleCursorAdapter {

        public DevicesCursorAdapter(Context context)
        {
            super(context,
                    R.layout.expandable_list_item_with_options, null,
                    new String[]{
                            Devices.NAME,
                            Devices.ADDRESS,
                            Devices.ENABLED
                    },
                    new int[]{
                            android.R.id.text1,
                            android.R.id.text2
                    }, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            super.bindView(view, context, cursor);
            final TextView address = (TextView) view.findViewById(android.R.id.text2);
            final CheckBox button = (CheckBox) view.findViewById(android.R.id.selectedIcon);

            final String device = address.getText().toString();
            final int column = cursor.getColumnIndex(Devices.ENABLED);
            final boolean enabled = cursor.getInt(column) == 1;

            Log.d(TAG, "device: " + device + " enabled: " + enabled);

            button.setChecked(enabled);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    final boolean b = button.isChecked();
                    Log.d(TAG, "onClick() device: " + device + " enabled: " + b);
                    presenter.onDeviceStateChanged(device, b);
                }
            });
        }
    }
}
