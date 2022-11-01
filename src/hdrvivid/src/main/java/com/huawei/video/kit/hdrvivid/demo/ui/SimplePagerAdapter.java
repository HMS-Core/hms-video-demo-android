/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.huawei.video.kit.hdrvivid.demo.R;

/**
 * fragment pager adapter
 * 
 * @since 2022/5/5
 */
public class SimplePagerAdapter extends FragmentPagerAdapter {
    public static final int POSITION_IDX_NATIVE_RENDER = 0;

    public static final int POSITION_IDX_JAVA_RENDER = 1;

    public static final int POSITION_IDX_BRIGHTNESS = 2;
    private final String[] titles;

    public SimplePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        titles = new String[] {context.getString(R.string.tab_title_native_interface),
            context.getString(R.string.tab_title_java_interface),
            context.getString(R.string.tab_title_brightness_interface)};
    }

    @Override
    public Fragment getItem(int position) {
        if (position == POSITION_IDX_NATIVE_RENDER) {
            return new TestNativeRenderAPIFragment();
        } else if (position == POSITION_IDX_JAVA_RENDER) {
            return new TestJavaRenderAPIFragment();
        } else if (position == POSITION_IDX_BRIGHTNESS) {
            return new TestAbilityAPIFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
