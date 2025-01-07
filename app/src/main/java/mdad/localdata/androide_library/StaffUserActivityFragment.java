package mdad.localdata.androide_library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StaffUserActivityFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private SearchView searchView;
    private TextView tvNoUsers;
    private Button btnRetry;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private static final String GET_USERS_URL = Constants.GET_ALL_USERS_URL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_user_activity, container, false);
        recyclerViewUsers = rootView.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        tvNoUsers = rootView.findViewById(R.id.tvNoUsers);
        btnRetry = rootView.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v-> fetchUsers());

        fetchUsers();

        searchView = rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private final Handler handler = new Handler();
            private Runnable workRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }
                workRunnable = () -> filterUsers(newText);
                handler.postDelayed(workRunnable, 300); // Debounce input
                return true;
            }
        });


        return rootView;
    }

    private void fetchUsers() {
        if (!isNetworkAvailable()) {
            handleNoData("No internet connection. Please check your connection.");
            return;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USERS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray usersArray = jsonObject.getJSONArray("users");
                            tvNoUsers.setVisibility(View.GONE);
                            btnRetry.setVisibility(View.GONE);
                            recyclerViewUsers.setVisibility(View.VISIBLE);
                            searchView.setVisibility(View.VISIBLE);
                            userList.clear();
                            filteredList.clear();
                            for (int i = 0; i < usersArray.length(); i++) {
                                JSONObject userObject = usersArray.getJSONObject(i);
                                userList.add(new User(
                                        userObject.getInt("user_id"),
                                        userObject.getString("username"),
                                        userObject.getString("role"),
                                        userObject.getString("created_at")
                                ));
                            }
                            filteredList.addAll(userList);

                            userAdapter = new UserAdapter(filteredList, user -> openUserDetails(user));
                            recyclerViewUsers.setAdapter(userAdapter);
                        } else {
                            //Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            handleNoData(jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Toast.makeText(getContext(), "Error loading users.", Toast.LENGTH_SHORT).show();
                        handleNoData("Error loading users.");
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void openUserDetails(User user) {
        Fragment userDetailsFragment = StaffUserDetailsFragment.newInstance(user.getUserId(), user.getUsername(), user.getCreatedAt());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, userDetailsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }

        if (filteredList.isEmpty()) {
            tvNoUsers.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
        } else {
            tvNoUsers.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);;
            recyclerViewUsers.setVisibility(View.VISIBLE);

            if (userAdapter != null) {
                userAdapter.notifyDataSetChanged();
            }
        }

        //reviewAdapter.updateReviews(filteredReviewList);
    }

    private void handleNoData(String message) {
        if (!isAdded()) return; // Ensure fragment is attached
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), "An unknown error occurred.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        tvNoUsers.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        recyclerViewUsers.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
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
