package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.android.popularmovies.data.MoviesContract;

public class DetailActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
    {
        /* Add the log tag
        * */
        private static final String LOG_TAG = DetailFragment.class.getName();

        private static final String MOVIE_SHARE_HASHTAG = "#PopularMovies ";

        private ShareActionProvider mShareActioProvider;

        private String mMovieStr;

        private static final int DETAIL_LOADER = 0;

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
                //MoviesContract.SortEntry.COLUMN_SORT_SETTING,
                MoviesContract.MoviesEntry.COLUMN_POPULARITY,
                MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
                MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
        };
        // These indices are tied to DISCOVER_MOVIES_COLUMNS.  If DISCOVER_MOVIES_COLUMNS changes, these
        // must change.
        //static final int COL_MOVIES_ID = 0;
        static final int COL_MOVIE_TITLE = 1;
        //static final int COL_MOVIE_ID = 2;
        static final int COL_OVERVIEW = 3;
        static final int COL_POSTER_PATH= 4;
        //static final int COL__SORT_SETTING = 5;
        static final int COL_POPULARITY = 5;
        static final int COL_VOTE_AVERAGE = 6;
        static final int COL_RELEASE_DATE = 7;

        public DetailFragment()
        {
            setHasOptionsMenu(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            /*View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            // The detail Activity called via intent. Inspect the intent for additional data.
            Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                mMovieStr = intent.getDataString();

            }
            if (null != mMovieStr)
            {
                ((TextView) rootView.findViewById(R.id.detail_text)).setText(mMovieStr);
            }*/
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }



        /* Add the options menu to the fragment. */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActioProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach the intent to this ShareActionProvider. You can update this at any time,
            // like when the user selects new piece of data they might like to share.
            if(mMovieStr != null)
            {
                mShareActioProvider.setShareIntent(createShareMovieIntent());
            }


        }
        /* The share intent. */
        private Intent createShareMovieIntent()
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            //The FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET is depricated as of API 21
            //By the documentation FLAG_ACTIVITY NEW_DOCUMENT should be used:
            //http://developer.android.com/reference/android/content/Intent.html
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mMovieStr + MOVIE_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }
            Uri uri = intent.getData();
            Log.d(LOG_TAG, String.valueOf(uri));
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    uri,
                    DISCOVER_MOVIES_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = data.getString(COL_MOVIE_TITLE);

            String moviesOverView =
                    data.getString(COL_OVERVIEW);

            //boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatRating(
                    data.getDouble(COL_VOTE_AVERAGE));

            String low = Utility.formatRating(
                    data.getDouble(COL_POPULARITY));

            mMovieStr = String.format("%s - %s - %s/%s", dateString, moviesOverView, high, low);
            Log.d(LOG_TAG, data.getString(COL_OVERVIEW));
            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
            detailTextView.setText(mMovieStr);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActioProvider != null) {
                mShareActioProvider.setShareIntent(createShareMovieIntent());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
