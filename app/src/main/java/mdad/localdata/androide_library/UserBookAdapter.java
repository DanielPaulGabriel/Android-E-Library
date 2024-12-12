package mdad.localdata.androide_library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Objects;

public class UserBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String BASE_URL = Constants.BASE_URL;
    private List<ListItem> items; // Updated to hold both books and headers

    public UserBookAdapter(List<ListItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_genre_header, parent, false);
            return new GenreHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_book, parent, false);
            return new UserBookViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenreHeaderViewHolder) {
            GenreHeader genreHeader = (GenreHeader) items.get(position);
            ((GenreHeaderViewHolder) holder).tvGenre.setText(genreHeader.getGenre());
        } else if (holder instanceof UserBookViewHolder) {
            UserBook book = (UserBook) items.get(position);
            UserBookViewHolder bookHolder = (UserBookViewHolder) holder;

            bookHolder.tvTitle.setText(book.getTitle());
            bookHolder.tvAuthor.setText(book.getAuthor());
            bookHolder.tvBorrowDate.setText("Borrowed: " + book.getBorrow_date());
            bookHolder.tvDueDate.setText("Due: " + book.getDue_date());

            /*if (Objects.equals(book.getReturn_date(), "null")) {
                bookHolder.tvReturnDate.setText("Not Returned");
            } else {
                bookHolder.tvReturnDate.setText("Returned: " + book.getReturn_date());
            }*/

            Glide.with(bookHolder.itemView.getContext())
                    .load(Constants.BASE_URL + book.getCoverPath())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(bookHolder.ivCover);
            // Make the item clickable
            bookHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Borrowed Book Details Fragment
                    Fragment borrowedBookDetailsFragment = BorrowedBookDetailsFragment.newInstance(
                            book.getBookId(),
                            book.getBorrowId(),
                            Constants.BASE_URL + book.getCoverPath(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getSummary()
                    );

                    // Use the FragmentManager to replace the current fragment
                    ((AppCompatActivity) bookHolder.itemView.getContext())
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, borrowedBookDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class GenreHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenre;

        public GenreHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenre = itemView.findViewById(R.id.tvGenre);
        }
    }

    public static class UserBookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvBorrowDate, tvDueDate, tvReturnDate;
        ImageView ivCover;

        public UserBookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            //tvReturnDate = itemView.findViewById(R.id.tvReturnDate);
            ivCover = itemView.findViewById(R.id.ivBookCover);
        }
    }
}
