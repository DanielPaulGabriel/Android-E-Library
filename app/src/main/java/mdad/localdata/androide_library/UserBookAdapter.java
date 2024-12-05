package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserBookAdapter extends RecyclerView.Adapter<UserBookAdapter.UserBooksViewHolder> {

    private List<UserBook> userBooks;

    public UserBookAdapter(List<UserBook> userBooks) {
        this.userBooks = userBooks;
    }

    @NonNull
    @Override
    public UserBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_book, parent, false);
        return new UserBooksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserBooksViewHolder holder, int position) {
        UserBook book = userBooks.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvBorrowDate.setText("Borrowed: " + book.getBorrow_date());
        holder.tvDueDate.setText("Due: " + book.getDue_date());

        if (book.getReturn_date() == null) {
            holder.tvReturnDate.setText("Not Returned");
        } else {
            holder.tvReturnDate.setText("Returned: " + book.getReturn_date());
        }

        // Load book cover image
        Glide.with(holder.itemView.getContext())
                .load(book.getCoverPath())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.ivCover);
    }

    @Override
    public int getItemCount() {
        return userBooks.size();
    }

    public static class UserBooksViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvBorrowDate, tvDueDate, tvReturnDate;
        ImageView ivCover;

        public UserBooksViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvReturnDate = itemView.findViewById(R.id.tvReturnDate);
            ivCover = itemView.findViewById(R.id.ivBookCover);
        }
    }
}
