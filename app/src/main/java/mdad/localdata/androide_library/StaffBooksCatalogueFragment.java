package mdad.localdata.androide_library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class StaffBooksCatalogueFragment extends Fragment {
    private TextView tvNoBooks;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private SearchView searchView;
    private Button btnRetry;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>();
    private static final String BOOKS_URL = Constants.GET_ALL_BOOKS_URL;
    private FloatingActionButton fabAddBook, fabCloudDownload;

    public StaffBooksCatalogueFragment() {
        // Required empty public constructor
    }

    public static StaffBooksCatalogueFragment newInstance() {
        return new StaffBooksCatalogueFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_books_catalogue, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        tvNoBooks = rootView.findViewById(R.id.tvNoBooks);
        searchView = rootView.findViewById(R.id.searchView);
        btnRetry = rootView.findViewById(R.id.btnRetry);
        fabAddBook = rootView.findViewById(R.id.fabAddBook);
        //fabCloudDownload = rootView.findViewById(R.id.fabCloudDownload);


        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));


        loadBooks();
        btnRetry.setOnClickListener(v->loadBooks());

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBooks(newText);
                return true;
            }
        });

        // Add book functionality
        fabAddBook.setOnClickListener(v -> {
            // Open AddBookFragment or show Add Book Dialog
            Fragment staffAddBookFragment = StaffAddBookFragment.newInstance();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffAddBookFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Cloud Download functionality
        /*fabCloudDownload.setOnClickListener(v -> {
            // Open AddBookFragment or show Add Book Dialog
            Fragment staffBookCataloguePopulateFragment = StaffBooksCataloguePopulateFragment.newInstance();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffBookCataloguePopulateFragment)
                    .addToBackStack(null)
                    .commit();
        });*/

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
                            tvNoBooks.setVisibility(View.GONE);
                            btnRetry.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.VISIBLE);
                            fabAddBook.setVisibility(View.VISIBLE);
                            bookList.clear();
                            JSONArray books = jsonObject.getJSONArray("books");
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
                            }
                            filteredList.addAll(bookList);
                            bookAdapter = new BookAdapter(getContext(), filteredList);
                            bookAdapter.setOnBookActionListener(book -> {
                                Fragment staffBookDetailsFragment = StaffBookDetailsFragment.newInstance(
                                        book.getBookId(),
                                        book.getTitle(),
                                        book.getAuthor(),
                                        book.getGenre(),
                                        book.getQuantity(),
                                        book.getSummary(),
                                        book.getCoverPath()
                                );

                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, staffBookDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            recyclerView.setAdapter(bookAdapter);
                        } else {
                            String message =  jsonObject.getString("message");
                            handleNoData(message);
                            //Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            tvNoBooks.setVisibility(View.VISIBLE);
                            btnRetry.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            searchView.setVisibility(View.GONE);
                            fabAddBook.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleNoData("Error loading books.");
                        //Toast.makeText(getContext(), "Error loading books.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = error.getMessage();
                    handleNoData(errorMessage);
                    /*if (isAdded()) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }*/

                });

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(bookList);
        } else {
            for (Book book : bookList) {
                if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                        book.getGenre().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(book);
                }
            }
        }
        if (filteredList.isEmpty()) {
            tvNoBooks.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {
            tvNoBooks.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
        }
        if (bookAdapter != null) {
            bookAdapter.notifyDataSetChanged();
        }
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
        fabAddBook.setVisibility(View.GONE);
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
