package mdad.localdata.androide_library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookCatalogueFragment extends Fragment {
    private TextView tvNoBooks;
    private Button btnRetry;
    private ChipGroup chipGroupGenres;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>();
    private static final String BOOKS_URL = Constants.GET_ALL_BOOKS_URL;

    public BookCatalogueFragment() {
        // Required empty public constructor
    }

    public static BookCatalogueFragment newInstance() {
        return new BookCatalogueFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_catalogue, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        tvNoBooks = rootView.findViewById(R.id.tvNoBooks);
        btnRetry = rootView.findViewById(R.id.btnRetry);
        searchView = rootView.findViewById(R.id.searchView);
        chipGroupGenres = rootView.findViewById(R.id.chipGroupGenres);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        loadBooks();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private Runnable workRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }
                workRunnable = () -> filterBooks(newText);
                handler.postDelayed(workRunnable, 300); // Debounce input
                return true;
            }
        });
        btnRetry.setOnClickListener(v->loadBooks());

        return rootView;
    }

    private void loadBooks() {
        if (!isNetworkAvailable()) {
            handleNoData("No internet connection. Please check your connection.");
            return;
        }
        bookList.clear();
        filteredList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, BOOKS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            if (!isAdded()) return; // Ensure fragment is attached

                            tvNoBooks.setVisibility(View.GONE);
                            btnRetry.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.VISIBLE);

                            bookList.clear();
                            JSONArray books = jsonObject.getJSONArray("books");
                            List<String> genres = new ArrayList<>();
                            for (int i = 0; i < books.length(); i++) {
                                JSONObject bookObject = books.getJSONObject(i);
                                bookList.add(new Book(
                                        bookObject.getInt("book_id"),
                                        bookObject.getString("title"),
                                        bookObject.getString("author"),
                                        bookObject.getString("genre"),
                                        bookObject.getString("summary"),
                                        bookObject.getInt("quantity"),
                                        bookObject.getString("content_path"),
                                        bookObject.getString("cover_path")
                                ));
                                // Collect unique genres
                                String genre = bookObject.getString("genre");
                                if (!genres.contains(genre)) {
                                    genres.add(genre);
                                }
                            }

                            chipGroupGenres.post(() -> setupGenreChips(genres));
                            filteredList.addAll(bookList);

                            if (filteredList.isEmpty()) {
                                tvNoBooks.setVisibility(View.VISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                searchView.setVisibility(View.GONE);
                            }

                            bookAdapter = new BookAdapter(requireContext(), filteredList);
                            bookAdapter.setOnBookActionListener(book -> {
                                Fragment bookDetailsFragment = BookDetailsFragment.newInstance(
                                        book.getBookId(),
                                        Constants.BASE_URL + book.getCoverPath(),
                                        book.getTitle(),
                                        book.getAuthor(),
                                        book.getSummary()
                                );

                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, bookDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            recyclerView.setAdapter(bookAdapter);
                        } else {
                            handleNoData("No books available.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleNoData("JSON Parsing Error.");
                    }
                },
                error -> handleNoData("Error: Server is offline or unreachable.")
        );

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    private void applyFilters(String query, String genre) {
        filteredList.clear();

        for (Book book : bookList) {
            boolean matchesSearch = query.isEmpty() || book.getTitle().toLowerCase().contains(query.toLowerCase())
                    || book.getAuthor().toLowerCase().contains(query.toLowerCase());

            boolean matchesGenre = genre.equalsIgnoreCase("All") || book.getGenre().equalsIgnoreCase(genre);

            if (matchesSearch && matchesGenre) {
                filteredList.add(book);
            }
        }

        if (filteredList.isEmpty()) {
            tvNoBooks.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoBooks.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (bookAdapter != null) {
            bookAdapter.notifyDataSetChanged();
        }
    }
    private void filterBooks(String query) {
        String selectedGenre = getSelectedGenre(); // Helper method to get the currently selected genre
        applyFilters(query, selectedGenre);
    }
    private void filterBooksByGenre(String genre) {
        searchView = getView().findViewById(R.id.searchView);
        String query = searchView.getQuery().toString(); // Get the current search query
        applyFilters(query, genre);
    }
    private void setupGenreChips(List<String> genres) {
        chipGroupGenres.removeAllViews();
        for (String genre : genres) {
            Chip chip = new Chip(getContext());
            chip.setText(genre);
            chip.setCheckable(true);
            chipGroupGenres.addView(chip);
        }

        // Add "All" chip for resetting the filter
        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        chipGroupGenres.addView(allChip);

        // Set a listener to handle chip selection
        chipGroupGenres.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedGenre = getSelectedGenre();
            filterBooksByGenre(selectedGenre);
        });
    }
    private String getSelectedGenre() {
        int selectedChipId = chipGroupGenres.getCheckedChipId();
        if (selectedChipId != -1) {
            Chip selectedChip = chipGroupGenres.findViewById(selectedChipId);
            if (selectedChip != null) {
                return selectedChip.getText().toString();
            }
        }
        return "All"; // Default to "All" if no chip is selected
    }
    private void handleNoData(String message) {
        if (!isAdded()) return; // Ensure fragment is attached
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), "An unknown error occurred.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        tvNoBooks.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

}
