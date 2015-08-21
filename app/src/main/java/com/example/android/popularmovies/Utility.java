package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Raz3r on 18/08/15.
 */
public class Utility {

    private static final String ratingMax = "10.0";

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_order_popularity));
    }
    public static String formatRating(Double userrating) {
        return Double.toString(userrating) + "/" + ratingMax;
    }
}
