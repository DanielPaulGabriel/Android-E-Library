package mdad.localdata.androide_library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    private static final String GET_USER_BOOKS_URL = Constants.GET_USER_BOOKS_URL;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public UserBooksFragment() {
        // Required empty public constructor
    }

    public static UserBooksFragment newInstance(String param1, String param2) {
        UserBooksFragment fragment = new UserBooksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_books, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewUserBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        TextView tvNoBooks = rootView.findViewById(R.id.tvNoBooks);

        // Fetch User ID
        int userId = SharedPrefsManager.getUserId(getContext());
        if(userId != -1 ){
            fetchBooks(userId, recyclerView, tvNoBooks);
        }
        return rootView;
    }
    private void fetchBooks(int userId, RecyclerView recyclerView, TextView tvNoBooks) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USER_BOOKS_URL + "?user_id=" + userId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {

                                JSONArray autoReturnedBooks = jsonObject.optJSONArray("auto_returned_books");
                                if (autoReturnedBooks != null) {
                                    for (int i = 0; i < autoReturnedBooks.length(); i++) {
                                        JSONObject book = autoReturnedBooks.getJSONObject(i);
                                        sendNotification("Book Auto-Returned", "Your borrowed book \"" + book.getString("title") + "\" has been auto-returned.");
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
                            } else {
                                tvNoBooks.setVisibility(View.VISIBLE);
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
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
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

}