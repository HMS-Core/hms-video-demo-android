/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.video.kit.demo.view;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.huawei.hms.videokit.player.WisePlayer;
import com.huawei.video.kit.demo.R;
import com.huawei.video.kit.demo.adapter.SelectPlayDataAdapter;
import com.huawei.video.kit.demo.contract.OnPlayWindowListener;
import com.huawei.video.kit.demo.contract.OnWisePlayerListener;
import com.huawei.video.kit.demo.entity.PlayEntity;
import com.huawei.video.kit.demo.utils.Constants;
import com.huawei.video.kit.demo.utils.DataFormatUtil;
import com.huawei.video.kit.demo.utils.DeviceUtil;
import com.huawei.video.kit.demo.utils.DialogUtil;
import com.huawei.video.kit.demo.utils.LogUtil;
import com.huawei.video.kit.demo.utils.PlayControlUtil;
import com.huawei.video.kit.demo.utils.TimeUtil;

/**
 * Play View
 */
public class PlayView {
    // Context
    private Context context;

    // Play SurfaceView
    private SurfaceView surfaceView;

    // Play TextureView
    private TextureView textureView;

    // Play seekBar
    private SeekBar seekBar;

    // Current play time
    private TextView currentTimeTv;

    // Current play total time
    private TextView totalTimeTv;

    // Play/Stop button
    private ImageView playImg;

    // refresh button
    private ImageView playRefreshImg;

    // Video buffer view
    private RelativeLayout videoBufferLayout;

    // Video buffer load percentage
    private TextView bufferPercentTv;

    // Back button
    private TextView backTv;

    // Video name
    private TextView currentPlayNameTv;

    // Play speed value
    private TextView speedTv;

    // Setting
    private TextView settingsTv;

    // Full screen Button
    private Button fullScreenBt;

    // Video action button layout
    private FrameLayout controlLayout;

    // Video bottom layout
    private LinearLayout contentLayout;

    // Video name
    private TextView videoNameTv;

    // Video width and height
    private TextView videoSizeTv;

    // Video play total time
    private TextView videoTimeTv;

    // Buffer progress
    private TextView videoDownloadTv;

    // Video of the current rate
    private TextView videoBitrateTv;

    // Video data view
    private RecyclerView playRecyclerView;

    // adapter
    private SelectPlayDataAdapter selectPlayDataAdapter;

    // Android listener
    private OnPlayWindowListener onPlayWindowListener;

    // VideoKit SDK listener
    private OnWisePlayerListener onWisePlayerListener;

    // Switch bitrate
    private TextView switchBitrateTv;

    // Switching bitrate prompt
    private TextView switchingBitrateTv;

    /**
     * Constructor
     *
     * @param context Context
     * @param onPlayWindowListener Android listener
     * @param onWisePlayerListener VideoKit SDK listener
     */
    public PlayView(Context context, OnPlayWindowListener onPlayWindowListener,
        OnWisePlayerListener onWisePlayerListener) {
        this.context = context;
        this.onPlayWindowListener = onPlayWindowListener;
        this.onWisePlayerListener = onWisePlayerListener;
    }

    /**
     * Get parent view
     *
     * @return Parent view
     */
    public View getContentView() {
        View view = LayoutInflater.from(context).inflate(R.layout.play_view, null);
        initView(view);
        initRecycleView(view);
        showDefaultValueView();
        return view;
    }

    /**
     * Init view
     *
     * @param view Parent view
     */
    private void initView(View view) {
        if (view != null) {
            surfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
            textureView = (TextureView) view.findViewById(R.id.texture_view);
            if (PlayControlUtil.isSurfaceView()) {
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.addCallback(onPlayWindowListener);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                textureView.setVisibility(View.GONE);
                surfaceView.setVisibility(View.VISIBLE);
            } else {
                textureView.setSurfaceTextureListener(onPlayWindowListener);
                textureView.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE);
            }
            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            seekBar.setOnSeekBarChangeListener(onWisePlayerListener);
            currentTimeTv = (TextView) view.findViewById(R.id.current_time_tv);
            totalTimeTv = (TextView) view.findViewById(R.id.total_time_tv);
            playImg = (ImageView) view.findViewById(R.id.play_btn);
            playImg.setOnClickListener(onPlayWindowListener);
            playRefreshImg = (ImageView) view.findViewById(R.id.play_refresh);
            playRefreshImg.setOnClickListener(onPlayWindowListener);
            backTv = (TextView) view.findViewById(R.id.back_tv);
            backTv.setOnClickListener(onPlayWindowListener);
            fullScreenBt = (Button) view.findViewById(R.id.fullscreen_btn);
            fullScreenBt.setOnClickListener(onPlayWindowListener);
            videoBufferLayout = (RelativeLayout) view.findViewById(R.id.buffer_rl);
            videoBufferLayout.setVisibility(View.GONE);
            controlLayout = (FrameLayout) view.findViewById(R.id.control_layout);
            bufferPercentTv = (TextView) view.findViewById(R.id.play_process_buffer);
            contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
            currentPlayNameTv = (TextView) view.findViewById(R.id.video_name_tv);
            speedTv = (TextView) view.findViewById(R.id.play_speed_btn);
            speedTv.setOnClickListener(onPlayWindowListener);
            settingsTv = (TextView) view.findViewById(R.id.setting_tv);
            settingsTv.setOnClickListener(onPlayWindowListener);
            videoNameTv = (TextView) view.findViewById(R.id.tv_video_name);
            videoSizeTv = (TextView) view.findViewById(R.id.video_width_and_height);
            videoTimeTv = (TextView) view.findViewById(R.id.video_time);
            videoDownloadTv = (TextView) view.findViewById(R.id.video_download_speed);
            videoBitrateTv = (TextView) view.findViewById(R.id.video_bitrate);
            switchBitrateTv = (TextView) view.findViewById(R.id.switch_bitrate_tv);
            switchBitrateTv.setOnClickListener(onPlayWindowListener);
            switchBitrateTv.setVisibility(View.GONE);
            switchingBitrateTv = (TextView) view.findViewById(R.id.switching_bitrate_tv);
            switchingBitrateTv.setVisibility(View.GONE);
        }
    }

    /**
     * Init recycleView
     *
     * @param view Parent view
     */
    private void initRecycleView(View view) {
        playRecyclerView = (RecyclerView) view.findViewById(R.id.player_recycler_view);
        playRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        selectPlayDataAdapter = new SelectPlayDataAdapter(context, onPlayWindowListener);
        playRecyclerView.setAdapter(selectPlayDataAdapter);
    }

    /**
     * Set list data
     *
     * @param list Data
     */
    public void setRecycleData(List<PlayEntity> list) {
        selectPlayDataAdapter.setSelectPlayList(list);
    }

    /**
     * Update play view
     *
     * @param wisePlayer WisePlayer
     */
    public void updatePlayView(WisePlayer wisePlayer) {
        if (wisePlayer != null) {
            int totalTime = wisePlayer.getDuration();
            LogUtil.i(String.valueOf(totalTime));
            seekBar.setMax(totalTime);
            totalTimeTv.setText(TimeUtil.formatLongToTimeStr(totalTime));
            currentTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
            seekBar.setProgress(0);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update buffer
     *
     * @param percent percent
     */
    public void updateBufferingView(int percent) {
        LogUtil.d("show buffering view loading");
        if (videoBufferLayout.getVisibility() == View.GONE) {
            videoBufferLayout.setVisibility(View.VISIBLE);
        }
        bufferPercentTv.setText(percent + "%");
    }

    /**
     * Show buffer view
     */
    public void showBufferingView() {
        videoBufferLayout.setVisibility(View.VISIBLE);
        bufferPercentTv.setText("0%");
    }

    /**
     * Dismiss buffer view
     */
    public void dismissBufferingView() {
        LogUtil.d("dismiss buffering view loading");
        videoBufferLayout.setVisibility(View.GONE);
    }

    /**
     * Set stop background
     */
    public void setPauseView() {
        playImg.setImageResource(R.drawable.ic_full_screen_suspend_normal);
    }

    /**
     * Set start background
     */
    public void setPlayView() {
        playImg.setImageResource(R.drawable.ic_play);
    }

    /**
     * Update seekBar
     *
     * @param progress progress
     */
    public void updatePlayProgressView(int progress, int bufferPosition, long bufferingSpeed, int bitrate) {
        seekBar.setProgress(progress);
        seekBar.setSecondaryProgress(bufferPosition);
        currentTimeTv.setText(TimeUtil.formatLongToTimeStr(progress));
        videoDownloadTv.setText(context.getResources().getString(R.string.video_download_speed, bufferingSpeed));
        if (bitrate == 0) {
            videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate_empty));
        } else {
            videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate, bitrate));
        }
    }

    /**
     * Set full screen layout
     *
     * @param name Video name
     */
    public void setFullScreenView(String name) {
        fullScreenBt.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        currentPlayNameTv.setVisibility(View.VISIBLE);
        surfaceView.setLayoutParams(
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        currentPlayNameTv.setText(name);
    }

    /**
     * Set portrait layout
     */
    public void setPortraitView() {
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            DeviceUtil.dp2px(context, Constants.HEIGHT_DP)));
        fullScreenBt.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.VISIBLE);
        currentPlayNameTv.setVisibility(View.INVISIBLE);
        currentPlayNameTv.setText(null);
    }

    /**
     * Set play complete
     */
    public void updatePlayCompleteView() {
        playImg.setImageResource(R.drawable.ic_play);
        controlLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Player back to the background
     */
    public void onPause() {
        dismissBufferingView();
    }

    /**
     * Update Video view
     *
     * @param wisePlayer WisePlayer
     * @param name Video name
     */
    public void setContentView(WisePlayer wisePlayer, String name) {
        if (wisePlayer != null) {
            videoNameTv.setText(context.getResources().getString(R.string.video_name, name));
            videoSizeTv.setText(context.getResources()
                .getString(R.string.video_width_and_height, wisePlayer.getVideoWidth(), wisePlayer.getVideoHeight()));
            videoTimeTv.setText(context.getResources()
                .getString(R.string.video_time, TimeUtil.formatLongToTimeStr(wisePlayer.getDuration())));
        }
    }

    /**
     * Get SurfaceView
     *
     * @return SurfaceView
     */
    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    /**
     * Get TextureView
     *
     * @return TextureView
     */
    public TextureView getTextureView() {
        return textureView;
    }

    /**
     * Show setting dialog
     *
     * @param settingType Setting type
     * @param showTextList Setting text list
     * @param selectIndex Default select index
     */
    public void showSettingDialog(int settingType, List<String> showTextList, int selectIndex) {
        DialogUtil.onSettingDialogSelectIndex(context, settingType, showTextList, selectIndex, onPlayWindowListener);
    }

    /**
     * Show setting dialog
     *
     * @param settingType Setting type
     * @param showTextList Setting text list
     * @param selectValue Default select value
     */
    public void showSettingDialogValue(int settingType, List<String> showTextList, String selectValue) {
        DialogUtil.onSettingDialogSelectValue(context, settingType, showTextList, selectValue, onPlayWindowListener);
    }

    /**
     * Show getting dialog
     * 
     * @param gettingType Getting type
     * @param showTextList Setting text list
     * @param selectValue Default select value
     */
    public void showGettingDialog(int gettingType, List<String> showTextList, int selectIndex) {
        DialogUtil.onGettingDialogSelectIndex(context, gettingType, showTextList, selectIndex, onPlayWindowListener);
    }

    /**
     * Set speed button text
     *
     * @param speedText speed value
     */
    public void setSpeedButtonText(String speedText) {
        speedTv.setText(speedText);
    }

    /**
     * Set default value
     */
    public void showDefaultValueView() {
        currentTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
        totalTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
        speedTv.setText("1.0x");
        videoNameTv.setText(context.getResources().getString(R.string.video_name, ""));
        videoSizeTv.setText(context.getResources().getString(R.string.video_width_and_height, 0, 0));
        videoTimeTv.setText(context.getResources().getString(R.string.video_time, TimeUtil.formatLongToTimeStr(0)));
        videoDownloadTv.setText(context.getResources().getString(R.string.video_download_speed, 0));
        videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate_empty));
    }

    /**
     * Reset video view
     */
    public void reset() {
        showBufferingView();
        playImg.setImageResource(R.drawable.ic_play);
        showDefaultValueView();
        hiddenSwitchingBitrateTextView();
        hiddenSwitchBitrateTextView();
        setSwitchBitrateTv(0);
    }

    /**
     * Show switch bitrate textView
     */
    public void showSwitchBitrateTextView() {
        if (switchBitrateTv.getVisibility() == View.GONE) {
            switchBitrateTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hidden switch bitrate textView
     */
    public void hiddenSwitchBitrateTextView() {
        if (switchBitrateTv.getVisibility() == View.VISIBLE) {
            switchBitrateTv.setVisibility(View.GONE);
        }
    }

    /**
     * set switch bitrate textView
     * @param videoHeight  video height
     */
    public void setSwitchBitrateTv(int videoHeight) {
        switchBitrateTv.setText(DataFormatUtil.getVideoQuality(context, videoHeight));
    }

    /**
     * Show switching bitrate textView
     */
    public void showSwitchingBitrateTextView(String textValue) {
        if (switchingBitrateTv.getVisibility() == View.GONE) {
            switchingBitrateTv.setVisibility(View.VISIBLE);
        }
        switchingBitrateTv.setText(Html.fromHtml(context.getString(R.string.resolution_switching, textValue)));
    }

    /**
     * Update textView show value
     * 
     * @param textValue value
     */
    public void updateSwitchingBitrateTextView(String textValue) {
        switchingBitrateTv.setText(textValue);
    }

    /**
     * Hidden switching bitrate textView
     */
    public void hiddenSwitchingBitrateTextView() {
        if (switchingBitrateTv.getVisibility() == View.VISIBLE) {
            switchingBitrateTv.setVisibility(View.GONE);
        }
    }
}
