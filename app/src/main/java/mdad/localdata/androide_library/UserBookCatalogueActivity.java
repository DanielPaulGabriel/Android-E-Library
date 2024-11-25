package mdad.localdata.androide_library;

import android.os.Bundle;
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

public class UserBookCatalogueActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private static final String BOOKS_URL = Constants.GET_ALL_BOOKS_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_book_catalogue);

        recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)); // 2 columns in the grid

        loadBooks();
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
                                bookAdapter = new BookAdapter(UserBookCatalogueActivity.this, bookList);
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
}
