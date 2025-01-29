package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserBookDetailsFragment extends Fragment {
    private static final String RETURN_BOOK_URL = Constants.RETURN_BOOK_URL;
    private static final String ARG_BOOK_ID = "bookId";
    private static final String ARG_BORROW_ID = "borrowId";
    private static final String ARG_COVER_URL = "coverUrl";
    private static final String ARG_CONTENT_URL = "contentUrl";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_SUMMARY = "summary";

    private int bookId, borrowId;
    private String coverUrl, contentUrl, title, author, summary;
    private ImageView ivBookCover;
    private TextView tvTitle, tvAuthor, tvSummary;
    private ImageButton btnBack;
    private Button btnRead, btnListen, btnReturn, btnShare;

    public static UserBookDetailsFragment newInstance(int bookId, int borrowId, String coverUrl, String contentUrl, String title, String author, String summary) {
        UserBookDetailsFragment fragment = new UserBookDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putInt(ARG_BORROW_ID, borrowId);
        args.putString(ARG_COVER_URL, coverUrl);
        args.putString(ARG_CONTENT_URL, contentUrl);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_SUMMARY, summary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
            borrowId = getArguments().getInt(ARG_BORROW_ID);
            coverUrl = getArguments().getString(ARG_COVER_URL);
            contentUrl = getArguments().getString(ARG_CONTENT_URL);
            title = getArguments().getString(ARG_TITLE);
            author = getArguments().getString(ARG_AUTHOR);
            summary = getArguments().getString(ARG_SUMMARY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_borrowed_book_details, container, false);

        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        tvTitle = rootView.findViewById(R.id.tvBookTitle);
        tvAuthor = rootView.findViewById(R.id.tvBookAuthor);
        tvSummary = rootView.findViewById(R.id.tvBookSummary);
        btnBack = rootView.findViewById(R.id.btnBack);
        btnRead = rootView.findViewById(R.id.btnReadBook);
        btnListen = rootView.findViewById(R.id.btnListenBook);
        btnReturn = rootView.findViewById(R.id.btnReturnBook);
        btnShare = rootView.findViewById(R.id.btnShareBook);
        System.out.println("Book Path: "+ contentUrl);

        // Populate data
        Glide.with(this).load(coverUrl+"?t="+System.currentTimeMillis()).into(ivBookCover);
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvSummary.setText(summary);

        // Implement button actions (e.g., read, listen, or return the book)
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnReturn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Return Book")
                    .setMessage("Are you sure you want to return this book?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        returnBook(borrowId);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();

        });
        btnRead.setOnClickListener(v ->{
            Fragment readerViewFragment = ReaderViewFragment.newInstance(
                            title,
                            contentUrl,
                            bookId
                    );
            // Use the FragmentManager to replace the current fragment
            ((AppCompatActivity) requireContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, readerViewFragment)
                    .addToBackStack(null)
                    .commit();
        });
        btnListen.setOnClickListener(v ->{
            Fragment listenerViewFragment = ListenerViewFragment.newInstance(
                    bookId,
                    coverUrl,
                    title
            );
            // Use the FragmentManager to replace the current fragment
            ((AppCompatActivity) requireContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, listenerViewFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBook(title, author , getResources().getString(R.string.app_name));
            }
        });

        return rootView;
    }
    private void returnBook(int borrow){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,RETURN_BOOK_URL ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("borrow_id", String.valueOf(borrowId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);

    }
    private void shareBook(String bookTitle, String author, String appName) {
        String message = "Hi, I'm currently reading '" + bookTitle + "' by '" + author + "' on " + appName + "! 📖📚";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

}
