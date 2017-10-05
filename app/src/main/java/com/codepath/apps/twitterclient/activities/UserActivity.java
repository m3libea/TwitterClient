package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.api.TwitterClient;
import com.codepath.apps.twitterclient.databinding.ActivityUserBinding;
import com.codepath.apps.twitterclient.fragments.UserTimelineFragment;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;

import org.parceler.Parcels;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    private final String TAG = "UserActivity";
    private TwitterClient client;
    private User user;

    private ArrayList<Tweet> tweets;

    ActivityUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user);

        client = TwitterApplication.getRestClient();

        user = Parcels.unwrap(getIntent().getExtras().getParcelable("user"));

        setupView();

    }

    private void setupView() {
        setupToolbar();
        binding.setUser(user);

        Glide.with(this)
                .load(user.getProfileImageURL())
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
        Glide.with(this)
                .load(user.getProfileBannerURL())
                .centerCrop()
                .into(binding.ivBanner);

        Fragment fragment = UserTimelineFragment.newInstance(user);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fmTimeline, fragment)
                .commit();

        binding.tvFollowers.setOnClickListener(view -> {
            Intent i = new Intent(UserActivity.this, FollowActivity.class);
            i.putExtra("user", Parcels.wrap(user));
            startActivity(i);

        });

        binding.tvFollowing.setOnClickListener(view -> {
            Intent i = new Intent(UserActivity.this, FollowActivity.class);
            i.putExtra("user", Parcels.wrap(user));
            i.putExtra("following", true);
            startActivity(i);

        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(user.getName());
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
