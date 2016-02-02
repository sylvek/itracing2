package net.sylvek.itracing2;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by sylvek on 28/12/2015.
 */
public abstract class CommonActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(false);
    }

    protected void setRefreshing(final boolean refreshing)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run()
            {
                mSwipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }
}
