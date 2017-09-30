package com.codepath.apps.twitterclient.models;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by m3libea on 3/26/17.
 */

@Parcel(analyze={Tweet.class})
public class Tweet {
    public String body;
    public long uid;
    public User user;
    public String createdAt;
    public Boolean retweeted;
    Integer rtCount = 0;
    public Boolean favorited;
    Integer fCount = 0;

    List<MediaTweet> media;

    public Tweet() {
    }

    public long getUid() {
        return uid;
    }

    public String getBody() {
        return body;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static Tweet fromJSON(JSONObject jsonObject){
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.uid = jsonObject.getLong("id");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.retweeted = jsonObject.getBoolean("retweeted");
            if(!jsonObject.isNull("retweet_count")) {
                tweet.rtCount = jsonObject.getInt("retweet_count");
            }
            tweet.favorited = jsonObject.getBoolean("favorited");
            if(!jsonObject.isNull("favourites_count")) {
                tweet.fCount = jsonObject.getInt("favourites_count");
            }
            if(!jsonObject.isNull("entities")) {
                JSONObject entitiesObj = jsonObject.getJSONObject("entities");
                if(!entitiesObj.isNull("media")) {
                    List<MediaTweet> media = MediaTweet.fromJSONArray(entitiesObj.getJSONArray("media"));
                    for (MediaTweet m : media) {
                        m.tweetUid = tweet.getUid();
                    }
                    tweet.media = media;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tweet;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray){
        ArrayList<Tweet> tweets = new ArrayList<>();

        for(int i = 0; i<jsonArray.length() ; i++){
            try {
                JSONObject tweetObjet = jsonArray.getJSONObject(i);
                Tweet tweet = fromJSON(tweetObjet);
                if(tweet != null ){
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return tweets;
    }

    public String getRelativeTimeAgo() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(createdAt).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public String getFormattedDate() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String formattedDate = "";
        try {
            Date date = sf.parse(createdAt);
            formattedDate = DateFormat.getDateTimeInstance().format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }

    public String getOneMedia() {
        String url = null;
        if (media != null && !media.isEmpty()) {
            url = media.get(0).getMediaUrl();
        }

        return url;
    }

}
