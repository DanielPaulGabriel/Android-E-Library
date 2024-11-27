package mdad.localdata.androide_library;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BookDetailsActivity extends AppCompatActivity {

    private ImageView ivBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvBookDescription;
    private ImageButton btnBack;
    private Button btnBorrow;
    private int bookId;
    private int userId;
    private static final String BORROW_URL = Constants.BORROW_BOOK_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Initialize Views
        ivBookCover = findViewById(R.id.ivBookCover);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookDescription = findViewById(R.id.tvBookDescription);
        btnBack = findViewById(R.id.btnBack);
        btnBorrow = findViewById(R.id.btnBorrow);
        userId = SharedPrefsManager.getUserId(this);


        // Get Book Data from Intent
        Intent intent = getIntent();
        bookId = intent.getIntExtra("bookId", -1);
        String coverUrl = intent.getStringExtra("coverUrl");
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String description = intent.getStringExtra("description");

        // Populate Views
        Glide.with(this).load(coverUrl).into(ivBookCover);
        tvBookTitle.setText(title);
        tvBookAuthor.setText(author);
        tvBookDescription.setText(description);

        // Back Button Listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous screen
            }
        });

        // Borrow Button Listener
        btnBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(bookId+","+userId);
                borrowBook(bookId, userId);
            }
        });
    }

    private void borrowBook(int bookId, int userId) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,BORROW_URL ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("BorrowBookResponse", response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                Toast.makeText(BookDetailsActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                btnBorrow.setEnabled(false);
                                btnBorrow.setText("Borrowed");
                            } else {
                                Toast.makeText(BookDetailsActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                btnBorrow.setEnabled(false);
                                btnBorrow.setText("Borrow");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BookDetailsActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BookDetailsActivity.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("book_id", String.valueOf(bookId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}