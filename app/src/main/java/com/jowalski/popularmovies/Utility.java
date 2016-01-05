package com.jowalski.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Supplemental functions that may be useful across classes.
 */
public class Utility {

    public static String formatReleaseDate (Context context, Date relDate) {
        // getBestDateTime... is only available for SDK >= 18
        if (Build.VERSION.SDK_INT >= 18) {
            String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(),
                    context.getString(R.string.format_rel_date));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(relDate);
        } else {
            return (DateFormat.getMediumDateFormat(context)).format(relDate);
        }
    }

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_sort_by_pop));
    }

}
