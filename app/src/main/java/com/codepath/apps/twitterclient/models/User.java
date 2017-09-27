package com.codepath.apps.twitterclient.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by m3libea on 3/26/17.
 */

@Parcel
public class User {

    public String name;
    public long uuid;
    public String screenName;
    public String profileImageURL;

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public long getUuid() {
        return uuid;
    }

    public String getProfileImageURL() {
        return profileImageURL.replace("_normal", "_bigger");
    }

    public static User fromJSON(JSONObject jsonObject){
        User u = new User();

        try {
            u.name = jsonObject.getString("name");

            u.uuid = jsonObject.getLong("id");
            u.screenName = jsonObject.getString("screen_name");
            u.profileImageURL = jsonObject.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;

    }
}
