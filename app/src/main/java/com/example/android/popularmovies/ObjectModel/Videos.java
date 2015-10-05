package com.example.android.popularmovies.ObjectModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * Created by Raz3r on 05/10/2015.
 */
public class Videos implements Parcelable {

    String mVideoId;
    String mVideoKey;
    String mVideoName;
    String mVideoSite;
    String mVideoSize;
    String mVideoType;

    public Videos(String videoId, String videoKey,
                  String videoName, String videoSite,
                  String videoSize, String videoType) {
        mVideoId = videoId;
        mVideoKey = videoKey;
        mVideoName = videoName;
        mVideoSite = videoSite;
        mVideoSize = videoSize;
        mVideoType = videoType;
    }

    private Videos(Parcel in) {
        String[] videoData = new String[6];
        in.readStringArray(videoData);

        mVideoId = videoData[0];
        mVideoKey = videoData[1];
        mVideoName = videoData[2];
        mVideoSite = videoData[3];
        mVideoSize = videoData[4];
        mVideoType = videoData[5];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                mVideoId,
                mVideoKey,
                mVideoName,
                mVideoSite,
                mVideoSize,
                mVideoType
        });
    }
    public static final Creator<Videos> CREATOR = new Creator<Videos>() {

        @Override
        public Videos createFromParcel(Parcel parcel) {
            return new Videos(parcel);
        }

        @Override
        public Videos[] newArray(int i) {
            return new Videos[i];
        }
    };
}
