package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private SearchView searchView;
    private Button btnRedirect, btnRetry;
    private List<Review> reviewList = new ArrayList<>();
    private List<Review> filteredReviewList = new ArrayList<>();
    private static final String GET_USER_REVIEWS_URL = Constants.GET_USER_REVIEWS_URL;
    private static final String DELETE_REVIEW_URL = Constants.DELETE_REVIEW_URL;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewUserReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvNoReviews = rootView.findViewById(R.id.tvNoReviews);
        btnRedirect = rootView.findViewById(R.id.btnRedirect);
        btnRetry = rootView.findViewById(R.id.btnRetry);
        searchView = rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private Runnable workRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterReviews(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }
                workRunnable = () -> filterReviews(newText);
                handler.postDelayed(workRunnable, 300); // Debounce input
                return true;
            }
        });

        btnRedirect.setOnClickListener(v->{
            Fragment bookCatalogueFragment = BookCatalogueFragment.newInstance();

            // Use the FragmentManager to replace the current fragment
            ((AppCompatActivity) requireContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, bookCatalogueFragment)
                    .addToBackStack(null)
                    .commit();
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_catalog);
        });

        int userId = SharedPrefsManager.getUserId(requireContext());
        if (userId != -1) {
            fetchReviews(userId);
        }
        btnRetry.setOnClickListener(v->fetchReviews(userId));
        return rootView;
    }

    private void fetchReviews(int userId) {
        if (!isNetworkAvailable()) {
            handleNoData("No internet connection. Please check your connection.");
            return;
        }
        tvNoReviews.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

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
                                filteredReviewList.addAll(reviewList);
                                if(!filteredReviewList.isEmpty()){
                                    searchView.setVisibility(View.VISIBLE);
                                }
                                UserReviewAdapter adapter = new UserReviewAdapter(filteredReviewList, new UserReviewAdapter.OnReviewActionListener() {
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
                            } else {
                                tvNoReviews.setVisibility(View.VISIBLE);
                                btnRedirect.setVisibility(View.VISIBLE);
                                searchView.setVisibility(View.GONE);
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
                            filteredReviewList.remove(review);
                            recyclerView.getAdapter().notifyDataSetChanged();

                            // Show "No Reviews" message if the list is empty
                            if (reviewList.isEmpty()) {
                                tvNoReviews.setVisibility(View.VISIBLE);
                                btnRedirect.setVisibility(View.VISIBLE);
                                searchView.setVisibility(View.GONE);
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


    private void filterReviews(String query) {
        filteredReviewList.clear();
        if (query.isEmpty()) {
            filteredReviewList.addAll(reviewList);
        } else {
            for (Review review : reviewList) {
                if (review.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        review.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                    filteredReviewList.add(review);
                }
            }
        }

        if (filteredReviewList.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            btnRedirect.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
            btnRedirect.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);;
            recyclerView.setVisibility(View.VISIBLE);
            if (reviewAdapter != null) {
                reviewAdapter.notifyDataSetChanged();
            }
        }

        //reviewAdapter.updateReviews(filteredReviewList);
    }

    private void handleNoData(String message) {
        if (!isAdded()) return; // Ensure fragment is attached
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), "An unknown error occurred.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        tvNoReviews.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        btnRedirect.setVisibility(View.GONE);
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
