package com.example.photobooth.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.photobooth.R;
import com.example.photobooth.activity.AddAlbumActivity;
import com.example.photobooth.activity.AlbumDetailActivity;
import com.example.photobooth.adapter.AlbumAdapter;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.models.Album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText searchEditText;
    private GridView searchResultsGrid;
    private View noSearchResultsLayout;
    private View suggestionsLayout;
    private ImageButton addAlbumButton;
    private AlbumController albumController;
    private AlbumAdapter adapter;
    private List<Map<String, String>> searchResults;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchEditText = view.findViewById(R.id.searchEditText);
        searchResultsGrid = view.findViewById(R.id.searchResultsGrid);
        noSearchResultsLayout = view.findViewById(R.id.noSearchResultsLayout);
        suggestionsLayout = view.findViewById(R.id.suggestionsLayout);
        addAlbumButton = view.findViewById(R.id.imageButton);

        // Initialize controller
        albumController = new AlbumController(requireContext());

        // Initialize search results list
        searchResults = new ArrayList<>();

        // Setup adapter
        adapter = new AlbumAdapter(requireContext(), searchResults);
        searchResultsGrid.setAdapter(adapter);

        // Setup search functionality
        setupSearch();

        // Setup click listeners
        setupClickListeners();

        // Show suggestions by default
        showSuggestions();

        return view;
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showSuggestions();
                } else {
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        searchResults.clear();
        
        List<Album> allAlbums = albumController.getAllAlbums();
        for (Album album : allAlbums) {
            if (album.getTitle().toLowerCase().contains(query.toLowerCase())) {
                Map<String, String> item = new HashMap<>();
                item.put("id", album.getId());
                item.put("title", album.getTitle());
                
                // Get album cover
                Bitmap cover = albumController.getAlbumCover(album.getCoverPath());
                if (cover != null) {
                    item.put("imgUrl", album.getCoverPath());
                }
                
                searchResults.add(item);
            }
        }

        updateSearchResults();
    }

    private void updateSearchResults() {
        if (searchResults.isEmpty()) {
            suggestionsLayout.setVisibility(View.GONE);
            searchResultsGrid.setVisibility(View.GONE);
            noSearchResultsLayout.setVisibility(View.VISIBLE);
        } else {
            suggestionsLayout.setVisibility(View.GONE);
            noSearchResultsLayout.setVisibility(View.GONE);
            searchResultsGrid.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showSuggestions() {
        searchResultsGrid.setVisibility(View.GONE);
        noSearchResultsLayout.setVisibility(View.GONE);
        suggestionsLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        addAlbumButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddAlbumActivity.class);
            startActivity(intent);
        });

        searchResultsGrid.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String albumId = item.get("id");
            if (albumId != null) {
                Intent intent = new Intent(requireContext(), AlbumDetailActivity.class);
                intent.putExtra("album_id", albumId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh search results when returning to the fragment
        performSearch(searchEditText.getText().toString());
    }
}