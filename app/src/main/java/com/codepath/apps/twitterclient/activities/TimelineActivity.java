package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetsAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivityTimelineBinding;
import com.codepath.apps.twitterclient.external.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.fragments.ComposeFragment;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity  implements ComposeFragment.ComposeDialogListener{

    private final String TAG = "Timeline";
    private TwitterClient client;
    private TweetsAdapter aTweets;
    private EndlessRecyclerViewScrollListener listener;

    private ArrayList<Tweet> tweets;
    private User user;

    ActivityTimelineBinding binding;
    FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        client = TwitterApplication.getRestClient();

        fm = getSupportFragmentManager();

        tweets = new ArrayList<>();

        getUser();
        setupView();

        populateTimeline(1, -1);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // Handle Intent from external app
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

                // Make sure to check whether returned data will be null.
                String titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT);
                composeTweet(titleOfPage, urlOfPage);
            }
        }

    }

    private void composeTweet(String titleOfPage, String urlOfPage) {
        StringBuffer body = new StringBuffer();
        body.append(titleOfPage);
        body.append(" : ");
        body.append(urlOfPage);

        String bCompose = body.substring(0, Math.min(getResources().getInteger(R.integer.max_tweet_length), body.length()));
        ComposeFragment tweetCompose = ComposeFragment.newInstance(user,bCompose);
        tweetCompose.show(fm, "fragment_compose");

    }

    private void getUser() {

        client.getAccount(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                user = User.fromJSON(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "Error getting the account" + errorResponse.toString());
            }
        });
    }

    private void setupView() {
        setSupportActionBar(binding.included.toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setRecyclerView();

        binding.faCompose.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment tweetCompose = ComposeFragment.newInstance(user);
            tweetCompose.show(fm, "fragment_compose");
        });
    }

    private void setRecyclerView() {
        aTweets = new TweetsAdapter(this, tweets);
        binding.rvTweets.setAdapter(aTweets);
        LinearLayoutManager lyManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(lyManager);

        //Infinite scroll
        listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                populateTimeline(1, tweets.get(tweets.size()-1).getUid() - 1);
            }
        };

        binding.rvTweets.addOnScrollListener(listener);

        //Line between rows
        TweetDividerDecoration line = new TweetDividerDecoration(this);
        binding.rvTweets.addItemDecoration(line);

        //Refresh action
        binding.swipeContainer.setOnRefreshListener(() -> {
            Log.d("TAG", "Refresh");
            refreshTimeline(tweets.isEmpty() ? 1 : tweets.get(0).getUid(), -1);

        });
        binding.swipeContainer.setColorSchemeResources(R.color.primary,
                R.color.primary_dark,
                R.color.twitterLight,
                R.color.accent);

    }

    private void refreshTimeline(long sinceId, long maxId){
        client.getHomeTimeline(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Refresh tweets: " + response.toString());

                tweets.addAll(0,Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
                binding.swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
            }
        });
    }
    private void populateTimeline(int sinceId, long maxId){
        client.getHomeTimeline(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Populate tweets: " + response.toString());

                tweets.addAll(Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
            }
        });
    }

    @Override
    public void onFinishingTweet(String body, Boolean tweet) {
        if (tweet){
            client.composeTweet(body.substring(0, Math.min(getResources().getInteger(R.integer.max_tweet_length),
                    body.length())),
                    new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        Tweet tweet = Tweet.fromJSON(response);
                        tweets.add(0, tweet);
                        aTweets.notifyDataSetChanged();
                        binding.rvTweets.scrollToPosition(0);
                        Log.d(TAG, "Create Tweet: " + response.toString());

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d(TAG, errorResponse.toString());
                    }
            });
        }else{
            if (body!= null){
                //Save draft on Sharedpreferences
                SharedPreferences pref =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("draft", body);
                edit.commit();
                Log.d(TAG, "Draft saved " + body);

            }
        }
    }
}
