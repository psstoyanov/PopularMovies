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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDBHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.SortEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.MoviesEntry.TABLE_NAME);

        mContext.deleteDatabase(MoviesDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the sort order entry and movies entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.SortEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> sortorderColumnHashSet = new HashSet<String>();
        sortorderColumnHashSet.add(MoviesContract.SortEntry._ID);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            sortorderColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                sortorderColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createTestSortOrderValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testSortOrderTable() {
        insertSortOrder();

    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createMoviesValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testMoviesTable() {
        // First insert the sort order, and then use the sortorderRowId to insert
        // the movies. Make sure to cover as many failure cases as you can.


        // Instead of rewriting all of the code we've already written in testSortOrderTable
        // we can move this code to insertSortOrder and then call insertSortOrder from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testSortOrderTable can only return void because it's a test.

        long sortorderRowId = insertSortOrder();
        // Make sure we have a valid row ID.
        assertFalse("Error: Sort Order Not Inserted Correctly", sortorderRowId == -1L);

        // First step: Get reference to writable database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second step (Movies): Create ContentValues of what you want to insert
        // (you can use the createMoviesValues TestUtilities function if you wish)
        ContentValues moviesValues = TestUtilities.createMoviesValues(sortorderRowId);

        // Third step (Movies): Insert ContentValues into database and get a row ID back
        long moviesRowId = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, moviesValues);
        assertTrue( moviesRowId != -1);

        // Fourth step (Movies): Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor movieCursor = db.query(
                MoviesContract.MoviesEntry.TABLE_NAME, // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // column to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row
        assertTrue( " Error: No Records returned from movie query", movieCursor.moveToFirst());

        // Fifth step (Movie): Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("testInsertReadDb movieEntry failed to validate",
                movieCursor, moviesValues);

        // Finally, close the cursor and database
        /*assertFalse( " Error: More than one record returned from movie query",
                movieCursor.moveToNext());*/

        movieCursor.close();
        dbHelper.close();

    }


    /*
        Students: This is a helper method for the testMoviesTable quiz. You can move your
        code from testSortOrderTable to here so that you can call this code from both
        testMoviesTable and testSortOrderTable.
     */
    public long insertSortOrder() {



        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Settings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        // Second step: Create ContentValues of what you want to insert
        // (you can use the createTestSortOrderValues if you wish)
        ContentValues testValues = TestUtilities.createTestSortOrderValues();

        // Third step: Insert ContentValues into database and get a row ID back
        long sortorderRowId;
        sortorderRowId = db.insert(MoviesContract.SortEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(sortorderRowId != -1);

        // Data is inserted. IN THEORY! Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.SortEntry.TABLE_NAME, // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // column to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query.
        assertTrue( " Error: No Records returned from sort order query", cursor.moveToFirst());

        // Fifth step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Sort Order query Validation Failed",
                cursor, testValues);
        // Move the cursor to demonstrate that there is only one record in the database.
        assertFalse(" Error: More than one record returned from sort order entry.",
                cursor.moveToNext());

        // Sixth step: Close the cursor and database.
        cursor.close();
        db.close();
        return sortorderRowId;
    }
}
