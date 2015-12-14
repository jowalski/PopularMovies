package com.jowalski.popularmovies.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.jowalski.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Utilities for tests.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, 8382);
        movieValues.put(MovieContract.MovieEntry.COLUMN_ICON, 1324);
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIG_TITLE, "Vivre sa vie");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,
                "In director Jean-Luc Godard's landmark drama, Nana (Anna Karina), " +
                        "a young Parisian woman who works in a record shop, finds herself " +
                        "disillusioned by poverty and a crumbling marriage. Hoping to become " +
                        "an actress and break into films, Nana is once again disappointed " +
                        "when nothing comes of her dreams...");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "1962-09-20");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                "http://static.rogerebert.com/uploads/review/primary_image/reviews/great-movie-vivre-sa-vie--my-life-to-live-1963/hero_EB20010401REVIEWS08104010301AR.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 5.2);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Vivre sa vie");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.2);
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 38254);

        return movieValues;
    }

    static ContentValues createReviewValues() {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, 8382);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Roger Ebert");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT,
                "Godard. We all went to Jean-Luc Godard in the 1960s. We stood in " +
                        "the rain outside the Three Penny Cinema, waiting for the " +
                        "next showing of \"Weekend\" (1968). One year the New York " +
                        "Film Festival showed two of his movies, or was it three?");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL,
                "http://www.rogerebert.com/reviews/great-movie-vivre-sa-vie--my-life-to-live-1963");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_LAST_RESULTS_PAGE, 1);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_LAST_RESULTS_POSITION, 1);

        return reviewValues;
    }

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
            assertEquals("Column '" + columnName + "': Value '" +
                    valueCursor.getString(idx) +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue,
                    valueCursor.getString(idx));
        }
    }

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
