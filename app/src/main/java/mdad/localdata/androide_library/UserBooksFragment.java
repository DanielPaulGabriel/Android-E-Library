package mdad.localdata.androide_library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
                response -> {
                    try {
                        Log.e("fetchBooksResponse",response);
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray booksArray = jsonObject.getJSONArray("borrowed_books");
                            List<UserBook> books = new ArrayList<>();
                            for (int i = 0; i < booksArray.length(); i++) {
                                JSONObject bookJson = booksArray.getJSONObject(i);
                                UserBook book = new UserBook(
                                        bookJson.getInt("book_id"),
                                        bookJson.getString("title"),
                                        bookJson.getString("author"),
                                        bookJson.optString("genre", null),
                                        bookJson.getString("summary"),
                                        bookJson.getString("cover_path"),
                                        bookJson.getString("borrow_date"),
                                        bookJson.getString("due_date"),
                                        bookJson.optString("return_date", null)
                                );
                                books.add(book);
                            }

                            if (books.isEmpty()) {
                                tvNoBooks.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoBooks.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                // Set up RecyclerView
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                UserBookAdapter adapter = new UserBookAdapter(books);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(stringRequest);
    }
}