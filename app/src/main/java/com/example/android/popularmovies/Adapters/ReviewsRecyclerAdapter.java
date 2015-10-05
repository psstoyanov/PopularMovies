package com.example.android.popularmovies.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.ObjectModel.Reviews;
import com.example.android.popularmovies.ObjectModel.Videos;
import com.example.android.popularmovies.R;

import java.util.ArrayList;

/**
 * Created by Raz3r on 05/10/2015.
 */
public class ReviewsRecyclerAdapter extends RecyclerView.Adapter<ReviewsRecyclerAdapter.ViewHolder> {

    private static final String LOG_TAG = ReviewsRecyclerAdapter.class.getSimpleName();

    Context mContext;

    private ArrayList<Reviews> reviewitems = new ArrayList<>();

    public ReviewsRecyclerAdapter(Context context) {
        this.reviewitems = new ArrayList<Reviews>();
        mContext = context;
    }

    public ReviewsRecyclerAdapter(ArrayList<Reviews> items) {
        this.reviewitems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        //Log.d(TAG, "Cursor pos: " + getCursor().getPosition());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Reviews item = reviewitems.get(position);
        holder.review_author.setText(item.getAuthor());
        holder.review_contents.setText(item.getReviewContent());
        //holder.video_name.setText(item.getAuthor());

        //holder.image.setImageBitmap(null);
        //Picasso.with(holder.image.getContext()).cancelRequest(holder.image);
        //Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
        //holder.itemView.setTag(item);
    }

    public void add(Reviews item, int position) {
        this.reviewitems.add(position, item);
        notifyItemInserted(position);
    }

    public void clear() {
        this.reviewitems.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return this.reviewitems.size();
    }

    public void add(Reviews review) {
        this.reviewitems.add(review);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //public ImageView image;
        public TextView review_author;
        public TextView review_contents;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemView.setClickable(true);
            //image = (ImageView) itemView.findViewById(R.id.image);
            review_author = (TextView) itemView.findViewById(R.id.review_author);
            review_contents = (TextView) itemView.findViewById(R.id.review_content);
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
    }
}
