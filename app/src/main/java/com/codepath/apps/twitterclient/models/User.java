package com.codepath.apps.twitterclient.models;

import com.codepath.apps.twitterclient.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by m3libea on 3/26/17.
 */

@Table(database = MyDatabase.class)
@Parcel(analyze = User.class )
public class User extends BaseModel{

    @Column
    public String name;

    @PrimaryKey
    @Column
    public Long uuid;

    @Column
    public String screenName;

    @Column
    public String profileImageURL;

    @Column
    public String profileBannerURL;

    @Column
    public Integer following;

    @Column
    public Integer followers;

    @Column
    public String location;

    @Column
    public String description;

    @Column
    public Integer statusesCount;

    @Column
    public Boolean verified;

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

    public String getProfileBannerURL() {
        return profileBannerURL;
    }

    public Integer getFollowing() {
        return following;
    }

    public Integer getFollowers() {
        return followers;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public static User fromJSON(JSONObject jsonObject){
        User u = new User();

        try {
            u.name = jsonObject.getString("name");

            u.uuid = jsonObject.getLong("id");
            u.screenName = jsonObject.getString("screen_name");
            u.profileImageURL = jsonObject.getString("profile_image_url");
            u.followers = jsonObject.getInt("followers_count");
            u.following = jsonObject.getInt("friends_count");
            u.description = jsonObject.getString("description");
            u.location = jsonObject.getString("location");
            u.statusesCount = jsonObject.getInt("statuses_count");
            u.verified = jsonObject.getBoolean("verified");
            if (!jsonObject.isNull("profile_banner_url")) {
                u.profileBannerURL = jsonObject.getString("profile_banner_url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;

    }

    public String getCFollowers(){
        String count = null;

        if(followers >= 10000){
            double div = followers/10000;
            count = div + "K";
        }else if (followers > 0){
            count = followers.toString();
        }else{
            count = "";
        }
        return count;
    }

    public String getCFollowing(){
        String count = null;

        if(following >= 10000){
            double div = following/10000;
            count = div + "K";
        }else if (following > 0){
            count = following.toString();
        }else{
            count = "";
        }
        return count;
    }
}
