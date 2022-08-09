/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.huawei.hms.videokit.hdrvivid.ability.HdrAbility;
import com.huawei.video.kit.hdrvivid.demo.R;
import com.huawei.video.kit.hdrvivid.demo.SimpleJni;
import com.huawei.video.kit.hdrvivid.demo.SimpleProcessor;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * the fragment page for brightness api test
 *
 * @since 2022/5/5
 */
public class TestAbilityAPIFragment extends Fragment {

    private int interfaceType = 0;

    private SimpleJni simpleJni = null;

    private SimpleProcessor simpleProcessor = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_ability_api_view, container, false);
        final TextView textResult = view.findViewById(R.id.text_result);

        RadioGroup interfaceGroup = view.findViewById(R.id.interface_type);
        interfaceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.interface_java:
                        interfaceType = 0;
                        break;
                    case R.id.interface_native:
                        interfaceType = 1;
                        break;
                    default:
                        break;
                }
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.outputColorFormat);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.getSupportedHdrType:
                        if (interfaceType == 0) {
                            String hdrType = HdrAbility.getSupportedHdrType();
                            textResult.setText(!TextUtils.isEmpty(hdrType) ? hdrType : "null");
                        } else {
                            initNative();
                            String hdrType = simpleJni.nativeGetSupportedHdrType();
                            textResult.setText(!TextUtils.isEmpty(hdrType) ? hdrType : "null");
                        }
                        break;
                    case R.id.setHdrAbility:
                        if (interfaceType == 0) {
                            textResult.setText(HdrAbility.setHdrAbility(true) + "");
                        } else {
                            initNative();
                            textResult.setText(simpleJni.nativeSetHdrAbility(true) + "");
                        }
                        break;
                }
            }
        });

        return view;
    }

    private void initNative() {
        SimpleSetting.getInstance().setFilePath(Constants.SRC_MOVIE_FILE_DIR);
        simpleJni = SimpleJni.getInstance();
        simpleProcessor = SimpleProcessor.getInstance();
        simpleProcessor.init(getContext());
    }
}
