package mdad.localdata.androide_library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private static final String GET_USERS_URL = Constants.GET_ALL_USERS_URL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_user_activity, container, false);

        recyclerViewUsers = rootView.findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchUsers();

        return rootView;
    }

    private void fetchUsers() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_USERS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray usersArray = jsonObject.getJSONArray("users");
                            userList.clear();

                            for (int i = 0; i < usersArray.length(); i++) {
                                JSONObject userObject = usersArray.getJSONObject(i);
                                userList.add(new User(
                                        userObject.getInt("user_id"),
                                        userObject.getString("username"),
                                        userObject.getString("role"),
                                        userObject.getString("created_at")
                                ));
                            }

                            userAdapter = new UserAdapter(userList, user -> openUserDetails(user));
                            recyclerViewUsers.setAdapter(userAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error loading users.", Toast.LENGTH_SHORT).show();
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
}
