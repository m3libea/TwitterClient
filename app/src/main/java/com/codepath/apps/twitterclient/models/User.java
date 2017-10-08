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

    public long getUid() {
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

    public static ArrayList<User> fromJSONArray(JSONArray jsonArray){
        ArrayList<User> users = new ArrayList<>();

        for(int i = 0; i<jsonArray.length() ; i++){
            try {
                JSONObject userObjet = jsonArray.getJSONObject(i);
                User user = fromJSON(userObjet);
                if(user != null ){
                    user.save();
                    users.add(user);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return users;
    }

    public String getCFollowers() {
        if(followers < 10000){
            return Integer.toString(followers);
        }else{
            return coolFormat(followers, 0);
        }
    }

    public String getCFollowing(){
        if(following < 10000){
            return Integer.toString(following);
        }else{
            return coolFormat(following, 0);
        }
    }

    private static char[] c = new char[]{'K', 'M', 'B', 'T'};

    private static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) %10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : coolFormat(d, iteration+1));

    }
}
