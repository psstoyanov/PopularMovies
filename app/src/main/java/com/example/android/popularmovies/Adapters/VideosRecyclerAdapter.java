package com.example.android.popularmovies.Adapters;

import android.support.v7.widget.RecyclerView;
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


    private ArrayList<Videos> items = new ArrayList<>();

    public VideosRecyclerAdapter()
    {
        this.items = new ArrayList<Videos>();
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


    @Override public int getItemCount() {
        return this.items.size();
    }

    public void add(Videos videos) {
        this.items.add(videos);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //public ImageView image;
        public TextView video_name;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemView.setClickable(true);
            //image = (ImageView) itemView.findViewById(R.id.image);
            video_name = (TextView) itemView.findViewById(R.id.video_name);
        }
    }
}
