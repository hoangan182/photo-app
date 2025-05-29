package com.example.photobooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photobooth.R;
import com.example.photobooth.models.PhotoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupedPhotoAdapter extends BaseAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final Context context;
    private final List<Object> items;
    private final Map<Long, List<PhotoItem>> groupedPhotos;
    private final SimpleDateFormat dateFormat;

    public GroupedPhotoAdapter(Context context, List<PhotoItem> photos) {
        this.context = context;
        this.items = new ArrayList<>();
        this.groupedPhotos = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Group photos by date
        for (PhotoItem photo : photos) {
            long date = photo.getCaptureDate();
            if (!groupedPhotos.containsKey(date)) {
                groupedPhotos.put(date, new ArrayList<>());
            }
            groupedPhotos.get(date).add(photo);
        }

        // Create items list with headers and photos
        for (Map.Entry<Long, List<PhotoItem>> entry : groupedPhotos.entrySet()) {
            items.add(entry.getKey()); // Add date as header
            items.addAll(entry.getValue()); // Add photos for this date
        }
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
    public int getItemViewType(int position) {
        return items.get(position) instanceof Long ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        
        if (type == TYPE_HEADER) {
            // Header view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false);
            }
            TextView dateText = convertView.findViewById(R.id.dateHeader);
            long date = (Long) items.get(position);
            dateText.setText("Ngày " + dateFormat.format(new Date(date)));
            return convertView;
        } else {
            // Photo item view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false);
            }
            ImageView imageView = convertView.findViewById(R.id.imageView);
            PhotoItem photo = (PhotoItem) items.get(position);
            
            Glide.with(context)
                .load(photo.getPath())
                .centerCrop()
                .into(imageView);
                
            return convertView;
        }
    }
} 