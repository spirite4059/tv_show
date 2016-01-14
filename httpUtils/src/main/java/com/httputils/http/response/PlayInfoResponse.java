package com.httputils.http.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zfy on 2015/8/24.
 */
public class PlayInfoResponse implements Parcelable {


    public String remotevid;// 第三方播放id
    public int siteId;// 第三方网站Id
    public int duration;// 时长
    public String sourceURL;// 播放网址

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remotevid);
        dest.writeInt(siteId);
        dest.writeInt(duration);
        dest.writeString(sourceURL);

    }

    public static final Parcelable.Creator<PlayInfoResponse> CREATOR = new Parcelable.Creator<PlayInfoResponse>() {

        @Override
        public PlayInfoResponse[] newArray(int size) {
            return new PlayInfoResponse[size];
        }

        @Override
        public PlayInfoResponse createFromParcel(Parcel source) {

            PlayInfoResponse play = new PlayInfoResponse();
            play.remotevid = source.readString();
            play.siteId = source.readInt();
            play.duration = source.readInt();
            play.sourceURL = source.readString();
            return play;
        }
    };



}
