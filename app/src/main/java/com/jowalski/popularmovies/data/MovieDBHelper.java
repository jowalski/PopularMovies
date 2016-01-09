package com.jowalski.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MovieDBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = MovieDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 5;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieContract.MovieEntry.TABLE_MOVIES + "(" +
                MovieContract.MovieEntry._ID +
                " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_ICON +
                " INTEGER," +
                MovieContract.MovieEntry.COLUMN_ORIG_TITLE +
                " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_OVERVIEW +
                " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE +
                " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_POSTER_PATH +
                " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_POPULARITY +
                " REAL NOT NULL," +
                MovieContract.MovieEntry.COLUMN_TITLE +
                " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE +
                " REAL NOT NULL," +
                MovieContract.MovieEntry.COLUMN_VOTE_COUNT +
                " INTEGER NOT NULL," +
                MovieContract.MovieEntry.COLUMN_POPULARITY_QUERY +
                " INTEGER DEFAULT 0," +
                MovieContract.MovieEntry.COLUMN_RATING_QUERY +
                " INTEGER DEFAULT 0," +
                MovieContract.MovieEntry.COLUMN_FAVORITE +
                " INTEGER DEFAULT 0," +

                // ensure new movie information results in an update
                " UNIQUE (" + MovieContract.MovieEntry._ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                MovieContract.ReviewEntry.TABLE_REVIEWS + "(" +
                MovieContract.ReviewEntry._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID +
                " INTEGER NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_AUTHOR +
                " TEXT NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_CONTENT +
                " TEXT NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_URL +
                " TEXT NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_LAST_RESULTS_PAGE +
                " INTEGER NOT NULL," +
                MovieContract.ReviewEntry.COLUMN_LAST_RESULTS_POSITION +
                " INTEGER NOT NULL," +

                // link movie_id column as foreign key to the movie table
                " FOREIGN KEY (" + MovieContract.ReviewEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MovieContract.MovieEntry.TABLE_MOVIES +
                " (" + MovieContract.MovieEntry._ID + "))";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(LOG_TAG, "Upgrading database from version " + i + " to " +
                i1 + ". OLD DATA WILL BE DESTROYED");

        // Drop the table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_MOVIES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_REVIEWS);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MovieContract.MovieEntry.TABLE_MOVIES + "'");
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MovieContract.ReviewEntry.TABLE_REVIEWS + "'");

        // re-create database
        onCreate(sqLiteDatabase);
    }
}
