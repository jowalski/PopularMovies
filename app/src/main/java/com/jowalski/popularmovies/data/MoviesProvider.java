package com.jowalski.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by jowalski on 12/10/15.
 */
public class MoviesProvider extends ContentProvider {
    private static final String LOG_TAG = MoviesProvider.class.getSimpleName();
    private MoviesDBHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int MOVIE = 100;
    private static final int MOVIE_WITH_ID = 101;
    private static final int MOVIE_WITH_REVIEWS = 200;
    private static final int MOVIE_WITH_REVIEWS_WITH_ID = 201;
    private static final int REVIEW = 300;
    private static final int REVIEW_WITH_ID = 301;
    private static final int REVIEW_WITH_MOVIE_ID = 401;

    public static final String MOVIE_WREV_URI_ABR = "wrev";
    public static final String MOVIE_ID_URI_ABR = "mid";

    private static final SQLiteQueryBuilder sMoviewReviewsQueryBuilder;

    static {
        sMoviewReviewsQueryBuilder = new SQLiteQueryBuilder();

        // movies INNER JOIN reviews ON movies._id = reviews.movie_id
        sMoviewReviewsQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_MOVIES + " INNER JOIN " +
                        MoviesContract.ReviewEntry.TABLE_REVIEWS +
                        " ON " + MoviesContract.MovieEntry.TABLE_MOVIES +
                        "." + MoviesContract.MovieEntry.COLUMN_TMDB_ID +
                        " = " + MoviesContract.ReviewEntry.TABLE_REVIEWS +
                        "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID);
    }

    private static UriMatcher buildUriMatcher() {
        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // add a code for each type of URI you want
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES + "/#",
                MOVIE_WITH_ID);
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES +
                "/" + MOVIE_WREV_URI_ABR, MOVIE_WITH_REVIEWS);
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES +
                "/" + MOVIE_WREV_URI_ABR + "/#", MOVIE_WITH_REVIEWS_WITH_ID);
        matcher.addURI(authority, MoviesContract.ReviewEntry.TABLE_REVIEWS, REVIEW);
        matcher.addURI(authority, MoviesContract.ReviewEntry.TABLE_REVIEWS + "/#",
                REVIEW_WITH_ID);
        matcher.addURI(authority, MoviesContract.ReviewEntry.TABLE_REVIEWS +
                "/" + MOVIE_ID_URI_ABR + "/#", REVIEW_WITH_MOVIE_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            // All Movies selected
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            // Individual movie based on Id selected
            case MOVIE_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        MoviesContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case MOVIE_WITH_REVIEWS: {
                retCursor = sMoviewReviewsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case MOVIE_WITH_REVIEWS_WITH_ID: {
                retCursor = sMoviewReviewsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        MoviesContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case REVIEW: {
                // All reviews selected
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case REVIEW_WITH_ID: {
                // single review with matching _id
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        MoviesContract.ReviewEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case REVIEW_WITH_MOVIE_ID: {
                // All reviews matching the specified MOVIE_ID
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            default: {
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_DIR_TYPE;
            case REVIEW_WITH_MOVIE_ID:
                return MoviesContract.ReviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_MOVIES, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_REVIEWS, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MoviesContract.ReviewEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match) {
            case MOVIE:
                numDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_MOVIES, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MoviesContract.MovieEntry.TABLE_MOVIES + "'");
                break;
            case MOVIE_WITH_ID:
                numDeleted = db.delete(MoviesContract.MovieEntry.TABLE_MOVIES,
                        MoviesContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MoviesContract.MovieEntry.TABLE_MOVIES + "'");
                break;
            case REVIEW:
                numDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_REVIEWS, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MoviesContract.ReviewEntry.TABLE_REVIEWS + "'");
                break;
            case REVIEW_WITH_ID:
                numDeleted = db.delete(MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        MoviesContract.ReviewEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '");
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                // allows for multiple transactions
                db.beginTransaction();

                // keep track of successful inserts
                int numInserted = 0;
                try {
                    for (ContentValues value : values) {
                        if (value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try {
                            _id = db.insertOrThrow(MoviesContract.MovieEntry.TABLE_MOVIES,
                                    null, value);
                        } catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            MoviesContract.MovieEntry.COLUMN_TITLE)
                                    + " but value is already in database.");
                        }
                        if (_id != -1){
                            numInserted++;
                        }
                    }
                    if (numInserted > 0){
                        // If no errors, declare a successful transaction.
                        // database will not populate if this is not called
                        db.setTransactionSuccessful();
                    }
                } finally {
                    // all transactions occur at once
                    db.endTransaction();
                }
                if (numInserted > 0){
                    // if there was successful insertion, notify the content resolver that there
                    // was a change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numInserted;
            case REVIEW:
                // allows for multiple transactions
                db.beginTransaction();

                // keep track of successful inserts
                numInserted = 0;
                try {
                    for (ContentValues value : values) {
                        if (value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try {
                            _id = db.insertOrThrow(MoviesContract.ReviewEntry.TABLE_REVIEWS,
                                    null, value);
                        } catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            MoviesContract.ReviewEntry.COLUMN_AUTHOR)
                                    + " but value is already in database.");
                        }
                        if (_id != -1){
                            numInserted++;
                        }
                    }
                    if (numInserted > 0){
                        // If no errors, declare a successful transaction.
                        // database will not populate if this is not called
                        db.setTransactionSuccessful();
                    }
                } finally {
                    // all transactions occur at once
                    db.endTransaction();
                }
                if (numInserted > 0){
                    // if there was successful insertion, notify the content resolver that there
                    // was a change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated = 0;

        if (contentValues == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case MOVIE: {
                numUpdated = db.update(MoviesContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        s,
                        strings);
                break;
            }
            case MOVIE_WITH_ID: {
                numUpdated = db.update(MoviesContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        MoviesContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            case REVIEW: {
                numUpdated = db.update(MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        contentValues,
                        s,
                        strings);
                break;
            }
            case REVIEW_WITH_ID: {
                numUpdated = db.update(MoviesContract.ReviewEntry.TABLE_REVIEWS,
                        contentValues,
                        MoviesContract.ReviewEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (numUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }
}
