package mdad.localdata.androide_library;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class UserBookCatalogueFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>();
    private static final String BOOKS_URL = Constants.GET_ALL_BOOKS_URL;

    public UserBookCatalogueFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_book_catalogue, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        SearchView searchView = rootView.findViewById(R.id.searchView);

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

        return rootView;
    }

    private void loadBooks() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, BOOKS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
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
                            recyclerView.setAdapter(bookAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "JSON Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(bookList);
        } else {
            for (Book book : bookList) {
                if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(book);
                }
            }
        }
        if (bookAdapter != null) {
            bookAdapter.notifyDataSetChanged();
        }
    }
}
