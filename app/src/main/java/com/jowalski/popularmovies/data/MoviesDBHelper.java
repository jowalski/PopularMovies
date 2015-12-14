package com.jowalski.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jowalski on 12/10/15.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = MoviesDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 3;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MoviesContract.MovieEntry.TABLE_MOVIES + "(" +
                MoviesContract.MovieEntry.COLUMN_TMDB_ID +
                " INTEGER PRIMARY KEY," +
                MoviesContract.MovieEntry.COLUMN_ICON +
                " INTEGER NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_ORIG_TITLE +
                " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_OVERVIEW +
                " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE +
                " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_POSTER_PATH +
                " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_POPULARITY +
                " REAL NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_TITLE +
                " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE +
                " REAL NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT +
                " INTEGER NOT NULL," +

                // ensure new movie information results in an update
                " UNIQUE (" + MoviesContract.MovieEntry.COLUMN_TMDB_ID +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                MoviesContract.ReviewEntry.TABLE_REVIEWS + "(" +
                MoviesContract.ReviewEntry._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.ReviewEntry.COLUMN_MOVIE_ID +
                " INTEGER NOT NULL," +
                MoviesContract.ReviewEntry.COLUMN_AUTHOR +
                " TEXT NOT NULL," +
                MoviesContract.ReviewEntry.COLUMN_CONTENT +
                " TEXT NOT NULL," +
                MoviesContract.ReviewEntry.COLUMN_URL +
                " TEXT NOT NULL," +
                MoviesContract.ReviewEntry.COLUMN_LAST_RESULTS_PAGE +
                " INTEGER NOT NULL," +
                MoviesContract.ReviewEntry.COLUMN_LAST_RESULTS_POSITION +
                " INTEGER NOT NULL," +

                // link movie_id column as foreign key to the movie table
                " FOREIGN KEY (" + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID +
                ") REFERENCES " + MoviesContract.MovieEntry.TABLE_MOVIES +
                " (" + MoviesContract.MovieEntry.COLUMN_TMDB_ID + "))";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(LOG_TAG, "Upgrading database from version " + i + " to " +
                i1 + ". OLD DATA WILL BE DESTROYED");

        // Drop the table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.TABLE_MOVIES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewEntry.TABLE_REVIEWS);
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MoviesContract.MovieEntry.TABLE_MOVIES + "'");
        sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MoviesContract.ReviewEntry.TABLE_REVIEWS + "'");

        // re-create database
        onCreate(sqLiteDatabase);
    }
}
