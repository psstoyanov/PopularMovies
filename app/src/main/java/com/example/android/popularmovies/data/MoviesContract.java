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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract
{
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.popularmovies/weather/ is a valid path for
    // looking at weather data. content://com.example.android.popularmovies/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_SORT_ORDER = "sort_order";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the sort order table
        Students: This is where you will add the strings.  (Similar to what has been
        done for MovieEntry)
     */
    public static final class SortEntry implements BaseColumns {

        // Content URI representing the base location
        // for the table.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SORT_ORDER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SORT_ORDER;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SORT_ORDER;


        public static final String TABLE_NAME = "sortorder";

        // The sort order setting string will be sent to MovieDBApi
        // as a parameter.
        public static final String COLUMN_SORT_SETTING = "sort_setting";

        public static Uri buildSortUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    // : Change weather table to movies table with the appropriate entries.
    /* Inner class that defines the table contents of the weather table */
    public static final class MoviesEntry implements BaseColumns {

        // Content URI representing the base location
        // for the table.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;


        // The table name.
        public static final String TABLE_NAME = "movies";

        // Column with the foreign key into the sortorder table.
        public static final String COLUMN_SORT_KEY = "sortorder_id";

        // Movies id as returned by API
        public static final String COLUMN_MOVIE_ID = "movies_id";

        // // The movie release date
        public static final String COLUMN_RELEASE_DATE = "date";
        // Movies title as returned by API
        public static final String COLUMN_MOVIE_TITLE = "movies_title";

        // Movie overview as provided by API.
        public static final String COLUMN_OVERVIEW = "movie_overview";

        // The movie rating and popularity (stored as floats)
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_AVERAGE = "rating";

        // Movie thumbnail poster url
        public static final String COLUMN_POSTER_PATH = "thumbnail";

        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /*
            Student: Fill in this buildMoviesSortorder function
         */
        public static Uri buildMoviesSortorder(String sortSetting)
        {
            // Append path segment for the sort order to the movie URI
            return CONTENT_URI.buildUpon().appendPath(sortSetting).build();
        }

        public static Uri buildMoviesSortordernWithSpecificMovieID(
                String sortSetting, long movieId) {
            //long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon().appendPath(sortSetting)
                    .appendQueryParameter(COLUMN_MOVIE_ID, Long.toString(movieId)).build();
        }

        public static Uri buildMovieSortOrderWithMovieID(String sortorderSetting, long movieID) {
            return CONTENT_URI.buildUpon().appendPath(sortorderSetting)
                    .appendPath(Long.toString(movieID)).build();
        }

        public static String getSortSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getMovieIDFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getTheMovieIDFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_MOVIE_ID);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }

    }
}
