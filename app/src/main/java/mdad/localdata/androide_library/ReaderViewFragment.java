package mdad.localdata.androide_library;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.barteksc.pdfviewer.PDFView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReaderViewFragment extends Fragment {
    private View rootView;
    private PDFView pdfViewer;
    private static final String ARG_TITLE = "bookTitle";
    private static final String ARG_CONTENT = "contentUrl";
    private static final String ARG_BOOK_ID = "bookId";
    private static final String GET_BOOK_TEXT_URL = Constants.GET_BOOK_TEXT_URL;

    private String bookTitle;
    private String bookContent;
    private TextView tvContent, tvTitle;
    private Button btnPrevious, btnNext;
    private ImageButton btnBack;
    private EditText etPageNumber;
    private int currentPage = 1;
    private int totalPages = 0;
    private int bookId;

    public ReaderViewFragment() {
        // Required empty public constructor
    }

    public static ReaderViewFragment newInstance(String bookTitle, String bookContent, int bookId) {
        ReaderViewFragment fragment = new ReaderViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, bookTitle);
        args.putString(ARG_CONTENT, bookContent);
        args.putInt(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookTitle = getArguments().getString(ARG_TITLE);
            bookContent = getArguments().getString(ARG_CONTENT);
            bookId = getArguments().getInt(ARG_BOOK_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reader_view, container, false);
        tvContent = rootView.findViewById(R.id.tvContent);
        tvTitle = rootView.findViewById(R.id.tvTitle);
        btnPrevious = rootView.findViewById(R.id.btnPrevious);
        btnNext = rootView.findViewById(R.id.btnNext);
        btnBack = rootView.findViewById(R.id.btnBack);
        etPageNumber = rootView.findViewById(R.id.etPageNumber);
        tvContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        tvTitle.setText(bookTitle);
        pdfViewer = rootView.findViewById(R.id.pdfViewer);
        System.out.println("Book Path: "+ bookContent);
        if (bookContent.endsWith(".pdf")) {
            //loadPdf(bookContent);
            pdfViewer.setVisibility(View.GONE);
            loadPage(currentPage);

        }else{
            pdfViewer.setVisibility(View.GONE);
            loadPage(currentPage);
        }

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadPage(currentPage);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadPage(currentPage);
            }
        });
        etPageNumber.setOnEditorActionListener((v, actionId, event) -> {
            int enteredPage = parsePageNumber(etPageNumber.getText().toString());
            if (isValidPage(enteredPage)) {
                currentPage = enteredPage;
                loadPage(currentPage);
            } else {
                Toast.makeText(requireContext(), "Invalid page number", Toast.LENGTH_SHORT).show();
                etPageNumber.setText(String.valueOf(currentPage)); // Reset to the current page
            }
            return true;
        });
        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

        return rootView;
    }

    private void loadPage(int pageNumber) {
        String url = GET_BOOK_TEXT_URL + "?book_id=" + bookId + "&page_number=" + pageNumber;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            tvContent.setText(jsonObject.getString("content"));
                            totalPages = jsonObject.getInt("total_pages");
                            etPageNumber.setText(String.valueOf(pageNumber));
                            tvContent.scrollTo(0, 0);
                            etPageNumber.clearFocus();
                            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etPageNumber.getWindowToken(), 0);
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error loading page.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadPdf(String pdfUrl) {
        new Thread(() -> {
            try {
                // Download the PDF file
                File pdfFile = downloadFile(pdfUrl, "book.pdf");
                if (pdfFile != null) {
                    requireActivity().runOnUiThread(() -> pdfViewer.fromFile(pdfFile).load());
                    tvContent.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private File downloadFile(String fileUrl, String fileName) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        File file = new File(requireContext().getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();

        return file;
    }
    private boolean isValidPage(int pageNumber) {
        return pageNumber > 0 && pageNumber <= totalPages;
    }

    private int parsePageNumber(String pageString) {
        try {
            return Integer.parseInt(pageString);
        } catch (NumberFormatException e) {
            return -1; // Invalid page
        }
    }

}
