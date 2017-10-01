package com.codepath.apps.twitterclient.models;

import android.text.format.DateUtils;

import com.codepath.apps.twitterclient.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

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

@Table(database = MyDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel{

    @PrimaryKey
    @Column
    public String body;

    @Column
    public Long uid;

    @ForeignKey(tableClass = User.class)
    @Column
    public User user;

    @Column
    public String createdAt;

    @Column
    public Boolean retweeted;

    @Column
    Integer rtCount = 0;

    @Column
    public Boolean favorited;

    @Column
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

    public Boolean getRetweeted() {
        return retweeted;
    }

    public Integer getRtCount() {
        return rtCount;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public Integer getfCount() {
        return fCount;
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
            if(!jsonObject.isNull("extended_entities")) {
                JSONObject entitiesObj = jsonObject.getJSONObject("extended_entities");
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
                    tweet.persistTweet();
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return tweets;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "media")
    public List<MediaTweet> getMedia() {
        if (media == null || media.isEmpty()) {
            media = SQLite.select()
                    .from(MediaTweet.class)
                    .where(MediaTweet_Table.tweetUid.eq(uid))
                    .queryList();
        }
        return media;
    }
    public String getRelativeTimeAgo() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        StringBuffer relativeDate = new StringBuffer();
        try {
            long dateMillis = sf.parse(createdAt).getTime();
            long nowMillis = System.currentTimeMillis();

            long diff = nowMillis - dateMillis;

            if (diff >= DateUtils.YEAR_IN_MILLIS){
                relativeDate.append(diff/DateUtils.YEAR_IN_MILLIS);
                relativeDate.append(" y");
            }else if (diff >= DateUtils.WEEK_IN_MILLIS){
                relativeDate.append(diff/DateUtils.WEEK_IN_MILLIS);
                relativeDate.append(" w");
            }else if (diff >= DateUtils.DAY_IN_MILLIS){
                relativeDate.append(diff/DateUtils.DAY_IN_MILLIS);
                relativeDate.append(" d");
            }else if (diff >= DateUtils.HOUR_IN_MILLIS){
                relativeDate.append(diff/DateUtils.HOUR_IN_MILLIS);
                relativeDate.append(" h");
            }else if (diff >= DateUtils.MINUTE_IN_MILLIS){
                relativeDate.append(diff/DateUtils.MINUTE_IN_MILLIS);
                relativeDate.append(" m");
            }else{
                relativeDate.append(diff/DateUtils.DAY_IN_MILLIS);
                relativeDate.append(" s");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate.toString();
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

    public MediaTweet getOneMedia() {
        MediaTweet m = null;

        if (media != null && !media.isEmpty()) {

            MediaTweet video= null;
            for (MediaTweet e : media){
                if (e.getType().equals("video")){
                    video =  e;
                }
            }

            m = video != null? video : media.get(0);
        }

        return m;
    }

    public void persistTweet() {
        this.getUser().save();
        if (!this.getMedia().isEmpty()) {
            for(MediaTweet m: this.getMedia()) {
                m.save();
            }
        }
        this.save();
    }

    public String getRTCount(){
        String count = null;

        if(rtCount >= 10000){
            double div = rtCount/10000;
            count = div + "K";
        }else if (rtCount > 0){
            count = rtCount.toString();
        }else{
            count = "";
        }
        return count;
    }

    public String getLiked(){
        String count = null;

        if(fCount >= 10000){
            double div = fCount/10000;
            count = div + "K";
        }else if (fCount > 0){
            count = fCount.toString();
        }else{
            count = "";
        }
        return count;
    }

}
