package com.example.photobooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.photobooth.R;
import com.example.photobooth.models.PhotoItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    private final Context context;
    private final List<PhotoItem> photos;
    private final Set<Integer> selectedItems;
    private final boolean isMultiSelect;

    // Constructor for simple mode (without selection)
    public ImageAdapter(Context context, List<PhotoItem> photos) {
        this(context, photos, new HashSet<>(), false);
    }

    // Constructor for multi-select mode
    public ImageAdapter(Context context, List<PhotoItem> photos, Set<Integer> selectedItems, boolean isMultiSelect) {
        this.context = context;
        this.photos = photos;
        this.selectedItems = selectedItems;
        this.isMultiSelect = isMultiSelect;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public PhotoItem getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.selectionOverlay = convertView.findViewById(R.id.selectionOverlay);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Load image using Glide
        Glide.with(context)
            .load(photos.get(position).getPath())
            .centerCrop()
            .into(holder.imageView);

        // Handle selection state
        if (isMultiSelect) {
            holder.selectionOverlay.setVisibility(View.VISIBLE);
            if (selectedItems.contains(position)) {
                holder.selectionOverlay.setAlpha(0.5f);
                holder.imageView.setScaleX(0.9f);
                holder.imageView.setScaleY(0.9f);
            } else {
                holder.selectionOverlay.setAlpha(0f);
                holder.imageView.setScaleX(1f);
                holder.imageView.setScaleY(1f);
            }
        } else {
            holder.selectionOverlay.setVisibility(View.GONE);
            holder.imageView.setScaleX(1f);
            holder.imageView.setScaleY(1f);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        View selectionOverlay;
    }
}
