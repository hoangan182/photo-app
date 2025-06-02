package com.example.photobooth.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photobooth.R;
import com.example.photobooth.activity.FullScreenImageActivity;
import com.example.photobooth.models.PhotoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhotoGroupAdapter extends BaseAdapter {
    private final Context context;
    private final List<PhotoGroup> groups;
    private boolean isMultiSelect = false;
    private final Map<String, Boolean> selectedItems = new HashMap<>();
    private OnPhotoSelectionListener selectionListener;

    public interface OnPhotoSelectionListener {
        void onPhotoSelected(PhotoItem photo, boolean isSelected);
        void onPhotoLongClick(PhotoItem photo);
    }

    public void setOnPhotoSelectionListener(OnPhotoSelectionListener listener) {
        this.selectionListener = listener;
    }

    public PhotoGroupAdapter(Context context, List<PhotoItem> photos) {
        this.context = context;
        this.groups = groupPhotosByDate(photos);
    }

    private List<PhotoGroup> groupPhotosByDate(List<PhotoItem> photos) {
        Log.d("PhotoGroupAdapter", "Grouping " + photos.size() + " photos by date");
        Map<Date, List<PhotoItem>> groupedPhotos = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (PhotoItem photo : photos) {
            long captureTime = photo.getCaptureDate();
            Date date = new Date(captureTime);
            String dateStr = dateFormat.format(date);
            try {
                Date groupDate = dateFormat.parse(dateStr);
                if (groupDate != null) {
                    groupedPhotos.computeIfAbsent(groupDate, k -> new ArrayList<>()).add(photo);
                }
            } catch (Exception e) {
                Log.e("PhotoGroupAdapter", "Error grouping photo by date", e);
            }
        }

        List<PhotoGroup> result = new ArrayList<>();
        for (Map.Entry<Date, List<PhotoItem>> entry : groupedPhotos.entrySet()) {
            // Sort photos within each group by capture time (newest first)
            List<PhotoItem> sortedPhotos = new ArrayList<>(entry.getValue());
            sortedPhotos.sort((p1, p2) -> Long.compare(p2.getCaptureDate(), p1.getCaptureDate()));
            result.add(new PhotoGroup(entry.getKey(), sortedPhotos));
        }
        
        // Sort groups by date (newest first)
        result.sort((g1, g2) -> g2.date.compareTo(g1.date));
        Log.d("PhotoGroupAdapter", "Created " + result.size() + " photo groups");
        return result;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public PhotoGroup getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo_group, parent, false);
        }

        PhotoGroup group = getItem(position);
        TextView dateHeader = convertView.findViewById(R.id.dateHeader);
        GridView photoGrid = convertView.findViewById(R.id.photoGrid);

        // Format date to show day of week and full date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi"));
        String formattedDate = dateFormat.format(group.date);
        dateHeader.setText(formattedDate);

        // Set up GridView
        photoGrid.setAdapter(new ImageAdapter(context, group.photos));
        photoGrid.setNumColumns(3);
        photoGrid.setHorizontalSpacing(4);
        photoGrid.setVerticalSpacing(4);

        // Calculate and set the height of the GridView
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int itemWidth = (screenWidth - (4 * 4)) / 3; // 4dp padding on each side, 4dp spacing between items
        int itemHeight = itemWidth; // Keep aspect ratio 1:1
        int numRows = (int) Math.ceil(group.photos.size() / 3.0);
        int totalHeight = (itemHeight * numRows) + (4 * (numRows - 1)); // Add spacing between rows

        ViewGroup.LayoutParams params = photoGrid.getLayoutParams();
        params.height = totalHeight;
        photoGrid.setLayoutParams(params);

        photoGrid.setOnItemClickListener((parent1, view, position1, id) -> {
            PhotoItem photo = group.photos.get(position1);
            if (isMultiSelect) {
                // Toggle selection when in multi-select mode
                togglePhotoSelection(photo);
            } else {
                // Open full screen view when not in multi-select mode
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("photo_path", photo.getPath());
                ((Activity) context).startActivityForResult(intent, 1);
            }
        });

        photoGrid.setOnItemLongClickListener((parent1, view, position1, id) -> {
            if (!isMultiSelect) {
                // Start multi-select mode
                isMultiSelect = true;
                PhotoItem photo = group.photos.get(position1);
                togglePhotoSelection(photo);
                if (selectionListener != null) {
                    selectionListener.onPhotoSelected(photo, true);
                    selectionListener.onPhotoLongClick(photo);
                }
            }
            return true;
        });

        return convertView;
    }

    private void togglePhotoSelection(PhotoItem photo) {
        boolean isSelected = !selectedItems.getOrDefault(photo.getPath(), false);
        selectedItems.put(photo.getPath(), isSelected);
        if (selectionListener != null) {
            selectionListener.onPhotoSelected(photo, isSelected);
        }
        notifyDataSetChanged();
    }

    public void setMultiSelect(boolean multiSelect) {
        if (!multiSelect) {
            selectedItems.clear();
        }
        isMultiSelect = multiSelect;
        notifyDataSetChanged();
    }

    public List<PhotoItem> getSelectedItems() {
        List<PhotoItem> selected = new ArrayList<>();
        for (PhotoGroup group : groups) {
            for (PhotoItem photo : group.photos) {
                if (selectedItems.getOrDefault(photo.getPath(), false)) {
                    selected.add(photo);
                }
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedItems.clear();
        isMultiSelect = false;
        notifyDataSetChanged();
    }

    private int getSelectedCount() {
        return (int) selectedItems.values().stream().filter(Boolean::booleanValue).count();
    }

    public void updatePhotos(List<PhotoItem> newPhotos) {
        Log.d("PhotoGroupAdapter", "Updating photos - new count: " + newPhotos.size());
        this.groups.clear();
        this.groups.addAll(groupPhotosByDate(newPhotos));
        Log.d("PhotoGroupAdapter", "Grouped into " + this.groups.size() + " groups");
        notifyDataSetChanged();
    }

    private static class PhotoGroup {
        final Date date;
        final List<PhotoItem> photos;

        PhotoGroup(Date date, List<PhotoItem> photos) {
            this.date = date;
            this.photos = photos;
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private final Context context;
        private final List<PhotoItem> photos;

        public ImageAdapter(Context context, List<PhotoItem> photos) {
            this.context = context;
            this.photos = photos;
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
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            }

            PhotoItem photo = getItem(position);
            ImageView imageView = convertView.findViewById(R.id.imgPhoto);
            View overlay = convertView.findViewById(R.id.overlay);
            RadioButton radioButton = convertView.findViewById(R.id.radioSelect);

            // Load and display image
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath());
            imageView.setImageBitmap(bitmap);

            // Handle selection state
            boolean isSelected = selectedItems.getOrDefault(photo.getPath(), false);
            overlay.setVisibility(isMultiSelect && isSelected ? View.VISIBLE : View.GONE);
            radioButton.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);
            radioButton.setChecked(isSelected);
            
            // Make radio button non-interactive
            radioButton.setClickable(false);
            radioButton.setFocusable(false);

            return convertView;
        }
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d("PhotoGroupAdapter", "notifyDataSetChanged called");
        super.notifyDataSetChanged();
    }
} 