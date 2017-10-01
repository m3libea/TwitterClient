package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.activities.TweetActivity;
import com.codepath.apps.twitterclient.databinding.ItemTweetBinding;
import com.codepath.apps.twitterclient.models.MediaTweet;
import com.codepath.apps.twitterclient.models.Tweet;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

/**
 * Created by m3libea on 9/25/17.
 */

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private Context context;
    private List<Tweet> tweets;

    public class ViewHolder extends RecyclerView.ViewHolder{

        ItemTweetBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(Tweet tweet){

            Glide.with(getContext())
                    .load(tweet.getUser().getProfileImageURL())
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(binding.ivProfile) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            binding.ivProfile.setImageDrawable(circularBitmapDrawable);
                    }
            });

            binding.llRow.setOnClickListener(view -> {
                Intent i = new Intent(context, TweetActivity.class);
                i.putExtra("tweet", Parcels.wrap(tweet));
                context.startActivity(i);
            });

            MediaTweet m = tweet.getOneMedia();
            if(m != null){

                if (m.getType().equals("photo")) {
                    Glide.with(getContext())
                            .load(m.getMediaUrl())
                            .bitmapTransform(new RoundedCornersTransformation(getContext(), 20, 5))
                            .into(binding.ivMedia);
                    binding.ivMedia.setVisibility(View.VISIBLE);
                    binding.vvVideo.setVisibility(View.GONE);
                }else{
                    binding.vvVideo.setVisibility(View.VISIBLE);
                    setVideo(m.getVideoURL());
                    binding.ivMedia.setVisibility(View.GONE);
                }


            }else{
                binding.ivMedia.setVisibility(View.GONE);
                binding.vvVideo.setVisibility(View.GONE);
            }

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
                mHandler.postDelayed(() -> videoTouched[0] = false, 200);
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
    }



    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);

        return new ViewHolder(tweetView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Tweet tweet = tweets.get(position);

        holder.bind(tweet);
        holder.binding.setTweet(tweet);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }


}
