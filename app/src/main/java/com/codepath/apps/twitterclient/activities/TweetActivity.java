package com.codepath.apps.twitterclient.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivityTweetBinding;
import com.codepath.apps.twitterclient.fragments.ComposeFragment;
import com.codepath.apps.twitterclient.models.MediaTweet;
import com.codepath.apps.twitterclient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetActivity extends AppCompatActivity implements ComposeFragment.ComposeDialogListener{

    private final String TAG = "TweetActivity";
    private TwitterClient client;
    private Tweet tweet;
    ActivityTweetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet);


        client = TwitterApplication.getRestClient();

        tweet = Parcels.unwrap(getIntent().getExtras().getParcelable("tweet"));

        setupView();


    }

    private void setupView() {
        setupActionBar();
        binding.setTweet(tweet);

        if((tweet.getRtCount() == 0) && (tweet.getfCount() == 0)){
            binding.viewt.setVisibility(View.GONE);
        }

        if(tweet.getRtCount() == 0){
            binding.tvRT.setVisibility(View.GONE);
        }

        if(tweet.getfCount() == 0){
            binding.tvLikes.setVisibility(View.GONE);
        }



        //Profile image
        Glide.with(this)
                .load(tweet.getUser().getProfileImageURL())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(binding.ivProfile) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        binding.ivProfile.setImageDrawable(circularBitmapDrawable);
                    }
                });

        binding.ivProfile.setOnClickListener(view -> {
            Intent i = new Intent(this, UserActivity.class);
            i.putExtra("user", Parcels.wrap(tweet.user));
            startActivity(i);
        });

        //Set Media
        MediaTweet m = tweet.getOneMedia();

        if(m != null){
            if (m.getType().equals("photo")) {

                Glide.with(this)
                        .load(m.getMediaUrl())
                        .bitmapTransform(new RoundedCornersTransformation(this, 20, 5))
                        .into(binding.ivMedia);
                binding.ivMedia.setVisibility(View.VISIBLE);
            }else {
                setVideo(m.getVideoURL());

                binding.vvVideo.setVisibility(View.VISIBLE);
            }

        }

        binding.btReply.setOnClickListener(view -> composeReply());
    }

    private void setVideo(String url) {
        binding.vvVideo.setVideoPath(url);

        final boolean[] videoTouched = {false};

        binding.vvVideo.setOnPreparedListener(mp -> mp.setLooping(true));

        Handler mHandler = new Handler();

        binding.vvVideo.setOnTouchListener((view, motionEvent) -> {
            if(!videoTouched[0]) {
                videoTouched[0] = true;
            }
            if (binding.vvVideo.isPlaying()) {
                binding.vvVideo.pause();
            } else {
                binding.vvVideo.start();
            }
            mHandler.postDelayed(() -> videoTouched[0] = false, 100);
            return true;
        });
        binding.vvVideo.setOnErrorListener((media, what, extra) -> {
            if (what == 100)
            {
                binding.vvVideo.stopPlayback();
                setVideo(url);
            }
            return true;
        });
    }

    private void composeReply() {
        binding.btReply.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment tweetCompose = ComposeFragment.newInstance(tweet.getUser().getScreenName());
            tweetCompose.show(fm, "fragment_compose");
        });
    }

    private void setupActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Tweet");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishingTweet(String body, Boolean tweet) {
        String minBody = body.substring(0, Math.min(getResources().getInteger(R.integer.max_tweet_length), body.length()));
        if (isNetworkAvailable()){
            client.composeReply(this.tweet.getUser().getScreenName(), this.tweet.getUid(), minBody, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "Tweet created: " + response.toString());
                    Toast toast = Toast.makeText(TweetActivity.this, R.string.toastTweetCreated, Toast.LENGTH_SHORT);
                    toast.show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, errorResponse.toString());
                }
            });
        }else{
            Snackbar bar = Snackbar.make(findViewById(R.id.activity_tweet), getResources().getString(R.string.connection_error) , Snackbar.LENGTH_LONG);
            bar.show();
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
