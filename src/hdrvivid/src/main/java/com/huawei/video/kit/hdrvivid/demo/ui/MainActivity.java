/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huawei.video.kit.hdrvivid.demo.R;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleErrorUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;

/**
 * the main page for api test
 * 
 * @since 2022/5/5
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE =
        {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private SimplePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initViews();
        SimpleErrorUtils.setContext(getApplicationContext());
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new SimplePagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(final int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    SimpleSetting.getInstance().setApiType(Constants.API_TYPE_NATIVE);
                } else if (position == 1) {
                    SimpleSetting.getInstance().setApiType(Constants.API_TYPE_JAVA);
                } else {
                    SimpleSetting.getInstance().setApiType(Constants.API_TYPE_NATIVE);
                }

                Log.d(TAG, "addOnPageChangeListener, position: " + position);
            }
        });

        verifyStoragePermissions(this);
    }

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}