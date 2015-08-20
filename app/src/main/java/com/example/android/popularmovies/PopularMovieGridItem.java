package com.example.android.popularmovies;

import android.database.Cursor;

import com.example.android.popularmovies.data.MoviesContract;

/**
 * Created by Raz3r on 29/07/15.
 */
public class PopularMovieGridItem
{
    private String mName;
    private String mThumbnail;

    public String getmName()
    {
        return mName;
    }

    public void setmName(String name)
    {
        this.mName = name;
    }

    public String getThumbnail()
    {
        return mThumbnail;
    }

    public void setmThumbnail(String thumbnail)
    {
        this.mThumbnail = thumbnail;
    }

    public static PopularMovieGridItem fromCursor(Cursor cursor)
    {
        PopularMovieGridItem newItem = new PopularMovieGridItem();
        //int idx_movie_title = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE);
        //int idx_poster_path = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH);

        newItem.setmName(cursor.getString(MovieGridFragment.COL_MOVIE_TITLE));
        newItem.setmThumbnail(cursor.getString(MovieGridFragment.COL_POSTER_PATH));
        return newItem;
    }

}
