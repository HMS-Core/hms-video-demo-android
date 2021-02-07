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

package com.huawei.video.kit.demo.utils;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.videokit.player.CacheInfo;
import com.huawei.hms.videokit.player.Preloader;
import com.huawei.hms.videokit.player.WisePlayerFactory;
import com.huawei.video.kit.demo.R;
import com.huawei.video.kit.demo.VideoKitPlayApplication;
import com.huawei.video.kit.demo.contract.OnDialogConfirmListener;
import com.huawei.video.kit.demo.contract.OnDialogInputValueListener;
import com.huawei.video.kit.demo.contract.OnHomePageListener;
import com.huawei.video.kit.demo.contract.OnPlaySettingListener;
import com.huawei.video.kit.demo.view.PlaySettingDialog;

/**
 * Dialog tools
 */
public class DialogUtil {
    /**
     * Set Bitrate dialog
     *
     * @param context Context
     */
    public static void setInitBitrate(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.init_bitrate, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(StringUtil.getStringFromResId(context, R.string.video_init_bitrate_setting))
            .setView(view)
            .create();
        dialog.show();

        final CheckBox checkUpTo = (CheckBox) view.findViewById(R.id.check_up_to);
        if (PlayControlUtil.getInitType() == 0) {
            checkUpTo.setChecked(true);
        } else {
            checkUpTo.setChecked(false);
        }
        final EditText initBitrateEt = (EditText) view.findViewById(R.id.init_bitrate_et);
        initBitrateEt.setText(String.valueOf(PlayControlUtil.getInitBitrate()));
        final EditText widthEt = (EditText) view.findViewById(R.id.init_width_et);
        widthEt.setText(String.valueOf(PlayControlUtil.getInitWidth()));
        final EditText heightEt = (EditText) view.findViewById(R.id.init_height_et);
        heightEt.setText(String.valueOf(PlayControlUtil.getInitHeight()));

        Button okBt = (Button) view.findViewById(R.id.ok_bt);
        Button cancelBt = (Button) view.findViewById(R.id.cancel_bt);

        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayControlUtil.setInitType(checkUpTo.isChecked() ? 0 : 1);
                PlayControlUtil.setInitBitrate(Integer.parseInt(initBitrateEt.getText().toString()));
                PlayControlUtil.setInitHeight(Integer.parseInt(heightEt.getText().toString()));
                PlayControlUtil.setInitWidth(Integer.parseInt(widthEt.getText().toString()));
                dialog.dismiss();
            }
        });

        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Home page setup dialog
     *
     * @param context Context
     * @param playSettingType Set the play type
     * @param settingList Set the list of options
     * @param defaultSelect The default Settings string
     * @param onHomePageListener Click listener
     */
    public static void showVideoTypeDialog(Context context, int playSettingType, List<String> settingList,
        int defaultSelect, OnHomePageListener onHomePageListener) {
        PlaySettingDialog playSettingDialog = new PlaySettingDialog(context);
        playSettingDialog.initDialog(onHomePageListener, playSettingType);
        playSettingDialog.setList(settingList);
        playSettingDialog.setSelectIndex(defaultSelect);
        playSettingDialog.setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        playSettingDialog.show();
    }

    /**
     * Play activity Settings dialog
     *
     * @param context Context
     * @param settingType Set the play type
     * @param showTextList Set the list of options
     * @param selectIndex The default Settings index
     * @param onPlaySettingListener Click listener
     */
    public static void onSettingDialogSelectIndex(Context context, int settingType, List<String> showTextList,
        int selectIndex, OnPlaySettingListener onPlaySettingListener) {
        PlaySettingDialog dialog = new PlaySettingDialog(context).setList(showTextList);
        dialog.setTitle(StringUtil.getStringFromResId(context, R.string.settings));
        dialog.setSelectIndex(selectIndex);
        dialog.setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        dialog.initDialog(onPlaySettingListener, settingType);
        dialog.show();
    }

    /**
     * Play activity Settings dialog
     *
     * @param context Context
     * @param settingType Set the play type
     * @param showTextList Set the list of options
     * @param selectValue The default Settings string
     * @param onPlaySettingListener Click listener
     */
    public static void onSettingDialogSelectValue(Context context, int settingType, List<String> showTextList,
        String selectValue, OnPlaySettingListener onPlaySettingListener) {
        PlaySettingDialog dialog = new PlaySettingDialog(context).setList(showTextList);
        dialog.setTitle(StringUtil.getStringFromResId(context, R.string.settings));
        dialog.setSelectValue(selectValue)
            .setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        dialog.initDialog(onPlaySettingListener, settingType);
        dialog.show();
    }

    /**
     * Get the volume Settings dialog
     *
     * @param context Context
     * @param onDialogInputValueListener Click listener
     */
    public static void showSetVolumeDialog(Context context,
        final OnDialogInputValueListener onDialogInputValueListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.set_volume_dialog, null);
        final AlertDialog dialog =
            new AlertDialog.Builder(context).setTitle(StringUtil.getStringFromResId(context, R.string.video_set_volume))
                .setView(view)
                .create();
        dialog.show();
        final EditText volumeValueEt = (EditText) view.findViewById(R.id.set_volume_et);
        Button okBt = (Button) view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = (Button) view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDialogInputValueListener != null) {
                    String inputText = "";
                    if (volumeValueEt.getText() != null) {
                        inputText = volumeValueEt.getText().toString();
                    }
                    onDialogInputValueListener.dialogInputListener(inputText);
                    dialog.dismiss();
                }
            }
        });
        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Set the bitrate range dialog
     *
     * @param context Context
     */
    public static void showBitrateRangeDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.bitrate_range_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(StringUtil.getStringFromResId(context, R.string.video_bitrate_range_setting_title))
            .setView(view)
            .create();
        dialog.show();
        final EditText bitrateMinSetting = (EditText) view.findViewById(R.id.bitrate_min_setting);
        final EditText bitrateMaxSetting = (EditText) view.findViewById(R.id.bitrate_max_setting);
        Button okBt = (Button) view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = (Button) view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitrateMinSetting.getText() != null) {
                    PlayControlUtil.setMinBitrate(Integer.parseInt(bitrateMinSetting.getText().toString()));
                }
                if (bitrateMaxSetting.getText() != null) {
                    PlayControlUtil.setMaxBitrate(Integer.parseInt(bitrateMaxSetting.getText().toString()));
                }
                dialog.dismiss();
            }
        });
        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * init preloader
     * 
     * @param context Context
     * @param onDialogConfirmListener Dialog confirm listener
     */
    public static void initPreloaderDialog(final Context context,
        final OnDialogConfirmListener onDialogConfirmListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.bitrate_range_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(StringUtil.getStringFromResId(context, R.string.video_init_preloader))
            .setView(view)
            .create();
        dialog.show();
        final TextView minTextView = view.findViewById(R.id.bitrate_min_tv);
        final TextView maxTextView = view.findViewById(R.id.bitrate_max_tv);
        final EditText bitrateMinSetting = (EditText) view.findViewById(R.id.bitrate_min_setting);
        final EditText bitrateMaxSetting = (EditText) view.findViewById(R.id.bitrate_max_setting);
        bitrateMinSetting.setInputType(InputType.TYPE_CLASS_TEXT);
        minTextView.setText(R.string.video_preloader_path);
        maxTextView.setText(R.string.video_preloader_total_size);
        bitrateMinSetting.setText("/sdcard/preloader");
        bitrateMaxSetting.setText("209715200");
        Button okBt = (Button) view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = (Button) view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Preloader preloader = VideoKitPlayApplication.getWisePlayerFactory().createPreloader();
                if (preloader != null && !TextUtils.isEmpty(bitrateMinSetting.getText())
                    && !TextUtils.isEmpty(bitrateMaxSetting.getText())) {
                    PlayControlUtil.setPreloader(preloader);
                    String path = bitrateMinSetting.getText().toString();
                    if (FileUtil.createFile(path)) {
                        int size = Integer.parseInt(bitrateMaxSetting.getText().toString());
                        int result = preloader.initCache(path, size);
                        PlayControlUtil.setInitResult(result);
                    } else {
                        Toast.makeText(context, context.getString(R.string.create_file_fail), Toast.LENGTH_SHORT)
                            .show();
                    }
                }
                onDialogConfirmListener.onConfirm();
                dialog.dismiss();
            }
        });
        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Add single cache
     * 
     * @param context Context
     */
    public static void addSingleCacheDialog(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_single_cache_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle(StringUtil.getStringFromResId(context, R.string.video_add_single_cache))
            .setView(view)
            .create();
        dialog.show();
        final EditText cacheUrl = (EditText) view.findViewById(R.id.et_cache_url);
        final EditText cachePlayParam = (EditText) view.findViewById(R.id.et_cache_playparam);
        final EditText cacheAppId = (EditText) view.findViewById(R.id.et_cache_appid);
        final EditText cacheWidth = (EditText) view.findViewById(R.id.et_cache_width);
        final EditText cacheHeight = (EditText) view.findViewById(R.id.et_cache_height);
        final EditText cacheSize = (EditText) view.findViewById(R.id.et_cache_size);
        final EditText cachePriority = (EditText) view.findViewById(R.id.et_cache_priority);
        final EditText cacheVideoFormat = (EditText) view.findViewById(R.id.et_cache_format);
        Button okBt = (Button) view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = (Button) view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CacheInfo cacheInfo = new CacheInfo();
                int priority = 0;
                if (!TextUtils.isEmpty(cacheUrl.getText())) {
                    cacheInfo.setUrl(cacheUrl.getText().toString());
                }
                if (!TextUtils.isEmpty(cachePlayParam.getText())) {
                    cacheInfo.setPlayParam(cachePlayParam.getText().toString());
                }
                if (!TextUtils.isEmpty(cacheAppId.getText())) {
                    cacheInfo.setAppId(cacheAppId.getText().toString());
                }
                if (!TextUtils.isEmpty(cacheWidth.getText())) {
                    cacheInfo.setWidth(Integer.parseInt(cacheWidth.getText().toString()));
                }
                if (!TextUtils.isEmpty(cacheHeight.getText())) {
                    cacheInfo.setHeight(Integer.parseInt(cacheHeight.getText().toString()));
                }
                if (!TextUtils.isEmpty(cacheSize.getText())) {
                    cacheInfo.setCacheSize(Integer.parseInt(cacheSize.getText().toString()));
                }
                if (!TextUtils.isEmpty(cachePriority.getText())) {
                    priority = Integer.parseInt(cachePriority.getText().toString());
                }
                if (!TextUtils.isEmpty(cacheVideoFormat.getText())) {
                    cacheInfo.setVideoFormat(Integer.parseInt(cacheVideoFormat.getText().toString()));
                }
                if (!TextUtils.isEmpty(cacheInfo.getUrl())) {
                    if (PlayControlUtil.getPreloader() != null) {
                        PlayControlUtil.getPreloader().addCache(cacheInfo, priority);
                    } else {
                        Toast
                            .makeText(context, context.getString(R.string.video_add_single_cache_fail),
                                Toast.LENGTH_SHORT)
                            .show();
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.video_preload_url), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Update Server country dialog
     *
     * @param context Context
     */
    public static void updateServerCountryDialog(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.update_country_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(StringUtil.getStringFromResId(context, R.string.update_server_country_title))
                .setView(view)
                .create();
        dialog.show();
        final EditText bitrateMaxSetting = (EditText) view.findViewById(R.id.server_country_setting);
        Button okBt = (Button) view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = (Button) view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitrateMaxSetting.getText() == null || TextUtils.isEmpty(bitrateMaxSetting.getText().toString())) {
                    Toast.makeText(context, context.getString(R.string.update_server_country_toast_empty), Toast.LENGTH_SHORT)
                            .show();
                } else if (bitrateMaxSetting.getText().length() != 2) {
                    Toast.makeText(context, context.getString(R.string.update_server_country_toast_length), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    WisePlayerFactory.updateServeCountry(bitrateMaxSetting.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        cancelBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
