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
import com.codepath.apps.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

/**
 * Created by m3libea on 10/2/17.
 */

public class UserTimelineFragment extends TweetsFragment {

    private final String TAG = "HTLFragment";
    private TwitterClient client;

    private EndlessRecyclerViewScrollListener listener;

    private User user;


    public static UserTimelineFragment newInstance(User user){
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));

        UserTimelineFragment fragment = new UserTimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();

        user = Parcels.unwrap(getArguments().getParcelable("user"));
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
        client.getUserTimeline(user.screenName, 25, sinceId, maxId, new JsonHttpResponseHandler(){
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
        client.getUserTimeline(user.screenName, 25,sinceId, maxId, new JsonHttpResponseHandler(){
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
