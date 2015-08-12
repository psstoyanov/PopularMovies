/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.android.popularmovies.data.MoviesContract.MoviesEntry;

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

public class FetchMovieTask extends AsyncTask<String, Void, PopularMovieGridItem[]>
{

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final String ratingMax = "10.0";

    private GridAdapter mAdapter;

    private ArrayAdapter<String> mMoviesAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context, GridAdapter moviesAdapter) {
        mContext = context;
        mAdapter = moviesAdapter;
    }

    private boolean DEBUG = true;



    /**
     * Prepare the movie rating for presentation.
     */
    private String formatRating(Float userrating) {


        return Float.toString(userrating) + "/" + ratingMax;
    }


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @return the row ID of the added location.
     */
    long addsortOrder(String locationSetting) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        return -1;
    }

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
    /*String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues moviesValues = cvv.elementAt(i);
            String highAndLow = formatHighLows(
                    moviesValues.getAsDouble(MoviesEntry.COLUMN_MAX_TEMP),
                    moviesValues.getAsDouble(MoviesEntry.COLUMN_MIN_TEMP));
            resultStrs[i] = getReadableDateString(
                    moviesValues.getAsLong(MoviesEntry.COLUMN_DATE)) +
                    " - " + moviesValues.getAsString(MoviesEntry.COLUMN_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }*/

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private PopularMovieGridItem[] getMoviesDataFromJson(String moviesdataJsonStr,
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

            String sortorderID = (sortSetting);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            String[] resultStrs = new String[movieArray.length()];
            PopularMovieGridItem[] resultMovies = new PopularMovieGridItem[movieArray.length()];
            // we start at the day returned by local time. Otherwise this is a mess.
            //int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            //dayTime = new Time();

            for(int i = 0; i < movieArray.length(); i++) {
                // These are the values that will be collected.
                String movie_title;
                String movie_release_date;
                int movie_id;

                String movie_overview;

                double movie_popularity;
                double movie_rating;


                String baseImgURL = "http://image.tmdb.org/t/p/";
                String sizeImg = "w185//";
                String movie_thumbnail_base;
                String movie_thumbnail;


                String description;
                int weatherId;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);

                /*// Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);*/

                /*pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);*/

                // Description is in a child array called "weather", which is 1 element long.
                // That element also contains a weather code.
                /*JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);*/

                /*// Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);*/

                movie_title = movieObject.getString(MDB_TITLE);
                movie_release_date = movieObject.getString(MDB_RELEASE_DATE);
                movie_id = movieObject.getInt(MDB_ID);

                movie_overview = movieObject.getString(MDB_OVERVIEW);

                movie_popularity = movieObject.getDouble(MDB_POPULARITY);
                movie_rating = movieObject.getDouble(MDB_VOTE_AVARAGE);

                movie_thumbnail_base = movieObject.getString(MDB_POSTER);
                movie_thumbnail = baseImgURL + sizeImg + movie_thumbnail_base;



                ContentValues moviesValues = new ContentValues();

                moviesValues.put(MoviesEntry.COLUMN_SORT_KEY, sortorderID);
                moviesValues.put(MoviesEntry.COLUMN_MOVIE_TITLE,movie_title);
                moviesValues.put(MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                moviesValues.put(MoviesEntry.COLUMN_OVERVIEW, movie_overview);
                moviesValues.put(MoviesEntry.COLUMN_POSTER_PATH, movie_thumbnail);
                moviesValues.put(MoviesEntry.COLUMN_POPULARITY, movie_popularity);
                moviesValues.put(MoviesEntry.COLUMN_VOTE_AVERAGE, formatRating((float) movie_rating));
                moviesValues.put(MoviesEntry.COLUMN_RELEASE_DATE, movie_release_date);
                cVVector.add(moviesValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
            }

            // Sort order:  Ascending, by date.
           // String sortOrder = MoviesEntry.COLUMN_DATE + " ASC";
           // Uri weatherForLocationUri = MoviesEntry.buildWeatherLocationWithStartDate(
           // locationSetting, System.currentTimeMillis());

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert

//            Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
//                    null, null, null, sortOrder);
//
//            cVVector = new Vector<ContentValues>(cur.getCount());
//            if ( cur.moveToFirst() ) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    DatabaseUtils.cursorRowToContentValues(cur, cv);
//                    cVVector.add(cv);
//                } while (cur.moveToNext());
//            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");

            //String[] resultStrs = convertContentValuesToUXFormat(cVVector);
            return resultMovies;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected PopularMovieGridItem[] doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String sortQuery = params[0];
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


        String format = "json";
        String units = "metric";
        int numDays = 14;

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
            Log.e(LOG_TAG, "Error ", e);
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
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMoviesDataFromJson(moviedbJsonStr, sortQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    /*@Override
    protected void onPostExecute(String[] result) {
        if (result != null && mMoviesAdapter != null) {
            mMoviesAdapter.clear();
            for(String dayForecastStr : result) {
                mMoviesAdapter.add(dayForecastStr);
            }
            // New data is back from the server.  Hooray!
        }
    }*/
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
}