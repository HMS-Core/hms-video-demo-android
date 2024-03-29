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

package com.huawei.video.kit.demo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.video.kit.demo.R;
import com.huawei.video.kit.demo.contract.OnItemClickListener;
import com.huawei.video.kit.demo.entity.PlayEntity;
import com.huawei.video.kit.demo.utils.LogUtil;
import com.huawei.video.kit.demo.utils.StringUtil;

/**
 * Play recyclerView adapter
 */
public class SelectPlayDataAdapter extends RecyclerView.Adapter<SelectPlayDataAdapter.PlayViewHolder> {
    private static final String TAG = "SelectPlayDataAdapter";

    // Data sources list
    private List<PlayEntity> playList;

    // Context
    private Context context;

    // Click item listener
    private OnItemClickListener onItemClickListener;

    /**
     * Constructor
     *
     * @param context Context
     * @param onItemClickListener Listener
     */
    public SelectPlayDataAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        playList = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Set list data
     *
     * @param playList Play data
     */
    public void setSelectPlayList(List<PlayEntity> playList) {
        if (this.playList.size() > 0) {
            this.playList.clear();
        }
        this.playList.addAll(playList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_play_item, parent, false);
        return new PlayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlayViewHolder holder, final int position) {
        if (playList.size() > position && holder != null) {
            PlayEntity playEntity = playList.get(position);
            if (playEntity == null) {
                LogUtil.i(TAG, "current item data is empty.");
                return;
            }
            StringUtil.setTextValue(holder.playName, playEntity.getName());
            StringUtil.setTextValue(holder.playUrl, playEntity.getUrl());
            StringUtil.setTextValue(holder.playType, String.valueOf(playEntity.getUrlType()));
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    /**
     * Show view holder
     */
    static class PlayViewHolder extends ViewHolder {

        // The video name
        private TextView playName;

        // The video type
        private TextView playType;

        // The video url
        private TextView playUrl;

        /**
         * Constructor
         *
         * @param itemView Item view
         */
        public PlayViewHolder(View itemView) {
            super(itemView);
            if (itemView != null) {
                playName = itemView.findViewById(R.id.play_name);
                playType = itemView.findViewById(R.id.play_type);
                playUrl = itemView.findViewById(R.id.play_url);
            }
        }
    }
}
