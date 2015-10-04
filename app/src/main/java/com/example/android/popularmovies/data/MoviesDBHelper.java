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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.MoviesContract.SortEntry;
import com.example.android.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Manages a local database for movies data.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create table to hold sort orders. The sort order consists of the
        // string supplied in the sortorder setting.
        final String SQL_CREATE_SORT_TABLE = "CREATE TABLE " + SortEntry.TABLE_NAME + " (" +
                SortEntry._ID  + " INTEGER PRIMARY KEY, " +
                SortEntry.COLUMN_SORT_SETTING + " TEXT UNIQUE NOT NULL " +
                " );";

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the sort entry associated with this movie data
                MoviesEntry.COLUMN_SORT_KEY + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL ," +
                MoviesEntry.COLUMN_MOVIE_TAGLINE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_RUNTIME + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_BACKDROP_IMG + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_HOMEPAGE + " TEXT NOT NULL, " +

                MoviesEntry.COLUMN_POPULARITY + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_VOTE_AVERAGE + " INTEGER NOT NULL, " +

                MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +


                // Set up the sort column as a foreign key to location table.
                " FOREIGN KEY (" + MoviesEntry.COLUMN_SORT_KEY + ") REFERENCES " +
                SortEntry.TABLE_NAME + " (" + SortEntry._ID + "), " +

                // To assure the application have just one movie entry from the database
                // per sort order, it's created a UNIQUE constraint with REPLACE strategy
                // However, now that we want to add movies to the Favourites
                // sort order the DBHelper has to allow duplicates.
                " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ", " +
                MoviesEntry.COLUMN_SORT_KEY + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SORT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SortEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
