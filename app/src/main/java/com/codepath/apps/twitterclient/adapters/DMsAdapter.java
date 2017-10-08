package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.databinding.ItemDmBinding;
import com.codepath.apps.twitterclient.models.DirectMessage;

import java.util.List;

/**
 * Created by m3libea on 10/8/17.
 */

public class DMsAdapter extends RecyclerView.Adapter<DMsAdapter.ViewHolder> {

    private Context context;
    private List<DirectMessage> dms;

    public class ViewHolder extends RecyclerView.ViewHolder{

        ItemDmBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(DirectMessage dm){

            Glide.with(context)
                    .load(dm.getSender().getProfileImageURL())
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
        }
    }



    public DMsAdapter(Context context, List<DirectMessage> dms) {
        this.context = context;
        this.dms = dms;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View followView = inflater.inflate(R.layout.item_dm, parent, false);

        return new ViewHolder(followView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final DirectMessage dm = dms.get(position);

        holder.bind(dm);
        holder.binding.setDm(dm);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return dms.size();
    }
}
