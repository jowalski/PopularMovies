package com.jowalski.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    static final SimpleDateFormat TMDB_SDF = new SimpleDateFormat(FetchMoviesTask.TMDB_DATE_FORMAT, Locale.US);

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // this is called by intent with Movie info
        Intent intent = getActivity().getIntent();
        if (intent != null &&
                intent.hasExtra(MainActivityFragment.DET_ACT_MOVIE_OBJ_INTENT_KEY)) {
            Movie movie = intent.getParcelableExtra(MainActivityFragment.DET_ACT_MOVIE_OBJ_INTENT_KEY);

            // set View fields
            ((TextView) rootView.findViewById(R.id.title_textview))
                    .setText(movie.title);

            ImageView iconView = (ImageView) rootView.findViewById(R.id.movie_poster_icon);
            Picasso.with(getContext())
                    .load(movie.posterPath)
                    .into(iconView);

            ((TextView) rootView.findViewById(R.id.rel_date_textview))
                    .setText(parseAndFormatReleaseDate(movie.releaseDate));

            ((TextView) rootView.findViewById(R.id.rating_textview))
                    .setText(String.format(getContext().getString(R.string.format_rating),
                            movie.vote_average));

            ((TextView) rootView.findViewById(R.id.rating_out_of_textview))
                    .setText(String.format(getContext().getString(R.string.format_rating_out_of),
                            FetchMoviesTask.TMDB_VOTE_OUT_OF));

            ((TextView) rootView.findViewById(R.id.plot_textview))
                    .setText(movie.overview);

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onCreateView: finished " + movie.title +
                        ". loaded/cached image from " + movie.posterPath);
            }
        }

        return rootView;
    }

    String parseAndFormatReleaseDate(String relDateStr) {
        Date relDate = null;

        // first parse the date string from theMovieDB
        try {
            relDate = TMDB_SDF.parse(relDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // next, format according to rule in strings.xml
        return Utility.formatReleaseDate(getContext(), relDate);
    }
}
