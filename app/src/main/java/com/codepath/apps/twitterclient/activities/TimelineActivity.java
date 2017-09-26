package com.codepath.apps.twitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetsAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.external.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private TweetsAdapter aTweets;
    private EndlessRecyclerViewScrollListener listener;

    private ArrayList<Tweet> tweets;

    @BindView(R.id.rvTweets)
    RecyclerView rvTweets;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient();

        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tweets = new ArrayList<>();

        setRecyclerView();

    }

    private void setRecyclerView() {
        aTweets = new TweetsAdapter(this, tweets);
        rvTweets.setAdapter(aTweets);
        LinearLayoutManager lyManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(lyManager);

        populateTimeline(1, 0);

        listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                populateTimeline(1, tweets.get(tweets.size()-1).getUid() - 1);
            }
        };

        rvTweets.addOnScrollListener(listener);

        TweetDividerDecoration line = new TweetDividerDecoration(this);
        rvTweets.addItemDecoration(line);
    }

    private void populateTimeline(int sinceId, long maxId){
        client.getHomeTimeline(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG", response.toString());

                tweets.addAll(Tweet.fromJSONArray(response));
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }
}
