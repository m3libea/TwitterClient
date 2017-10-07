package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetsAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.FragmentTweetsBinding;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by m3libea on 10/2/17.
 */

public class TweetsFragment extends Fragment implements TweetsAdapter.TweetActionListener, ComposeFragment.ComposeDialogListener{

    private TweetsAdapter aTweets;

    public ArrayList<Tweet> tweets;
    public TwitterClient client;


    public FragmentTweetsBinding binding;
    public LinearLayoutManager lyManager;
    private final String TAG = "TweetsFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets, container, false);
        client = TwitterApplication.getRestClient();

        tweets = new ArrayList<>();

        setRecyclerView();

        return binding.getRoot();

    }


    private void setRecyclerView() {
        aTweets = new TweetsAdapter(getContext(), tweets);
        aTweets.setActionListener(this);
        binding.rvTweets.setAdapter(aTweets);
        lyManager = new LinearLayoutManager(getContext());
        binding.rvTweets.setLayoutManager(lyManager);

        //Line between rows
        TweetDividerDecoration line = new TweetDividerDecoration(getContext());
        binding.rvTweets.addItemDecoration(line);

        binding.swipeContainer.setColorSchemeResources(R.color.primary,
                R.color.primary_dark,
                R.color.twitterLight,
                R.color.accent);

    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void addItems(JSONArray response){
        tweets.addAll(Tweet.fromJSONArray(response));
        aTweets.notifyDataSetChanged();
    }

    public void addRefresh(JSONArray response){
        tweets.addAll(0,Tweet.fromJSONArray(response));
        aTweets.notifyDataSetChanged();
        binding.swipeContainer.setRefreshing(false);
    }

    public void addFromQuery(List<Tweet> tweets) {
        tweets.addAll(tweets);
        aTweets.notifyDataSetChanged();
    }

    public void addFront(Tweet t){
        tweets.add(0, t);
        aTweets.notifyDataSetChanged();
        binding.rvTweets.scrollToPosition(0);
    }

    public void cleanRV(){
        tweets.clear();
        aTweets.notifyDataSetChanged();
    }

    @Override
    public void reply(Tweet tweet) {
        FragmentManager fm = getChildFragmentManager();
        ComposeFragment tweetCompose = ComposeFragment.newInstance(tweet.getUser().getScreenName(),tweet, true);
        tweetCompose.show(fm, "fragment_compose");
    }
    @Override
    public void onFinishingTweet(String body, Tweet t, Boolean tweet) {
        String minBody = body.substring(0, Math.min(getResources().getInteger(R.integer.max_tweet_length), body.length()));
        if (isNetworkAvailable()){
            client.composeReply(t.getUser().getScreenName(), t.getUid(), minBody, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Tweet created: " + response.toString());
                    Toast toast = Toast.makeText(getContext(), R.string.toastTweetCreated, Toast.LENGTH_SHORT);
                    toast.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }else{
            Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_tweet), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG);
            bar.show();
        }
    }
    @Override
    public void setLikeStatus(Tweet tweet, boolean b) {
        if (b) {
            client.postFavorite(tweet,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Liked" + response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }else{
            client.destroyFavorite(tweet,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "DestroyLiked" + response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }
    }

    @Override
    public void retweet(Tweet tweet) {
        client.postRetweet(tweet, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
            }
        });
    }
}
