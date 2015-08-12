package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your MoviesContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_SORT_ORDER = "popularity";
    static final String TEST_ORDER = "popularity";  // By popularity.

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createMoviesValues(long sortRowId) {
        ContentValues moviesValues = new ContentValues();
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_SORT_KEY, TEST_ORDER);
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "2015");
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, 1.1);
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, "Despicable me");
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, "Asteroids");
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, 1.2);
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 1.3);
        moviesValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, "blahblah.jpg");

        return moviesValues;
    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        SortEntry part of the MoviesContract.
     */
    static ContentValues createTestSortOrderValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.SortEntry.COLUMN_SORT_SETTING, TEST_SORT_ORDER);

        return testValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        SortEntry part of the MoviesContract as well as the MoviesDBHelper.
     */
    static long insertTestSortOrderValues(Context context) {
        // insert our test records into the database
        MoviesDBHelper dbHelper = new MoviesDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTestSortOrderValues();

        long sortorderRowId;
        sortorderRowId = db.insert(MoviesContract.SortEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", sortorderRowId != -1);

        return sortorderRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
