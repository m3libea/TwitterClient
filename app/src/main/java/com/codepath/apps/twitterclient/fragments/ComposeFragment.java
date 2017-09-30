package com.codepath.apps.twitterclient.fragments;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.databinding.FragmentComposeBinding;
import com.codepath.apps.twitterclient.models.User;

import org.parceler.Parcels;

import static android.content.ContentValues.TAG;

public class ComposeFragment extends DialogFragment {

    FragmentComposeBinding binding;
    String body;
    User user;
    String username;


    public interface ComposeDialogListener {
        void onFinishingTweet(String body, Boolean tweet);
    }

    public ComposeFragment() {

    }

    public static ComposeFragment newInstance(User user ) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment newInstance(User user, String body) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("body", body);
        args.putParcelable("user", Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment newInstance(String username) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_compose, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        body = (String) getArguments().get("body");
        user = Parcels.unwrap(getArguments().getParcelable("user"));
        username = (String) getArguments().get("username");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        int maxTweetChar = getResources().getInteger(R.integer.max_tweet_length);
        binding.tvReply.setVisibility(View.GONE);
        binding.tvCharsLeft.setText(Integer.toString(maxTweetChar));

        //Set Body from intent

        if(body!=null){
            binding.etBody.setText(body);
            setCharsLeft(maxTweetChar, binding.etBody.getEditableText());
        }

        //If user not null, set image
        if(user!=null){
            Glide.with(getContext())
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
        }else{
            binding.ivProfile.setVisibility(View.GONE);
        }

        //Check if draft saved

        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (pref.contains("draft")){
            binding.btnDraft.setVisibility(View.VISIBLE);
            binding.btnDraft.setOnClickListener(view13 -> getDraft(pref, maxTweetChar));
        }

        //Check if user for reply

        if (username != null){
            binding.tvReply.setText("Replying to " + username);
            binding.tvReply.setVisibility(View.VISIBLE);
        }

        //Listener for buttons
        binding.btnTweet.setOnClickListener(view1 -> postTweet());
        binding.btnClose.setOnClickListener(view12 -> close());

        //Listener to get the counter of chars left
        binding.etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setCharsLeft(maxTweetChar, editable);

            }
        });

    }

    private void getDraft(SharedPreferences pref, int max) {
        String bDraft = pref.getString("draft", null);
        binding.etBody.setText(bDraft);
        setCharsLeft(max, binding.etBody.getEditableText());
        pref.edit().remove("draft").commit();
        binding.btnDraft.setVisibility(View.GONE);
    }

    private void setCharsLeft(int maxTweetChar, Editable editable) {
        int left = maxTweetChar - editable.length();

        if (left < 0){
            binding.tvCharsLeft.setTextColor(ContextCompat.getColor(getContext(),R.color.badText));
        }else{
            binding.tvCharsLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.lightText));
        }

        binding.tvCharsLeft.setText(Integer.toString(left));
    }

    private void close() {
        ComposeDialogListener listener = (ComposeDialogListener) getActivity();

        //If edit text not empty
        if (binding.etBody.getText().toString().trim().length() > 0) {
                //Dialog to ask user if want to store the tweet on device or not.
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);

                builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                    String bUser = binding.etBody.getText().toString();
                    Toast toast = Toast.makeText(getActivity(), R.string.toastDraftSaved, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(TAG, "OK pushed");
                    listener.onFinishingTweet(bUser, false);
                    dismiss();
                });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                    Toast toast = Toast.makeText(getActivity(), R.string.toastDraftNoSaved, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(TAG, "Cancel pushed");
                    listener.onFinishingTweet(null, false);
                    dismiss();
                });

                builder.create().show();
        }else{
            listener.onFinishingTweet(null, false);
            dismiss();
        }

    }

    private void postTweet() {
        ComposeDialogListener listener = (ComposeDialogListener) getActivity();

        String bUser = binding.etBody.getText().toString();

        if(bUser.isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.toastEmptyTweet, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            listener.onFinishingTweet(bUser, true);
            dismiss();
        }
    }

    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.75), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }
}
