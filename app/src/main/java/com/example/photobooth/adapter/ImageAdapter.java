package com.example.photobooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.photobooth.R;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> imageUrls;

    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItem = convertView;
        if (gridItem == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            gridItem = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = gridItem.findViewById(R.id.imageItemView);
        Glide.with(context)
                .load(imageUrls.get(position))
                .placeholder(R.drawable.logo)
                .into(imageView);

        return gridItem;
    }
}
