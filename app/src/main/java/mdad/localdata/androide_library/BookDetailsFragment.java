package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import java.util.Random;

public class BookDetailsFragment extends Fragment {

    private static final String ARG_BOOK_ID = "bookId";
    private static final String ARG_COVER_URL = "coverUrl";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_DESCRIPTION = "description";
    private View rootView;
    private int bookId;
    private int userId;
    private String coverUrl;
    private String title;
    private String author;
    private String description;
    private ImageView ivBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvBookDescription, tvReviewsTitle, tvNoReviews;
    private ImageButton btnBack;
    private Button btnBorrow, btnSubmitReview;
    private RatingBar bookRatingBar;
    private EditText editReviewText;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private static final String BORROW_URL = Constants.BORROW_BOOK_URL;
    private static final String SUBMIT_REVIEW_URL = Constants.CREATE_REVIEW_URL;
    private static final String GET_BOOK_REVIEWS_URL = Constants.GET_ALL_BOOK_REVIEWS_URL;
    private static final String GET_BORROW_STATUS = Constants.GET_BORROW_STATUS;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public static BookDetailsFragment newInstance(int bookId, String coverUrl, String title, String author, String description) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putString(ARG_COVER_URL, coverUrl);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
            coverUrl = getArguments().getString(ARG_COVER_URL);
            title = getArguments().getString(ARG_TITLE);
            author = getArguments().getString(ARG_AUTHOR);
            description = getArguments().getString(ARG_DESCRIPTION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_book_details, container, false);

        btnBack = rootView.findViewById(R.id.btnBack);
        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        tvBookTitle = rootView.findViewById(R.id.tvBookTitle);
        tvBookAuthor = rootView.findViewById(R.id.tvBookAuthor);
        tvBookDescription = rootView.findViewById(R.id.tvBookDescription);
        btnBorrow = rootView.findViewById(R.id.btnBorrow);
        bookRatingBar = rootView.findViewById(R.id.bookRatingBar);
        editReviewText = rootView.findViewById(R.id.editReviewText);
        btnSubmitReview = rootView.findViewById(R.id.btnSubmitReview);
        tvReviewsTitle = rootView.findViewById(R.id.tvReviewsTitle);
        tvNoReviews = rootView.findViewById(R.id.tvNoReviews);
        recyclerView = rootView.findViewById(R.id.recyclerViewReviews);

        // Retrieve Id of logged in user
        userId = SharedPrefsManager.getUserId(requireContext());
        //System.out.println("BorrowBook user id input:"+ userId);


        Glide.with(this).load(coverUrl+"?t="+System.currentTimeMillis()).into(ivBookCover);
        tvBookTitle.setText(title);
        tvBookAuthor.setText(author);
        tvBookDescription.setText(description);


        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnBorrow.setOnClickListener(v -> borrowBook(bookId, userId));

        btnSubmitReview.setOnClickListener(v -> submitReview());
        fetchBorrowStatus(bookId, userId);
        fetchReviews(bookId);

        return rootView;
    }

    private void borrowBook(int bookId, int userId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BORROW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Log.d("BorrowBookResponse", response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                showSuccessDialog("Book Borrowed Successfully!");
                                //Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                btnBorrow.setEnabled(false);
                                btnBorrow.setText("Borrowed");
                                btnBorrow.setBackgroundColor(getResources().getColor(R.color.grey_disabled));
                                String due_date = jsonObject.getString("due_date");
                                sendNotification("Book Borrowed", "Your borrowed book is due on \"" + due_date);
                            } else {
                                Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                //btnBorrow.setEnabled(false);
                                //btnBorrow.setText("Borrow");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    private void fetchBorrowStatus(int bookId, int userId) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_BORROW_STATUS+ "?user_id="+userId+"&book_id="+bookId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                btnBorrow.setEnabled(false);
                                btnBorrow.setText("Borrowed");
                                btnBorrow.setBackgroundColor(getResources().getColor(R.color.grey_disabled));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(requireContext(), "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    private void submitReview() {
        // Get user input
        final String reviewText = editReviewText.getText().toString().trim();
        final float rating = bookRatingBar.getRating();

        // Validate input
        if (reviewText.isEmpty()) {
            Toast.makeText(requireContext(), "Please write a review before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(requireContext(), "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send request to server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SUBMIT_REVIEW_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("SubmitReviewResponse", response);
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                // Notify the user
                                //Toast.makeText(requireContext(), "Review Submitted Successfully!", Toast.LENGTH_SHORT).show();

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

                                showSuccessDialog("Review Submitted Successfully!");
                            } else {
                                Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error processing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("book_id", String.valueOf(bookId));
                params.put("rating", String.valueOf((int) rating));
                params.put("review_text", reviewText);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    // Fetch reviews from the server and populate RecyclerView
    private void fetchReviews(int bookId) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_BOOK_REVIEWS_URL + "?book_id=" + bookId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log.d("fetchServerResponse", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                tvReviewsTitle.setText("Existing Reviews");
                                tvNoReviews.setVisibility(View.GONE);
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
                                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                                reviewAdapter = new ReviewAdapter(reviews);
                                recyclerView.setAdapter(reviewAdapter);
                                if (reviews.isEmpty()) {
                                    tvReviewsTitle.setText("No Book Reviews Found");
                                } else {
                                    tvReviewsTitle.setText("Existing Reviews");
                                }
                            }
                            else{
                                tvReviewsTitle.setText("No Book Reviews Found");
                                tvNoReviews.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            tvReviewsTitle.setText("No Book Reviews Found");
                            tvNoReviews.setVisibility(View.VISIBLE);
                            //Toast.makeText(requireContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("book_borrow", "Book Borrow Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(requireContext(), "auto_return")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_borrowed_books)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(new Random().nextInt(), notification);
    }
    private void showSuccessDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        // Find the Lottie animation view
        LottieAnimationView lottieSuccess = dialogView.findViewById(R.id.lottieSuccess);
        TextView tvSuccessMessage = dialogView.findViewById(R.id.tvSuccessMessage);
        tvSuccessMessage.setText(msg);
        lottieSuccess.setVisibility(View.VISIBLE);
        lottieSuccess.playAnimation();

        // Show the dialog
        alertDialog.show();

        // Automatically dismiss the dialog after 2 seconds
        new Handler().postDelayed(() -> {
            alertDialog.dismiss();
        }, 2000);
    }
}