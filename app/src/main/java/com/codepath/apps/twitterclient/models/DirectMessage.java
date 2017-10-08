package com.codepath.apps.twitterclient.models;

import android.text.format.DateUtils;

import com.codepath.apps.twitterclient.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by m3libea on 10/8/17.
 */

@Table(database = MyDatabase.class)
@Parcel(analyze={DirectMessage.class})
public class DirectMessage extends BaseModel {

    @PrimaryKey
    @Column
    Long uid;

    @Column
    String text;

    @ForeignKey(tableClass = User.class)
    @Column
    User sender;

    @ForeignKey(tableClass = User.class)
    @Column
    User recipient;

    @Column
    String createdAt;

    public DirectMessage() {
        super();
    }

    public Long getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getCreatedAt() {
        return createdAt;
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

    public static DirectMessage fromJSON(JSONObject jsonObject) {
        DirectMessage dm = new DirectMessage();

        try {
            dm.uid = jsonObject.getLong("id");
            dm.createdAt = jsonObject.getString("created_at");
            dm.sender = User.fromJSON(jsonObject.getJSONObject("sender"));
            dm.recipient = User.fromJSON(jsonObject.getJSONObject("recipient"));
            dm.text = jsonObject.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dm;
    }

    public static List<DirectMessage> fromJSONArray(JSONArray jsonArray) {
        ArrayList<DirectMessage> dms = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                DirectMessage d = DirectMessage.fromJSON(jsonArray.getJSONObject(i));
                d.persist();
                dms.add(d);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return dms;
    }

    public static List<DirectMessage> getLastDirectMessages(int number) {
        return SQLite.select()
                .from(DirectMessage.class)
                .orderBy(DirectMessage_Table.uid, false)
                .limit(number)
                .queryList();
    }

    public void persist() {
        if (sender != null) {
            sender.save();
        }
        if (recipient != null) {
            recipient.save();
        }
        save();
    }
}