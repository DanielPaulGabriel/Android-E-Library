package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
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
    private static final String ARG_TITLE = "bookTitle";
    private static final String ARG_CONTENT = "contentUrl";
    private static final String ARG_BOOK_ID = "bookId";
    private static final String GET_BOOK_TEXT_URL = Constants.GET_BOOK_TEXT_URL;

    private String bookTitle;
    private String bookContent;
    private TextView tvContent, tvTitle;
    private Button btnPrevious, btnNext, btnFirst, btnLast;
    private ImageButton btnBack, btnSettings;
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
        btnFirst = rootView.findViewById(R.id.btnFirst);
        btnLast = rootView.findViewById(R.id.btnLast);
        etPageNumber = rootView.findViewById(R.id.etPageNumber);
        tvContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        tvTitle.setText(bookTitle);

        btnSettings = rootView.findViewById(R.id.btnSettings);

        btnSettings.setOnClickListener(v -> openTextSettingsDialog());

        // Apply stored settings
        SharedPreferences prefs = requireContext().getSharedPreferences("ReaderSettings", Context.MODE_PRIVATE);
        float fontSize = prefs.getFloat("font_size", 16f);
        float lineSpacing = prefs.getFloat("line_spacing", 1.5f);
        String fontType = prefs.getString("font_type", "sans-serif");
        String bgColor = prefs.getString("bg_color", "#FFFFFF");

        applyReaderSettings(fontSize, lineSpacing, fontType, bgColor);

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
        btnFirst.setOnClickListener(v->{
            loadPage(1);
        });
        btnLast.setOnClickListener(v->{
            loadPage(totalPages);
        });

        loadPage(currentPage);

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

    private void openTextSettingsDialog() { // Options menu config
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_text_settings, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        SeekBar seekBarFontSize = dialogView.findViewById(R.id.seekBarFontSize);
        SeekBar seekBarLineSpacing = dialogView.findViewById(R.id.seekBarLineSpacing);
        Spinner spinnerFontType = dialogView.findViewById(R.id.spinnerFontType);
        Spinner spinnerBgColor = dialogView.findViewById(R.id.spinnerBgColor);
        Button btnApplySettings = dialogView.findViewById(R.id.btnApplySettings);

        // Load stored settings
        SharedPreferences prefs = requireContext().getSharedPreferences("ReaderSettings", Context.MODE_PRIVATE);
        float savedFontSize = prefs.getFloat("font_size", 16f);
        float savedLineSpacing = prefs.getFloat("line_spacing", 1.5f);
        String savedFontType = prefs.getString("font_type", "sans-serif");
        String savedBgColor = prefs.getString("bg_color", "#FFFFFF");

        seekBarFontSize.setProgress((int) savedFontSize);
        seekBarLineSpacing.setProgress((int) (savedLineSpacing * 10));

        // Populate font type spinner
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"Sans-serif", "Serif", "Monospace"});
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontType.setAdapter(fontAdapter);
        spinnerFontType.setSelection(fontAdapter.getPosition(savedFontType));

        // Populate background color spinner
        ArrayAdapter<String> bgColorAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"White", "Sepia", "Black"});
        bgColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBgColor.setAdapter(bgColorAdapter);
        spinnerBgColor.setSelection(bgColorAdapter.getPosition(savedBgColor));

        btnApplySettings.setOnClickListener(v -> {
            float fontSize = seekBarFontSize.getProgress();
            float lineSpacing = seekBarLineSpacing.getProgress() / 10.0f;
            String fontType = spinnerFontType.getSelectedItem().toString();
            String bgColor = spinnerBgColor.getSelectedItem().toString();

            // Apply settings
            applyReaderSettings(fontSize, lineSpacing, fontType, bgColor);

            // Save settings
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("font_size", fontSize);
            editor.putFloat("line_spacing", lineSpacing);
            editor.putString("font_type", fontType);
            editor.putString("bg_color", bgColor);
            editor.apply();

            dialog.dismiss();
        });

        dialog.show();
    }

    // Apply settings to the Reader View
    private void applyReaderSettings(float fontSize, float lineSpacing, String fontType, String bgColor) {
        tvContent.setTextSize(fontSize);
        tvContent.setLineSpacing(lineSpacing, 1.0f);
        tvContent.setTypeface(Typeface.create(fontType.toLowerCase(), Typeface.NORMAL));

        switch (bgColor) {
            case "White":
                tvContent.setBackgroundColor(Color.WHITE);
                tvContent.setTextColor(Color.BLACK);
                break;
            case "Sepia":
                tvContent.setBackgroundColor(Color.parseColor("#F5DEB3"));
                tvContent.setTextColor(Color.DKGRAY);
                break;
            case "Black":
                tvContent.setBackgroundColor(Color.BLACK);
                tvContent.setTextColor(Color.WHITE);
                break;
        }
    }


}
