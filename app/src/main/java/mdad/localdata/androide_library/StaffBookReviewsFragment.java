package mdad.localdata.androide_library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffBookReviewsFragment extends Fragment {

    private static final String ARG_BOOK_ID = "bookId";
    private int bookId;
    private RecyclerView recyclerView;
    private StaffBookReviewsAdapter adapter;
    private TextView tvNoReviews;
    private ImageButton btnBack;

    public StaffBookReviewsFragment() {
        // Required empty public constructor
    }

    public static StaffBookReviewsFragment newInstance(int bookId) {
        StaffBookReviewsFragment fragment = new StaffBookReviewsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_book_reviews, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewBookReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvNoReviews = rootView.findViewById(R.id.tvNoReviews);
        btnBack = rootView.findViewById(R.id.btnBack);

        loadReviews();

        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

        return rootView;
    }

    private void loadReviews() {
        String url = Constants.GET_BOOK_REVIEWS_URL + "?book_id=" + bookId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray reviewsArray = jsonObject.getJSONArray("reviews");
                            List<Review> reviewList = new ArrayList<>();

                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject review = reviewsArray.getJSONObject(i);
                                reviewList.add(new Review(
                                        review.getInt("review_id"),
                                        review.getString("username"),
                                        review.getInt("rating"),
                                        review.getString("review_text"),
                                        review.getString("created_at")
                                ));
                            }

                            if (reviewList.isEmpty()) {
                                tvNoReviews.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoReviews.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter = new StaffBookReviewsAdapter(getContext(), reviewList, this::deleteReview);
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    private void deleteReview(int reviewId) {
        String url = Constants.DELETE_REVIEW_URL;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getContext(), "Review deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Refresh the reviews
                },
                error -> Toast.makeText(getContext(), "Error deleting review.", Toast.LENGTH_SHORT).show()) {
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