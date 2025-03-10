package mdad.localdata.androide_library;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.appcompat.app.AppCompatDelegate;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private int currentMode;
    private EditText etUsername, etPassword;
    private Button btnEditCredentials, btnSaveChanges, btnCancel, btnLogout, btnDelete, btnToggleTheme;
    private static final String UPDATE_USER_DETAILS_URL = Constants.UPDATE_USER_DETAILS_URL;
    private static final String DELETE_USER_URL = Constants.DELETE_USER_URL;
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        currentMode = SharedPrefsManager.getThemeMode(requireContext());
        etUsername = rootView.findViewById(R.id.etUsername);
        etPassword = rootView.findViewById(R.id.etPassword);
        btnEditCredentials = rootView.findViewById(R.id.btnEditCredentials);
        btnSaveChanges = rootView.findViewById(R.id.btnSaveChanges);
        btnLogout = rootView.findViewById(R.id.btnLogout);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        btnDelete = rootView.findViewById(R.id.btnDelete);
        btnToggleTheme = rootView.findViewById(R.id.btnToggleTheme);

        loadUserData();

        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch event is within the bounds of the drawableEnd
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (etPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        // Switch to hidden password
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_not_visible_grey, 0);
                    } else {
                        // Switch to visible password
                        etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visible_grey, 0);
                    }
                    // Move the cursor to the end of the text
                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Edit Credentials Button Listener
        btnEditCredentials.setOnClickListener(v -> {
            // Enable editing
            toggleEditing(true);

            // Hide Edit button, show Save button
            btnEditCredentials.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.GONE);
            btnToggleTheme.setVisibility(View.GONE);
        });

        // Cancel Edit Credentials Button Listener
        btnCancel.setOnClickListener(v -> {
            // Enable editing
            toggleEditing(false);

            // Hide Edit button, show Save button
            btnEditCredentials.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnSaveChanges.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
            btnToggleTheme.setVisibility(View.VISIBLE);


            loadUserData();
        });

        // Save Changes Button Listener
        btnSaveChanges.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newPassword)) {
                Toast.makeText(requireContext(), "Both fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                updateProfile(newUsername, newPassword);

            }
        });

        // Logout Button Listener
        btnLogout.setOnClickListener(v -> {
            SharedPrefsManager.clearUserData(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                handleNoData("No internet connection. Please check your connection.");
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteUser())
                    .setNegativeButton("No", null)
                    .show();
        });
        /*btnToggleTheme.setOnClickListener(v -> {
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
        });*/

        btnToggleTheme.setOnClickListener(v -> {
            String[] themes = {"Light", "Dark", "System"};
            int currentThemeIndex = getCurrentThemeIndex();

            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Theme")
                    .setSingleChoiceItems(themes, currentThemeIndex, (dialog, which) -> {
                        setTheme(themes[which]);
                        dialog.dismiss(); // Close the dialog after selection
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return rootView;
    }
    private void setTheme(String theme) {
        switch (theme) {
            case "Light":
                SharedPrefsManager.saveThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "Dark":
                SharedPrefsManager.saveThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            case "System":
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    SharedPrefsManager.saveThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    Toast.makeText(requireContext(), "System theme is not supported on your device.", Toast.LENGTH_SHORT).show();
                    SharedPrefsManager.saveThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_NO); // Fallback to Light theme
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;

            default:
                SharedPrefsManager.saveThemeMode(requireContext(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        Toast.makeText(requireContext(), theme + " theme enabled", Toast.LENGTH_SHORT).show();
    }
    private int getCurrentThemeIndex() {
        int mode = SharedPrefsManager.getThemeMode(requireContext());
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return 0; // Light
            case AppCompatDelegate.MODE_NIGHT_YES:
                return 1; // Dark
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                return 2; // System
        }
    }
    private void loadUserData() {
        String currentUsername = SharedPrefsManager.getUsername(requireContext());
        String currentPassword = SharedPrefsManager.getPassword(requireContext());
        etUsername.setText(currentUsername);
        etPassword.setText(currentPassword);
    }

    private void toggleEditing(boolean isEnabled) {
        etUsername.setFocusable(isEnabled);
        etUsername.setFocusableInTouchMode(isEnabled);
        etPassword.setFocusable(isEnabled);
        etPassword.setFocusableInTouchMode(isEnabled);


        if (isEnabled) {
            etUsername.setBackgroundResource(android.R.drawable.edit_text);
            etPassword.setBackgroundResource(android.R.drawable.edit_text);

            // Set text color for editable state
            etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        } else {
            etUsername.setBackgroundResource(android.R.color.transparent);
            etPassword.setBackgroundResource(android.R.color.transparent);

            int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                // Dark Mode
                etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                // Light Mode
                etUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                etPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }
        }
    }


    private void updateProfile(String username, String password) {
        int userId = SharedPrefsManager.getUserId(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_USER_DETAILS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.saveUsername(requireContext(), username);
                            SharedPrefsManager.savePassword(requireContext(), password);
                            // Disable editing after saving
                            toggleEditing(false);

                            // Show Edit button, hide Save button
                            btnToggleTheme.setVisibility(View.VISIBLE);
                            btnEditCredentials.setVisibility(View.VISIBLE);
                            btnDelete.setVisibility(View.VISIBLE);
                            btnLogout.setVisibility(View.VISIBLE);
                            btnSaveChanges.setVisibility(View.GONE);
                            btnCancel.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    private void deleteUser(){
        int userId = SharedPrefsManager.getUserId(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_USER_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Profile deleted!", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.clearUserData(requireContext());
                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
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


