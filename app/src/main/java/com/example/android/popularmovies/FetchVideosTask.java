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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.android.popularmovies.Adapters.VideosRecyclerAdapter;
import com.example.android.popularmovies.ObjectModel.Videos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FetchVideosTask extends AsyncTask<String, Void, ArrayList<Videos>> {

    private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

    private VideosRecyclerAdapter mVideosAdapter;

    //private ArrayAdapter<String> mVideosAdapter;
    private final Context mContext;

    String movieID;
    String itemToReturn;

    public FetchVideosTask(Context context,VideosRecyclerAdapter videoadapter) {
        mContext = context;
        mVideosAdapter = videoadapter;
        //mAdapter = moviesAdapter;
    }

    private boolean DEBUG = true;


    /**
     * Prepare the movie rating for presentation.
     * @param userrating
     */
    /*private String formatRating(Double userrating) {
        return Double.toString(userrating) + "/" + ratingMax;
    }
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }*/


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param sortSetting The location string used to request updates from the server.
     * @return the row ID of the added location.
     */

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
    //Udacity 4C Loaders
    /*PopularMovieGridItem[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        //String[] resultStrs = new String[cvv.size()];
        PopularMovieGridItem[] resultMovies = new PopularMovieGridItem[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues moviesValues = cvv.elementAt(i);
            String highAndLow = formatRating(
                    moviesValues.getAsDouble(MoviesEntry.COLUMN_POPULARITY));
            resultMovies[i] = new PopularMovieGridItem();
            resultMovies[i].setmThumbnail(moviesValues.getAsString(MoviesEntry.COLUMN_POSTER_PATH));
            resultMovies[i].setmName(moviesValues.getAsString(MoviesEntry.COLUMN_MOVIE_TITLE));
            //resultStrs[i] =
            //        moviesValues.getAsString(MoviesEntry.COLUMN_RELEASE_DATE) +
            //        " - " + moviesValues.getAsString(MoviesEntry.COLUMN_OVERVIEW) +
            //        " - " + highAndLow;

        }
        return resultMovies;
    }*/

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private ArrayList<Videos> getVideosReviewDataFromJson(String moviesdataJsonStr)
            throws JSONException {

        // The ArrayList of Videos to be returned.
        ArrayList<Videos> videoToReturn = new ArrayList<>();

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        //final String OWM_CITY = "city";
        //final String OWM_CITY_NAME = "name";

        // These are the names of the JSON objects that need to be extracted.
        final String MDB_RESULTS = "results";

        final String MDB_ID = "id";

        // For videos
        final String MDB_KEY = "key";
        final String MDB_NAME = "name";
        final String MDB_SITE = "site";
        final String MDB_SIZE = "size";
        final String MDB_TYPE = "type";


        try {
            JSONObject moviesdataJson = new JSONObject(moviesdataJsonStr);
            JSONArray movieArray = moviesdataJson.getJSONArray(MDB_RESULTS);

            //JSONObject cityJson = moviesdataJson.getJSONObject(OWM_CITY);
            //String cityName = cityJson.getString(OWM_CITY_NAME);
            //JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            //double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            //double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            //long sortorderID = addsortOrder(sortSetting);

            // Insert the new weather information into the database
            //Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            //String[] resultStrs = new String[movieArray.length()];
            //PopularMovieGridItem[] resultMovies = new PopularMovieGridItem[movieArray.length()];
            // we start at the day returned by local time. Otherwise this is a mess.
            //int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            //dayTime = new Time();


            for (int i = 0; i < movieArray.length(); i++) {
                String videoId;
                String videoKey;
                String videoName;
                String videoSite;
                String videoSize;
                String videoType;

                // Get the JSON object representing the video
                JSONObject movieObject = movieArray.getJSONObject(i);

                videoId = movieObject.getString(MDB_ID);
                videoKey = movieObject.getString(MDB_KEY);
                videoName = movieObject.getString(MDB_NAME);
                videoSite = movieObject.getString(MDB_SITE);
                videoSize = movieObject.getString(MDB_SIZE);
                videoType = movieObject.getString(MDB_TYPE);
                Log.d(LOG_TAG, videoName);
                Videos newVideo = new Videos(videoId,videoKey,
                        videoName,videoSite,videoSize,videoType);
                videoToReturn.add(newVideo);
                //Log.d(LOG_TAG, videoId);
            }
            return videoToReturn;
            /*// Sort order:  Ascending, by date.
            // String sortOrder = MoviesEntry.COLUMN_VOTE_AVERAGE + " ASC";
             //Uri weatherForLocationUri = MoviesEntry.buildWeatherLocationWithStartDate(
             Uri moviesWithSortOrderUri = MoviesEntry.buildMoviesSortorder(sortSetting);

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert

            //Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
            //        null, null, null, sortOrder);
            Cursor cur = mContext.getContentResolver().query(moviesWithSortOrderUri,
                    null,
                    null,
                    null,
                    null
            );
            Log.d(LOG_TAG, String.valueOf(cur.getCount()));

            cVVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() )
            {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cVVector.add(cv);
                } while (cur.moveToNext());
            }*/

            //Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");
            //Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

            //String[] resultStrs = convertContentValuesToUXFormat(cVVector);
            //PopularMovieGridItem[] resultMovies = convertContentValuesToUXFormat(cVVector);
            /*for (PopularMovieGridItem movieItemStr : resultMovies)
            {
                Log.d(LOG_TAG, movieItemStr.getmName());
            }*/


            //return resultMovies;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected ArrayList<Videos> doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String movieId = params[0];

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
                    "http://api.themoviedb.org/3/movie/";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL + movieId + "/" + "videos").buildUpon()
                    .appendQueryParameter(KEY_PARAM, Constans.MOVIEDB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            //Log.d(LOG_TAG, String.valueOf(url));

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
            ArrayList<Videos> videosToReturn =  getVideosReviewDataFromJson(moviedbJsonStr);
            return videosToReturn;
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

        /*try {
            return getMoviesDataFromJson(moviedbJsonStr, sortQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.*/
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Videos> result) {
        if (result != null && mVideosAdapter != null) {
            mVideosAdapter.clear();
            for(Videos videoObject : result) {
                mVideosAdapter.add(videoObject);
            }
            // New data is back from the server.  Hooray!
        }
    }
    /*@Override
    protected void onPostExecute(PopularMovieGridItem[] results)
    {
        if (results != null && mAdapter != null)
        {
            //for (int i = 0; i < mAdapter.getItemCount(); i++)
            //{
                mAdapter.clearAll();
                    for (PopularMovieGridItem movieItemObj : results)
                    {
                        PopularMovieGridItem newMovie = new PopularMovieGridItem();
                        newMovie.setmName(movieItemObj.getmName());
                        newMovie.setmThumbnail(movieItemObj.getThumbnail());
                        mAdapter.add(newMovie);
                    }
                //Log.d(LOG_TAG, " Results size: " + results.length);
                //Log.d(LOG_TAG, results.toString());
               //for (int y = 0; y < results.length; y++)
               //{
               //    PopularMovieGridItem newMovie = new PopularMovieGridItem();
               //    newMovie.setmName(results[y].getmName());
               //    newMovie.setmThumbnail(results[y].getThumbnail());
               //    mAdapter.add(newMovie);
               //}
            //}
        }
    }*/
}