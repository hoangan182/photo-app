package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.ImageAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrashActivity extends AppCompatActivity {
    private List<String> imageUrls = new ArrayList<>();

    TextView txtBackTrash;
    ImageView imgTrashPageOption;
    GridView gridViewTrash;
    ConstraintLayout noTrashImageLayout;

    ImageView imgShowOption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        gridViewTrash = findViewById(R.id.gridViewTrash);
        noTrashImageLayout = findViewById(R.id.noTrashImageLayout);
        imgTrashPageOption = findViewById(R.id.imgTrashPageOption);
        txtBackTrash = findViewById(R.id.txtBackTrash);

        imgTrashPageOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTrashOption();}
        });
        txtBackTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageUrls = Arrays.asList(
                "https://photo.znews.vn/w660/Uploaded/mdf_eioxrd/2021_07_06/2.jpg",
                "https://images2.thanhnien.vn/zoom/700_438/528068263637045248/2024/1/26/e093e9cfc9027d6a142358d24d2ee350-65a11ac2af785880-17061562929701875684912-37-0-587-880-crop-1706239860681642023140.jpg",
                "https://vcdn1-dulich.vnecdn.net/2021/07/16/1-1626437591.jpg?w=460&h=0&q=100&dpr=1&fit=crop&s=wkxNSU_JeGofMu90v5u03g",
                "https://nads.1cdn.vn/2024/11/22/74da3f39-759b-4f08-8850-4c8f2937e81a-1_mangeshdes.png",
                "https://hoanghamobile.com/tin-tuc/wp-content/uploads/2024/11/tai-hinh-nen-dep-mien-phi.jpg",
                "https://d1hjkbq40fs2x4.cloudfront.net/2017-08-21/files/landscape-photography_1645.jpg",
                "https://cdnphoto.dantri.com.vn/aerztjLQz4WGhQnIqEocC_FLsLw=/thumb_w/960/2020/03/03/thanhbinh-1-a-3-docx-1583197236967.jpeg"

        );
        if (imageUrls.isEmpty()) {
            noTrashImageLayout.setVisibility(View.VISIBLE);
            gridViewTrash.setVisibility(View.GONE);
        } else {
            noTrashImageLayout.setVisibility(View.GONE);
            gridViewTrash.setVisibility(View.VISIBLE);
        }

        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        gridViewTrash.setAdapter(adapter);

        gridViewTrash.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, FullScreenDeletedImage.class);
            intent.putExtra("image_url", imageUrls.get(position));
            startActivity(intent);
        });
    }
        private void showTrashOption() {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.trash_page_option, null);
            bottomSheetDialog.setContentView(view);

            TextView deleteAll = view.findViewById(R.id.optionDeleteAllPhoto);
            TextView restoreAll = view.findViewById(R.id.optionRestoreAllPhoto);

            bottomSheetDialog.show();

        }
    }
