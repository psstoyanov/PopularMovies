package com.example.android.popularmovies;

import android.content.Intent;
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
    public static class DetailFragment extends Fragment
    {
        /* Add the log tag
        * */
        private static final String LOG_TAG = DetailFragment.class.getName();

        private static final String MOVIE_SHARE_HASHTAG = "#PopularMovies ";
        private String mMovieStr;

        public DetailFragment()
        {
            setHasOptionsMenu(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            // The detail Activity called via intent. Inspect the intent for additional data.
            Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                mMovieStr = intent.getStringExtra(Intent.EXTRA_TEXT);

            }
            if (null != mMovieStr)
            {
                ((TextView) rootView.findViewById(R.id.detail_text)).setText(mMovieStr);
            }
            return rootView;
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

        /* Add the options menu to the fragment. */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            ShareActionProvider mShareActioProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach the intent to this ShareActionProvider. You can update this at any time,
            // like when the user selects new piece of data they might like to share.
            if(mShareActioProvider != null)
            {
                mShareActioProvider.setShareIntent(createShareMovieIntent());
            }
            else {
                Log.d(LOG_TAG, "ShareActionProvider is null???");
            }


        }

    }
}
