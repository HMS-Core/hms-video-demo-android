/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.videokit.hdrability.ability.HdrAbility;
import com.huawei.hms.videokit.hdrvivid.render.HdrVividRender;
import com.huawei.video.kit.hdrvivid.demo.R;
import com.huawei.video.kit.hdrvivid.demo.SimpleProcessor;
import com.huawei.video.kit.hdrvivid.demo.base.VideoInfo;
import com.huawei.video.kit.hdrvivid.demo.utils.Constants;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleSetting;
import com.huawei.video.kit.hdrvivid.demo.utils.SimpleTimeUtils;
import com.huawei.video.kit.hdrvivid.demo.utils.VideoInfoUtils;

/**
 * the preview page for api test
 *
 * @since 2022/5/5
 */
public class SimplePreview extends AppCompatActivity {
    private static final String TAG = "SimplePreview";

    private static final long DELAY_MILLIS = 100L;

    private static final float RATIO_HIGHLIGHT = 1.5f;

    private static final float RATIO_NORMAL = 1.0f;

    private Handler handler = new Handler();

    private boolean isShowInfo = false;

    private SurfaceView surfaceView;

    private CaptionView captionView;

    private SurfaceHolder surfaceHolder;

    private Button btnPlay;

    private Button btnStop;

    private TextView textViewVideoInfo;

    private Spinner brightnessSpinner;

    private static final int SPINNER_POSITION_HDR_LAYER = 0;

    private static final int SPINNER_POSITION_HDR_LAYER_RESUME = 1;

    private static final int SPINNER_POSITION_CAPTION_LAYER = 2;

    private static final int SPINNER_POSITION_CAPTION_LAYER_RESUME = 3;

    private static final int SPINNER_POSITION_INFO_SHOW = 4;

    private static final int SPINNER_POSITION_INFO_HIDE = 5;

    private SimpleProcessor simpleProcessor = null;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isShowInfo) {
                refreshVideoInfo();
                handler.postDelayed(this, DELAY_MILLIS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mp_surface_view);

        simpleProcessor = SimpleProcessor.getInstance();

        initView();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeSurfaceViewSize(VideoInfoUtils.getInstance().getVideoInfo().getWidth(),
            VideoInfoUtils.getInstance().getVideoInfo().getHeight());

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (simpleProcessor.status == SimpleProcessor.STATUS_PAUSEED) {
                simpleProcessor.resumePlay();
                btnPlay.setText(getApplicationContext().getString(R.string.video_control_pause));
            }

            // Avoid the layout misalignment during rotation.
            if (brightnessSpinner.getSelectedItemId() == SPINNER_POSITION_CAPTION_LAYER) {
                brightnessSpinner.setSelection(SPINNER_POSITION_CAPTION_LAYER_RESUME);
            } else {
                brightnessSpinner.setSelection(SPINNER_POSITION_CAPTION_LAYER);
            }
        }
    }

    private void changeSurfaceViewSize(int videoWidth, int videoHeight) {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = windowManager.getDefaultDisplay().getHeight();
        float max = Math.max((float) videoWidth / (float) width, (float) videoHeight / (float) height);

        int newWidth = (int) Math.ceil((float) videoWidth / max);
        int newHeight = (int) Math.ceil((float) videoHeight / max);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(newWidth, newHeight);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        Log.d(TAG,
            "changeSurfaceViewSize newWidth:" + newWidth + " newHeight:" + newHeight + " max:" + max + " " + width + " "
                + height + " " + videoWidth + " " + videoHeight + " " + getResources().getConfiguration().orientation);
    }

    private void initView() {
        surfaceView = findViewById(R.id.my_surface_view);
        btnPlay = findViewById(R.id.mp_surface_btn_play);
        btnStop = findViewById(R.id.mp_surface_btn_stop);
        brightnessSpinner = findViewById(R.id.brightness_spinner);
        textViewVideoInfo = findViewById(R.id.play_info);
        textViewVideoInfo.getBackground().setAlpha(0);

        captionView = findViewById(R.id.my_caption_view);
        captionView.setZOrderOnTop(true);
        captionView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated, apiType: " + SimpleSetting.getInstance().getApiType());
                simpleProcessor.initHdrAbility(surfaceView);

                simpleProcessor.init(getApplicationContext());

                simpleProcessor.setOutputSurface(surfaceView);

                changeSurfaceViewSize(VideoInfoUtils.getInstance().getVideoInfo().getWidth(),
                    VideoInfoUtils.getInstance().getVideoInfo().getHeight());

                simpleProcessor.startPlay();

                showVideoInfo();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged, width=" + width + ", height=" + height + ", format=" + format);
                simpleProcessor.resizeOutputSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed begin");

                simpleProcessor.stopPlay();
                simpleProcessor.release();
                stopShowVideoInfo();
                simpleProcessor.releaseHdrAbility();
                Log.d(TAG, "surfaceDestroyed end");
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (simpleProcessor.status == SimpleProcessor.STATUS_PLAYING) {
                    simpleProcessor.pausePlay();
                    btnPlay.setText(getApplicationContext().getString(R.string.video_control_play));
                } else {
                    simpleProcessor.resumePlay();
                    btnPlay.setText(getApplicationContext().getString(R.string.video_control_pause));
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleProcessor.stopPlay();
                simpleProcessor.release();
                stopShowVideoInfo();
                finish();
            }
        });

        List<String> actions = new ArrayList<>();
        actions.add(getApplicationContext().getString(R.string.btn_hdr_layer));
        actions.add(getApplicationContext().getString(R.string.btn_hdr_layer_resume));
        actions.add(getApplicationContext().getString(R.string.btn_caption_layer));
        actions.add(getApplicationContext().getString(R.string.btn_caption_layer_resume));
        actions.add(getApplicationContext().getString(R.string.btn_info_show));
        actions.add(getApplicationContext().getString(R.string.btn_info_hide));
        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(getApplicationContext(), R.layout.action_spinner_item, actions);
        brightnessSpinner.setAdapter(adapter);
        brightnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case SPINNER_POSITION_HDR_LAYER:
                        HdrAbility.setHdrLayer(surfaceView, true);
                        break;
                    case SPINNER_POSITION_HDR_LAYER_RESUME:
                        HdrAbility.setHdrLayer(surfaceView, false);
                        break;
                    case SPINNER_POSITION_CAPTION_LAYER:
                        HdrAbility.setCaptionsLayer(captionView, RATIO_HIGHLIGHT);
                        break;
                    case SPINNER_POSITION_CAPTION_LAYER_RESUME:
                        HdrAbility.setCaptionsLayer(captionView, RATIO_NORMAL);
                        break;
                    case SPINNER_POSITION_INFO_SHOW:
                        showVideoInfo();
                        break;
                    case SPINNER_POSITION_INFO_HIDE:
                        hideVideoInfo();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void hideVideoInfo() {
        textViewVideoInfo.setVisibility(View.INVISIBLE);
        captionView.setVisibility(View.INVISIBLE);
        isShowInfo = false;
    }

    private void showVideoInfo() {
        textViewVideoInfo.setVisibility(View.VISIBLE);
        captionView.setVisibility(View.VISIBLE);
        isShowInfo = true;
        handler.post(runnable);
    }

    private void stopShowVideoInfo() {
        isShowInfo = false;
        VideoInfoUtils.getInstance().onFinishPlay();
    }

    private void refreshVideoInfo() {
        Context context = getApplicationContext();
        VideoInfo videoInfo = VideoInfoUtils.getInstance().getVideoInfo();

        final StringBuffer sb = new StringBuffer();
        sb.append(context.getString(R.string.setting_api_type)).append(Constants.COLON);
        switch (SimpleSetting.getInstance().getApiType()) {
            case Constants.API_TYPE_JAVA:
                sb.append(Constants.DESC_API_TYPE_JAVA);
                break;
            case Constants.API_TYPE_NATIVE:
                sb.append(Constants.DESC_API_TYPE_NATIVE);
                break;
            default:
                break;
        }
        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.setting_input_mode)).append(Constants.COLON);
        switch (SimpleSetting.getInstance().getInputMode()) {
            case Constants.INPUT_MODE_SURFACE:
                sb.append(Constants.DESC_MODE_SURFACE);
                break;
            default:
                break;
        }

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.setting_output_mode)).append(Constants.COLON);
        switch (SimpleSetting.getInstance().getOutputMode()) {
            case Constants.OUT_MODE_SURFACE:
                sb.append(Constants.DESC_MODE_SURFACE);
                break;
            case Constants.OUT_MODE_BUFFER:
                sb.append(Constants.DESC_MODE_BUFFER);

                sb.append(Constants.CR_LF);
                sb.append(context.getString(R.string.setting_output_color_space)).append(Constants.COLON);
                switch (SimpleSetting.getInstance().getOutputColorSpace()) {
                    case HdrVividRender.COLORSPACE_BT709:
                        sb.append(Constants.DESC_COLORSPACE_BT709);
                        break;
                    case HdrVividRender.COLORSPACE_P3:
                        sb.append(Constants.DESC_COLORSPACE_P3);
                        break;
                    case HdrVividRender.COLORSPACE_BT2020:
                        sb.append(Constants.DESC_COLORSPACE_BT2020);
                        break;
                    default:
                        break;
                }

                sb.append(Constants.CR_LF);
                sb.append(context.getString(R.string.setting_output_color_format)).append(Constants.COLON);
                switch (SimpleSetting.getInstance().getOutputColorFormat()) {
                    case HdrVividRender.COLORFORMAT_NV12:
                        sb.append(Constants.DESC_COLORFORMAT_NV12);
                        break;
                    case HdrVividRender.COLORFORMAT_YUV420P10:
                        sb.append(Constants.DESC_COLORFORMAT_YUV420P10);
                        break;
                    case HdrVividRender.COLORFORMAT_R8G8B8A8:
                        sb.append(Constants.DESC_COLORFORMAT_R8G8B8A8);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.setting_brightness))
            .append(Constants.COLON)
            .append(SimpleSetting.getInstance().getBrightness());

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.etof_type)).append(Constants.COLON);
        switch (videoInfo.getTf()) {
            case HdrVividRender.TRANSFUNC_PQ:
                sb.append(Constants.DESC_TRANSFUNC_PQ);
                break;
            case HdrVividRender.TRANSFUNC_HLG:
                sb.append(Constants.DESC_TRANSFUNC_HLG);
                break;
            default:
                break;
        }
        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.resolution))
            .append(Constants.COLON)
            .append(videoInfo.getWidth())
            .append(Constants.MULIT)
            .append(videoInfo.getHeight());

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.progress))
            .append(Constants.COLON)
            .append(SimpleTimeUtils.getTimeString(VideoInfoUtils.getInstance().getPlaybackInfo().getPtsUs()))
            .append(Constants.LEFT_SLASH)
            .append(SimpleTimeUtils.getTimeString(videoInfo.getDurationUs()));

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.frame_rate))
            .append(Constants.COLON)
            .append(VideoInfoUtils.getInstance().getFrameRate());

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.total_frames))
            .append(Constants.COLON)
            .append(VideoInfoUtils.getInstance().getPlaybackInfo().getTotalNum());

        textViewVideoInfo.post(new Runnable() {
            @Override
            public void run() {
                textViewVideoInfo.setText(sb.toString());
            }
        });
    }

}