package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Raz3r on 29/07/15.
 * The adapter for the RecyclerView.
 * It will display a grid of thumbnails and the respective movie names.
 */
public class GridAdapter extends CursorRecyclerAdapter<GridAdapter.ViewHolder>
{
    private List<PopularMovieGridItem> mItems;
    private int itemLayout;
    private static final String TAG = "CustomAdapter";
    Context mContext;
    Cursor mCursor;


    public GridAdapter(Context context,Cursor cursor)
    {
        super(context, cursor);
        mCursor = cursor;
        //mItems = new ArrayList<>();

        //Initializing the adapter with an empty object.
        //PopularMovieGridItem movies = new PopularMovieGridItem();
        //mItems.add(0, movies);
        //notifyItemInserted(0);


        /*for (int i = 0; i < 15; i++)
        {
            PopularMovieGridItem movies = new PopularMovieGridItem();
            movies.setmName("Test " + Integer.toString(i));
            movies.setmThumbnail("http://img13.deviantart.net/e8e8/i/2012/045/2/0/passage_wallpaper_by_trenchmaker-d4pp3zd.jpg");
            mItems.add(i, movies);
            notifyItemInserted(i);
        }*/

    }





    public void add(PopularMovieGridItem item, int position)
    {
        mItems.add(position, item);
        notifyItemInserted(position);
    }

    public void add(PopularMovieGridItem item)
    {
        int pos = mItems.size();
        mItems.add(item);
        notifyItemInserted(pos);
    }

    public void remove(PopularMovieGridItem item)
    {
        int position = mItems.indexOf(item);
        mItems.remove(position);
        notifyItemRemoved(position);
    }
    public void clearAll()
    {
        int size = mItems.size();
        mItems.clear();
        notifyItemRangeRemoved(0, size);
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        Log.d(TAG,"Cursor pos: "+ getCursor().getPosition());
        return viewHolder;
    }

    /*@Override
    public void onBindViewHolder(ViewHolder viewHolder, int i)
    {
        PopularMovieGridItem movielist = mItems.get(i);
        viewHolder.tvtitles.setText(movielist.getmName());

        //viewHolder.imgThumbnail.setImageResource(movielist.getThumbnail());

        //Use psso to load an image.
        Picasso.with(viewHolder.imgThumbnail.getContext()).cancelRequest(viewHolder.imgThumbnail);

        //Add a fit and center function from Picasso
        //Also changed how the view itself will handle it.
        Picasso.with(viewHolder.imgThumbnail.getContext()).load(movielist.getThumbnail()).placeholder(R.drawable.blank_thumbnail)
                .fit().centerInside().into(viewHolder.imgThumbnail);

        //Picasso.with(viewHolder.imgThumbnail.getContext()).load(R.drawable.grid_item_mock).into(viewHolder.imgThumbnail);

    }*/

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor)
    {

        PopularMovieGridItem movielist = PopularMovieGridItem.fromCursor(cursor);
        //Log.d(TAG, "viewHolder pos: " + String.valueOf(viewHolder.getItemId()));
        //Log.d(TAG, "Cursor pos: "+ String.valueOf(cursor.getPosition()));
        viewHolder.tvtitles.setText(movielist.getmName());

        //viewHolder.imgThumbnail.setImageResource(movielist.getThumbnail());

        //Use psso to load an image.
        Picasso.with(viewHolder.imgThumbnail.getContext()).cancelRequest(viewHolder.imgThumbnail);
        //Add a fit and center function from Picasso
        //Also changed how the view itself will handle it.
        Picasso.with(viewHolder.imgThumbnail.getContext()).load(movielist.getThumbnail()).placeholder(R.drawable.blank_thumbnail)
                .fit().centerInside().into(viewHolder.imgThumbnail);
    }

    /*@Override
    public int getItemCount()
    {

        return mItems.size();
    }*/


    class ViewHolder extends RecyclerView.ViewHolder
    {

        public ImageView imgThumbnail;
        public TextView tvtitles;

        public ViewHolder(final View itemView)
        {

            super(itemView);

            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            tvtitles = (TextView) itemView.findViewById(R.id.tv_movie_title);

            /*  onClickListener for separate items from the adapter. Use: http://antonioleiva.com/recyclerview/*/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context mContext = itemView.getContext();
                    //Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                   // Log.d(TAG, "Element " + getCursor().moveToPosition(getAdapterPosition()) + " clicked.");
                    //Log.d(TAG, "Element " + getCursor().getString(MovieGridFragment.COL_MOVIE_TITLE) + " clicked.");
                    String sortorderSetting = Utility.getPreferredSortOrder(mContext);
                    getCursor().moveToFirst();
                    getCursor().moveToPosition(getAdapterPosition());
                    Intent intent = new Intent(mContext, DetailActivity.class)
                            .setData(MoviesContract.MoviesEntry.buildMovieSortOrderWithMovieID(
                                    sortorderSetting, getCursor().getLong(MovieGridFragment.COL_MOVIE_ID)
                            ));
                    mContext.startActivity(intent);
                }
                  //Log.d(TAG, "Element " + getCursor().getString(MovieGridFragment.COL_MOVIE_ID)  + " clicked.");
                  //PopularMovieGridItem itemToSend = mItems.get(getAdapterPosition());
                  //int i = getAdapterPosition();

                  //Intent intent = new Intent(mContext, DetailActivity.class)
                  //        .putExtra(Intent.EXTRA_TEXT, itemToSend.getmName() + " " + itemToSend.getThumbnail());
                  //mContext.startActivity(intent);
                  //Toast.makeText(itemView.getContext(),"Recycler View" + getAdapterPosition() + " clicked",Toast.LENGTH_SHORT).show();

            });
        }


    }
}
