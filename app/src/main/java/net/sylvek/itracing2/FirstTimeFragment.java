package net.sylvek.itracing2;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sylvek on 18/05/2015.
 */
public class FirstTimeFragment extends Fragment {

    private OnFirstTimeListener presenter;

    public static FirstTimeFragment instance()
    {
        return new FirstTimeFragment();
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
        return inflater.inflate(R.layout.fragment_first_time, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.fragment_first_time, menu);
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
        if (activity instanceof OnFirstTimeListener) {
            this.presenter = (OnFirstTimeListener) activity;
        } else {
            throw new ClassCastException("must implement OnFirstTimeListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.presenter.onFirstTimeStarted();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.presenter.onFirstTimeStopped();
    }

    public interface OnFirstTimeListener {

        void onScanStart();

        void onFirstTimeStarted();

        void onFirstTimeStopped();
    }
}
