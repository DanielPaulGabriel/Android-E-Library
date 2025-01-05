package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffBookReviewsAdapter extends RecyclerView.Adapter<StaffBookReviewsAdapter.ViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private OnReviewDeleteListener deleteListener;

    public interface OnReviewDeleteListener {
        void onDelete(int reviewId);
    }

    public StaffBookReviewsAdapter(Context context, List<Review> reviewList, OnReviewDeleteListener deleteListener) {
        this.context = context;
        this.reviewList = reviewList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvUsername.setText(review.getUsername());
        holder.tvComment.setText(review.getReviewText());
        holder.tvReviewDate.setText(review.getCreatedAt());
        holder.ratingBar.setRating((float) review.getRating());

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Call the delete listener to handle the deletion
                        deleteListener.onDelete(review.getReviewId());
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvReviewDate;
        RatingBar ratingBar;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
