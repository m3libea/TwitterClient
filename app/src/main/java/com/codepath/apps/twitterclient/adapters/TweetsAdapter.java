package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

/**
 * Created by m3libea on 9/25/17.
 */

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private Context context;
    private List<Tweet> tweets;

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.ivProfile)
        ImageView ivProfile;
        @BindView(R.id.tvScreenname)
        TextView tvScreenName;
        @BindView(R.id.tvUsername)
        TextView tvUsername;
        @BindView(R.id.tvBody)
        TextView tvBody;
        @BindView(R.id.tvDate)
        TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(Tweet tweet){
            tvUsername.setText(tweet.getUser().getName());
            tvScreenName.setText(tweet.getUser().getScreenName());
            tvBody.setText(tweet.getBody());
            tvDate.setText(tweet.getRelativeTimeAgo());

            Glide.with(getContext())
                    .load(tweet.getUser().getProfileImageURL())
                    .into(ivProfile);

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

        Tweet tweet = tweets.get(position);

        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}
