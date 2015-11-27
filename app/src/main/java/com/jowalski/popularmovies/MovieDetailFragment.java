package com.jowalski.popularmovies;

import android.view.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

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
            TextView titleTextView = (TextView) rootView.findViewById(R.id.title_textview);
            titleTextView.setText(movie.title);

            ImageView iconView = (ImageView) rootView.findViewById(R.id.movie_poster_icon);
            Picasso.with(getContext())
                    .load(movie.posterPath)
                    .into(iconView);

            TextView dateTextView = (TextView) rootView.findViewById(R.id.rel_date_textview);
            dateTextView.setText(movie.releaseDate);

            TextView ratingTextView = (TextView) rootView.findViewById(R.id.rating_textview);
            ratingTextView.setText(String.format("%.1f / 10", movie.vote_average));

            TextView plotTextView = (TextView) rootView.findViewById(R.id.plot_textview);
            plotTextView.setText(movie.overview);
        }

        return rootView;
    }

}
