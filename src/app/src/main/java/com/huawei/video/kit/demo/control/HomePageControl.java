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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.huawei.video.kit.demo.entity.PlayEntity;
import com.huawei.video.kit.demo.utils.Constants.UrlType;
import com.huawei.video.kit.demo.utils.DataFormatUtil;
import com.huawei.video.kit.demo.utils.PlayControlUtil;

/**
 * Home page control class
 */
public class HomePageControl {
    // Play list
    private List<PlayEntity> playEntityList;

    // Context
    private Context context;

    /**
     * Constructor
     *
     * @param context Context
     */
    public HomePageControl(Context context) {
        this.context = context;
        init();
    }

    /**
     * Init data
     */
    private void init() {
        playEntityList = new ArrayList<>();
    }

    /**
     * Access to the default address under the sources of information
     */
    public void loadPlayList() {
        playEntityList.clear();
        playEntityList.addAll(DataFormatUtil.getPlayList(context));
    }

    /**
     * A list of display data
     *
     * @return Data
     */
    public List<PlayEntity> getPlayList() {
        return playEntityList;
    }

    /**
     * If the data is empty
     *
     * @return The data is empty
     */
    private boolean isPlayListEmpty() {
        return playEntityList == null || playEntityList.size() == 0;
    }

    /**
     * The currently selected data is valid
     *
     * @param position Select position
     * @return Effective
     */
    private boolean isPlayEffective(int position) {
        return !isPlayListEmpty() && playEntityList.size() > position;
    }

    /**
     * Data for the selected titles
     *
     * @param position Select position
     * @return Data
     */
    public PlayEntity getPlayFromPosition(int position) {
        if (isPlayEffective(position)) {
            return playEntityList.get(position);
        }
        return null;
    }

    /**
     * Set play type 0: on demand (the default) 1: live
     *
     * @param videoType Play type
     */
    public void setVideoType(int videoType) {
        PlayControlUtil.setVideoType(videoType);
    }

    /**
     * Set the local loading View is SurfaceView or TextureView
     *
     * @param isSurfaceView Is SurfaceView
     */
    public void setSurfaceViewView(boolean isSurfaceView) {
        PlayControlUtil.setIsSurfaceView(isSurfaceView);
    }

    /**
     * Set the mute
     *
     * @param status Mute
     */
    public void setMute(boolean status) {
        PlayControlUtil.setIsMute(status);
    }

    /**
     * Set the play mode
     *
     * @param playMode The play mode
     */
    public void setPlayMode(int playMode) {
        PlayControlUtil.setPlayMode(playMode);
    }

    /**
     * Set the bandwidth switching mode
     *
     * @param mod The bandwidth switching mode
     */
    public void setBandwidthSwitchMode(int mod) {
        PlayControlUtil.setBandwidthSwitchMode(mod);
    }

    /**
     * Set the bitrate
     *
     * @param initBitrateEnable Whether to set up the bitrate
     */
    public void setInitBitrateEnable(boolean initBitrateEnable) {
        PlayControlUtil.setInitBitrateEnable(initBitrateEnable);
    }

    /**
     * Set close the logo whether affect all sources
     *
     * @param takeEffectOfAll Whether all sources
     */
    public void setBandwidthSwitchMode(boolean takeEffectOfAll) {
        PlayControlUtil.setTakeEffectOfAll(takeEffectOfAll);
    }

    /**
     * Set close logo
     *
     * @param closeLogo Close logo
     */
    public void setCloseLogo(boolean closeLogo) {
        PlayControlUtil.setCloseLogo(closeLogo);
    }

    /**
     * Get home page the input data
     */
    public PlayEntity getInputPlay(String inputUrl) {
        PlayEntity playEntity = new PlayEntity();
        playEntity.setUrl(inputUrl);
        playEntity.setUrlType(UrlType.URL);
        return playEntity;
    }
}
