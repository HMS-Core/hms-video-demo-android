/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
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

package com.huawei.video.kit.demo;

import android.app.Application;
import android.content.Context;

import com.huawei.hms.videokit.player.WisePlayerFactory;

/**
 * Application
 */
public class VideoKitPlayApplication extends Application {
    private static WisePlayerFactory wisePlayerFactory = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Get WisePlayer Factory
     * 
     * @return WisePlayer Factory
     */
    public static WisePlayerFactory getWisePlayerFactory() {
        return wisePlayerFactory;
    }

    public static void setWisePlayerFactory(WisePlayerFactory wisePlayerFactory) {
        VideoKitPlayApplication.wisePlayerFactory = wisePlayerFactory;
    }

    public static void release(Context context) {
        WisePlayerFactory.release(context);
    }
}
