package com.example.android.popularmovies.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.ObjectModel.Videos;
import com.example.android.popularmovies.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raz3r on 05/10/2015.
 */
public class VideosRecyclerAdapter extends RecyclerView.Adapter<VideosRecyclerAdapter.ViewHolder>  {


    private static final String LOG_TAG = VideosRecyclerAdapter.class.getSimpleName();

    Context mContext;

    private ArrayList<Videos> items = new ArrayList<>();

    public VideosRecyclerAdapter(Context context)
    {
        this.items = new ArrayList<Videos>();
        mContext = context;
    }

    public VideosRecyclerAdapter(ArrayList<Videos> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.video_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        //Log.d(TAG, "Cursor pos: " + getCursor().getPosition());
        return viewHolder;
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        Videos item = items.get(position);
        holder.video_name.setText(item.getName());

        //holder.image.setImageBitmap(null);
        //Picasso.with(holder.image.getContext()).cancelRequest(holder.image);
        //Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
        //holder.itemView.setTag(item);
    }

    public void add(Videos item, int position)
    {
        this.items.add(position, item);
        notifyItemInserted(position);
    }
    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }



    @Override public int getItemCount() {
        return this.items.size();
    }

    public void add(Videos videos) {
        this.items.add(videos);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        //public ImageView image;
        public TextView video_name;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemView.setClickable(true);
            //image = (ImageView) itemView.findViewById(R.id.image);
            video_name = (TextView) itemView.findViewById(R.id.video_name);
            /*  onClickListener for separate items from the adapter. Use: http://antonioleiva.com/recyclerview/*/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Context mContext = itemView.getContext();
                    /*String sortorderSetting = Utility.getPreferredSortOrder(mContext);
                    getCursor().moveToFirst();
                    getCursor().moveToPosition(getAdapterPosition());
                    ((MovieGridFragment.Callback) mContext).onItemSelected(MoviesContract.MoviesEntry.buildMovieSortOrderWithMovieID(
                            sortorderSetting, getCursor().getLong(MovieGridFragment.COL_MOVIE_ID)
                    ));
                    mSelectedItem = getAdapterPosition();
                    Log.d(TAG, String.valueOf(getAdapterPosition()));
                    notifyDataSetChanged();*/
                    setClickSelect(mContext, getAdapterPosition());
                    //notifyDataSetChanged();
                    //mContext.startActivity(intent);
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
    // Moved the ClickEvent outside of the scope of the ViewHolder.
    // This way it can be accessed outside of the adapter.
    public void setClickSelect(Context context, int pos)
    {
        Context mContext = context;
        Videos videoToCreateURL = items.get(pos);

        String videoURL = "http://www.youtube.com/watch?v=" + videoToCreateURL.getmVideoKey();
        //Log.d(LOG_TAG, videoURL);

        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoToCreateURL.getmVideoKey()));
            mContext.startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+videoToCreateURL.getmVideoKey()));
            mContext.startActivity(intent);
        }

    }


}
