package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder> {

    private List<Review> reviews;
    private OnReviewActionListener listener;

    public UserReviewAdapter(List<Review> reviews, OnReviewActionListener listener) {
        this.reviews = reviews;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_review, parent, false);
        return new UserReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvBookTitle.setText(review.getTitle());
        holder.tvAuthorName.setText(review.getAuthor());
        holder.tvCreatedAt.setText("Created At: " + review.getCreatedAt());
        holder.ratingBar.setRating(review.getRating());
        holder.tvReviewText.setText(review.getReviewText());
        Glide.with(holder.itemView.getContext())
                .load(Constants.BASE_URL + review.getCoverPath()+"?t="+System.currentTimeMillis())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.ivBookCover);

        // Edit Button
        holder.btnEdit.setOnClickListener(v -> listener.onEditReview(review));

        // Delete Button
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteReview(review));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class UserReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle,tvAuthorName, tvReviewText, tvCreatedAt;
        RatingBar ratingBar;
        Button btnEdit, btnDelete;
        ImageView ivBookCover;

        public UserReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(Review review);
    }
}
