package com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.ImageView;

import com.example.android.popularmovies.Constans;
import com.example.android.popularmovies.MainActivity;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

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

public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIES_NOTIFICATION_ID = 3004;

    private static final String[] DISCOVER_MOVIES_COLUMNS = new String[]{
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH
    };
    // These indices are tied to DISCOVER_MOVIES_COLUMNS.  If DISCOVER_MOVIES_COLUMNS changes, these
    // must change.
    //static final int COL_MOVIES_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_POSTER_PATH = 2;


    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        String sortQuery = Utility.getPreferredSortOrder(getContext());
        if (sortQuery == "favorite") {
            long sortorderID = addsortOrder(sortQuery);
            return;
        }

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

    private JSONObject fetchAdditionalMovieData(int movieId) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviedbextraJsonStr = null;
        JSONObject moviesdataJson = new JSONObject();

        try {
            // Construct the URL for the MovieDBApi query
            // Possible parameters are avaiable at MDB page API page, at
            // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
            final String MOVIE_EXTRA_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_EXTRA_BASE_URL + String.valueOf(movieId)).buildUpon()
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

            moviedbextraJsonStr = buffer.toString();
            moviesdataJson = new JSONObject(moviedbextraJsonStr);

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
        return moviesdataJson;
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

        final String MDB_EXTRA_BACKDROP_IMG = "backdrop_path";
        final String MDB_EXTRA_HOMEPAGE = "homepage";
        final String MDB_EXTRA_RUNTIME = "runtime";
        final String MDB_EXTRA_TAGLINE = "tagline";

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

                // The title, release date and the movie ID.
                String movie_title;
                String movie_release_date;
                int movie_id;

                //The runtime and homepage.
                String movie_runtime;
                String movie_homepage;

                // The tagline and overview.
                String movie_overview;
                String movie_tagline;

                // Popularity and rating.
                double movie_popularity;
                double movie_rating;


                String baseImgURL = "http://image.tmdb.org/t/p/";
                // TODO: optimize the target image size
                // TODO: Lesson 5- Video 19 "Adding images to the app"
                String sizeImg = "w185//";
                String sizeBackdrop = "w500";
                // The strings for the thumbnail and backdrop images
                String movie_thumbnail_base;
                String movie_thumbnail;

                String movie_backdrop_base;
                String movie_backdrop;

                //String description;
                //int weatherId;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);


                movie_title = movieObject.getString(MDB_TITLE);
                movie_release_date = movieObject.getString(MDB_RELEASE_DATE);
                movie_id = movieObject.getInt(MDB_ID);

                // Fetch the additional data for the movie as we now have the movieID.
                JSONObject movieExtra = fetchAdditionalMovieData(movie_id);

                //Grag the runtime and homepage.
                movie_runtime = movieExtra.getString(MDB_EXTRA_RUNTIME);

                movie_homepage = movieExtra.getString(MDB_EXTRA_HOMEPAGE);


                // Grab the tagline and overview.
                movie_tagline = movieExtra.getString(MDB_EXTRA_TAGLINE);
                movie_overview = movieObject.getString(MDB_OVERVIEW);

                // Grab the popularity and rating.
                movie_popularity = movieObject.getDouble(MDB_POPULARITY);
                movie_rating = movieObject.getDouble(MDB_VOTE_AVARAGE);

                // Grab the thumbnail base and create the thumbnail URL page.
                movie_thumbnail_base = movieObject.getString(MDB_POSTER);
                movie_thumbnail = baseImgURL + sizeImg + movie_thumbnail_base;

                // Grab the backdrop base and create the backdrop URL page.
                movie_backdrop_base = movieExtra.getString(MDB_EXTRA_BACKDROP_IMG);
                movie_backdrop = baseImgURL + sizeBackdrop + movie_backdrop_base;

                ContentValues moviesValues = new ContentValues();

                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_SORT_KEY, sortorderID);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movie_title);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TAGLINE, movie_tagline);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, movie_overview);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, movie_thumbnail);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_BACKDROP_IMG, movie_backdrop);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RUNTIME, movie_runtime);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_HOMEPAGE, movie_homepage);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, movie_popularity);
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, Utility.formatRating(movie_rating));
                moviesValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, movie_release_date);
                cVVector.add(moviesValues);
            }
            int inserted = 0;

            // add to database
            if (cVVector.size() > 0) {

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);


                inserted = getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);
                notifyMovies();
            }

            //Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyMovies() throws IOException {

        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String sortQuery = Utility.getPreferredSortOrder(context);

            Uri moviesUri = MoviesContract.MoviesEntry.buildMoviesSortorder(sortQuery);




            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(moviesUri, DISCOVER_MOVIES_COLUMNS, null, null, null);

            if (cursor.moveToFirst()) {
                String movietitle = cursor.getString(COL_MOVIE_TITLE);
                String moviePoster = cursor.getString(COL_POSTER_PATH);

                String title = context.getString(R.string.app_name);


                //build your notification here.
                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.notification_template_icon_bg)
                                .setContentTitle(title)
                                .setContentText(movietitle);

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                mNotificationManager.notify(MOVIES_NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
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
        Cursor sortCursor = getContext().getContentResolver().query(
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
            Uri insertedUri = getContext().getContentResolver().insert(
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

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
}