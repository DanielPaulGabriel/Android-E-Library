package mdad.localdata.androide_library;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffAccountsFragment extends Fragment {
    private RecyclerView recyclerView;
    private StaffAccountAdapter adapter;
    private List<StaffAccount> staffList = new ArrayList<>();
    private static final String GET_ALL_STAFF_URL = Constants.GET_ALL_USERS_URL;
    private static final String DELETE_STAFF_URL = Constants.DELETE_USER_URL;
    private static final String EDIT_STAFF_URL = Constants.UPDATE_USER_DETAILS_URL;
    private static final String CREATE_STAFF_URL = Constants.CREATE_STAFF_URL;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private Button btnLogout, btnToggleTheme, btnCreateAccount;
    private int currentMode;

    public StaffAccountsFragment() {
        // Required empty public constructor
    }

    public static StaffAccountsFragment newInstance(String param1, String param2) {
        StaffAccountsFragment fragment = new StaffAccountsFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_staff_accounts, container, false);
        currentMode = SharedPrefsManager.getThemeMode(requireContext());
        btnLogout = rootView.findViewById(R.id.btnLogout);
        btnToggleTheme = rootView.findViewById(R.id.btnToggleTheme);
        btnCreateAccount = rootView.findViewById(R.id.btnCreateAccount);
        recyclerView = rootView.findViewById(R.id.recyclerViewStaffAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadStaffAccounts();

        btnLogout.setOnClickListener(v->{
            SharedPrefsManager.clearUserData(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        btnToggleTheme.setOnClickListener(v->{
            int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES;

            // Save new theme mode
            SharedPrefsManager.saveThemeMode(requireContext(), newMode);

            // Apply new theme mode
            AppCompatDelegate.setDefaultNightMode(newMode);

            // Provide feedback
            String theme = (newMode == AppCompatDelegate.MODE_NIGHT_YES) ? "Dark Mode" : "Light Mode";
            Toast.makeText(requireContext(), theme + " enabled", Toast.LENGTH_SHORT).show();
        });
        btnCreateAccount.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                handleNoData("No internet connection. Please check your connection.");
                return;
            }
            // Handle create account
            Fragment createAccountFragment = CreateStaffAccountFragment.newInstance();
            ((AppCompatActivity) requireContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createAccountFragment)
                    .addToBackStack(null)
                    .commit();
        });
        return rootView;
    }

    private void loadStaffAccounts() {
        if (!isNetworkAvailable()) {
            handleNoData("No internet connection. Please check your connection.");
            return;
        }
        StringRequest request = new StringRequest(Request.Method.GET, GET_ALL_STAFF_URL +"?role=staff",
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            staffList.clear();
                            JSONArray accounts = jsonObject.getJSONArray("users");
                            for (int i = 0; i < accounts.length(); i++) {
                                JSONObject account = accounts.getJSONObject(i);
                                staffList.add(new StaffAccount(
                                        account.getInt("user_id"),
                                        account.getString("username"),
                                        account.getString("role")
                                ));
                            }
                            adapter = new StaffAccountAdapter(staffList, new StaffAccountAdapter.OnAccountActionListener() {
                                @Override
                                public void onEdit(StaffAccount account) {
                                    if (!isNetworkAvailable()) {
                                        handleNoData("No internet connection. Please check your connection.");
                                        return;
                                    }
                                    Fragment editFragment = EditStaffAccountFragment.newInstance(account.getId(), account.getUsername());
                                    ((AppCompatActivity) requireContext())
                                            .getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container, editFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }

                                @Override
                                public void onDelete(StaffAccount account) {
                                    if (!isNetworkAvailable()) {
                                        handleNoData("No internet connection. Please check your connection.");
                                        return;
                                    }
                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("Delete Account")
                                            .setMessage("Are you sure you want to delete this account?")
                                            .setPositiveButton("Yes", (dialog, which) -> deleteStaffAccount(account.getId()))
                                            .setNegativeButton("No", null)
                                            .show();
                                }
                            });
                            recyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing staff accounts.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void deleteStaffAccount(int staffId) {
        int userId = SharedPrefsManager.getUserId(requireContext());
        if(userId==staffId){
            Toast.makeText(requireContext(), "You cannot delete your own staff account, Please contact the admin.", Toast.LENGTH_SHORT).show();
        }
        else{
            StringRequest request = new StringRequest(Request.Method.POST, DELETE_STAFF_URL,
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                loadStaffAccounts();
                            } else {
                                Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error deleting account.", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", String.valueOf(staffId));
                    return params;
                }
            };
            Volley.newRequestQueue(requireContext()).add(request);


        }

    }

    private void handleNoData(String message) {
        if (!isAdded()) return; // Ensure fragment is attached
        if (message == null || message.trim().isEmpty()) {
            Toast.makeText(requireContext(), "An unknown error occurred.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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