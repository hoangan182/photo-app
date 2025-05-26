package com.example.photobooth.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.AlbumAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllAlbumActivity extends AppCompatActivity {

    GridView gridView;

    TextView txtBackAllAlbum;

    View noAlbumImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_album);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.txtBackAllAlbum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noAlbumImageLayout = findViewById(R.id.noAllImageLayout);
        gridView = findViewById(R.id.gridViewAllAlbum);

        // Tạo dữ liệu mẫu
        List<Map<String, String>> items = new ArrayList<>();

        // Item 1
        Map<String, String> item1 = new HashMap<>();
        item1.put("title", "Messenger");
        item1.put("imgUrl", "https://d1hjkbq40fs2x4.cloudfront.net/2017-08-21/files/landscape-photography_1645.jpg");
        items.add(item1);

        // Item 2
        Map<String, String> item2 = new HashMap<>();
        item2.put("title", "Facebook");
        item2.put("imgUrl", "https://photo.znews.vn/w660/Uploaded/mdf_eioxrd/2021_07_06/2.jpg");
        items.add(item2);

        // Item 3
        Map<String, String> item3 = new HashMap<>();
        item3.put("title", "Zalo");
        item3.put("imgUrl", "https://vcdn1-dulich.vnecdn.net/2021/07/16/1-1626437591.jpg?w=460&h=0&q=100&dpr=1&fit=crop&s=wkxNSU_JeGofMu90v5u03g");
        items.add(item3);

        // Item 4
        Map<String, String> item4 = new HashMap<>();
        item4.put("title", "Screenshot");
        item4.put("imgUrl", "https://nads.1cdn.vn/2024/11/22/74da3f39-759b-4f08-8850-4c8f2937e81a-1_mangeshdes.png");
        items.add(item4);

        // Item 5
        Map<String, String> item5 = new HashMap<>();
        item5.put("title", "Favorite");
        item5.put("imgUrl", "https://hoanghamobile.com/tin-tuc/wp-content/uploads/2024/11/tai-hinh-nen-dep-mien-phi.jpg");
        items.add(item5);

        if (items.isEmpty()) {
            noAlbumImageLayout.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            noAlbumImageLayout.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

        AlbumAdapter adapter = new AlbumAdapter(this, items);
        gridView.setAdapter(adapter);

        // Xử lý sự kiện click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> selectedItem = items.get(position);
                Toast.makeText(AllAlbumActivity.this, "Clicked: " + selectedItem.get("title"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}