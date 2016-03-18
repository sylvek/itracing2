package net.sylvek.itracing2.dashboard;

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
import android.widget.Toast;
import net.sylvek.itracing2.R;
import net.sylvek.itracing2.database.Database;
import net.sylvek.itracing2.database.Devices;
import net.sylvek.itracing2.database.Events;
import net.sylvek.itracing2.database.SQLiteCursorLoader;

/**
 * Created by sylvek on 04/03/2016.
 */
public class EventsHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private OnEventsHistoryListener presenter;

    private EventsCursorAdapter mAdapter;

    public static EventsHistoryFragment instance(final String address)
    {
        final EventsHistoryFragment instance = new EventsHistoryFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(Events.ADDRESS, address);
        instance.setArguments(arguments);
        instance.setRetainInstance(true);
        return instance;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnEventsHistoryListener) {
            this.presenter = (OnEventsHistoryListener) activity;
        } else {
            throw new ClassCastException("must implement OnEventsHistoryListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new EventsCursorAdapter(getActivity());
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                final String option = (String) view.findViewById(android.R.id.text1).getTag();
                Toast.makeText(getActivity(), option, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void refresh()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.events, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.export_events:
                this.presenter.onExportEvents();
                return true;
            case R.id.clear_events:
                // TODO confirm the deletion
                if (Events.removeEvents(getActivity(), getArguments().getString(Devices.ADDRESS))) {
                    this.refresh();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(0, null, this);
        return inflater.inflate(R.layout.events, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new SQLiteCursorLoader(
                getActivity(),
                Database.getDatabaseHelperInstance(getActivity()),
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
                            Events.CREATED,
                            Events.OPTION
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

            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final int optColumn = cursor.getColumnIndex(Events.OPTION);
            final String option = cursor.getString(optColumn);

            text1.setTag(option);
        }
    }

    public interface OnEventsHistoryListener {

        void onExportEvents();
    }
}
