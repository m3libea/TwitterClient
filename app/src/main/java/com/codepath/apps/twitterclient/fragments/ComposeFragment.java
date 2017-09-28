package com.codepath.apps.twitterclient.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.databinding.FragmentComposeBinding;

import static android.content.ContentValues.TAG;

public class ComposeFragment extends DialogFragment {

    FragmentComposeBinding binding;
    String body;

    public interface ComposeDialogListener {
        void onFinishingTweet(String body, Boolean tweet);
    }
    public ComposeFragment() {

    }

    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment newInstance(String body) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("body", body);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_compose, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        body = (String) getArguments().get("body");

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
        //TODO check if empty, else draft

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
