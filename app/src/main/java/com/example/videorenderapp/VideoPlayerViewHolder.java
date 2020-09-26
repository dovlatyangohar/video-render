package com.example.videorenderapp;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.videorenderapp.model.MediaObject;

public class VideoPlayerViewHolder extends RecyclerView.ViewHolder {

    ImageView thumbnail;
    TextView title;
    TextView description;
    ImageView downloadIcon;
    NumberProgressBar progressBar;
    RequestManager requestManager;


    public VideoPlayerViewHolder(@NonNull View itemView) {
        super(itemView);
        thumbnail = itemView.findViewById(R.id.thumbnailImageView);
        title = itemView.findViewById(R.id.titleTextView);
        description = itemView.findViewById(R.id.descriptionTextView);

        downloadIcon = itemView.findViewById(R.id.downloadIcon);

    }

    public void bind(MediaObject mediaObject, RequestManager requestManager) {
        this.requestManager = requestManager;
        title.setText(mediaObject.getTitle());
        description.setText(mediaObject.getDescription());
        if (mediaObject.isDownloaded()) {
            downloadIcon.setImageResource(R.drawable.ic_download_done);
        } else {
            downloadIcon.setImageResource(R.drawable.ic_file_download_black_24dp);
        }
        this.requestManager
                .load(mediaObject.getThumbnail())
                .into(thumbnail);
    }
}
