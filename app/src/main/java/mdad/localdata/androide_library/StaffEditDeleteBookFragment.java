package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StaffEditDeleteBookFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_GENRE = "genre";
    private static final String ARG_QUANTITY = "quantity";

    private int bookId;
    private String title, author, genre;
    private int quantity;

    private EditText etTitle, etAuthor, etGenre, etQuantity;
    private Button btnSave, btnCancel, btnDelete;
    private static final String DELETE_BOOK_URL = Constants.DELETE_BOOK_URL;

    public StaffEditDeleteBookFragment() {
        // Required empty constructor
    }

    public static StaffEditDeleteBookFragment newInstance(int bookId, String title, String author, String genre, int quantity) {
        StaffEditDeleteBookFragment fragment = new StaffEditDeleteBookFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_GENRE, genre);
        args.putInt(ARG_QUANTITY, quantity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
            title = getArguments().getString(ARG_TITLE);
            author = getArguments().getString(ARG_AUTHOR);
            genre = getArguments().getString(ARG_GENRE);
            quantity = getArguments().getInt(ARG_QUANTITY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_edit_delete_book, container, false);

        etTitle = rootView.findViewById(R.id.etTitle);
        etAuthor = rootView.findViewById(R.id.etAuthor);
        etGenre = rootView.findViewById(R.id.etGenre);
        etQuantity = rootView.findViewById(R.id.etQuantity);
        btnSave = rootView.findViewById(R.id.btnSave);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        btnDelete = rootView.findViewById(R.id.btnDelete);

        // Pre-fill fields with book details
        etTitle.setText(title);
        etAuthor.setText(author);
        etGenre.setText(genre);
        etQuantity.setText(String.valueOf(quantity));

        // Save changes
        btnSave.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString();
            String updatedAuthor = etAuthor.getText().toString();
            String updatedGenre = etGenre.getText().toString();
            int updatedQuantity = Integer.parseInt(etQuantity.getText().toString());

            // Call API to update book
            updateBook(bookId, updatedTitle, updatedAuthor, updatedGenre, updatedQuantity);
        });
        // Cancel changes
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        // Delete book
        btnDelete.setOnClickListener(v -> {
            // Call API to delete book
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteBook(bookId))
                    .setNegativeButton("No", null)
                    .show();
        });

        return rootView;
    }

    private void updateBook(int bookId, String title, String author, String genre, int quantity) {
        // Implement API call to update book details
        Toast.makeText(getContext(), "Book updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void deleteBook(int bookId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_BOOK_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Book deleted!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("book_id", String.valueOf(bookId));
                return params;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }
}
