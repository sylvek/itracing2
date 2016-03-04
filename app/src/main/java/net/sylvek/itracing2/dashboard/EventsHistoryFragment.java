package net.sylvek.itracing2.dashboard;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.sylvek.itracing2.R;
import net.sylvek.itracing2.database.Database;
import net.sylvek.itracing2.database.Devices;
import net.sylvek.itracing2.database.Events;
import net.sylvek.itracing2.database.SQLiteCursorLoader;

/**
 * Created by sylvek on 04/03/2016.
 */
public class EventsHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EventsHistoryFragment.class.toString();

    private EventsCursorAdapter mAdapter;

    public static EventsHistoryFragment instance(final String address)
    {
        final EventsHistoryFragment instance = new EventsHistoryFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(Devices.ADDRESS, address);
        instance.setArguments(arguments);
        instance.setRetainInstance(true);
        return instance;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new EventsCursorAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(0, null, this);
        final View view = inflater.inflate(R.layout.devices, container, false);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new SQLiteCursorLoader(
                getActivity(),
                Database.geDatabaseHelperInstance(getActivity()),
                Events.SELECT_EVENTS,
                new String[]{getArguments().getString(Devices.ADDRESS)}
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

    class EventsCursorAdapter extends SimpleCursorAdapter {

        public EventsCursorAdapter(Context context)
        {
            super(context,
                    android.R.layout.simple_list_item_2, null,
                    new String[]{
                            Events.NAME,
                            Events.CREATED
                    },
                    new int[]{
                            android.R.id.text1,
                            android.R.id.text2
                    }, 0);
        }
    }
}
