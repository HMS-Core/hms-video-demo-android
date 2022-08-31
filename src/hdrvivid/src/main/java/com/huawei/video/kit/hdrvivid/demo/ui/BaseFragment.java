/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.huawei.hms.videokit.hdrability.ability.HdrAbility;
import com.huawei.hms.videokit.hdrvivid.render.HdrVividRender;
import com.huawei.video.kit.hdrvivid.demo.R;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleErrorUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;
import com.huawei.video.kit.hdrvivid.demo.utils.VideoInfoUtils;

/**
 * base fragment
 *
 * @since 2022/5/5
 */
public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

    private View view;

    private RadioGroup inputModeRadioGroup;

    private RadioGroup outputModeRadioGroup;

    private RadioGroup outputColorSpaceRadioGroup;

    private RadioGroup outputColorFormatRadioGroup;

    private EditText brightnessEditText;

    private Spinner spinner;

    private Button startPlayButton;

    public static void startMpSurfaceActivity(final Context context) {
        if (context == null) {
            return;
        }

        context.startActivity(new Intent(context, SimplePreview.class));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.base_fragment, container, false);

        inputModeRadioGroup = view.findViewById(R.id.inputMode);
        spinner = view.findViewById(R.id.spinner);
        startPlayButton = view.findViewById(R.id.startPlay);
        outputColorSpaceRadioGroup = view.findViewById(R.id.outputColorSpace);
        outputColorFormatRadioGroup = view.findViewById(R.id.outputColorFormat);
        outputModeRadioGroup = view.findViewById(R.id.outputMode);
        brightnessEditText = view.findViewById(R.id.brightness);
        listAllMp4Files();
        setOnCheckedChangeListener();

        return view;
    }

    private void listAllMp4Files() {
        Log.d(TAG, "listAllMp4Files begin");
        spinner = view.findViewById(R.id.spinner);

        final File file = new File(Constants.SRC_MOVIE_FILE_DIR);
        if (!file.canRead()) {
            Log.d(TAG, "has no permission");
            return;
        }

        if (!file.isDirectory()) {
            Log.d(TAG, "wrong path");
            return;
        }

        MediaScannerConnection.scanFile(getContext(), new String[] {Constants.SRC_MOVIE_FILE_DIR}, null,
            new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            File[] files = file.listFiles();
                            List<String> fileNames = new ArrayList<>();
                            for (File f : files) {
                                if (f.isFile()) {
                                    fileNames.add(f.getName());
                                }
                            }

                            ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getContext(), R.layout.file_spinner_item, fileNames);
                            spinner.setAdapter(adapter);
                        }
                    });
                }
            });
    }

    private boolean getDemoSetting() {
        switch (inputModeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.input_surface:
                SimpleSetting.getInstance().setInputMode(Constants.INPUT_MODE_SURFACE);
                break;
            default:
                return false;
        }

        switch (outputModeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.output_surface:
                SimpleSetting.getInstance().setOutputMode(Constants.OUT_MODE_SURFACE);
                break;
            case R.id.output_buffer:
                SimpleSetting.getInstance().setOutputMode(Constants.OUT_MODE_BUFFER);
                break;
            default:
                return false;
        }

        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER) {

            switch (outputColorSpaceRadioGroup.getCheckedRadioButtonId()) {
                case R.id.output_709:
                    SimpleSetting.getInstance().setOutputColorSpace(HdrVividRender.COLORSPACE_BT709);
                    break;
                case R.id.output_p3:
                    SimpleSetting.getInstance().setOutputColorSpace(HdrVividRender.COLORSPACE_P3);
                    break;
                case R.id.output_2020:
                    SimpleSetting.getInstance().setOutputColorSpace(HdrVividRender.COLORSPACE_BT2020);
                    break;
                default:
                    return false;
            }

            switch (outputColorFormatRadioGroup.getCheckedRadioButtonId()) {
                case R.id.outputFormat_R8G8B8A8:
                    SimpleSetting.getInstance().setOutputColorFormat(HdrVividRender.COLORFORMAT_R8G8B8A8);
                    break;
                case R.id.outputFormat_NV12:
                    SimpleSetting.getInstance().setOutputColorFormat(HdrVividRender.COLORFORMAT_NV12);
                    break;
                case R.id.outputFormat_YUV420888:
                    SimpleSetting.getInstance().setOutputColorFormat(HdrVividRender.COLORFORMAT_YUV420_888);
                    break;
                case R.id.outputFormat_YUV420P10:
                    SimpleSetting.getInstance().setOutputColorFormat(HdrVividRender.COLORFORMAT_YUV420P10);
                    break;
                default:
                    return false;
            }
        }

        SimpleSetting.getInstance().setBrightness(Integer.parseInt(brightnessEditText.getText().toString()));
        if (spinner.getSelectedItem() == null) {
            return false;
        }
        SimpleSetting.getInstance().setFilePath(Constants.SRC_MOVIE_FILE_DIR + spinner.getSelectedItem().toString());
        return true;
    }

    private boolean isValidSetting() {
        Log.d(TAG, "isValidSetting " + brightnessEditText.getText().toString());
        String brightnessStr = brightnessEditText.getText().toString();
        if (!brightnessStr.isEmpty()) {
            try {
                int brightness = Integer.parseInt(brightnessStr);
                if (brightness < HdrAbility.BRIGHTNESS_NIT_MIN || brightness > HdrAbility.BRIGHTNESS_NIT_MAX) {
                    this.brightnessEditText
                        .setError(HdrAbility.BRIGHTNESS_NIT_MIN + "~" + HdrAbility.BRIGHTNESS_NIT_MAX);
                    return false;
                }
            } catch (NumberFormatException e) {
                this.brightnessEditText.setError(HdrAbility.BRIGHTNESS_NIT_MIN + "~" + HdrAbility.BRIGHTNESS_NIT_MAX);
                return false;
            }
        } else {
            this.brightnessEditText.setError(HdrAbility.BRIGHTNESS_NIT_MIN + "~" + HdrAbility.BRIGHTNESS_NIT_MAX);
            return false;
        }

        return true;
    }

    private void setOnCheckedChangeListener() {
        startPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidSetting()) {
                    return;
                }
                boolean ret = getDemoSetting();
                if (!ret) {
                    Log.d(TAG, "getDemoSetting failed");
                    return;
                }
                ret = VideoInfoUtils.getInstance().getVideoInfoFromFile(SimpleSetting.getInstance().getFilePath());
                if (!ret) {
                    Log.d(TAG, "initVideoInfo failed");
                    SimpleErrorUtils.showErrToast(SimpleErrorUtils.SIMPLE_ERR_FORMAT_NOT_SUPPORT);
                    return;
                }

                startMpSurfaceActivity(getContext());
            }
        });

        inputModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = inputModeRadioGroup.getCheckedRadioButtonId();
                RadioButton output2020 = view.findViewById(R.id.output_2020);
                if (outputModeRadioGroup.getCheckedRadioButtonId() == R.id.output_buffer) {
                    switch (id) {
                        case R.id.input_surface:
                            output2020.setEnabled(true);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        outputModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = outputModeRadioGroup.getCheckedRadioButtonId();
                Log.d(TAG, "outputModeRadioGroup onCheckedChanged " + id + " " + R.id.output_surface + " "
                    + R.id.output_buffer);
                switch (id) {
                    case R.id.output_surface:
                        outputColorSpaceRadioGroup.clearCheck();
                        for (int index = 0; index < outputColorSpaceRadioGroup.getChildCount(); index++) {
                            outputColorSpaceRadioGroup.getChildAt(index).setEnabled(false);
                        }

                        outputColorFormatRadioGroup.clearCheck();
                        for (int index = 0; index < outputColorFormatRadioGroup.getChildCount(); index++) {
                            outputColorFormatRadioGroup.getChildAt(index).setEnabled(false);
                        }
                        break;
                    case R.id.output_buffer:
                        RadioButton output709 = view.findViewById(R.id.output_709);
                        RadioButton output2020 = view.findViewById(R.id.output_2020);
                        RadioButton outputFormatR8G8B8A8 = view.findViewById(R.id.outputFormat_R8G8B8A8);

                        for (int index = 0; index < outputColorSpaceRadioGroup.getChildCount(); index++) {
                            RadioButton radioButton = (RadioButton) outputColorSpaceRadioGroup.getChildAt(index);
                            radioButton.setEnabled(true);
                        }

                        for (int index = 0; index < outputColorFormatRadioGroup.getChildCount(); index++) {
                            RadioButton radioButton = (RadioButton) outputColorFormatRadioGroup.getChildAt(index);
                            radioButton.setEnabled(true);
                        }

                        output709.setChecked(true);
                        outputFormatR8G8B8A8.setChecked(true);

                        break;
                    default:
                        break;
                }
            }
        });

        outputColorSpaceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = outputColorSpaceRadioGroup.getCheckedRadioButtonId();

                RadioButton outputFormatR8G8B8A8 = view.findViewById(R.id.outputFormat_R8G8B8A8);
                RadioButton outputFormatNV12 = view.findViewById(R.id.outputFormat_NV12);
                RadioButton outputFormatYUV420888 = view.findViewById(R.id.outputFormat_YUV420888);
                RadioButton outputFormatYUV420P10 = view.findViewById(R.id.outputFormat_YUV420P10);
                Log.d(TAG, "outputColorSpaceRadioGroup onCheckedChanged " + id + " " + R.id.output_709 + " "
                    + R.id.output_buffer);

                // input mode is surface, support: BT2020+YUV40P10、BT709/P3+NV12/YUV420888/R8G8B8A8
                // input mode is buffer, support: BT709/P3+NV12/YUV420888/R8G8B8A8
                switch (id) {
                    case R.id.output_709:
                    case R.id.output_p3:
                        outputFormatR8G8B8A8.setEnabled(true);
                        outputFormatNV12.setEnabled(true);
                        outputFormatYUV420888.setEnabled(true);
                        outputFormatYUV420P10.setEnabled(false);
                        if (outputFormatYUV420P10.isChecked()) {
                            outputFormatYUV420P10.setChecked(false);
                            outputFormatR8G8B8A8.setChecked(true);
                        }
                        break;
                    case R.id.output_2020:
                        outputFormatR8G8B8A8.setEnabled(false);
                        outputFormatNV12.setEnabled(false);
                        outputFormatYUV420888.setEnabled(false);
                        outputFormatYUV420P10.setEnabled(true);
                        outputFormatYUV420P10.setChecked(true);
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
