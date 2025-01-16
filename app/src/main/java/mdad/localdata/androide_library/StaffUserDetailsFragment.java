package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class StaffUserDetailsFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_CREATED_AT = "createdAt";

    private int userId;
    private String username;
    private String createdAt;

    private ImageButton btnBack;
    private TextView tvUsername, tvCreatedAt;
    private RecyclerView recyclerViewBorrowedBooks, recyclerViewUserReviews;
    private LineChart lineChart;

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
        btnBack = rootView.findViewById(R.id.btnBack);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvCreatedAt = rootView.findViewById(R.id.tvCreatedAt);
        recyclerViewBorrowedBooks = rootView.findViewById(R.id.recyclerViewBorrowedBooks);
        recyclerViewUserReviews = rootView.findViewById(R.id.recyclerViewUserReviews);
        lineChart = rootView.findViewById(R.id.lineChart);

        recyclerViewBorrowedBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUserReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        tvUsername.setText("Username: " + username);
        tvCreatedAt.setText("Account Created: " + createdAt);

        fetchBorrowedBooks();
        fetchUserReviews();
        fetchBorrowingStatistics();

        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

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

    private void fetchBorrowingStatistics() {
        String url = Constants.GET_BORROWING_STATISTICS + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject statsObject = jsonObject.getJSONObject("stats");
                            setupLineChart(statsObject);
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
    private void setupLineChart(JSONObject statsObject) {
        List<Entry> entries = new ArrayList<>();

        try {
            // Extract data from the statsObject
            Iterator<String> keys = statsObject.keys();
            List<String> months = new ArrayList<>();
            List<Integer> borrowCounts = new ArrayList<>();

            while (keys.hasNext()) {
                String month = keys.next();
                int borrowCount = statsObject.getInt(month);

                months.add(month);
                borrowCounts.add(borrowCount);
            }

            // Sort the data by month
            List<Pair<String, Integer>> sortedData = new ArrayList<>();
            for (int i = 0; i < months.size(); i++) {
                sortedData.add(new Pair<>(months.get(i), borrowCounts.get(i)));
            }

            Collections.sort(sortedData, (p1, p2) -> p1.first.compareTo(p2.first));

            for (int i = 0; i < sortedData.size(); i++) {
                String month = sortedData.get(i).first;
                int borrowCount = sortedData.get(i).second;

                // Add data to entries
                entries.add(new Entry(i, borrowCount));
            }

            // Setup X-axis labels
            List<String> xLabels = new ArrayList<>();
            for (Pair<String, Integer> pair : sortedData) {
                xLabels.add(pair.first); // Month names
            }

            LineDataSet dataSet = new LineDataSet(entries, "Books Borrowed");
            dataSet.setColor(getResources().getColor(R.color.dark_primary)); // Line color
            dataSet.setCircleColor(getResources().getColor(R.color.dark_primary)); // Point color
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false); // Disable value text on points

            LineData lineData = new LineData(dataSet);

            // Set up the chart
            lineChart.setData(lineData);
            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            lineChart.getXAxis().setDrawGridLines(false);
            lineChart.getXAxis().setGranularity(1f);
            lineChart.getXAxis().setGranularityEnabled(true);
            //lineChart.getXAxis().setLabelRotationAngle(45); // Rotate labels for readability
            lineChart.getXAxis().setTextSize(10);
            lineChart.setExtraBottomOffset(10f);

            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getAxisRight().setEnabled(false); // Disable right Y-axis
            lineChart.getDescription().setEnabled(false); // Disable description
            lineChart.getLegend().setEnabled(true);
            lineChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

            lineChart.animateX(1000);
            lineChart.invalidate(); // Refresh the chart
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error setting up chart", Toast.LENGTH_SHORT).show();
        }
    }

}
