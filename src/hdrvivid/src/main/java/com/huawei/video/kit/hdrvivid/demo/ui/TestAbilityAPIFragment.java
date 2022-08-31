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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.huawei.hms.videokit.hdrability.ability.HdrAbility;
import com.huawei.video.kit.hdrvivid.demo.R;

/**
 * the fragment page for brightness api test
 *
 * @since 2022/5/5
 */
public class TestAbilityAPIFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_ability_api_view, container, false);
        final TextView textResult = view.findViewById(R.id.text_result);

        RadioGroup radioGroup = view.findViewById(R.id.interfaceName);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.initHdrAbility:
                        int ret = HdrAbility.init(getContext());
                        textResult.setText(Integer.toString(ret));
                        break;
                    case R.id.getSupportedHdrType:
                        String hdrType = HdrAbility.getSupportedHdrType();
                        textResult.setText(!TextUtils.isEmpty(hdrType) ? hdrType : "null");
                        break;
                    case R.id.setHdrAbility:
                        textResult.setText(HdrAbility.setHdrAbility(true) + "");
                        break;
                }
            }
        });

        return view;
    }
}
