package com.example.android.popularmovies.ObjectModel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Raz3r on 05/10/2015.
 */
public class Reviews implements Parcelable {

    String mReviewId;
    String mAuthor;
    String mReviewContent;
    String mReviewUrl;

    public Reviews(String reviewId, String reviewAuthor,
                   String reviewContent, String reviewUrl) {
        mReviewId = reviewId;
        mAuthor = reviewAuthor;
        mReviewContent = reviewContent;
        mReviewUrl = reviewUrl;
    }

    private Reviews(Parcel in) {
        String[] reviewData = new String[4];
        in.readStringArray(reviewData);

        mReviewId = reviewData[0];
        mAuthor = reviewData[1];
        mReviewContent = reviewData[2];
        mReviewUrl = reviewData[3];
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getReviewContent() {
        return mReviewContent;
    }

    public String getReviewUrl() {
        return mReviewUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                mReviewId,
                mAuthor,
                mReviewContent,
                mReviewUrl,
        });
    }

    public static final Creator<Reviews> CREATOR = new Creator<Reviews>() {

        @Override
        public Reviews createFromParcel(Parcel parcel) {
            return new Reviews(parcel);
        }

        @Override
        public Reviews[] newArray(int i) {
            return new Reviews[i];
        }
    };
}
