package com.tanpn.applocker.intro;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;


public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter(FragmentManager fm) {
        super(fm);
    }

    private ViewPager viewPager;

    public IntroAdapter(FragmentManager fm, ViewPager viewPager) {
        super(fm);

        this.viewPager = viewPager;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroFragment.newInstance(Color.parseColor("#cfaf1f"), position);
            case 1:
                return IntroFragment.newInstance(Color.parseColor("#f44973"), position);
            case 2:
                return IntroFragment.newInstance(Color.parseColor("#1fcfbb"), position);
            case 3:
                return IntroFragment.newInstance(Color.parseColor("#3392ff"), position);
            case 4:
                return IntroFragment.newInstance(Color.parseColor("#c870f4"), position, viewPager);

            default:
                return IntroFragment.newInstance(Color.parseColor("#95a5a6"), position);
        }
    }

    @Override
    public int getCount() {
        return 6;
    }

}
