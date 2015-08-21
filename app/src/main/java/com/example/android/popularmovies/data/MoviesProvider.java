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
package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIES_WITH_SORT_ORDER = 101;
    static final int MOVIES_WITH_SORT_ORDER_AND_DATE = 102;
    static final int SORT_ORDER = 300;

    // This class helps construct queries and is used as the basis
    // the query function that is already used in the
    // SQLite database class.
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        // Initialize the SQLiteQueryBuilder in the static constructor
        // of the class, describing the join between both tables.
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies INNER JOIN sortorder ON movies.sortorder_id = sortorder._id

        // SetTables fills out the content in the form part of the SQL query.
        // Note, since both tables have a field with an _ID,
        // we must explicitly use the table name in order to disambiguate
        // which _ID we are talking about.
        sWeatherByLocationSettingQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.SortEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry.COLUMN_SORT_KEY +
                        " = " + MoviesContract.SortEntry.TABLE_NAME +
                        "." + MoviesContract.SortEntry._ID);
    }

    // Then we define the selection.
    // The selection is defined using the question mark replacement syntax.
    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            MoviesContract.SortEntry.TABLE_NAME +
                    "." + MoviesContract.SortEntry.COLUMN_SORT_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            MoviesContract.SortEntry.TABLE_NAME +
                    "." + MoviesContract.SortEntry.COLUMN_SORT_SETTING + " = ? AND " +
                    MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            MoviesContract.SortEntry.TABLE_NAME +
                    "." + MoviesContract.SortEntry.COLUMN_SORT_SETTING + " = ? AND " +
                    MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " = ? ";

    // A function to get the movies by sort setting using the same query builder.
    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = MoviesContract.MoviesEntry.getSortSettingFromUri(uri);
        String startDate = MoviesContract.MoviesEntry.getTheMovieIDFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (Integer.parseInt(startDate) == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // A function to get the movies by sort setting using the same query builder.
    // We do that for both the queries we're creating this way.
    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = MoviesContract.MoviesEntry.getSortSettingFromUri(uri);
        String date = MoviesContract.MoviesEntry.getMovieIDFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, date},
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the MOVIES, MOVIES_WITH_SORT_ORDER, MOVIES_WITH_SORT_ORDER_AND_DATE,
        and SORT_ORDER integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.

        // Why create a UriMatcher when you can use regular
        // expression instead? Because it is stupid...

        // All paths added to the UriMatcher have a corresponding code to return
        // when a match is found. The code passed into the constructor represents the code
        // to return for the root URI. It's common to use NO_MATCH as the code for
        // this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // For readability, this is a shortcut to the
        // MoviesContract.CONTENT_AUTHORITY.
        final String authority = MoviesContract.CONTENT_AUTHORITY;


        // 2) Use the addURI function to match each of the types.  Use the constants from
        // MoviesContract to help define the types to the UriMatcher.

        // For each type of URI we want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*", MOVIES_WITH_SORT_ORDER);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*/*", MOVIES_WITH_SORT_ORDER_AND_DATE);


        // 3) Return the new matcher!
        matcher.addURI(authority, MoviesContract.PATH_SORT_ORDER, SORT_ORDER);
        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new MoviesDBHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIES_WITH_SORT_ORDER_AND_DATE:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_SORT_ORDER:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case SORT_ORDER:
                return MoviesContract.SortEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // Finaly, added the functions into the query routine of the content provider,
            // for both MOVIES_WITH_SORT_ORDER_AND_DATE  and MOVIES_WITH_SORT_ORDER.
            // "weather/*/*"
            case MOVIES_WITH_SORT_ORDER_AND_DATE: {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case MOVIES_WITH_SORT_ORDER: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                //retCursor = null;
                break;
            }
            // "location"
            case SORT_ORDER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.SortEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                //retCursor = null;
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            // For movies, we just passed the parameters that came into the
            // content provider into the data base insert call.
            case MOVIES: {
                // normalizeDate(values);
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    // Throw an exception if the insert fails.
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SORT_ORDER: {
                long _id = db.insert(MoviesContract.SortEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.SortEntry.buildSortUri(_id);
                else //Throw an exception if the insert fails.
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // This makes delete all rows return the number of rows deleted.
        if (null == selection) selection = "1";
        switch (match) {
            // Student: Use the uriMatcher to match the MOVIES and SORT_ORDER URI's we are going to
            // handle.  If it doesn't match these, throw an UnsupportedOperationException.
            case MOVIES:
                rowsDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SORT_ORDER:
                rowsDeleted = db.delete(MoviesContract.SortEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
            // Student: A null value deletes all rows.  In my implementation of this, I only notified
            // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
            // is null.
        }
        // Oh, and you should notify the listeners here.
        if ( rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        // Student: return the actual rows deleted
        return rowsDeleted;
    }

    /*private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE)) {
            long dateValue = values.getAsLong(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE);
            values.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, MoviesContract.normalizeDate(dateValue));
        }
    }*/

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SORT_ORDER:
                rowsUpdated = db.update(MoviesContract.SortEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Student: return the actual rows deleted
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        //normalizeDate(value);
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}