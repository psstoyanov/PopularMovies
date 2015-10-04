package com.example.android.popularmovies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.data.MoviesContract;
import com.example.android.popularmovies.services.PopularMoviesService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int DISCOVER_MOVIES_LOADER = 0;
    private final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    private static final String BUNDLE_RECYCLER_LAYOUT = "layoutManager";
    private static final String SELECTED_KEY = "selected_position";

    // Specify the columns we need.
    private static final String[] DISCOVER_MOVIES_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.SortEntry.COLUMN_SORT_SETTING,
            MoviesContract.MoviesEntry.COLUMN_POPULARITY,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
    };

    // These indices are tied to DISCOVER_MOVIES_COLUMNS.  If DISCOVER_MOVIES_COLUMNS changes, these
    // must change.
    static final int COL_MOVIES_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_ID = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_POSTER_PATH = 4;
    static final int COL__SORT_SETTING = 5;
    static final int COL_POPULARITY = 6;
    static final int COL_VOTE_AVERAGE = 7;
    static final int COL_RELEASE_DATE = 8;

    RecyclerView mRecyclerView;
    private Runnable viewBeginTwoPane;
    private boolean mUseTwoPaneBeginLayout;
    RecyclerView.LayoutManager mLayoutManager;
    GridAdapter mAdapter;

    /**
     * +     * A callback interface that all activities containing this fragment must
     * +     * implement. This mechanism allows activities to be notified of item
     * +     * selections.
     * +
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        //Log.d(LOG_TAG, " onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        // Calling the RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);


        // The number of Columns
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GridAdapter(getActivity(), null);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                mAdapter.setmSelectedItem(savedInstanceState.getInt(SELECTED_KEY));
                //mRecyclerView.smoothScrollToPosition(savedInstanceState.getInt(SELECTED_KEY));
            }
            Log.d(LOG_TAG, " onCreateView restore instance");
        } else {
            updateMovieData();
            Log.d(LOG_TAG, " onCreateView restore instance null");
        }

        //Log.d(LOG_TAG, String.valueOf(mRecyclerView.getResources()));


        mRecyclerView.setAdapter(mAdapter);

        Log.d(LOG_TAG, " onCreateView");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            updateMovieData();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateMovieData() {
        //FetchMovieTask movieTask = new FetchMovieTask(getActivity());
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String sortorder = prefs.getString(getString(R.string.pref_sort_key),
        //        getString(R.string.pref_order_popularity));
        //Log.d(LOG_TAG, " updateMovieData");
        //String sortOrder = Utility.getPreferredSortOrder(getActivity());
        //movieTask.execute(sortOrder);

        Intent alarmIntent = new Intent(getActivity(),PopularMoviesService.AlarmReceiver.class);
        alarmIntent.putExtra(PopularMoviesService.SORT_ORDER_QUERY_EXTRA,
                Utility.getPreferredSortOrder(getActivity()));

        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(),0,
                alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        //getBroadcast(context, 0, i, 0);

        AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        //Set the AlarmManager to wake up the system.
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DISCOVER_MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

        //Log.d(LOG_TAG, " onActivityCreated");
    }


    // since we read the location when we create the loader, all we need to do is restart things
    void onSortOrderChanged() {
        updateMovieData();
        mAdapter.resetmSelectedItem();
        getLoaderManager().restartLoader(DISCOVER_MOVIES_LOADER, null, this);
    }

    /**
     * This is a method for Fragment.
     * You can do the same in onCreate or onRestoreInstanceState
     */
    /*@Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            Log.d(LOG_TAG, "onViewStateRestored");
        }
    }*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to INVALID_POSITION,
        // so check for that before storing.
        if (mAdapter.getmSelectedItem() != -1) {
            outState.putInt(SELECTED_KEY, mAdapter.getmSelectedItem());
        }
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().
                        onSaveInstanceState()
        );
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Quick fix: app was crashing in two pane mode
        // when returning from the SettingsActivity with the system back button
        // instead of the back button in the app bar.
        getLoaderManager().initLoader(0, null, this);
        Log.d(LOG_TAG, " onStart");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        String sortSetting = Utility.getPreferredSortOrder(getActivity());

        Uri discoverMoviesWithSortOrder = MoviesContract.MoviesEntry.buildMoviesSortorder(sortSetting);

        return new CursorLoader(getActivity(),
                discoverMoviesWithSortOrder,
                DISCOVER_MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, final Cursor cursor) {
        //mForecastAdapter.swapCursor(cursor);
        mAdapter.swapCursor(cursor);

        if (mAdapter.getmSelectedItem() != -1) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mAdapter.getmSelectedItem());
        }
        //In case we have two pane layout, we want to load the first item.
        else if (cursor != null && mUseTwoPaneBeginLayout)
        {

            // Check if the adapter is empty
            // and if the adapter has all items from the cursor
            // initialized before selecting the first item.
            // Otherwise, the app will crash when first loaded
            // as the tables are empty.
            if (mAdapter.getItemCount() == cursor.getCount() &&
                    mAdapter.getItemCount()!= 0)
            {
                mAdapter.notifyDataSetChanged();
                // Create a new runnable to perform the click event.
                // Caution! When using the system back
                viewBeginTwoPane = new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setClickSelect(getActivity(), cursor, 0);
                    }
                };
                // Launch a new thread to perform the action.
                Thread thread = new Thread(null, viewBeginTwoPane, "MagentoBackground");
                thread.start();
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    public void setUseTwoPaneBeginLayout(boolean useTwoPaneBeginView) {
        mUseTwoPaneBeginLayout = useTwoPaneBeginView;
    }


}
