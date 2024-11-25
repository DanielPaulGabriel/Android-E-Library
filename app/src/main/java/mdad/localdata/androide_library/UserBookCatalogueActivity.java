package mdad.localdata.androide_library;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import androidx.appcompat.widget.SearchView;

public class UserBookCatalogueActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>(); // Filtered data
    private static final String BOOKS_URL = Constants.GET_ALL_BOOKS_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_book_catalogue);

        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        SearchView searchView = findViewById(R.id.searchView);

        loadBooks();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private Runnable workRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Trigger search when the user presses enter
                filterBooks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }
                workRunnable = () -> filterBooks(newText);
                handler.postDelayed(workRunnable, 300); // Delay of 300ms for debouncing
                return true;
            }
        });
    }

    private void loadBooks() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, BOOKS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                bookAdapter = new BookAdapter(UserBookCatalogueActivity.this, filteredList);
                                recyclerView.setAdapter(bookAdapter);
                            } else {
                                Toast.makeText(UserBookCatalogueActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(UserBookCatalogueActivity.this, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserBookCatalogueActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(bookList); // Show all books if query is empty
        } else {
            for (Book book : bookList) {
                if (book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(book);
                }
            }
        }
        bookAdapter.notifyDataSetChanged(); // Update RecyclerView
    }

}
