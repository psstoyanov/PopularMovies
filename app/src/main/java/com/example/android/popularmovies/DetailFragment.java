package com.example.android.popularmovies;

/**
 * Created by Raz3r on 23/08/15.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.Adapters.ReviewsRecyclerAdapter;
import com.example.android.popularmovies.Adapters.VideosRecyclerAdapter;
import com.example.android.popularmovies.ObjectModel.Videos;
import com.example.android.popularmovies.Utils.CustomLinearLayoutManager;
import com.example.android.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /* Add the log tag
    * */
    private static final String LOG_TAG = DetailFragment.class.getName();

    private static final String MOVIE_SHARE_HASHTAG = "#PopularMovies ";

    static final String DETAIL_URI = "URI";

    private ShareActionProvider mShareActioProvider;

    private String mMovieStr;

    private Uri mUri;

    private boolean FavoriteButtonActive;

    Button mFavoriteButton;

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
            MoviesContract.MoviesEntry.COLUMN_MOVIE_RUNTIME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_TAGLINE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_BACKDROP_IMG,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_HOMEPAGE
    };
    // These indices are tied to DISCOVER_MOVIES_COLUMNS.  If DISCOVER_MOVIES_COLUMNS changes, these
    // must change.
    //static final int COL_MOVIES_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_ID = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_POSTER_PATH = 4;
    //static final int COL__SORT_SETTING = 5;
    static final int COL_POPULARITY = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_RELEASE_DATE = 7;
    static final int COL_RUNTIME = 8;
    static final int COL_TAGLINE = 9;
    static final int COL_BACKDROP_IMG = 10;
    static final int COL_HOMEPAGE = 11;

    // The videos recycler view.
    RecyclerView mVideoRecyclerView;
    RecyclerView.LayoutManager mVideoLayoutManager;
    VideosRecyclerAdapter mVideoAdapter;

    // The reviews recycler view.

    RecyclerView mReviewsRecyclerView;
    RecyclerView.LayoutManager mReviewsLayoutManager;
    ReviewsRecyclerAdapter mReviewsAdapter;



    public DetailFragment() {
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Calling the RecyclerViews
        mVideoRecyclerView = (RecyclerView) view.findViewById(R.id.video_recycler_view);
        mReviewsRecyclerView = (RecyclerView) view.findViewById(R.id.reviews_recycler_view);
        //mVideoRecyclerView.setHasFixedSize(true);

        mVideoLayoutManager = new CustomLinearLayoutManager(getActivity());
        mVideoRecyclerView.setScrollContainer(false);
        mVideoRecyclerView.setLayoutManager(mVideoLayoutManager);
        mVideoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mReviewsLayoutManager = new CustomLinearLayoutManager(getActivity());
        mReviewsRecyclerView.setScrollContainer(false);
        mReviewsRecyclerView.setLayoutManager(mReviewsLayoutManager);

        mVideoAdapter = new VideosRecyclerAdapter(getActivity());
        mReviewsAdapter = new ReviewsRecyclerAdapter(getActivity());

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        mVideoRecyclerView.setAdapter(mVideoAdapter);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        return view;
    }






    /* Add the options menu to the fragment. */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActioProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach the intent to this ShareActionProvider. You can update this at any time,
        // like when the user selects new piece of data they might like to share.
        if (mMovieStr != null) {
            mShareActioProvider.setShareIntent(createShareMovieIntent());
        }


    }

    /* The share intent. */
    private Intent createShareMovieIntent() {
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

    void onSortOrderChanged(String newSortOrder) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long movieID = MoviesContract.MoviesEntry.getMovieIDFromUri(uri);
            Uri updatedUri = MoviesContract.MoviesEntry.buildMovieSortOrderWithMovieID(newSortOrder, movieID);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Log.v(LOG_TAG, "In onCreateLoader");
        //Intent intent = getActivity().getIntent();
        //if (intent == null || intent.getData() == null) {
        //    return null;
        //}
        //Uri uri = intent.getData();
        //Log.d(LOG_TAG, String.valueOf(uri));
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DISCOVER_MOVIES_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        final String movieTitleString = data.getString(COL_MOVIE_TITLE);

        final String movieID = data.getString(COL_MOVIE_ID);

        final String movieOverView =
                data.getString(COL_OVERVIEW);

        //boolean isMetric = Utility.isMetric(getActivity());

        final String movieReleaseDate = data.getString(COL_RELEASE_DATE);

        final String movieTagline = data.getString(COL_TAGLINE);

        final Integer movieRuntime = data.getInt(COL_RUNTIME);

        final String movieThumbnail = data.getString(COL_POSTER_PATH);

        final String movieBackdropImg = data.getString(COL_BACKDROP_IMG);

        final String movieHomePage = data.getString(COL_HOMEPAGE);

        final String movieRating = Utility.formatRating(
                data.getDouble(COL_VOTE_AVERAGE));

        final String moviePopularity = Utility.formatRating(
                data.getDouble(COL_POPULARITY));

        mMovieStr = String.format("%s   %s   %s/%s", movieTitleString, movieOverView, movieRating, moviePopularity);
        //Log.d(LOG_TAG, data.getString(COL_OVERVIEW));


        // Set the title
        TextView detailMovieTitleTextView = (TextView) getView().findViewById(R.id.detail_movie_title);
        detailMovieTitleTextView.setText(movieTitleString);
        // Set the release date
        TextView detailMovieReleaseDateView = (TextView) getView().findViewById(R.id.detail_movie_releasedate);
        if (movieReleaseDate.length() > 0 && !movieReleaseDate.equals("null")) {
            detailMovieReleaseDateView.setText(movieReleaseDate);
        }
        // Set the runtime
        TextView detailMovieRuntimeView = (TextView) getView().findViewById(R.id.detail_movie_runtime);
        if (movieRuntime != null && movieRuntime != 0) {
            detailMovieRuntimeView.setText(movieRuntime + " min");
        }

        // Set the Rating
        TextView detailMovieRatingView = (TextView) getView().findViewById(R.id.detail_movie_rating);
        if (movieRating.length() > 0 && !movieRating.equals("null")) {
            detailMovieRatingView.setText(movieRating);
        }

        // Set the tagline
        TextView detailMovieTaglineView = (TextView) getView().findViewById(R.id.detail_movie_tagline);
        //Make the check for null string.
        if (movieTagline.length() > 0 && !movieTagline.equals("null")) {
            detailMovieTaglineView.setText(movieTagline);
        }
        // Set the overview
        TextView detailMovieOverView = (TextView) getView().findViewById(R.id.detail_movie_overview);
        detailMovieOverView.setText(movieOverView);

        // Set the thumbnail
        ImageView movieThumbnailView = (ImageView) getView().findViewById(R.id.detail_movie_thumbnail);
        Picasso.with(getActivity()).load(movieThumbnail).placeholder(R.drawable.blank_thumbnail)
                .fit().centerInside().into(movieThumbnailView);
        // Set the backdrop image
        ImageView movieBackdropView = (ImageView) getView().findViewById(R.id.detail_movie_backdrop);
        Picasso.with(getActivity()).load(movieBackdropImg).placeholder(R.drawable.blank_thumbnail)
                .fit().centerInside().into(movieBackdropView);

        FetchVideosTask FetchVideo= new FetchVideosTask(getActivity(),mVideoAdapter);
        FetchVideo.execute(data.getString(COL_MOVIE_ID));

        TextView ReviewsButton = (TextView)getView().findViewById(R.id.reviews_button);

        // Add an onClick listener to force fetch the reviews.
        // Also prevents the app from crashing.
        ReviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Reviews textview clicked.");
                FetchReviewsTask FetchReview= new FetchReviewsTask(getActivity(),mReviewsAdapter);
                FetchReview.execute(data.getString(COL_MOVIE_ID));
            }
        });


        FetchReviewsTask FetchReview= new FetchReviewsTask(getActivity(),mReviewsAdapter);
        FetchReview.execute(data.getString(COL_MOVIE_ID));

        mFavoriteButton = (Button) getView().findViewById(R.id.favorite_button);



        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Favorite button clicked");

                long sortorderID = addsortOrder("favorite");
                Vector<ContentValues> cVVector = new Vector<ContentValues>(1);

                String movie_title = movieTitleString;
                String movie_id = movieID;
                String movie_tagline =movieTagline;
                String movie_overview = movieOverView;
                String movie_thumbnail = movieThumbnail;
                String movie_backdrop = movieBackdropImg;
                int movie_runtime = movieRuntime;
                String movie_homepage = movieHomePage;
                String movie_popularity = moviePopularity;
                String movie_rating = movieRating;
                String movie_release_date = movieReleaseDate;

                ContentValues moviesValues = new ContentValues();
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_SORT_KEY, sortorderID);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movie_title);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TAGLINE,movie_tagline);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie_overview);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, movie_thumbnail);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_BACKDROP_IMG, movie_backdrop);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RUNTIME, movie_runtime);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_HOMEPAGE, movie_homepage);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, movie_popularity);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie_rating);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie_release_date);

                Log.d(LOG_TAG, movie_title);

                cVVector.add(moviesValues);
                // add to database
                if (cVVector.size() > 0) {
                    //Log.d(LOG_TAG, String.valueOf(cVVector.size()));
                    // Student: call bulkInsert to add the weatherEntries to the database here
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    getActivity().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);
                }
            }
        });


        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActioProvider != null) {
            mShareActioProvider.setShareIntent(createShareMovieIntent());
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    long addsortOrder(String sortSetting) {

        long sortRowId;

        // Students: First, check if the location with this city name exists in the db
        Cursor sortCursor = getActivity().getContentResolver().query(
                MoviesContract.SortEntry.CONTENT_URI,
                new String[]{MoviesContract.SortEntry._ID},
                MoviesContract.SortEntry.COLUMN_SORT_SETTING + " = ?",
                new String[]{sortSetting},
                null);
        if (sortCursor.moveToFirst()) {
            int locationIdIndex = sortCursor.getColumnIndex(MoviesContract.SortEntry._ID);
            sortRowId = sortCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues sortValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            //sortValues.put(MoviesContract.SortEntry.COLUMN_CITY_NAME, cityName);
            sortValues.put(MoviesContract.SortEntry.COLUMN_SORT_SETTING, sortSetting);
            //sortValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            //sortValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            //sortValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            //sortValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = getActivity().getContentResolver().insert(
                    MoviesContract.SortEntry.CONTENT_URI,
                    sortValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            sortRowId = ContentUris.parseId(insertedUri);
        }
        sortCursor.close();
        // Wait, that worked?  Yes!
        return sortRowId;
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
    }

}