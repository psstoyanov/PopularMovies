package com.example.android.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    GridAdapter mAdapter;


    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
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

        mAdapter = new GridAdapter();
        mRecyclerView.setAdapter(mAdapter);



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
    private void updateMovieData()
    {
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortorder = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_order_popularity));
        movieTask.execute(sortorder);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateMovieData();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, PopularMovieGridItem[]> {

        private final String LOG_TAG = FetchMovieTask.class.getName();
        private final String ratingMax = "10.0";

        /**
         * Prepare the movie rating for presentation.
         */
        private String formatRating(Float userrating) {


            return Float.toString(userrating) + "/" + ratingMax;
        }

        /**
         * Take the String representing the complete movielis in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private PopularMovieGridItem[] getMoviesDataFromJson(String moviesdataJsonStr, int numPages)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_OVERVIEW = "overview";
            final String MDB_POSTER = "poster_path";
            final String MDB_POPULARITY = "popularity";
            final String MDB_VOTE_AVARAGE = "vote_average";
            final String MDB_ID = "id";

            JSONObject moviesdataJson = new JSONObject(moviesdataJsonStr);

            JSONArray movieArray = moviesdataJson.getJSONArray(MDB_RESULTS);

            String[] resultStrs = new String[movieArray.length()];
            PopularMovieGridItem[] resultMovies = new PopularMovieGridItem[movieArray.length()];

            Log.d(LOG_TAG, Integer.toString(movieArray.length()));
            for (int i = 0; i < movieArray.length(); i++) {
                String name;
                double rating;
                String thumbnail;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);

                name = movieObject.getString(MDB_TITLE);

                rating = movieObject.getDouble(MDB_VOTE_AVARAGE);
                thumbnail = movieObject.getString(MDB_POSTER);
                //Log.d(LOG_TAG, movieObject.getString(MDB_OVERVIEW));


                resultStrs[i] = name + " - " + thumbnail + " - " + formatRating((float) rating);

                resultMovies[i] = new PopularMovieGridItem();
                resultMovies[i].setmName(name);
                String baseImgURL = "http://image.tmdb.org/t/p/";
                String sizeImg = "w185//";
                resultMovies[i].setmThumbnail(baseImgURL + sizeImg + thumbnail);
            }
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Movie entry: " + s);
            }

            return resultMovies;

        }

        @Override
        protected void onPostExecute(PopularMovieGridItem[] results) {
            if (results != null) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    mAdapter.clearAll();
                    /*for (String movieItemStr : results)
                    {
                        PopularMovieGridItem newMovie = new PopularMovieGridItem();
                        newMovie.setmName(movieItemStr);
                        mAdapter.add(newMovie);
                    }*/
                    //Log.d(LOG_TAG, " Results size: " + results.length);
                    for (int y = 0; y < results.length; y++) {
                        PopularMovieGridItem newMovie = new PopularMovieGridItem();
                        newMovie.setmName(results[y].getmName());
                        newMovie.setmThumbnail(results[y].getThumbnail());
                        mAdapter.add(newMovie);
                    }
                }
            }
        }


        @Override
        protected PopularMovieGridItem[] doInBackground(String... params) {

            // If there is no order, there's nothing to look up. Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviedbJsonStr = null;
            String sort_desc = ".desc";
            /* By default, the first page of results will be shown.
            *  TheMovieDB API displays 20 results per page.
            *  */
            int numPages = 1;


            try {
                // Construct the URL for the MovieDBApi query
                // Possible parameters are avaiable at MDB page API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String PAGE_PARAM = "page";
                final String KEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0] + sort_desc)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(numPages))
                        .appendQueryParameter(KEY_PARAM, Constans.MOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                //URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key="
                //        + Constans.MOVIEDB_API_KEY);

                // To check wether the URL gives the expected results,
                // or to analyze the JSON URL:
                // http://jsonformatter.curiousconcept.com/
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviedbJsonStr = buffer.toString();
                Log.v(LOG_TAG, "MovieDB Json Str: " + moviedbJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviedbJsonStr, numPages);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }
}
