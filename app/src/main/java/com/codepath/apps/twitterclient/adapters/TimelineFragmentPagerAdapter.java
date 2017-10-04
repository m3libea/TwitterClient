package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.codepath.apps.twitterclient.fragments.HometimelineFragment;
import com.codepath.apps.twitterclient.fragments.MentionsFragment;
import com.codepath.apps.twitterclient.utils.SmartFragmentStatePagerAdapter;

/**
 * Created by m3libea on 10/3/17.
 */

public class TimelineFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Tab1", "Tab2"};
    private Context context;

    public TimelineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {

        Fragment f;
        Log.d("Adapter", "Get Item " + position);

        if (position == 0){
            Log.d("Adapter", "Hometimeline " + position);
    
            f = new HometimelineFragment();
        }else if(position == 1){
            f = new MentionsFragment();
        }else{
            f = null;
        }
        return f;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d("Adapter","Position of the tab " +  position);
        return tabTitles[position];
    }
}
