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

package com.huawei.video.kit.demo.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.SurfaceView;
import android.view.TextureView;

import com.huawei.hms.videokit.player.InitBitrateParam;
import com.huawei.hms.videokit.player.StreamInfo;
import com.huawei.hms.videokit.player.VideoInfo;
import com.huawei.hms.videokit.player.WisePlayer;
import com.huawei.hms.videokit.player.common.PlayerConstants.CycleMode;
import com.huawei.hms.videokit.player.common.PlayerConstants.ScenarioType;
import com.huawei.video.kit.demo.VideoKitPlayApplication;
import com.huawei.video.kit.demo.contract.OnWisePlayerListener;
import com.huawei.video.kit.demo.entity.PlayEntity;
import com.huawei.video.kit.demo.utils.Constants.UrlType;
import com.huawei.video.kit.demo.utils.DataFormatUtil;
import com.huawei.video.kit.demo.utils.LogUtil;
import com.huawei.video.kit.demo.utils.PlayControlUtil;
import com.huawei.video.kit.demo.utils.StringUtil;

/**
 * Play controls class
 */
public class PlayControl {
    private static final String TAG = "PlayControl";

    // Context
    private Context context;

    // Player entity
    private WisePlayer wisePlayer;

    // The current play data
    private PlayEntity currentPlayData;

    // Player listener
    private OnWisePlayerListener onWisePlayerListener;

    // Play list data
    private List<PlayEntity> playEntityList;

    // Video play url start with Http/Https
    private boolean isHttpVideo = true;

    /**
     * Constructor
     *
     * @param context Context
     * @param onWisePlayerListener Player listener
     */
    public PlayControl(Context context, OnWisePlayerListener onWisePlayerListener) {
        this.context = context;
        this.onWisePlayerListener = onWisePlayerListener;
        init();
    }

    /**
     * Init
     */
    public void init() {
        initPlayer();
        setPlayListener();
    }

    /**
     * Set the play listener
     */
    private void setPlayListener() {
        if (wisePlayer != null) {
            wisePlayer.setErrorListener(onWisePlayerListener);
            wisePlayer.setEventListener(onWisePlayerListener);
            wisePlayer.setResolutionUpdatedListener(onWisePlayerListener);
            wisePlayer.setReadyListener(onWisePlayerListener);
            wisePlayer.setLoadingListener(onWisePlayerListener);
            wisePlayer.setPlayEndListener(onWisePlayerListener);
            wisePlayer.setSeekEndListener(onWisePlayerListener);
        }
    }

    /**
     * Init play fail
     *
     * @return Whether the failure
     */
    public boolean initPlayFail() {
        return wisePlayer == null;
    }

    /**
     * Init the player
     */
    private void initPlayer() {
        if (VideoKitPlayApplication.getWisePlayerFactory() == null) {
            return;
        }
        wisePlayer = VideoKitPlayApplication.getWisePlayerFactory().createWisePlayer();
    }

    /**
     * Get the current play data
     */
    public void setCurrentPlayData(Serializable serializable) {
        if (serializable != null && serializable instanceof PlayEntity) {
            currentPlayData = (PlayEntity) serializable;
        }
    }

    /**
     * Start the player, the state of ready to start
     */
    public void ready() {
        if (currentPlayData != null && wisePlayer != null) {
            LogUtil.d(TAG, "current play video url is :" + currentPlayData.getUrl());
            if (currentPlayData.getUrlType() == UrlType.URL) {
                setHttpVideo(true);
                wisePlayer.setPlayUrl(new String[] {currentPlayData.getUrl()});
            } else if (currentPlayData.getUrlType() == UrlType.URL_JSON) {
                setHttpVideo(false);
                wisePlayer.setPlayUrl(currentPlayData.getUrl(), currentPlayData.getAppId(), ScenarioType.ONLINE);
            } else if (currentPlayData.getUrlType() == UrlType.URL_MULTIPLE) {
                setHttpVideo(true);
                String[] strings = StringUtil.getStringArray(currentPlayData.getUrl(), "-SPAD-");
                wisePlayer.setPlayUrl(strings);
            } else {
                setHttpVideo(true);
                wisePlayer.setPlayUrl(new String[] {currentPlayData.getUrl()});
            }
            setBookmark();
            setPlayMode(PlayControlUtil.getPlayMode(), false);
            setMute(PlayControlUtil.isMute());
            setVideoType(PlayControlUtil.getVideoType(), false);
            setBandwidthSwitchMode(PlayControlUtil.getBandwidthSwitchMode(), false);
            setInitBitrateEnable();
            setBitrateRange();
            setCloseLogo();
            wisePlayer.ready();
        }
    }

    /**
     * Start playing
     */
    public void start() {
        wisePlayer.start();
    }

    /**
     * Get the current play time
     *
     * @return The current play time
     */
    public int getCurrentTime() {
        if (wisePlayer != null) {
            return wisePlayer.getCurrentTime();
        } else {
            return 0;
        }
    }

    /**
     * Get total time
     * @return Total time
     */
    public int getDuration() {
        if (wisePlayer != null) {
            return wisePlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * Drag the play
     *
     * @param progress Drag the time of position, the unit is milliseconds
     */
    public void updateCurProgress(int progress) {
        if (wisePlayer != null) {
            wisePlayer.seek(progress);
        }
    }

    /**
     * Binding player SurfaceView
     *
     * @param surfaceView The player SurfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        if (wisePlayer != null) {
            wisePlayer.setView(surfaceView);
        }
    }

    /**
     * Binding player TextureView
     *
     * @param textureView The player TextureView
     */
    public void setTextureView(TextureView textureView) {
        if (wisePlayer != null) {
            wisePlayer.setView(textureView);
        }
    }

    /**
     * The player suspend
     */
    public void suspend() {
        if (wisePlayer != null) {
            setBufferingStatus(false, false);
            wisePlayer.suspend();
        }
    }

    /**
     * Release player
     */
    public void release() {
        if (wisePlayer != null) {
            wisePlayer.release();
            wisePlayer = null;
        }
    }

    /**
     * stop play
     */
    public void stop() {
        if (wisePlayer != null) {
            wisePlayer.stop();
        }
    }

    /**
     * Set the play/pause state
     * @param isPlaying The player status
     */
    public void setPlayData(boolean isPlaying) {
        if (isPlaying) {
            wisePlayer.pause();
            setBufferingStatus(false, false);
        } else {
            wisePlayer.start();
            setBufferingStatus(true, false);
        }
    }

    /**
     * Gets the current play name
     *
     * @return The current play name
     */
    public String getCurrentPlayName() {
        if (currentPlayData != null) {
            return StringUtil.getNotEmptyString(currentPlayData.getName());
        } else {
            return "";
        }
    }

    /**
     * Players resume play
     *
     * @param play Resume after the player is in a state of play or pause state 0:1: pause play - 1: keep
     */
    public void playResume(int play) {
        if (wisePlayer != null) {
            setBufferingStatus(true, false);
            wisePlayer.resume(play);
        }
    }

    /**
     * Set the player whether to allow for the buffer.Usage scenarios such as suspended under 4g networks, not for the
     * buffer
     *
     * @param status False: stop the background buffer load.True: allows the background buffer load (the default)
     * @param isUpdateLocal Whether to update the local configuration
     */

    public void setBufferingStatus(boolean status, boolean isUpdateLocal) {
        if (wisePlayer != null && (isUpdateLocal || PlayControlUtil.isLoadBuff())) {
            wisePlayer.setBufferingStatus(status);
            if (isUpdateLocal) {
                PlayControlUtil.setLoadBuff(status);
            }
        }
    }

    /**
     * Set the speed
     *
     * @param speedValue The speed of the string
     */
    public void setPlaySpeed(String speedValue) {
        if (speedValue.equals("1.25x")) {
            wisePlayer.setPlaySpeed(1.25f);
        } else if (speedValue.equals("1.5x")) {
            wisePlayer.setPlaySpeed(1.5f);
        } else if (speedValue.equals("1.75x")) {
            wisePlayer.setPlaySpeed(1.75f);
        } else if (speedValue.equals("2.0x")) {
            wisePlayer.setPlaySpeed(2.0f);
        } else if (speedValue.equals("0.5x")) {
            wisePlayer.setPlaySpeed(0.5f);
        } else if (speedValue.equals("0.75x")) {
            wisePlayer.setPlaySpeed(0.75f);
        } else {
            wisePlayer.setPlaySpeed(1.0f);
        }
    }

    /**
     * Get the current cache progress
     *
     * @return Cache progress unit of milliseconds
     */
    public int getBufferTime() {
        if (wisePlayer != null) {
            return wisePlayer.getBufferTime();
        } else {
            return 0;
        }
    }

    /**
     * Get to download speed
     *
     * @return Download speed unit b/s
     */
    public long getBufferingSpeed() {
        if (wisePlayer != null) {
            return wisePlayer.getBufferingSpeed();
        } else {
            return 0;
        }
    }

    /**
     * Get bitrate list data (String)
     *
     * @return Bitrate list data
     */
    public List<String> getBitrateList() {
        List<String> bitrateList = new ArrayList<>();
        List<Integer> bitrateIntList = getBitrateIntegerList();
        for (int i = 0; i < bitrateIntList.size(); i++) {
            bitrateList.add(String.valueOf(bitrateIntList.get(i)));
        }
        return bitrateList;

    }

    /**
     * Get bitrate list data (Integer)
     *
     * @return Bitrate list data
     */
    private List<Integer> getBitrateIntegerList() {
        List<Integer> bitrateIntList = new ArrayList<>();
        if (wisePlayer != null) {
            VideoInfo videoInfo = wisePlayer.getVideoInfo();
            if (videoInfo != null && videoInfo.getStreamInfos() != null) {
                for (StreamInfo streamInfo : videoInfo.getStreamInfos()) {
                    if (streamInfo != null) {
                        bitrateIntList.add(streamInfo.getBitrate());
                    }
                }
                Collections.sort(bitrateIntList);
            }
        }
        return bitrateIntList;
    }

    /**
     * Gets the current rate in Dialog rate index in the list
     *
     * @return The location of the current rate, the default back to the first
     */
    public int getCurrentBitrateIndex() {
        List<Integer> bitrateIntList = getBitrateIntegerList();
        if (bitrateIntList.size() > 0) {
            return bitrateIntList.indexOf(getCurrentBitrate());
        } else {
            return 0;
        }
    }

    /**
     * Smooth transition rate
     *
     * @param currentBitrate The current need to set the bitrate
     */
    public void switchBitrateSmooth(int currentBitrate) {
        if (wisePlayer != null) {
            LogUtil.d(TAG, "switch bitrate smooth : currentBitrate " + currentBitrate);
            wisePlayer.switchBitrateSmooth(currentBitrate);
        }
    }

    /**
     * Designated transition rate
     *
     * @param currentBitrate The current need to set the bitrate
     */
    public void switchBitrateDesignated(int currentBitrate) {
        if (wisePlayer != null) {
            LogUtil.d(TAG, "switch bitrate designated : currentBitrate " + currentBitrate);
            wisePlayer.switchBitrateDesignated(currentBitrate);
        }
    }

    /**
     * Get the play stream bitrate
     *
     * @return The play stream bitrate
     */
    public int getCurrentBitrate() {
        if (wisePlayer != null) {
            StreamInfo streamInfo = wisePlayer.getCurrentStreamInfo();
            if (streamInfo != null) {
                return streamInfo.getBitrate();
            } else {
                LogUtil.d(TAG, "get current bitrate info is empty!");
            }
        }
        return 0;
    }

    /**
     * Set the bandwidth switching mode
     *
     * @param mod The bandwidth switching mode
     * @param updateLocate Whether to update the local configuration
     */
    public void setBandwidthSwitchMode(int mod, boolean updateLocate) {
        if (wisePlayer != null) {
            wisePlayer.setBandwidthSwitchMode(mod);
        }
        if (updateLocate) {
            PlayControlUtil.setBandwidthSwitchMode(mod);
        }
    }

    /**
     * Get play speed
     *
     * @return Play speed
     */
    public float getPlaySpeed() {
        if (wisePlayer != null) {
            return wisePlayer.getPlaySpeed();
        }
        return 1f;
    }

    /**
     * Close logo
     */
    public void closeLogo() {
        if (wisePlayer != null) {
            wisePlayer.closeLogo();
        }
    }

    /**
     * Set play mode
     *
     * @param playMode Play mode
     * @param updateLocate Whether to update the local configuration
     */
    public void setPlayMode(int playMode, boolean updateLocate) {
        if (wisePlayer != null) {
            wisePlayer.setPlayMode(playMode);
        }
        if (updateLocate) {
            PlayControlUtil.setPlayMode(playMode);
        }
    }

    /**
     * Get play mode
     *
     * @return Play mode
     */
    public int getPlayMode() {
        if (wisePlayer != null) {
            return wisePlayer.getPlayMode();
        } else {
            return 1;
        }
    }

    /**
     * Set cycle mode
     *
     * @param isCycleMode Whether open loop
     */
    public void setCycleMode(boolean isCycleMode) {
        if (wisePlayer != null) {
            wisePlayer.setCycleMode(isCycleMode ? CycleMode.MODE_CYCLE : CycleMode.MODE_NORMAL);
        }
    }

    /**
     * Whether cycle mode
     *
     * @return Is cycle mode
     */
    public boolean isCycleMode() {
        if (wisePlayer != null) {
            return wisePlayer.getCycleMode() == CycleMode.MODE_CYCLE;
        } else {
            return false;
        }
    }

    /**
     * Set the mute
     *
     * @param status Whether quiet
     */
    public void setMute(boolean status) {
        if (wisePlayer != null) {
            wisePlayer.setMute(status);
        }
        PlayControlUtil.setIsMute(status);
    }

    /**
     * Set the volume, the current player is interval [0, 1]
     *
     * @param volume The volume interval [0, 1]
     */
    public void setVolume(float volume) {
        if (wisePlayer != null) {
            LogUtil.d(TAG, "current set volume is " + volume);
            wisePlayer.setVolume(volume);
        }
    }

    /**
     * Set play type 0: on demand (the default) 1: live
     *
     * @param videoType Play types
     * @param updateLocate Whether to update the local configuration
     */
    public void setVideoType(int videoType, boolean updateLocate) {
        if (wisePlayer != null) {
            wisePlayer.setVideoType(videoType);
        }
        if (updateLocate) {
            PlayControlUtil.setVideoType(videoType);
        }
    }

    /**
     * Set change notification
     */
    public void setSurfaceChange() {
        if (wisePlayer != null) {
            wisePlayer.setSurfaceChange();
        }
    }

    /**
     * Set up the bitrate
     */
    public void setInitBitrateEnable() {
        if (PlayControlUtil.isInitBitrateEnable() && wisePlayer != null) {
            InitBitrateParam initBitrateParam = new InitBitrateParam();
            initBitrateParam.setBitrate(PlayControlUtil.getInitBitrate());
            initBitrateParam.setHeight(PlayControlUtil.getInitHeight());
            initBitrateParam.setWidth(PlayControlUtil.getInitWidth());
            initBitrateParam.setType(PlayControlUtil.getInitType());
            wisePlayer.setInitBitrate(initBitrateParam);
        }
    }

    /**
     * Set the bitrate range
     */
    public void setBitrateRange() {
        if (PlayControlUtil.isSetBitrateRangeEnable() && wisePlayer != null) {
            wisePlayer.setBitrateRange(PlayControlUtil.getMinBitrate(), PlayControlUtil.getMaxBitrate());
            PlayControlUtil.clearBitrateRange();
        }
    }

    /**
     * Remove the current play bookmark
     */
    public void clearPlayProgress() {
        if (currentPlayData != null) {
            LogUtil.d("clear current progress " + currentPlayData.getUrl());
            PlayControlUtil.clearPlayData(currentPlayData.getUrl());
        }
    }

    /**
     * Save the current sources of progress
     */
    public void savePlayProgress() {
        if (currentPlayData != null && wisePlayer != null) {
            LogUtil.d("save current progress " + wisePlayer.getCurrentTime());
            PlayControlUtil.savePlayData(currentPlayData.getUrl(), wisePlayer.getCurrentTime());
        }
    }

    /**
     * Bookmark play position
     */
    public void setBookmark() {
        if (currentPlayData != null) {
            int bookmark = PlayControlUtil.getPlayData(currentPlayData.getUrl());
            LogUtil.d("current book mark is " + bookmark);
            if (wisePlayer != null && bookmark != 0) {
                wisePlayer.setBookmark(bookmark);
            }
        }
    }

    /**
     * Get XML data in the sdcard
     *
     * @return Data list
     */
    public List<PlayEntity> getPlayList() {
        if (playEntityList == null || playEntityList.size() == 0) {
            playEntityList = new ArrayList<>();
            playEntityList.addAll(DataFormatUtil.getPlayList(context));
        }
        return playEntityList;
    }

    /**
     * Get the selected data
     *
     * @param position The selected position
     * @return The play data
     */
    public PlayEntity getPlayFromPosition(int position) {
        if (playEntityList != null && playEntityList.size() > position) {
            return playEntityList.get(position);
        }
        return null;
    }

    /**
     * Player reset
     */
    public void reset() {
        if (wisePlayer != null) {
            wisePlayer.reset();
        }
    }

    /**
     * Close logo
     */
    public void setCloseLogo() {
        if (wisePlayer != null) {
            if (PlayControlUtil.isCloseLogo()) {
                if (!PlayControlUtil.isTakeEffectOfAll()) {
                    PlayControlUtil.setCloseLogo(false);
                }
                wisePlayer.closeLogo();
            }
        }
    }

    /**
     * Video into the background
     */
    public void onPause() {
        if (currentPlayData != null && wisePlayer != null) {
            PlayControlUtil.savePlayData(currentPlayData.getUrl(), wisePlayer.getCurrentTime());
            suspend();
        }
    }

    public boolean isHttpVideo() {
        return isHttpVideo;
    }

    private void setHttpVideo(boolean httpVideo) {
        isHttpVideo = httpVideo;
    }
}
