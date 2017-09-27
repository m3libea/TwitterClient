package com.codepath.apps.twitterclient.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.databinding.FragmentComposeBinding;

public class ComposeFragment extends DialogFragment {

    FragmentComposeBinding binding;

    public interface ComposeDialogListener {
        void onFinishingFilter(String body, Boolean tweet);
    }
    public ComposeFragment() {

    }

    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_compose, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        int maxTweetChar = getResources().getInteger(R.integer.max_tweet_length);
        binding.tvReply.setVisibility(View.GONE);
        binding.tvCharsLeft.setText(Integer.toString(maxTweetChar));
        binding.btnTweet.setOnClickListener(view1 -> postTweet());
        binding.btnClose.setOnClickListener(view12 -> close());
        binding.etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int left = maxTweetChar - editable.length();

                if (left < 0){
                    binding.tvCharsLeft.setTextColor(ContextCompat.getColor(getContext(),R.color.badText));
                }else{
                    binding.tvCharsLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.lightText));
                }

                binding.tvCharsLeft.setText(Integer.toString(left));

            }
        });
    }

    private void close() {
        ComposeDialogListener listener = (ComposeDialogListener) getActivity();
        listener.onFinishingFilter(null, false);
        dismiss();
    }

    private void postTweet() {
        ComposeDialogListener listener = (ComposeDialogListener) getActivity();

        String body = binding.etBody.getText().toString();

        if(body.isEmpty()) {
            Toast toast = Toast.makeText(getActivity(), R.string.toastEmptyTweet, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            listener.onFinishingFilter(body, true);
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
