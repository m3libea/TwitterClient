package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.activities.UserActivity;
import com.codepath.apps.twitterclient.databinding.ItemFollowBinding;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.utils.PatternEditableBuilder;

import org.parceler.Parcels;

import java.util.List;
import java.util.regex.Pattern;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

/**
 * Created by m3libea on 9/25/17.
 */

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.ViewHolder>{

    private Context context;
    private List<User> users;

    private TweetClickListener listener;

    public void setListener(TweetClickListener listener) {
        this.listener = listener;
    }

    public interface TweetClickListener {
        void showUser(String screename);
        void showHT(String query);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemFollowBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(User user) {

            Glide.with(getContext())
                    .load(user.getProfileImageURL())
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
                Intent i = new Intent(context, UserActivity.class);
                i.putExtra("user", Parcels.wrap(user));
                context.startActivity(i);
            });

            binding.ivProfile.setOnClickListener(view -> {
                Intent i = new Intent(context, UserActivity.class);
                i.putExtra("user", Parcels.wrap(user));
                context.startActivity(i);
            });
            //SPAN hashtag, user

            binding.tvDescription.setText(user.getDescription());
            new PatternEditableBuilder()
                    .addPattern(Pattern.compile("\\@(\\w+)"), ContextCompat.getColor(context, R.color.primary),
                            text -> listener.showUser(text.replace("@","")))
                    .addPattern(Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.primary_dark),
                            text -> listener.showHT(text))
                    .into(binding.tvDescription);
        }
    }



    public FollowAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View followView = inflater.inflate(R.layout.item_follow, parent, false);

        return new ViewHolder(followView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final User user = users.get(position);

        holder.bind(user);
        holder.binding.setUser(user);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


}
