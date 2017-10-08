package com.codepath.apps.twitterclient.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TimelineFragmentPagerAdapter;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivityTimelineBinding;
import com.codepath.apps.twitterclient.fragments.ComposeFragment;
import com.codepath.apps.twitterclient.fragments.SearchFragment;
import com.codepath.apps.twitterclient.fragments.TweetsFragment;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.ComposeDialogListener{

    private final String TAG = "Timeline";
    private User user;

    private TwitterClient client;

    ActivityTimelineBinding binding;
    FragmentManager fm;
    private TimelineFragmentPagerAdapter aPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        client = TwitterApplication.getRestClient();

        fm = getSupportFragmentManager();

        aPager = new TimelineFragmentPagerAdapter(fm);

        setupView();

        getUser();

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
                Glide.with(TimelineActivity.this)
                        .load(user.getProfileImageURL())
                        .asBitmap()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(binding.ivProfile) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(TimelineActivity.this.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                binding.ivProfile.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "Error getting the account" + errorResponse.toString());
            }
        });
    }

    private void setupView() {
        setSupportActionBar(binding.toolbar);
        //getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View logo = binding.toolbar.getChildAt(0);

        logo.setOnClickListener(view -> {
            Intent i = new Intent(this, UserActivity.class);
            i.putExtra("user", Parcels.wrap(user));
            startActivity(i);
//            TweetsFragment fm = (TweetsFragment)aPager.getRegisteredFragment(binding.viewpager.getCurrentItem());
//            fm.binding.rvTweets.scrollToPosition(0);
        });


        //To avoid errors, increase the page limit
        binding.viewpager.setOffscreenPageLimit(3);
        binding.viewpager.setAdapter(aPager);

        binding.slidingTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.viewpager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.equals(binding.slidingTabs.getTabAt(2))){
                    SearchFragment sf = (SearchFragment) aPager.getRegisteredFragment(2);
                    sf.clean();
                }
            }
        });

        // Give the TabLayout the ViewPager
        binding.slidingTabs.setupWithViewPager(binding.viewpager);

        int[] imageResId = {
                R.drawable.ic_twitter_home,
                R.drawable.ic_twitter_alert_2,
                R.drawable.ic_twitter_search,
                R.drawable.ic_twitter_message};
        ColorStateList colors;

        if (Build.VERSION.SDK_INT >= 23) {
            colors = getResources().getColorStateList(R.color.tab_icon, getTheme());
        }
        else {
            colors = getResources().getColorStateList(R.color.tab_icon);
        }

        for (int i = 0; i < imageResId.length; i++) {

            TabLayout.Tab tab = binding.slidingTabs.getTabAt(i).setIcon(imageResId[i]);
            Drawable iconWrap = DrawableCompat.wrap(tab.getIcon());
            DrawableCompat.setTintList(iconWrap, colors);
            tab.setIcon(iconWrap);

        }

        binding.faCompose.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment tweetCompose = ComposeFragment.newInstance(user);
            tweetCompose.show(fm, "fragment_compose");
        });
    }


    @Override
    public void onFinishingTweet(String body, Tweet t, Boolean tweet) {
        if (tweet){
            if (isNetworkAvailable()) {
                client.composeTweet(body.substring(0, Math.min(getResources().getInteger(R.integer.max_tweet_length),
                        body.length())),
                        new JsonHttpResponseHandler(){
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                Tweet tweet = Tweet.fromJSON(response);

                                TweetsFragment fm = (TweetsFragment)aPager.getRegisteredFragment(0);
                                fm.addFront(tweet);
                                binding.viewpager.setCurrentItem(0);
                                Log.d(TAG, "Create Tweet: " + response.toString());

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.d(TAG, errorResponse.toString());
                            }
                        });
            }else{
                Snackbar bar = Snackbar.make(findViewById(R.id.activity_timeline), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG);
                bar.show();
            }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                binding.viewpager.setCurrentItem(2);
                ((SearchFragment) aPager.getRegisteredFragment(binding.viewpager.getCurrentItem())).clean();

                searchView.clearFocus();

                SearchFragment fm = (SearchFragment) aPager.getRegisteredFragment(2);

                fm.fetch(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuid = item.getItemId();


        switch (menuid) {
            case R.id.action_logout:
                //Dialog to ask user if want to logout the tweet on device or not.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_logout)
                        .setTitle(R.string.dialog_title_logout);

                builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                    client.clearAccessToken();
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);

                });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

                });

                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
