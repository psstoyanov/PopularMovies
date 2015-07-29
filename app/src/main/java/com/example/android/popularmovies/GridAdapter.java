package com.example.android.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raz3r on 29/07/15.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder>

{
    List<PopularMovieGridItem> mItems;

    public GridAdapter()
    {
        super();
        mItems = new ArrayList<PopularMovieGridItem>();
        PopularMovieGridItem movies = new PopularMovieGridItem();


        for (int i = 0; i < 15; i++)
        {
            movies.setmName("Test " + i);
            movies.setmThumbnail(R.drawable.grid_item_mock);
            mItems.add(movies);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i)
    {
        PopularMovieGridItem movielist = mItems.get(i);
        viewHolder.tvtitles.setText(movielist.getmName());
        viewHolder.imgThumbnail.setImageResource(movielist.getThumbnail());
    }

    @Override
    public int getItemCount()
    {

        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {

        public ImageView imgThumbnail;
        public TextView tvtitles;

        public ViewHolder(View itemView)
        {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            tvtitles = (TextView) itemView.findViewById(R.id.tv_movie_title);
        }


    }
}
