package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MovieGridFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    //private final String DISCOVERMOVIEGRIDFRAGMENT_TAG = "DMGfTAG";
    private final String DETAILMOVIEFRAGMENT_TAG = "DMfTAG";

    // A variable that will be used for two pane view on tablets.
    private boolean mTwoPane;
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSortOrder = Utility.getPreferredSortOrder(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailFragment(), DETAILMOVIEFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        MovieGridFragment movieGridFragment = ((MovieGridFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_moviegrid));
        // In case we have two pane layout, we want to view the first item
        // in the DetailFragment when the data is finished loading.
        movieGridFragment.setUseTwoPaneBeginLayout(mTwoPane);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean getTwoPane()
    { return mTwoPane; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getPreferredSortOrder(this);
        // update the sortOrder in our second pane using the fragment manager
        if (sortOrder != null && !sortOrder.equals(mSortOrder)) {
            MovieGridFragment ff = (MovieGridFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_moviegrid);
            if (null != ff) {
                ff.onSortOrderChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILMOVIEFRAGMENT_TAG);
            if (null != df) {
                df.onSortOrderChanged(sortOrder);
            }

            mSortOrder = sortOrder;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            final Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            final DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);


            final int WHAT = 1;
            // Add the Looper.getMainLooper so that the handler can run.
            Handler handler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg)
                {
                    if(msg.what == WHAT) getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment, DETAILMOVIEFRAGMENT_TAG)
                            .commit();
                }
            };
            handler.sendEmptyMessage(WHAT);


        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }

        PopularMoviesSyncAdapter.initializeSyncAdapter(this);
    }
}
