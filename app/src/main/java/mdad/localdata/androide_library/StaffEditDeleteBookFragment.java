package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

//import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StaffEditDeleteBookFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_GENRE = "genre";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_SUMMARY = "summary";


    private int bookId;
    private String title, author, genre, summary;
    private int quantity;

    private EditText etTitle, etAuthor, etGenre, etQuantity, etSummary;
    private Button btnSave, btnCancel, btnDelete, btnBookReviews, btnBookStatistics;
    private ImageView ivBookCover;
    private Uri selectedCoverUri;
    private Uri selectedContentUri;
    private static final String EDIT_BOOK_URL = Constants.EDIT_BOOK_URL;
    private static final String DELETE_BOOK_URL = Constants.DELETE_BOOK_URL;

    public StaffEditDeleteBookFragment() {
        // Required empty constructor
    }

    public static StaffEditDeleteBookFragment newInstance(int bookId, String title, String author, String genre, int quantity, String summary) {
        StaffEditDeleteBookFragment fragment = new StaffEditDeleteBookFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_GENRE, genre);
        args.putInt(ARG_QUANTITY, quantity);
        args.putString(ARG_SUMMARY, summary);

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
            summary = getArguments().getString(ARG_SUMMARY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_edit_delete_book, container, false);

        etTitle = rootView.findViewById(R.id.etTitle);
        etAuthor = rootView.findViewById(R.id.etAuthor);
        etGenre = rootView.findViewById(R.id.etGenre);
        etQuantity = rootView.findViewById(R.id.etQuantity);
        etSummary = rootView.findViewById(R.id.etSummary);
        Button btnSelectCover = rootView.findViewById(R.id.btnSelectCover);
        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        Button btnSelectContent = rootView.findViewById(R.id.btnSelectContent);
        btnSave = rootView.findViewById(R.id.btnSave);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        btnBookReviews = rootView.findViewById(R.id.btnBookReviews);
        btnBookStatistics = rootView.findViewById(R.id.btnBookStatistics);
        btnDelete = rootView.findViewById(R.id.btnDelete);

        // Pre-fill fields with book details
        etTitle.setText(title);
        etAuthor.setText(author);
        etGenre.setText(genre);
        etQuantity.setText(String.valueOf(quantity));
        etSummary.setText(summary);

        btnSelectCover.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        btnSelectContent.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, 2);
        });

        // Save changes
        btnSave.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString();
            String updatedAuthor = etAuthor.getText().toString();
            String updatedGenre = etGenre.getText().toString();
            String updatedSummary = etSummary.getText().toString();
            int updatedQuantity = Integer.parseInt(etQuantity.getText().toString());

            // Call API to update book
            updateBook(bookId, updatedTitle, updatedAuthor, updatedGenre,updatedSummary, updatedQuantity);
        });
        // Cancel changes
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        // Book Reviews
        btnBookReviews.setOnClickListener(v->{
            Fragment staffBookReviewsFragment = StaffBookReviewsFragment.newInstance(
                    bookId
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffBookReviewsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        // Book Statistics
        btnBookReviews.setOnClickListener(v->{
            Fragment staffBookStatisticsFragment = StaffBookStatisticsFragment.newInstance(
                    bookId
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, staffBookStatisticsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        // Delete book
        btnDelete.setOnClickListener(v -> {
            // Call API to delete book
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this Book?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteBook(bookId))
                    .setNegativeButton("No", null)
                    .show();
        });

        return rootView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            if (requestCode == 1) {
                selectedCoverUri = data.getData();
                Toast.makeText(requireContext(), "Cover image selected", Toast.LENGTH_SHORT).show();
                ivBookCover.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(selectedCoverUri)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(ivBookCover);
            } else if (requestCode == 2) {
                selectedContentUri = data.getData();
                Toast.makeText(requireContext(), "Content file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void updateBook(int bookId, String title, String author, String genre, String summary, int quantity) {
        System.out.println("Cover Uploaded: "+ selectedCoverUri);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("book_id", String.valueOf(bookId))
                .addFormDataPart("title", title)
                .addFormDataPart("author", author)
                .addFormDataPart("genre", genre)
                .addFormDataPart("summary", summary)
                .addFormDataPart("quantity", String.valueOf(quantity));

        try {
            if (selectedCoverUri != null) {
                addFileToRequestBody(builder, selectedCoverUri, "cover_file", "image/jpeg");

            }

            if (selectedContentUri != null) {
                addFileToRequestBody(builder, selectedContentUri, "content_file", "application/pdf");
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to process files", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        MultipartBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(EDIT_BOOK_URL)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to update book.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Book updated successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update book.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Unable to open InputStream");
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    private void addFileToRequestBody(MultipartBody.Builder builder, Uri uri, String formFieldName, String mimeType) throws IOException {
        String fileName = getFileName(uri);
        System.out.println("File Name: "+ fileName);
        builder.addFormDataPart(formFieldName, fileName,
                RequestBody.create(readBytesFromUri(uri), MediaType.parse(mimeType)));
    }

    private void deleteBook(int bookId) {
        System.out.println("Book ID: "+ bookId);
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, DELETE_BOOK_URL,
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
