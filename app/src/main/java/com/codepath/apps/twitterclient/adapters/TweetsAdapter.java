package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.activities.TweetActivity;
import com.codepath.apps.twitterclient.activities.UserActivity;
import com.codepath.apps.twitterclient.databinding.ItemTweetBinding;
import com.codepath.apps.twitterclient.models.MediaTweet;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.PatternEditableBuilder;

import org.parceler.Parcels;

import java.util.List;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

/**
 * Created by m3libea on 9/25/17.
 */

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private Context context;
    private List<Tweet> tweets;
    private TweetActionListener actionListener;
    private TweetClickListener clickListener;

    public interface TweetActionListener {
        void reply(Tweet tweet);
        void setLikeStatus(Tweet tweet, boolean b);
        void retweet(Tweet tweet);

    }

    public interface TweetClickListener {
        void showUser(String screename);
        void showHT(String query);
    }

    public void setActionListener(TweetActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setClickListener(TweetClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ItemTweetBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(Tweet tweet){

            int idcolor = binding.btRt.isSelected() ? R.color.primary : R.color.lightText;
            int color = ContextCompat.getColor(context, idcolor);
            binding.btRt.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable icon = ContextCompat
                    .getDrawable(context, binding.btLike.isSelected()? R.drawable.ic_twitter_like : R.drawable.ic_twitter_like_outline);
            binding.btLike.setBackground(icon);

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

            binding.ivProfile.setOnClickListener(view -> {
                Intent i = new Intent(context, UserActivity.class);
                i.putExtra("user", Parcels.wrap(tweet.user));
                context.startActivity(i);
            });

            //Set Media
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

            if(tweet.getRetweeted()){
                binding.btRt.setEnabled(false);
                binding.btRt.getBackground()
                                .setColorFilter(ContextCompat
                                .getColor(context, R.color.primary) ,PorterDuff.Mode.SRC_IN);
            }else{
                binding.btRt.setOnClickListener(view -> {
                    view.setSelected(!view.isSelected());
                    setRtBT(tweet);

                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                    int iColor = view.isSelected() ? R.color.primary : R.color.lightText;
                    int c = ContextCompat.getColor(context, iColor);
                    view.getBackground().setColorFilter(c, PorterDuff.Mode.SRC_IN);
                    if (actionListener != null){
                        actionListener.retweet(tweet);
                    }
                    tweet.save();
                    view.setEnabled(false);
                });
            }

            binding.btLike.setOnClickListener(view -> {
                view.setSelected(!view.isSelected());
                setLikeBT(tweet);

                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                Drawable i = ContextCompat
                        .getDrawable(context, view.isSelected()? R.drawable.ic_twitter_like : R.drawable.ic_twitter_like_outline);
                view.setBackground(i);
                if (actionListener != null){
                    actionListener.setLikeStatus(tweet, tweet.getFavorited());
                }
                tweet.save();
            });

            binding.btReply.setOnClickListener(view -> {
                actionListener.reply(tweet);
            });

            //SPAN

            binding.tvBody.setText(tweet.getBody());
            setExtraInfo(tweet);

            // Style clickable spans based on pattern
            new PatternEditableBuilder()
                    .addPattern(Pattern.compile("\\@(\\w+)"), ContextCompat.getColor(context, R.color.primary),
                            text -> clickListener.showUser(text.replace("@","")))
                    .addPattern(Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.primary_dark),
                            text -> clickListener.showHT(text))
                    .into(binding.tvBody);


        }

        private void setExtraInfo(Tweet tweet) {
            if (tweet.getRetweet()){
                binding.tvExtra.setText("Retweeted by " + tweet.getRetweetedBy());
                binding.ivExtra.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_twitter_retweet));
                binding.llInfo.setVisibility(View.VISIBLE);
            }else if (tweet.getReplyTo() != null) {
                binding.tvExtra.setText("Reply to " + tweet.getReplyTo());
                binding.ivExtra.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_reply));
                binding.llInfo.setVisibility(View.VISIBLE);
            }else{
                binding.llInfo.setVisibility(View.GONE);
            }
        }

        private void setLikeBT(Tweet tweet) {
            tweet.setFavorited(binding.btLike.isSelected());
            tweet.setfCount(tweet.getFavorited()? tweet.getfCount() + 1 : tweet.getfCount() - 1);
            if (tweet.getfCount() == 0){
                binding.tvLikes.setText(" ");
            }else{
                binding.tvLikes.setText(Integer.toString(tweet.getfCount()));
            }

        }

        private void setRtBT(Tweet tweet) {
            tweet.setRetweeted(binding.btRt.isSelected());
            tweet.setRtCount(tweet.getRtCount() + 1 );
            binding.tvRT.setText(Integer.toString(tweet.getRtCount()));
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
