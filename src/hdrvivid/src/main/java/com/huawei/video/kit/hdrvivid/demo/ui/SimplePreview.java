/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    Handler handler = new Handler();

    private boolean isShowInfo = false;

    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;

    private Button btnPlay;

    private Button btnStop;

    private Button btnInfo;

    private TextView textViewVideoInfo;

    private SimpleProcessor simpleProcessor = null;

    private final long DELAY_MILLIS = 100;

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
        if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER) {
            changeSurfaceViewSize(VideoInfoUtils.getInstance().getVideoInfo().getWidth(),
                VideoInfoUtils.getInstance().getVideoInfo().getHeight());
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (simpleProcessor.status == SimpleProcessor.STATUS_PAUSEED) {
                simpleProcessor.resumePlay();
                btnPlay.setText(getApplicationContext().getString(R.string.video_control_pause));
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
        btnInfo = findViewById(R.id.mp_surface_btn_info);
        textViewVideoInfo = findViewById(R.id.play_info);
        textViewVideoInfo.getBackground().setAlpha(0);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated, apiType: " + SimpleSetting.getInstance().getApiType());

                simpleProcessor.init(getApplicationContext());

                simpleProcessor.setOutputSurface(surfaceView);

                if (SimpleSetting.getInstance().getOutputMode() == Constants.OUT_MODE_BUFFER) {
                    changeSurfaceViewSize(VideoInfoUtils.getInstance().getVideoInfo().getWidth(),
                        VideoInfoUtils.getInstance().getVideoInfo().getHeight());
                }

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

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowInfo) {
                    hideVideoInfo();
                } else {
                    showVideoInfo();
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
    }

    private void hideVideoInfo() {
        textViewVideoInfo.setVisibility(View.INVISIBLE);
        btnInfo.setText(getApplicationContext().getString(R.string.btn_info_show));
        isShowInfo = false;
    }

    private void showVideoInfo() {
        textViewVideoInfo.setVisibility(View.VISIBLE);
        btnInfo.setText(getApplicationContext().getString(R.string.btn_info_hide));
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
                sb.append("Java");
                break;
            case Constants.API_TYPE_NATIVE:
                sb.append("Native");
                break;
            default:
                break;
        }
        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.setting_input_mode)).append(Constants.COLON);
        switch (SimpleSetting.getInstance().getInputMode()) {
            case Constants.INPUT_MODE_SURFACE:
                sb.append("Surface");
                break;
            default:
                break;
        }

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.setting_output_mode)).append(Constants.COLON);
        switch (SimpleSetting.getInstance().getOutputMode()) {
            case Constants.OUT_MODE_SURFACE:
                sb.append("Surface");
                break;
            case Constants.OUT_MODE_BUFFER:
                sb.append("Buffer");

                sb.append(Constants.CR_LF);
                sb.append(context.getString(R.string.setting_output_color_space)).append(Constants.COLON);
                switch (SimpleSetting.getInstance().getOutputColorSpace()) {
                    case HdrVividRender.COLORSPACE_BT709:
                        sb.append("BT.709");
                        break;
                    case HdrVividRender.COLORSPACE_P3:
                        sb.append("P3");
                        break;
                    case HdrVividRender.COLORSPACE_BT2020:
                        sb.append("BT.2020");
                        break;
                    default:
                        break;
                }

                sb.append(Constants.CR_LF);
                sb.append(context.getString(R.string.setting_output_color_format)).append(Constants.COLON);
                switch (SimpleSetting.getInstance().getOutputColorFormat()) {
                    case HdrVividRender.COLORFORMAT_NV12:
                        sb.append("NV12");
                        break;
                    case HdrVividRender.COLORFORMAT_YUV420P10:
                        sb.append("YUV420P10");
                        break;
                    case HdrVividRender.COLORFORMAT_R8G8B8A8:
                        sb.append("R8G8B8");
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
                sb.append("PQ");
                break;
            case HdrVividRender.TRANSFUNC_HLG:
                sb.append("HLG");
                break;
            default:
                break;
        }
        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.resolution))
            .append(Constants.COLON)
            .append(videoInfo.getWidth())
            .append("*")
            .append(videoInfo.getHeight());

        sb.append(Constants.CR_LF);
        sb.append(context.getString(R.string.progress))
            .append(Constants.COLON)
            .append(SimpleTimeUtils.getTimeString(VideoInfoUtils.getInstance().getPlaybackInfo().getPtsUs()))
            .append("/")
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