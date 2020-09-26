package com.example.videorenderapp;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.videorenderapp.model.MediaObject;

import java.util.List;

public class VideoPlayerAdapter extends RecyclerView.Adapter<VideoPlayerViewHolder> {
    private List<MediaObject> mediaObjects;
    private RequestManager requestManager;
    private int targetPosition;

    public int getTargetPosition() {
        return targetPosition;
    }

    private OnItemClickListener thumbnailClickListener, downloadClickListener;


    public VideoPlayerAdapter(List<MediaObject> mediaObjects, RequestManager requestManager) {
        this.mediaObjects = mediaObjects;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public VideoPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoPlayerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoPlayerViewHolder holder, int position) {
        targetPosition = position;
        MediaObject object = mediaObjects.get(position);


        holder.bind(object, requestManager);

        holder.thumbnail.setOnClickListener(v -> {
            if (thumbnailClickListener != null) {
                thumbnailClickListener.onItemClickListener(object, holder.getAdapterPosition());
            }
        });

        holder.downloadIcon.setOnClickListener(v -> {
            if (downloadClickListener != null) {
                downloadClickListener.onItemClickListener(object, position);
                notifyItemChanged(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mediaObjects.size();
    }

    interface OnItemClickListener {
        void onItemClickListener(MediaObject mediaObject, int position);
    }

    public void setThumbnailClickListener(OnItemClickListener itemClickListener) {
        this.thumbnailClickListener = itemClickListener;
    }

    public void setDownloadClickListener(OnItemClickListener downloadClickListener) {
        this.downloadClickListener = downloadClickListener;

    }

}
