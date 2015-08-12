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

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract {

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
        Inner class that defines the table contents of the location table
        Students: This is where you will add the strings.  (Similar to what has been
        done for MovieEntry)
     */
    public static final class SortEntry implements BaseColumns {
        public static final String TABLE_NAME = "sortorder";

        // The sort order setting string will be sent to MovieDBApi
        // as a parameter.
        public static final String COLUMN_SORT_SETTING = "sort_setting";

    }

    // : Change weather table to movies table with the appropriate entries.
    /* Inner class that defines the table contents of the weather table */
    public static final class MoviesEntry implements BaseColumns {

        // The table name.
        public static final String TABLE_NAME = "movies";

        // Column with the foreign key into the sortorder table.
        public static final String COLUMN_SORT_KEY = "sortorder_id";
        // // The movie release date
        public static final String COLUMN_RELEASE_DATE = "date";

        // Movies id as returned by API
        public static final String COLUMN_MOVIE_ID = "movies_id";

        // Movie overview as provided by API.
        public static final String COLUMN_OVERVIEW = "movie_overview";

        // The movie rating and popularity (stored as floats)
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_AVERAGE = "rating";

        // Movie thumbnail poster url
        public static final String COLUMN_POSTER_PATH = "thumbnail";

    }
}
