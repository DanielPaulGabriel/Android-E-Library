package mdad.localdata.androide_library;

import android.content.Context;
import android.content.Intent;
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

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private Context context;
    private List<Book> bookList;
    private OnBookActionListener listener;

    private static final String BASE_URL = Constants.BASE_URL;
    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }
    // Define the interface for click actions
    public interface OnBookActionListener {
        void onBookClick(Book book);
    }

    // Setter for the listener
    public void setOnBookActionListener(OnBookActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set title and author
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());

        // Load the book cover using Glide
        Glide.with(context)
                .load(BASE_URL+book.getCoverPath()+"?t="+System.currentTimeMillis()) // URL or file path of the book cover
                .placeholder(R.drawable.ic_placeholder) // Placeholder image
                .error(R.drawable.ic_error) // Error image
                .into(holder.ivBookCover);

        // Handle item click
       /* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment bookDetailsFragment = BookDetailsFragment.newInstance(
                        book.getBookId(),
                        BASE_URL + book.getCoverPath(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getSummary()
                );

                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, bookDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }*/
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        }
        );

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookAuthor;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
        }
    }

}
