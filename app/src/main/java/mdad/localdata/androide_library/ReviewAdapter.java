package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false); // Inflate review item layout
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUsername.setText(review.getUsername());
        holder.ratingBar.setRating(review.getRating());
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvCreatedAt.setText(review.getCreatedAt());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        notifyDataSetChanged(); // Notify the adapter to refresh the view
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvReviewText, tvCreatedAt;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
        }
    }

}
