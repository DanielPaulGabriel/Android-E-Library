package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class EditReviewFragment extends Fragment {

    private static final String ARG_REVIEW_ID = "review_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_COVER = "cover";
    private static final String ARG_SUMMARY = "summary";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_RATING = "rating";
    private static final String ARG_REVIEW_TEXT = "review_text";

    private int reviewId;
    private String title;
    private String cover;
    private String summary;
    private String author;
    private float rating;
    private String reviewText;

    private TextView tvBookTitle,tvAuthorName;
    private ImageView ivBookCover;

    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnSave, btnCancel;

    private static final String UPDATE_REVIEW_URL = Constants.UPDATE_REVIEW_URL;

    public static EditReviewFragment newInstance(int reviewId, String title, String cover, String summary, String author, float rating, String reviewText) {
        EditReviewFragment fragment = new EditReviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REVIEW_ID, reviewId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_COVER, cover);
        args.putString(ARG_SUMMARY, summary);
        args.putString(ARG_AUTHOR, author);
        args.putFloat(ARG_RATING, rating);
        args.putString(ARG_REVIEW_TEXT, reviewText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reviewId = getArguments().getInt(ARG_REVIEW_ID);
            title = getArguments().getString(ARG_TITLE);
            cover = getArguments().getString(ARG_COVER);
            summary = getArguments().getString(ARG_SUMMARY);
            author = getArguments().getString(ARG_AUTHOR);
            rating = getArguments().getFloat(ARG_RATING);
            reviewText = getArguments().getString(ARG_REVIEW_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_review, container, false);

        etReviewText = rootView.findViewById(R.id.etReviewText);
        ratingBar = rootView.findViewById(R.id.ratingBar);
        btnSave = rootView.findViewById(R.id.btnSave);
        btnCancel = rootView.findViewById(R.id.btnCancel);

        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        tvBookTitle = rootView.findViewById(R.id.tvBookTitle);
        tvAuthorName = rootView.findViewById(R.id.tvAuthorName);

        // Set existing review details
        etReviewText.setText(reviewText);
        ratingBar.setRating(rating);
        tvBookTitle.setText(title);
        tvAuthorName.setText(author);
        Glide.with(requireContext())
                .load(cover)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(ivBookCover);

        btnSave.setOnClickListener(v -> {
            String updatedReviewText = etReviewText.getText().toString();
            float updatedRating = ratingBar.getRating();

            if (TextUtils.isEmpty(updatedReviewText)) {
                Toast.makeText(getContext(), "Review text cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            updateReview(reviewId, updatedReviewText, updatedRating);
        });

        btnCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack(); // Return to previous fragment
        });


        return rootView;
    }

    private void updateReview(int reviewId, String updatedReviewText, float updatedRating) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_REVIEW_URL,
                response -> {
                    Toast.makeText(getContext(), "Review updated successfully", Toast.LENGTH_SHORT).show();
                    // Go back to the previous fragment or refresh the reviews
                    requireActivity().getSupportFragmentManager().popBackStack();
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("review_id", String.valueOf(reviewId));
                params.put("review_text", updatedReviewText);
                params.put("rating", String.valueOf(updatedRating));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }
}