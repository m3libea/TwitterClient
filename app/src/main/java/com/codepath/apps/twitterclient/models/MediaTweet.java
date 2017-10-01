package com.codepath.apps.twitterclient.models;
import com.codepath.apps.twitterclient.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by m3libea on 9/28/17.
 */

@Table(database = MyDatabase.class)
@Parcel(analyze = {MediaTweet.class})
public class MediaTweet extends BaseModel {

    @PrimaryKey
    @Column
    Long id;

    @Column
    Long tweetUid;

    @Column
    String displayUrl;

    @Column
    String expandedUrl;

    @Column
    String mediaUrl;

    @Column
    String type;

    @Column
    String videoURL;

    public MediaTweet() {
    }

    public Long getId() {
        return id;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public String getExpandedUrl() {
        return expandedUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getType() {
        return type;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public static MediaTweet fromJSON(JSONObject jsonObject) {
        MediaTweet media = new MediaTweet();

        try {
            media.id = jsonObject.getLong("id");
            media.displayUrl = jsonObject.getString("display_url");
            media.expandedUrl = jsonObject.getString("expanded_url");
            media.mediaUrl = jsonObject.getString("media_url");
            media.type = jsonObject.getString("type");

            if (!jsonObject.isNull("video_info")){
                media.videoURL = jsonObject.getJSONObject("video_info").getJSONArray("variants").getJSONObject(0).getString("url");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return media;
    }

    public static ArrayList<MediaTweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<MediaTweet> mes = new ArrayList<MediaTweet>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                mes.add(MediaTweet.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return mes;
    }
}

