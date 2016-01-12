package com.jowalski.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putInt(MovieAdapter.MOVIE_ID_EXTRA,
                    getIntent().getIntExtra(MovieAdapter.MOVIE_ID_EXTRA, -1));

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

}
