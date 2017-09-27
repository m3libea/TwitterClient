package com.codepath.apps.twitterclient.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity  implements ComposeFragment.ComposeDialogListener{

    private TwitterClient client;
    private TweetsAdapter aTweets;
    private EndlessRecyclerViewScrollListener listener;

    private ArrayList<Tweet> tweets;

    ActivityTimelineBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        client = TwitterApplication.getRestClient();

        tweets = new ArrayList<>();

        setupView();

    }

    private void setupView() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setRecyclerView();

        binding.faCompose.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment filterFragment = ComposeFragment.newInstance();
            filterFragment.show(fm, "fragment_compose");
        });
    }

    private void setRecyclerView() {
        aTweets = new TweetsAdapter(this, tweets);
        binding.rvTweets.setAdapter(aTweets);
        LinearLayoutManager lyManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(lyManager);

        populateTimeline(1, 0);

        listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                populateTimeline(1, tweets.get(tweets.size()-1).getUid() - 1);
            }
        };

        binding.rvTweets.addOnScrollListener(listener);

        TweetDividerDecoration line = new TweetDividerDecoration(this);
        binding.rvTweets.addItemDecoration(line);
    }

    private void populateTimeline(int sinceId, long maxId){
        client.getHomeTimeline(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("Timeline", "Populate tweets: " + response.toString());

                tweets.addAll(Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Timeline", errorResponse.toString());
            }
        });
    }

    @Override
    public void onFinishingFilter(String body, Boolean tweet) {
        if (tweet){
            //TODO postTweet
            Log.d("Timeline", "Coming back from compose: " + body);
//            client.composeTweet("This is a test! #codepath", new JsonHttpResponseHandler(){
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                    Log.d("DEBUG", response.toString());
//
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                    Log.d("DEBUG", errorResponse.toString());
//                }
//            });
        }
    }
}
