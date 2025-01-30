package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StaffAddBookFragment extends Fragment {

    private EditText etTitle, etAuthor, etGenre, etQuantity, etSummary;
    private Button btnSubmit, btnCancel;
    private ImageView ivBookCover;
    private Uri selectedCoverUri;
    private Uri selectedContentUri;
    private static final String CREATE_BOOK_URL = Constants.CREATE_BOOK_URL;

    public StaffAddBookFragment() {
        // Required empty public constructor
    }

    public static StaffAddBookFragment newInstance() {

        return new StaffAddBookFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_add_book, container, false);
        etTitle = rootView.findViewById(R.id.etTitle);
        etAuthor = rootView.findViewById(R.id.etAuthor);
        etGenre = rootView.findViewById(R.id.etGenre);
        etQuantity = rootView.findViewById(R.id.etQuantity);
        etSummary = rootView.findViewById(R.id.etSummary);
        Button btnSelectCover = rootView.findViewById(R.id.btnSelectCover);
        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        Button btnSelectContent = rootView.findViewById(R.id.btnSelectContent);
        btnSubmit = rootView.findViewById(R.id.btnSubmit);
        btnCancel = rootView.findViewById(R.id.btnCancel);

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
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String author = etAuthor.getText().toString();
            String genre = etGenre.getText().toString();
            String summary = etSummary.getText().toString();
            int quantity = Integer.parseInt(etQuantity.getText().toString());

            // Call API to update book
            createBook(title, author, genre, summary, quantity);
        });
        // Cancel changes
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Inflate the layout for this fragment
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

    private void createBook(String title, String author, String genre, String summary, int quantity) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
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
                .url(CREATE_BOOK_URL)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to create book.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showSuccessDialog("Book created successfully!");
                        //Toast.makeText(requireContext(), "Book created successfully!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(requireContext(), "Failed to create book.", Toast.LENGTH_SHORT).show();
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

    private void showSuccessDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        // Find the Lottie animation view
        LottieAnimationView lottieSuccess = dialogView.findViewById(R.id.lottieSuccess);
        TextView tvSuccessMessage = dialogView.findViewById(R.id.tvSuccessMessage);
        tvSuccessMessage.setText(msg);
        lottieSuccess.setVisibility(View.VISIBLE);
        lottieSuccess.playAnimation();

        // Show the dialog
        alertDialog.show();

        // Automatically dismiss the dialog after 2 seconds
        new Handler().postDelayed(() -> {
            alertDialog.dismiss();
        }, 2000);
    }
}