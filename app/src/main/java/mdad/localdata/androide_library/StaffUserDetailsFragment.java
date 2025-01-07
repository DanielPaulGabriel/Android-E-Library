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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StaffUserDetailsFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_CREATED_AT = "createdAt";

    private int userId;
    private String username;
    private String createdAt;

    private TextView tvUsername, tvCreatedAt;
    private RecyclerView recyclerViewBorrowedBooks, recyclerViewUserReviews;
    private BarChart barChart;

    public static StaffUserDetailsFragment newInstance(int userId, String username, String createdAt) {
        StaffUserDetailsFragment fragment = new StaffUserDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_CREATED_AT, createdAt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID);
            username = getArguments().getString(ARG_USERNAME);
            createdAt = getArguments().getString(ARG_CREATED_AT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_user_details, container, false);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvCreatedAt = rootView.findViewById(R.id.tvCreatedAt);
        recyclerViewBorrowedBooks = rootView.findViewById(R.id.recyclerViewBorrowedBooks);
        recyclerViewUserReviews = rootView.findViewById(R.id.recyclerViewUserReviews);
        barChart = rootView.findViewById(R.id.barChart);

        recyclerViewBorrowedBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUserReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        tvUsername.setText("Username: " + username);
        tvCreatedAt.setText("Account Created: " + createdAt);

        fetchBorrowedBooks();
        fetchUserReviews();
        //fetchBorrowingStatistics();

        return rootView;
    }
    private void fetchBorrowedBooks() {
        String url = Constants.GET_USER_BOOKS_URL + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray booksArray = jsonObject.getJSONArray("borrowed_books");
                            List<UserBook> borrowedBooks = new ArrayList<>();
                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject bookObject = booksArray.getJSONObject(i);
                                borrowedBooks.add(new UserBook(
                                        bookObject.getString("title"),
                                        bookObject.getString("due_date")
                                ));
                            }
                            StaffUserBorrowedBooksAdapter staffUserBorrowedBooksAdapter = new StaffUserBorrowedBooksAdapter(borrowedBooks);
                            recyclerViewBorrowedBooks.setAdapter(staffUserBorrowedBooksAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching borrowed books", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void fetchUserReviews() {
        String url = Constants.GET_USER_REVIEWS_URL + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray reviewsArray = jsonObject.getJSONArray("reviews");
                            List<Review> userReviews = new ArrayList<>();
                            for (int i = 0; i < reviewsArray.length(); i++) {
                                JSONObject reviewObject = reviewsArray.getJSONObject(i);
                                userReviews.add(new Review(
                                        reviewObject.getString("title"),
                                        reviewObject.getInt("rating"),
                                        reviewObject.getString("review_text")

                                ));
                            }
                            StaffUserReviewsAdapter staffUserReviewsAdapter = new StaffUserReviewsAdapter(userReviews);
                            recyclerViewUserReviews.setAdapter(staffUserReviewsAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching reviews", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /*private void fetchBorrowingStatistics() {
        String url = Constants.GET_BORROWING_STATISTICS_URL + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject statsObject = jsonObject.getJSONObject("stats");
                            setupBarChart(statsObject);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching statistics", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void setupBarChart(JSONObject statsObject) {
        try {
            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            int index = 0;
            JSONArray monthsArray = statsObject.names();
            for (int i = 0; i < monthsArray.length(); i++) {
                String month = monthsArray.getString(i);
                int count = statsObject.getInt(month);
                entries.add(new BarEntry(index, count));
                labels.add(month);
                index++;
            }

            BarDataSet dataSet = new BarDataSet(entries, "Books Borrowed");
            dataSet.setColor(Color.BLUE);

            BarData barData = new BarData(dataSet);
            barChart.setData(barData);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            barChart.invalidate();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to setup chart", Toast.LENGTH_SHORT).show();
        }
    }*/



}
