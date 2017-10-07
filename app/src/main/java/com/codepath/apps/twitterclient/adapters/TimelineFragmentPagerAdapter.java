package com.codepath.apps.twitterclient.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.codepath.apps.twitterclient.fragments.HometimelineFragment;
import com.codepath.apps.twitterclient.fragments.MentionsFragment;
import com.codepath.apps.twitterclient.fragments.SearchFragment;
import com.codepath.apps.twitterclient.utils.SmartFragmentStatePagerAdapter;

/**
 * Created by m3libea on 10/3/17.
 */

public class TimelineFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

    final int PAGE_COUNT = 3;

    public TimelineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {

        Fragment f;
        if (position == 0) {
            f = new HometimelineFragment();
        } else if (position == 1) {
            f = new MentionsFragment();
        } else if (position == 2){
            f = new SearchFragment();
        } else {
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
        return null;
    }
}
