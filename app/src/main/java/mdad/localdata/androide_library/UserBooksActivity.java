package mdad.localdata.androide_library;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class UserBooksActivity extends AppCompatActivity {
    private static final String GET_USER_BOOKS_URL = Constants.GET_USER_BOOKS_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_books);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewUserBooks);
        TextView tvNoBooks = findViewById(R.id.tvNoBooks);

        // Fetch User ID
        int userId = SharedPrefsManager.getUserId(this);

        fetchBooks(userId, recyclerView, tvNoBooks);
    }

    private void fetchBooks(int userId, RecyclerView recyclerView, TextView tvNoBooks) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USER_BOOKS_URL + "?user_id=" + userId,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray booksArray = jsonObject.getJSONArray("borrowed_books");
                            List<UserBook> books = new ArrayList<>();
                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject bookJson = booksArray.getJSONObject(i);
                                UserBook book = new UserBook(
                                        bookJson.getInt("book_id"),
                                        bookJson.getString("title"),
                                        bookJson.getString("author"),
                                        bookJson.optString("genre", null),
                                        bookJson.getString("summary"),
                                        bookJson.getString("cover_path"),
                                        bookJson.getString("borrow_date"),
                                        bookJson.getString("due_date"),
                                        bookJson.optString("return_date", null)
                                );
                                books.add(book);
                            }

                            if (books.isEmpty()) {
                                tvNoBooks.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoBooks.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                // Set up RecyclerView
                                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                UserBookAdapter adapter = new UserBookAdapter(books);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}