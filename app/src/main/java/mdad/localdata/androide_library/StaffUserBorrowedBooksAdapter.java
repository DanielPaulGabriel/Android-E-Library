package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffUserBorrowedBooksAdapter extends RecyclerView.Adapter<StaffUserBorrowedBooksAdapter.ViewHolder> {
    private List<UserBook> borrowedBooks;

    public StaffUserBorrowedBooksAdapter(List<UserBook> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_user_borrowed_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserBook book = borrowedBooks.get(position);
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvDueDate.setText("Due: " + book.getDue_date());
    }

    @Override
    public int getItemCount() {
        return borrowedBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvDueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
