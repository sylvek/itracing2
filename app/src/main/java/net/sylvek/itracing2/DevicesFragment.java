package net.sylvek.itracing2;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import net.sylvek.itracing2.database.Devices;
import net.sylvek.itracing2.database.SQLiteCursorLoader;

/**
 * Created by sylvek on 18/05/2015.
 */
public class DevicesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private OnDevicesListener presenter;
    private DevicesCursorAdapter mAdapter;

    public static DevicesFragment instance()
    {
        return new DevicesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(0, null, this);
        return inflater.inflate(R.layout.devices, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.devices, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_scan) {
            this.presenter.onScanStart();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                final TextView address = (TextView) view.findViewById(android.R.id.text2);
                presenter.onDevice(address.getText().toString());
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

    public interface OnDevicesListener {

        void onScanStart();

        void onDevicesStarted();

        void onDevicesStopped();

        void onDevice(String address);
    }

    class DevicesCursorAdapter extends SimpleCursorAdapter {
        public DevicesCursorAdapter(Context context)
        {
            super(context,
                    android.R.layout.simple_expandable_list_item_2, null,
                    new String[]{
                            Devices.NAME,
                            Devices.ADDRESS
                    },
                    new int[]{
                            android.R.id.text1,
                            android.R.id.text2
                    }, 0);
        }
    }
}
