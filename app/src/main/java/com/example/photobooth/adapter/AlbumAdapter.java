package com.example.photobooth.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photobooth.R;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlbumAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> items;
    private Set<Integer> selectedItems;
    private boolean isMultiSelect;

    public AlbumAdapter(Context context, List<Map<String, String>> items) {
        this(context, items, new HashSet<>(), false);
    }

    public AlbumAdapter(Context context, List<Map<String, String>> items, Set<Integer> selectedItems, boolean isMultiSelect) {
        this.context = context;
        this.items = items;
        this.selectedItems = selectedItems;
        this.isMultiSelect = isMultiSelect;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_album_item, parent, false);
        }

        Map<String, String> item = items.get(position);
        String title = item.get("title");
        String imgUrl = item.get("imgUrl");

        TextView titleTextView = convertView.findViewById(R.id.item_text);
        ImageView albumCoverImageView = convertView.findViewById(R.id.item_image);
        View selectionOverlay = convertView.findViewById(R.id.selection_overlay);

        titleTextView.setText(title);

        if (imgUrl != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imgUrl);
                if (bitmap != null) {
                    albumCoverImageView.setImageBitmap(bitmap);
                } else {
                    albumCoverImageView.setImageResource(R.drawable.default_album_cover);
                }
            } catch (Exception e) {
                albumCoverImageView.setImageResource(R.drawable.default_album_cover);
            }
        } else {
            albumCoverImageView.setImageResource(R.drawable.default_album_cover);
        }

        // Handle selection state
        if (isMultiSelect) {
            selectionOverlay.setVisibility(selectedItems.contains(position) ? View.VISIBLE : View.GONE);
        } else {
            selectionOverlay.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedItems() {
        return selectedItems;
    }

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        if (!isMultiSelect) {
            clearSelection();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelect() {
        return isMultiSelect;
    }
}