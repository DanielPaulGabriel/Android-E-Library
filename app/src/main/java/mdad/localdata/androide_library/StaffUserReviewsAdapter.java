package mdad.localdata.androide_library;

import android.media.Rating;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffUserReviewsAdapter extends RecyclerView.Adapter<StaffUserReviewsAdapter.ViewHolder> {
    private List<Review> userReviews;

    public StaffUserReviewsAdapter(List<Review> userReviews) {
        this.userReviews = userReviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_user_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = userReviews.get(position);
        holder.tvTitle.setText(review.getTitle());
        holder.ratingBarReview.setRating(review.getRating());
        holder.tvReviewText.setText(review.getReviewText());
    }

    @Override
    public int getItemCount() {
        return userReviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvReviewText;
        RatingBar ratingBarReview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
        }
    }
}

