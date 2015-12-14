package com.jowalski.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    private void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                MoviesContract.ReviewEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            assertEquals("Error: Records not deleted from Movie table during delete", 0,
                    cursor.getCount());
            cursor.close();
        } else {
            assertTrue("Error: Cursor is null.", false);
        }

        cursor = mContext.getContentResolver().query(
                MoviesContract.ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            assertEquals("Error: Records not deleted from Review table during delete", 0,
                    cursor.getCount());
            cursor.close();
        } else {
            assertTrue("Error: Cursor is null.", false);
        }

    }

    private void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testBasicReviewQuery() {
        // insert our test records into the database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues reviewValues = TestUtilities.createReviewValues();

        long reviewRowId = db.insert(MoviesContract.ReviewEntry.TABLE_REVIEWS,
                null, reviewValues);
        assertTrue("Unable to Insert Review Entry into the Database", reviewRowId != -1);
        db.close();

        // Test the basic content provider query
        Cursor reviewCursor = mContext.getContentResolver().query(
                MoviesContract.ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicReviewQuery", reviewCursor, reviewValues);
    }

    public void testBasicMovieQuery() {
        // insert our test records into the database
        MoviesDBHelper dbHelper = new MoviesDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        db.insert(MoviesContract.MovieEntry.TABLE_MOVIES, null, testValues);

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, testValues);
    }

    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createMovieValues();

        Uri movieUri = mContext.getContentResolver()
                .insert(MoviesContract.MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MoviesContract.MovieEntry.COLUMN_TMDB_ID, movieRowId);
        updatedValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Breathless");
        updatedValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "1961-02-07");

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, tco);

        int count = mContext.getContentResolver().update(
                MoviesContract.MovieEntry.CONTENT_URI,
                updatedValues,
                MoviesContract.MovieEntry.COLUMN_TMDB_ID + "= ?",
                new String[]{Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                MoviesContract.MovieEntry.COLUMN_TMDB_ID + " = " + movieRowId,
                null,
                null
        );

        TestUtilities.validateCursor("testUpdateMovie.  Error validating movie entry update.",
                cursor, updatedValues);
        if (cursor != null) {
            cursor.close();
        } else {
        assertTrue("Error: Cursor is null.", false);
        }
    }

    public void testInsertReadProvider() {
        ContentValues movieValues = TestUtilities.createMovieValues();

        // Register a content observer for our insert.
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, movieValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, movieValues);

        ContentValues reviewValues = TestUtilities.createReviewValues();
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MoviesContract.ReviewEntry.CONTENT_URI, true, tco);

        Uri reviewInsertUri = mContext.getContentResolver()
                .insert(MoviesContract.ReviewEntry.CONTENT_URI, reviewValues);
        assertTrue(reviewInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor reviewCursor = mContext.getContentResolver().query(
                MoviesContract.ReviewEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ReviewEntry insert.",
                reviewCursor, reviewValues);

        // Add the movie values in with the review data so that we can make
        // sure that the join worked and we actually get all the values back
        reviewValues.putAll(movieValues);

        reviewCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.buildMovieWReviewsUri(movieRowId),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Review and Movie Data.",
                reviewCursor, reviewValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, movieObserver);

        TestUtilities.TestContentObserver reviewObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.ReviewEntry.CONTENT_URI, true, reviewObserver);

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        reviewObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
        mContext.getContentResolver().unregisterContentObserver(reviewObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    private static ContentValues[] createBulkInsertMovieValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TMDB_ID, 8382 + i);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ICON, 1324 - i);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIG_TITLE,
                    "Vivre sa vie " + Integer.toString(i));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW,
                    "In director Jean-Luc Godard's landmark drama, Nana (Anna Karina), " +
                            "a young Parisian woman who works in a record shop, finds herself " +
                            "disillusioned by poverty and a crumbling marriage. Hoping to become " +
                            "an actress and break into films, Nana is once again disappointed " +
                            "when nothing comes of her dreams...");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "1962-09-20");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
                    "http://static.rogerebert.com/uploads/review/primary_image/reviews/great-movie-vivre-sa-vie--my-life-to-live-1963/hero_EB20010401REVIEWS08104010301AR.jpg");
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 5.2);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE,
                    "Vivre sa vie " + Integer.toString(i));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.2);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, 38254 - i);

            returnContentValues[i] = movieValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
//        ContentValues testValues = TestUtilities.createMovieValues();
//        Uri movieUri = mContext.getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, testValues);
//        long movieRowId = ContentUris.parseId(movieUri);
//
//        assertTrue(movieRowId != -1);
//
//        Cursor cursor = mContext.getContentResolver().query(
//                MoviesContract.MovieEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null
//        );
//
//        TestUtilities.validateCursor("testBulkInsert. Error validating MovieEntry.",
//                cursor, testValues);

        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                MoviesContract.MovieEntry.COLUMN_TMDB_ID // sort order, by TMDB_ID descending
        );

        if (cursor != null) {
            assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);
            cursor.moveToFirst();

            for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
                TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
                        cursor, bulkInsertContentValues[i]);
            }
            cursor.close();
        } else {
            assertTrue("Error: Cursor is null.", false);
        }
    }
}