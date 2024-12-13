package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private TextView tvNoReviews;
    private List<Review> reviewList = new ArrayList<>();
    private static final String GET_USER_REVIEWS_URL = Constants.GET_USER_REVIEWS_URL;
    private static final String DELETE_REVIEW_URL = Constants.DELETE_REVIEW_URL;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewUserReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvNoReviews = rootView.findViewById(R.id.tvNoReviews);


        int userId = SharedPrefsManager.getUserId(requireContext());
        if (userId != -1) {
            fetchReviews(userId);
        }

        return rootView;
    }

    private void fetchReviews(int userId) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USER_REVIEWS_URL + "?user_id=" + userId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                reviewList.clear();
                                JSONArray reviewsArray = jsonObject.getJSONArray("reviews");
                                for (int i = 0; i < reviewsArray.length(); i++) {
                                    JSONObject reviewJson = reviewsArray.getJSONObject(i);
                                    reviewList.add(new Review(
                                            reviewJson.getInt("review_id"),
                                            reviewJson.getInt("book_id"),
                                            reviewJson.getInt("rating"),
                                            reviewJson.getString("review_text"),
                                            reviewJson.getString("title"),
                                            reviewJson.getString("author"),
                                            reviewJson.getString("cover_path"),
                                            reviewJson.getString("summary"),
                                            reviewJson.getString("created_at")
                                    ));
                                }
                                UserReviewAdapter adapter = new UserReviewAdapter(reviewList, new UserReviewAdapter.OnReviewActionListener() {
                                    @Override
                                    public void onEditReview(Review review) {
                                        Fragment editReviewFragment = EditReviewFragment.newInstance(
                                                review.getReviewId(),
                                                review.getTitle(),
                                                Constants.BASE_URL + review.getCoverPath(),
                                                review.getSummary(),
                                                review.getAuthor(),
                                                review.getRating(),
                                                review.getReviewText()
                                        );
                                        ((AppCompatActivity) getContext())
                                                .getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_container, editReviewFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    }

                                    @Override
                                    public void onDeleteReview(Review review) {
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle("Delete Review")
                                                .setMessage("Are you sure you want to delete this review?")
                                                .setPositiveButton("Yes", (dialog, which) -> deleteReview(review))
                                                .setNegativeButton("No", null)
                                                .show();
                                    }
                                });
                                recyclerView.setAdapter(adapter);
                            }
                            else{
                                tvNoReviews.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }
    private void deleteReview(Review review) {
        int reviewId = review.getReviewId();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_REVIEW_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Review deleted!", Toast.LENGTH_SHORT).show();

                            // Remove the review from the list and refresh the adapter
                            reviewList.remove(review);
                            recyclerView.getAdapter().notifyDataSetChanged();

                            // Show "No Reviews" message if the list is empty
                            if (reviewList.isEmpty()) {
                                tvNoReviews.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review_id", String.valueOf(reviewId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

}
