package com.jowalski.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jowalski on 11/26/15.
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new MovieDetailFragment())
                    .commit();
        }
    }

}
