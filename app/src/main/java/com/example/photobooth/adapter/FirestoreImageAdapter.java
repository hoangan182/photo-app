package com.example.photobooth.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.photobooth.R;
import com.example.photobooth.activity.ImageDetailActivity;
import com.example.photobooth.models.Image;

import java.util.List;

public class FirestoreImageAdapter extends BaseAdapter {
    private static final String TAG = "FirestoreImageAdapter";
    private final Context context;
    private final List<Image> images;

    public FirestoreImageAdapter(Context context, List<Image> images) {
        this.context = context;
        this.images = images;
        Log.d(TAG, "Adapter initialized with " + images.size() + " images");
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Image getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.progressBar = convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Image image = getItem(position);
        holder.progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Loading image at position " + position + " with URL: " + image.getUrl());

        if (image.getUrl() != null && !image.getUrl().isEmpty()) {
            Glide.with(context)
                .load(image.getUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Error loading image at position " + position + ": " + e.getMessage());
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Successfully loaded image at position " + position);
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imageView);
        } else {
            Log.e(TAG, "Invalid URL for image at position " + position);
            holder.progressBar.setVisibility(View.GONE);
            holder.imageView.setImageResource(R.drawable.image_error);
        }

        // Set click listener
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, image);
            context.startActivity(intent);
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
} 