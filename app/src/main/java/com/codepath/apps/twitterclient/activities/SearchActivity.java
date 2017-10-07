package com.codepath.apps.twitterclient.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivitySearchBinding;
import com.codepath.apps.twitterclient.fragments.SearchFragment;

public class SearchActivity extends AppCompatActivity{

    private final String TAG = "SearchActivity";
    private TwitterClient client;
    String query;
    private ActivitySearchBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);


        client = TwitterApplication.getRestClient();

        query = getIntent().getStringExtra("query");

        setupView();
    }

    private void setupView() {
        setupActionBar();

        SearchFragment fm = SearchFragment.newInstance(query);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fmSearch, fm)
                .commit();
    }

    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
