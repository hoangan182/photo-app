package com.example.photobooth.adapter;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photobooth.R;

import java.util.List;
import java.util.Map;

public class AlbumAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, String>> items; // Danh sách dữ liệu

    public AlbumAdapter(Context context, List<Map<String, String>> items) {
        this.context = context;
        this.items = items;
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
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_album_item, parent, false);

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.item_image);
            holder.textView = convertView.findViewById(R.id.item_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, String> item = items.get(position);
        if (item != null) {
            holder.textView.setText(item.get("title"));

            String imageUrl = item.get("imgUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        // ... các tùy chọn khác
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.logo);
            }
        }

        // Thiết lập kích thước động để phù hợp với GridView
        int displayWidth = getScreenWidth() / 2; // Chia đôi màn hình
        holder.imageView.getLayoutParams().width = displayWidth - 32; // Trừ padding
        holder.imageView.getLayoutParams().height = displayWidth - 32;

        return convertView;
    }

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}