package mdad.localdata.androide_library;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookDetailsActivity extends AppCompatActivity {

    private ImageView ivBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvBookDescription;
    private ImageButton btnBack;
    private Button btnBorrow, btnSubmitReview;
    private RatingBar bookRatingBar;
    private EditText editReviewText;
    private int bookId;
    private int userId;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private static final String BORROW_URL = Constants.BORROW_BOOK_URL;
    private static final String SUBMIT_REVIEW_URL = Constants.CREATE_REVIEW_URL;
    private static final String GET_BOOK_REVIEWS_URL = Constants.GET_ALL_BOOK_REVIEWS_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        ivBookCover = findViewById(R.id.ivBookCover);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        tvBookDescription = findViewById(R.id.tvBookDescription);
        btnBorrow = findViewById(R.id.btnBorrow);
        bookRatingBar = findViewById(R.id.bookRatingBar);
        editReviewText = findViewById(R.id.editReviewText);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        // Retrieve Id of logged in user
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
                borrowBook(bookId, userId);
            }
        });

        // Submit Review Button Listener
        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });

        fetchReviews(bookId);
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

    private void submitReview() {
        // Get user input
        final String reviewText = editReviewText.getText().toString().trim();
        final float rating = bookRatingBar.getRating();

        // Validate input
        if (reviewText.isEmpty()) {
            Toast.makeText(BookDetailsActivity.this, "Please write a review before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(BookDetailsActivity.this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send request to server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SUBMIT_REVIEW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                // Notify the user
                                Toast.makeText(BookDetailsActivity.this, "Review Submitted Successfully!", Toast.LENGTH_SHORT).show();

                                // Create a new Review object and add it to the adapter's list
                                Review newReview = new Review(
                                        jsonObject.getInt("review_id"), // Get review ID from the response
                                        jsonObject.getString("username"), // Server should return the username
                                        (int) rating, // Rating
                                        reviewText, // Review text
                                        jsonObject.getString("created_at") // Timestamp
                                );

                                // Update the adapter with the new review
                                List<Review> updatedReviews = new ArrayList<>(reviewAdapter.getReviews());
                                updatedReviews.add(0, newReview); // Add to the top of the list
                                reviewAdapter.updateReviews(updatedReviews);

                                // Clear input fields
                                editReviewText.setText("");
                                bookRatingBar.setRating(0);
                            } else {
                                Toast.makeText(BookDetailsActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BookDetailsActivity.this, "Error processing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BookDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("book_id", String.valueOf(bookId));
                params.put("rating", String.valueOf((int) rating));
                params.put("review", reviewText);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // Fetch reviews from the server and populate RecyclerView
    private void fetchReviews(int bookId) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_BOOK_REVIEWS_URL + "?book_id=" + bookId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("fetchServerResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                JSONArray reviewsArray = jsonObject.getJSONArray("reviews");
                                List<Review> reviews = new ArrayList<>();
                                for (int i = 0; i < reviewsArray.length(); i++) {
                                    JSONObject reviewJson = reviewsArray.getJSONObject(i);
                                    Review review = new Review(
                                            reviewJson.getInt("review_id"),
                                            reviewJson.getString("username"),
                                            reviewJson.getInt("rating"),
                                            reviewJson.getString("review_text"),
                                            reviewJson.getString("created_at")
                                    );
                                    reviews.add(review);
                                }

                                // Set up RecyclerView
                                RecyclerView recyclerView = findViewById(R.id.recyclerViewReviews);
                                recyclerView.setLayoutManager(new LinearLayoutManager(BookDetailsActivity.this));
                                reviewAdapter = new ReviewAdapter(reviews);
                                recyclerView.setAdapter(reviewAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BookDetailsActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BookDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


}