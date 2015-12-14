package com.jowalski.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by jowalski on 12/13/15.
 */
public class PopularMoviesDebug extends Application {
    private static final String LOG_TAG = PopularMoviesDebug.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        // Stetho.initializeWithDefaults(this);
    }

}
