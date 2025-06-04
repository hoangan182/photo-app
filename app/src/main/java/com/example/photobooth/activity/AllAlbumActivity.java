package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.AlbumAdapter;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.models.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllAlbumActivity extends AppCompatActivity {

    private GridView gridView;
    private TextView txtBackAllAlbum;
    private View noAlbumImageLayout;
    private ImageView imgAddAlbum;
    private AlbumController albumController;
    private AlbumAdapter adapter;
    private ActionMode actionMode;
    private View topBarLayout;

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_album_selection, menu);
            toggleTopBar(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                deleteSelectedAlbums();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.setMultiSelect(false);
            actionMode = null;
            toggleTopBar(true);
        }
    };

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

        // Initialize controller
        albumController = new AlbumController(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();

        // Load albums
        loadAlbums();
    }

    private void initializeViews() {
        gridView = findViewById(R.id.gridViewAllAlbum);
        txtBackAllAlbum = findViewById(R.id.txtBackAllAlbum);
        noAlbumImageLayout = findViewById(R.id.noAllImageLayout);
        imgAddAlbum = findViewById(R.id.imgShowOptionAllPhoto);
        topBarLayout = findViewById(R.id.topBarLayout);
    }

    private void setupClickListeners() {
        txtBackAllAlbum.setOnClickListener(v -> finish());

        imgAddAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAlbumActivity.class);
            startActivity(intent);
        });

        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            if (adapter.isMultiSelect()) {
                adapter.toggleSelection(position);
                updateActionModeTitle();
            } else {
                Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
                String albumId = item.get("id");
                if (albumId != null) {
                    Intent intent = new Intent(AllAlbumActivity.this, AlbumDetailActivity.class);
                    intent.putExtra("album_id", albumId);
                    startActivity(intent);
                }
            }
        });

        gridView.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            if (!adapter.isMultiSelect()) {
                adapter.setMultiSelect(true);
                adapter.toggleSelection(position);
                actionMode = startActionMode(actionModeCallback);
                updateActionModeTitle();
            }
            return true;
        });
    }

    private void updateActionModeTitle() {
        if (actionMode != null) {
            int count = adapter.getSelectedItems().size();
            actionMode.setTitle(count + " selected");
        }
    }

    private void deleteSelectedAlbums() {
        Set<Integer> selectedPositions = adapter.getSelectedItems();
        List<Map<String, String>> items = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (!selectedPositions.contains(i)) {
                items.add((Map<String, String>) adapter.getItem(i));
            }
        }

        // Delete albums from storage
        for (int position : selectedPositions) {
            Map<String, String> item = (Map<String, String>) adapter.getItem(position);
            String albumId = item.get("id");
            if (albumId != null) {
                albumController.deleteAlbum(albumId);
            }
        }

        // Update adapter
        adapter = new AlbumAdapter(this, items);
        gridView.setAdapter(adapter);

        // Show/hide empty state
        if (items.isEmpty()) {
            noAlbumImageLayout.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            noAlbumImageLayout.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

        Toast.makeText(this, "Deleted " + selectedPositions.size() + " album(s)", Toast.LENGTH_SHORT).show();
    }

    private void loadAlbums() {
        List<Album> albums = albumController.getAllAlbums();
        List<Map<String, String>> items = new ArrayList<>();

        for (Album album : albums) {
            Map<String, String> item = new HashMap<>();
            item.put("id", album.getId());
            item.put("title", album.getTitle());
            
            // Get album cover
            Bitmap cover = albumController.getAlbumCover(album.getCoverPath());
            if (cover != null) {
                item.put("imgUrl", album.getCoverPath());
            }
            
            items.add(item);
        }

        if (items.isEmpty()) {
            noAlbumImageLayout.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            noAlbumImageLayout.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

        adapter = new AlbumAdapter(this, items);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbums(); // Reload albums when returning from AddAlbumActivity
    }

    private void toggleTopBar(boolean show) {
        if (topBarLayout != null) {
            topBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}