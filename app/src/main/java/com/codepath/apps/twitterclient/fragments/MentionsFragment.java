package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.external.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by m3libea on 10/2/17.
 */

public class MentionsFragment extends TweetsFragment {

    private final String TAG = "MentionsFragment";
    private TwitterClient client;

    private EndlessRecyclerViewScrollListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  super.onCreateView(inflater, container, savedInstanceState);

        setupView();

        getTimeline();
        return view;
    }

    private void setupView() {

        //Set listener for refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            Log.d("TAG", "Refresh");
            if(isNetworkAvailable()){
                refreshTimeline(tweets.isEmpty() ? 1 : tweets.get(0).getUid(), -1);
            }else{
                Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG);
                bar.show();
                binding.swipeContainer.setRefreshing(false);
            }

        });

        //Set listener for endlessscrol

        listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (isNetworkAvailable()){
                    populateTimeline(1, tweets.get(tweets.size()-1).getUid() - 1);
                }else{
                    Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_SHORT);
                    bar.show();
                }
            }
        };

        binding.rvTweets.addOnScrollListener(listener);
    }

    private void getTimeline() {
        if (isNetworkAvailable()){
            showProgressBar();
            populateTimeline(1, -1);

        }else{
            Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG)
                    .setAction("Retry", v -> getTimeline());
            bar.show();
        }
    }

    private void populateTimeline(int sinceId, long maxId){
        client.getMentions(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Populate tweets: " + response.toString());
                hideProgressBar();
                addItems(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideProgressBar();
                Log.d(TAG, errorResponse.toString());
            }
        });
    }

    //Move to timeline Fragment
    private void refreshTimeline(long sinceId, long maxId){
        client.getMentions(sinceId, maxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Refresh tweets: " + response.toString());
                addRefresh(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
            }
        });
    }
}
