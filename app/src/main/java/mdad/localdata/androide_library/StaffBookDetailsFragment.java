package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class StaffBookDetailsFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_GENRE = "genre";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_SUMMARY = "summary";
    private static final String ARG_COVER_URL= "coverUrl";

    private int bookId, quantity;
    private String title, author, genre, summary, coverUrl;
    private static final String DELETE_BOOK_URL = Constants.DELETE_BOOK_URL;

    private TextView tvTitle, tvAuthor, tvGenre, tvQuantity, tvSummary;
    private Button btnEdit, btnDelete, btnBookReviews, btnBookStatistics;
    private ImageButton btnBack;
    private ImageView ivBookCover;

    public StaffBookDetailsFragment() {
        // Required empty public constructor
    }


    public static StaffBookDetailsFragment newInstance(int bookId, String title, String author, String genre, int quantity, String summary, String coverUrl) {
        StaffBookDetailsFragment fragment = new StaffBookDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_GENRE, genre);
        args.putInt(ARG_QUANTITY, quantity);
        args.putString(ARG_SUMMARY, summary);
        args.putString(ARG_COVER_URL, coverUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
            title = getArguments().getString(ARG_TITLE);
            author = getArguments().getString(ARG_AUTHOR);
            genre = getArguments().getString(ARG_GENRE);
            quantity = getArguments().getInt(ARG_QUANTITY);
            summary = getArguments().getString(ARG_SUMMARY);
            coverUrl = getArguments().getString(ARG_COVER_URL);

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_book_details, container, false);
        tvTitle = rootView.findViewById(R.id.tvTitle);
        tvAuthor = rootView.findViewById(R.id.tvAuthor);
        tvGenre = rootView.findViewById(R.id.tvGenre);
        tvQuantity = rootView.findViewById(R.id.tvQuantity);
        tvSummary = rootView.findViewById(R.id.tvSummary);
        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        btnBack = rootView.findViewById(R.id.btnBack);
        btnEdit = rootView.findViewById(R.id.btnEdit);
        btnDelete = rootView.findViewById(R.id.btnDelete);
        btnBookReviews = rootView.findViewById(R.id.btnBookReviews);
        btnBookStatistics = rootView.findViewById(R.id.btnBookStatistics);

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvGenre.setText(genre);
        tvSummary.setText(summary);
        tvQuantity.setText(String.valueOf(quantity));
        Glide.with(this).load(Constants.BASE_URL+coverUrl+"?t="+System.currentTimeMillis()).into(ivBookCover);

        btnEdit.setOnClickListener(v->{
            Fragment editDeleteBookFragment = StaffEditDeleteBookFragment.newInstance(
                    bookId,
                    title,
                    author,
                    genre,
                    quantity,
                    summary
            );

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editDeleteBookFragment)
                    .addToBackStack(null)
                    .commit();
        });
        btnDelete.setOnClickListener(v -> {
            // Call API to delete book
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this Book?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteBook(bookId))
                    .setNegativeButton("No", null)
                    .show();
        });
        // Book Reviews
        btnBookReviews.setOnClickListener(v->{
            Fragment staffBookReviewsFragment = StaffBookReviewsFragment.newInstance(
                    bookId
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffBookReviewsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        // Book Statistics
        btnBookStatistics.setOnClickListener(v->{
            Fragment staffBookStatisticsFragment = StaffBookStatisticsFragment.newInstance(
                    bookId
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffBookStatisticsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

        return rootView;
    }

    private void deleteBook(int bookId) {
        System.out.println("Book ID: "+ bookId);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, DELETE_BOOK_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Book deleted!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
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
                params.put("book_id", String.valueOf(bookId));
                return params;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }
}