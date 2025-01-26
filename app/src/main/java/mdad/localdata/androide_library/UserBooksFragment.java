package mdad.localdata.androide_library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class UserBooksFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerView;
    private TextView tvNoBooks;
    private Button btnRedirect, btnRetry;
    private RequestQueue requestQueue;

    private static final String GET_USER_BOOKS_URL = Constants.GET_USER_BOOKS_URL;

    public UserBooksFragment() {
        // Required empty public constructor
    }

    public static UserBooksFragment newInstance(String param1, String param2) {

        return new UserBooksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
    }
    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this); // Cancels requests tagged with this fragment
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_books, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewUserBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvNoBooks = rootView.findViewById(R.id.tvNoBooks);
        btnRedirect = rootView.findViewById(R.id.btnRedirect);
        btnRetry = rootView.findViewById(R.id.btnRetry);

        btnRedirect.setOnClickListener(v->{
            Fragment bookCatalogueFragment = BookCatalogueFragment.newInstance();

            // Use the FragmentManager to replace the current fragment
            ((AppCompatActivity) requireContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, bookCatalogueFragment)
                    .addToBackStack(null)
                    .commit();
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_catalog);
        });

        // Fetch User ID
        int userId = SharedPrefsManager.getUserId(getContext());
        if(userId != -1 ){
            fetchBooks(userId, recyclerView, tvNoBooks);
        }
        btnRetry.setOnClickListener(v->fetchBooks(userId, recyclerView, tvNoBooks));

        return rootView;
    }
    private void fetchBooks(int userId, RecyclerView recyclerView, TextView tvNoBooks) {
        if (!isNetworkAvailable()) {
            handleNoData("No internet connection. Please check your connection.");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USER_BOOKS_URL + "?user_id=" + userId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {

                                tvNoBooks.setVisibility(View.GONE);
                                btnRetry.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                JSONArray autoReturnedBooks = jsonObject.optJSONArray("auto_returned_books");
                                if (autoReturnedBooks != null) {
                                    for (int i = 0; i < autoReturnedBooks.length(); i++) {
                                        JSONObject book = autoReturnedBooks.getJSONObject(i);
                                        sendNotification("Book Returned", "Your borrowed book \"" + book.getString("title") + "\" has been returned.");
                                    }
                                }

                                JSONArray booksArray = jsonObject.getJSONArray("borrowed_books");
                                List<UserBook> books = new ArrayList<>();
                                for (int i = 0; i < booksArray.length(); i++) {
                                    JSONObject bookJson = booksArray.getJSONObject(i);
                                    books.add(new UserBook(
                                            bookJson.getInt("book_id"),
                                            bookJson.getInt("borrow_id"),
                                            bookJson.getString("title"),
                                            bookJson.getString("author"),
                                            bookJson.getString("genre"),
                                            bookJson.getString("summary"),
                                            bookJson.getString("cover_path"),
                                            bookJson.getString("content_path"),
                                            bookJson.getString("borrow_date"),
                                            bookJson.getString("due_date"),
                                            bookJson.optString("return_date", null)
                                    ));
                                }
                                // Group books by genre and add headers
                                List<ListItem> items = new ArrayList<>();
                                Collections.sort(books, Comparator.comparing(UserBook::getGenre));
                                String lastGenre = "";
                                for (UserBook book : books) {
                                    if (!book.getGenre().equalsIgnoreCase(lastGenre)) {
                                        lastGenre = book.getGenre();
                                        items.add(new GenreHeader(lastGenre));
                                    }
                                    items.add(book);
                                }

                                // Pass the list with headers to the adapter
                                UserBookAdapter adapter = new UserBookAdapter(items);
                                recyclerView.setAdapter(adapter);

                                if (items.isEmpty()) {
                                    tvNoBooks.setVisibility(View.VISIBLE);
                                    btnRedirect.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    tvNoBooks.setVisibility(View.GONE);
                                    btnRedirect.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                tvNoBooks.setVisibility(View.VISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error loading books", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded()) {
                            // Fragment is not attached, avoid handling the error
                            return;
                        }
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        stringRequest.setTag(this);
        requestQueue.add(stringRequest);
    }
    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("auto_return", "Auto Return Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(requireContext(), "auto_return")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_borrowed_books)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(new Random().nextInt(), notification);
    }
    private void handleNoData(String message) {
        if (!isAdded()) return; // Ensure fragment is attached
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), "An unknown error occurred.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        tvNoBooks.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

}