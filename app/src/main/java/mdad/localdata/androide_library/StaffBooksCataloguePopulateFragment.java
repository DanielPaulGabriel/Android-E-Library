package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StaffBooksCataloguePopulateFragment extends Fragment {
    private Spinner spinnerGenre;
    private Button btnFetchBooks;
    private ImageButton btnBack;

    private static final String GUTENDEX_API = "http://gutendex.com/books";

    public StaffBooksCataloguePopulateFragment() {
        // Required empty public constructor
    }

    public static StaffBooksCataloguePopulateFragment newInstance() {

        return new StaffBooksCataloguePopulateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_cloud_books_catalogue, container, false);
        spinnerGenre = rootView.findViewById(R.id.spinnerGenre);
        btnFetchBooks = rootView.findViewById(R.id.btnFetchBooks);
        btnBack = rootView.findViewById(R.id.btnBack);

        // Set predefined genres in the spinner
        populateGenreSpinner();

        // Handle back button click
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Fetch books when the button is clicked
        btnFetchBooks.setOnClickListener(v -> {
            String selectedGenre = spinnerGenre.getSelectedItem().toString();
            System.out.println("Selected Genre: "+selectedGenre);
            fetchBooksFromGutendex(selectedGenre);
        });

        return rootView;
    }
    private void populateGenreSpinner() {
        // Predefined genres
        String[] genres = {"Fiction", "Science", "Fantasy"};

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, genres);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the spinner
        spinnerGenre.setAdapter(adapter);
    }


    private void fetchBooksFromGutendex(String genre) {
        String url = GUTENDEX_API + "?topic=" + genre + "&languages=en&limit=1";
        System.out.println("URL: "+url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        System.out.println(response);
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            Toast.makeText(requireContext(), "No books found for the selected genre.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject book = results.getJSONObject(i);

                            // Extract book metadata
                            String title = book.optString("title", "No Title");
                            JSONArray authorsArray = book.optJSONArray("authors");
                            String author = (authorsArray != null && authorsArray.length() > 0)
                                    ? authorsArray.getJSONObject(0).optString("name", "Unknown Author")
                                    : "Unknown Author";
                            String summary = "Summary not available"; // Gutendex does not provide summaries
                            String genreName = genre;
                            JSONObject formats = book.getJSONObject("formats");
                            String coverUrl = formats.optString("image/jpeg", "");
                            String contentUrl = formats.optString("text/plain", "");

                            System.out.println("Book: "+title+author+summary+genre+coverUrl+contentUrl);

                            // Save the book details
                            saveBookToDatabase(title, author, genreName, summary, coverUrl, contentUrl);
                        }

                        Toast.makeText(requireContext(), "Books fetched and added to the catalog!", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to parse book data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error fetching books: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }



    private void saveBookToDatabase(String title, String author, String genre, String summary, String coverUrl, String contentUrl) {
        int quantity = 5;
        System.out.println("Book: "+title+author+summary+genre+coverUrl+contentUrl);

    }
}