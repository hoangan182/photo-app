package com.example.photobooth.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.photobooth.R;
import com.example.photobooth.controllers.AlbumController;

import java.util.List;
import java.util.Map;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> items;
    private AlbumController albumController;

    public PhotoAdapter(Context context, List<Map<String, String>> items) {
        this.context = context;
        this.items = items;
        this.albumController = new AlbumController(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        }

        Map<String, String> item = items.get(position);
        String photoPath = item.get("path");

        ImageView photoImageView = convertView.findViewById(R.id.imgPhoto);

        if (photoPath != null) {
            Bitmap bitmap = albumController.getAlbumPhoto(photoPath);
            if (bitmap != null) {
                photoImageView.setImageBitmap(bitmap);
            } else {
                photoImageView.setImageResource(R.drawable.default_album_cover);
            }
        } else {
            photoImageView.setImageResource(R.drawable.default_album_cover);
        }

        return convertView;
    }
} 