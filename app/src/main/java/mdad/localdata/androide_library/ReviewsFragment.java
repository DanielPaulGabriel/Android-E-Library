package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private static final String GET_USER_REVIEWS_URL = Constants.GET_USER_REVIEWS_URL;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewUserReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                                            reviewJson.getString("created_at")
                                    ));
                                }
                                UserReviewAdapter adapter = new UserReviewAdapter(reviewList, new UserReviewAdapter.OnReviewActionListener() {
                                    @Override
                                    public void onEditReview(Review review) {
                                        // Navigate to Edit Review Screen or Show Popup
                                    }

                                    @Override
                                    public void onDeleteReview(Review review) {
                                        // Handle Review Deletion
                                    }
                                });
                                recyclerView.setAdapter(adapter);
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
}
