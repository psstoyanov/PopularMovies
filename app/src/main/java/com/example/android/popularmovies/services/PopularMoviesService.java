package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.util.Log;

import com.example.android.popularmovies.Constans;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Raz3r on 22/09/15.
 */
public class PopularMoviesService extends IntentService {

    private final String LOG_TAG = PopularMoviesService.class.getSimpleName();
    public static final String SORT_ORDER_QUERY_EXTRA = "soqe";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PopularMoviesService() {
        super("PopularMovies");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String sortQuery = intent.getStringExtra(SORT_ORDER_QUERY_EXTRA);
        String sort_desc = ".desc";
        /* By default, the first page of results will be shown.
            *  TheMovieDB API displays 20 results per page.
            *  */
        int numPages = 1;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviedbJsonStr = null;

        try {
            // Construct the URL for the MovieDBApi query
            // Possible parameters are avaiable at MDB page API page, at
            // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
            final String MOVIE_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie";
            final String SORT_PARAM = "sort_by";
            final String PAGE_PARAM = "page";
            final String KEY_PARAM = "api_key";
            final String APPEND_DURATION = "append_to_response";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortQuery + sort_desc)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(numPages))
                    .appendQueryParameter(KEY_PARAM, Constans.MOVIEDB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            // Create the request to MovieDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
                return;
            }
            moviedbJsonStr = buffer.toString();
            getMoviesDataFromJson(moviedbJsonStr, sortQuery);
            //Log.v(LOG_TAG, "MovieDB Json Str: " + moviedbJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMoviesDataFromJson(String moviesdataJsonStr,
                                       String sortSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        //final String OWM_CITY = "city";
        //final String OWM_CITY_NAME = "name";

        // These are the names of the JSON objects that need to be extracted.
        final String MDB_RESULTS = "results";
        final String MDB_TITLE = "original_title";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_OVERVIEW = "overview";
        final String MDB_POSTER = "poster_path";
        final String MDB_POPULARITY = "popularity";
        final String MDB_VOTE_AVARAGE = "vote_average";
        final String MDB_ID = "id";

        try {
            JSONObject moviesdataJson = new JSONObject(moviesdataJsonStr);
            JSONArray movieArray = moviesdataJson.getJSONArray(MDB_RESULTS);

            //JSONObject cityJson = moviesdataJson.getJSONObject(OWM_CITY);
            //String cityName = cityJson.getString(OWM_CITY_NAME);
            //JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            //double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            //double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long sortorderID = addsortOrder(sortSetting);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            //String[] resultStrs = new String[movieArray.length()];
            //PopularMovieGridItem[] resultMovies = new PopularMovieGridItem[movieArray.length()];
            // we start at the day returned by local time. Otherwise this is a mess.
            //int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            //dayTime = new Time();

            for (int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.
                String movie_title;
                String movie_release_date;
                int movie_id;

                String movie_overview;

                double movie_popularity;
                double movie_rating;


                String baseImgURL = "http://image.tmdb.org/t/p/";
                // TODO: optimize the target image size
                // TODO: Lesson 5- Video 19 "Adding images to the app"
                String sizeImg = "w185//";
                String movie_thumbnail_base;
                String movie_thumbnail;


                //String description;
                //int weatherId;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);


                movie_title = movieObject.getString(MDB_TITLE);
                movie_release_date = movieObject.getString(MDB_RELEASE_DATE);
                movie_id = movieObject.getInt(MDB_ID);
                String test_something = "http://api.themoviedb.org/3/movie/"
                        + movie_id + "?api_key=" + Constans.MOVIEDB_API_KEY
                        + "&append_to_response=videos,reviews";
                Log.d(LOG_TAG, test_something);

                movie_overview = movieObject.getString(MDB_OVERVIEW);

                movie_popularity = movieObject.getDouble(MDB_POPULARITY);
                movie_rating = movieObject.getDouble(MDB_VOTE_AVARAGE);

                movie_thumbnail_base = movieObject.getString(MDB_POSTER);
                movie_thumbnail = baseImgURL + sizeImg + movie_thumbnail_base;


                ContentValues moviesValues = new ContentValues();

                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_SORT_KEY, sortorderID);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movie_title);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie_overview);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, movie_thumbnail);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, movie_popularity);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, Utility.formatRating(movie_rating));
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie_release_date);
                cVVector.add(moviesValues);
            }
            int inserted = 0;

            // add to database
            if (cVVector.size() > 0) {
                //Log.d(LOG_TAG, String.valueOf(cVVector.size()));
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = this.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);
            }

            //Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param sortSetting The location string used to request updates from the server.
     * @return the row ID of the added location.
     */
    long addsortOrder(String sortSetting) {

        long sortRowId;

        // Students: First, check if the location with this city name exists in the db
        Cursor sortCursor = this.getContentResolver().query(
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
            Uri insertedUri = this.getContentResolver().insert(
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

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, PopularMoviesService.class);
            sendIntent.putExtra(PopularMoviesService.SORT_ORDER_QUERY_EXTRA,
                    intent.getStringExtra(PopularMoviesService.SORT_ORDER_QUERY_EXTRA));
            context.startService(sendIntent);
        }
    }

}
