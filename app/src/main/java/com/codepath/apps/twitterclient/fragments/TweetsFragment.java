package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsAdapter;
import com.codepath.apps.twitterclient.databinding.FragmentTweetsBinding;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.TweetDividerDecoration;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m3libea on 10/2/17.
 */

public class TweetsFragment extends Fragment {

    private TweetsAdapter aTweets;

    public ArrayList<Tweet> tweets;

    public FragmentTweetsBinding binding;
    public LinearLayoutManager lyManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tweets, container, false);

        tweets = new ArrayList<>();

        setRecyclerView();

        return binding.getRoot();

    }


    private void setRecyclerView() {
        aTweets = new TweetsAdapter(getContext(), tweets);
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
        binding.rvTweets.scrollToPosition(0);    }
}
