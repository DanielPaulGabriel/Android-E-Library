package mdad.localdata.androide_library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ListenerViewFragment extends Fragment {
    private ImageView ivBookCover;
    private ImageButton btnBack;
    private EditText etPageNumber;
    private SeekBar seekBarPage;
    private TextToSpeech tts;
    private Button btnPlayTTS, btnStopTTS;
    private Spinner spinnerLanguage, spinnerSpeed;
    private String bookContent, coverUrl, title;
    private int currentPage = 1;
    private int totalPages = 0;
    private int bookId;
    public ListenerViewFragment() {
        // Required empty public constructor
    }

    public static ListenerViewFragment newInstance(int bookId, String coverUrl, String title) {
        ListenerViewFragment fragment = new ListenerViewFragment();
        Bundle args = new Bundle();
        args.putInt("bookId", bookId);
        args.putString("coverUrl", coverUrl);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt("bookId");
            coverUrl = getArguments().getString("coverUrl");
            title = getArguments().getString("title");
        }

        // Initialize Text-to-Speech
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(1.0f);
            } else {
                Toast.makeText(requireContext(), "TTS initialization failed!", Toast.LENGTH_SHORT).show();
            }
        });
        SharedPreferences prefs = requireContext().getSharedPreferences("BookProgress", Context.MODE_PRIVATE);
        currentPage = prefs.getInt("book_" + bookId, 1); // Default to page 1 if not found
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listener_view, container, false);

        btnBack = rootView.findViewById(R.id.btnBack);
        btnPlayTTS = rootView.findViewById(R.id.btnPlayTTS);
        btnStopTTS = rootView.findViewById(R.id.btnStopTTS);
        ivBookCover = rootView.findViewById(R.id.ivBookCover);
        etPageNumber = rootView.findViewById(R.id.etPageNumber);
        seekBarPage = rootView.findViewById(R.id.seekBarPage);

        spinnerLanguage = rootView.findViewById(R.id.spinnerLanguage);
        spinnerSpeed = rootView.findViewById(R.id.spinnerSpeed);


        Glide.with(this).load(coverUrl+"?t="+System.currentTimeMillis()).into(ivBookCover);


        // Fetch the first page
        //loadPage(1);

        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

        btnPlayTTS.setOnClickListener(v -> {
            // Check if there is saved progress
            SharedPreferences prefs1 = requireContext().getSharedPreferences("BookProgress", Context.MODE_PRIVATE);
            int savedPage = prefs1.getInt("book_" + bookId, -1); // Default to -1 if no saved progress
            System.out.println("Saved page: " + savedPage);

            if (savedPage == -1) {
                // No saved progress
                Toast.makeText(requireContext(), "No saved progress found. Starting from page 1.", Toast.LENGTH_SHORT).show();
                currentPage = 1; // Start from the first page
            } else if (currentPage != savedPage) {
                // Update currentPage to match saved progress
                currentPage = savedPage;
                Toast.makeText(requireContext(), "Resuming from page " + currentPage, Toast.LENGTH_SHORT).show();
            }

            // Load the page and then play the TTS
            loadPage(currentPage);

            Intent serviceIntent = new Intent(getContext(), BookPlayerService.class);
            serviceIntent.putExtra("bookContent", bookContent); // Pass the book content
            serviceIntent.putExtra("bookTitle", title); // Pass the book title
            requireContext().startService(serviceIntent);
        });


        btnStopTTS.setOnClickListener(v -> {
            pausePlayback();
            Intent serviceIntent = new Intent(getContext(), BookPlayerService.class);
            requireContext().stopService(serviceIntent);
        });

        seekBarPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int selectedPage = progress + 1;
                    if (selectedPage > 0 && selectedPage <= totalPages) {
                        currentPage = selectedPage;
                        loadPage(currentPage);
                        saveProgress(); // Save progress when navigating pages
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = (String) parent.getItemAtPosition(position);
                switch (selectedLanguage) {
                    case "English (US)":
                        pausePlayback();
                        tts.setLanguage(Locale.US);
                        break;
                    case "English (UK)":
                        pausePlayback();
                        tts.setLanguage(Locale.UK);
                        break;
                    case "French":
                        pausePlayback();
                        tts.setLanguage(Locale.FRANCE);
                        break;
                    case "German":
                        pausePlayback();
                        tts.setLanguage(Locale.GERMANY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle no selection
                tts.setLanguage(Locale.US);
            }
        });

        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpeed = (String) parent.getItemAtPosition(position);
                switch (selectedSpeed) {
                    case "Slow":
                        pausePlayback();
                        tts.setSpeechRate(0.5f);
                        break;
                    case "Normal":
                        pausePlayback();
                        tts.setSpeechRate(1.0f);
                        break;
                    case "Fast":
                        pausePlayback();
                        tts.setSpeechRate(1.5f);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle no selection\
                tts.setSpeechRate(1.0f);
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
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                tts.stop(); // Stop the service when TTS is done
            }

            @Override
            public void onError(String utteranceId) { }
        });

        return rootView;
    }

    private void loadPage(int pageNumber) {
        setPlayButtonState(false); // Disable play button during loading
        String url = Constants.GET_BOOK_TEXT_URL + "?book_id=" + bookId + "&page_number=" + pageNumber;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            bookContent = jsonObject.getString("content");
                            totalPages = jsonObject.getInt("total_pages");
                            seekBarPage.setMax(totalPages); // Set max here after fetching total pages
                            seekBarPage.setProgress(currentPage - 1); // Pages are 1-based, SeekBar is 0-based
                            setPlayButtonState(true); // Enable play button during loading
                            etPageNumber.setText(String.valueOf(pageNumber));
                            etPageNumber.clearFocus();
                            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etPageNumber.getWindowToken(), 0);
                            // Ensure content is available before starting TTS
                            if (bookContent != null && !bookContent.trim().isEmpty()) {
                                tts.speak(bookContent, TextToSpeech.QUEUE_FLUSH, null, null);
                            } else {
                                Toast.makeText(requireContext(), "No content to read.", Toast.LENGTH_SHORT).show();
                            }
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

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null; // Nullify reference
        }
        super.onDestroy();
    }

    private void saveProgress() {
        SharedPreferences prefs = requireContext().getSharedPreferences("BookProgress", Context.MODE_PRIVATE);
        prefs.edit().putInt("book_" + bookId, currentPage).apply();
    }
    private void pausePlayback() {
        if (tts.isSpeaking()) {
            tts.stop();
            saveProgress();
            Toast.makeText(requireContext(), "Playback stopped. Progress saved.", Toast.LENGTH_SHORT).show();
        }
    }
    private void setPlayButtonState(boolean isEnabled) {
        btnPlayTTS.setEnabled(isEnabled);
    }
    @Override
    public void onPause() {
        super.onPause();
        saveProgress();
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
