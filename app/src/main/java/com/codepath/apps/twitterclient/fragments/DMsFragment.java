package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.DMsAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.FragmentTweetsBinding;
import com.codepath.apps.twitterclient.external.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.models.DirectMessage;
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

public class DMsFragment extends Fragment {

    private DMsAdapter aDMs;

    public ArrayList<DirectMessage> dms;
    public TwitterClient client;


    public FragmentTweetsBinding binding;
    public LinearLayoutManager lyManager;
    private final String TAG = "DMsFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets, container, false);
        client = TwitterApplication.getRestClient();

        dms = new ArrayList<>();

        setRecyclerView();

        getDMessages();

        return binding.getRoot();

    }

    private void getDMessages() {

        if (isNetworkAvailable()) {
            showProgressBar();
            populate(25, 1, -1);
        } else {
            // Go to persistence
            hideProgressBar();
            DirectMessage.getLastDirectMessages(50).forEach(this::addDM);
        }
    }

    private void populate(int count, long sinceId, long maxId) {
        client.getDirectMessages(count, sinceId,maxId, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Populate dms: " + response.toString());

                hideProgressBar();

                List<DirectMessage> rDMessages = DirectMessage.fromJSONArray(response);

                for(DirectMessage d : rDMessages){
                    appendMessage(d);
                }
                binding.swipeContainer.setRefreshing(false);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "Error: " + errorResponse.toString());

                hideProgressBar();
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }


    private void setRecyclerView() {
        aDMs = new DMsAdapter(getContext(), dms);
        binding.rvTweets.setAdapter(aDMs);
        lyManager = new LinearLayoutManager(getContext());
        binding.rvTweets.setLayoutManager(lyManager);

        //Line between rows
        TweetDividerDecoration line = new TweetDividerDecoration(getContext());
        binding.rvTweets.addItemDecoration(line);

        binding.swipeContainer.setColorSchemeResources(R.color.primary,
                R.color.primary_dark,
                R.color.twitterLight,
                R.color.accent);

        //Set listener for refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            Log.d("TAG", "Refresh");
            if(isNetworkAvailable()){
                populate(25, dms.isEmpty() ? 1 : dms.get(0).getUid(), -1);
            }else{
                Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG);
                bar.show();
                binding.swipeContainer.setRefreshing(false);
            }

        });

        //Set listener for endlessscrol

        EndlessRecyclerViewScrollListener listener = new EndlessRecyclerViewScrollListener(lyManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (isNetworkAvailable()) {
                    showProgressBar();
                    populate(25, 1, dms.get(dms.size() - 1).getUid() - 1);
                } else {
                    Snackbar bar = Snackbar.make(getActivity().findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT);
                    bar.show();
                }
            }
        };

        binding.rvTweets.addOnScrollListener(listener);

    }

    public void addDM(DirectMessage d) {
        if (dms.isEmpty()) {
            appendMessage(d);
        } else if (d.getUid() > dms.get(0).getUid()) {
            prependMessage(d);
        } else if (d.getUid() <= dms.get(dms.size() - 1).getUid()) {
            appendMessage(d);
        }
    }

    private void appendMessage(DirectMessage t) {
        dms.add(t);
        aDMs.notifyItemInserted(dms.size());
    }

    private void prependMessage(DirectMessage t) {
        // Prepend to list and scroll
        dms.add(0, t);
        aDMs.notifyItemInserted(0);
        binding.rvTweets.scrollToPosition(0);
    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void showProgressBar(){
        binding.pBar.setVisibility(View.VISIBLE);
    }
    public void hideProgressBar(){
        binding.pBar.setVisibility(View.GONE);
    }
}
