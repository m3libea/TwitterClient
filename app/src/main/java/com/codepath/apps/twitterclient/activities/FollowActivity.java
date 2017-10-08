package com.codepath.apps.twitterclient.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.FollowAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivityFollowBinding;
import com.codepath.apps.twitterclient.external.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class FollowActivity extends AppCompatActivity implements FollowAdapter.TweetClickListener{

    private final String TAG = "FollowActivity";
    private TwitterClient client;
    ActivityFollowBinding binding;
    private User user;
    private boolean following;

    List<User> users;
    private FollowAdapter aTweets;
    private LinearLayoutManager lyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_follow);
        client = TwitterApplication.getRestClient();

        users = new ArrayList<>();
        user = Parcels.unwrap(getIntent().getExtras().getParcelable("user"));
        following = getIntent().getExtras().getBoolean("following", false);

        setupView();

        getFollow(user, -1);
    }

    private void setupView() {

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String title = following ? "Following" : "Followers";
        getSupportActionBar().setTitle(title);

        aTweets = new FollowAdapter(this, users);
        aTweets.setListener(this);
        binding.rvUsers.setAdapter(aTweets);
        lyManager = new LinearLayoutManager(getContext());
        binding.rvUsers.setLayoutManager(lyManager);

        //Line between rows
        TweetDividerDecoration line = new TweetDividerDecoration(getContext());
        binding.rvUsers.addItemDecoration(line);

        EndlessRecyclerViewScrollListener listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (isNetworkAvailable()) {
                    getFollow(user, users.get(users.size() - 1).getUid());
                } else {
                    Snackbar bar = Snackbar.make(findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT);
                    bar.show();
                }
            }
        };

        binding.rvUsers.addOnScrollListener(listener);
    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void getFollow(User user, long cursor){

        if (following) {
            client.getFollowing(user, cursor, 50, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Populate users: " + response.toString());

                    try {
                        addItems(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }else{
            client.getFollowers(user, cursor, 50, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Populate followers: " + response.toString());

                    try {
                        addItems(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }

    }

    public void addItems(JSONObject response) throws JSONException {
        users.addAll(User.fromJSONArray(response.getJSONArray("users")));
        aTweets.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showUser(String screename) {
        client.getUser(screename, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                User user = null;
                try {
                    user = User.fromJSON(response.getJSONObject(0));
                    Intent i = new Intent(FollowActivity.this, UserActivity.class);
                    i.putExtra("user", Parcels.wrap(user));
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void showHT(String query) {
        Intent i = new Intent(FollowActivity.this, SearchActivity.class);
        i.putExtra("query", query);
        startActivity(i);
    }
}
