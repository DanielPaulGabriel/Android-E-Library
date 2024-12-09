package mdad.localdata.androide_library;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnEditCredentials, btnSaveChanges, btnCancel, btnLogout;

    private static final String UPDATE_USER_DETAILS_URL = Constants.UPDATE_USER_DETAILS_URL;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        etUsername = rootView.findViewById(R.id.etUsername);
        etPassword = rootView.findViewById(R.id.etPassword);
        btnEditCredentials = rootView.findViewById(R.id.btnEditCredentials);
        btnSaveChanges = rootView.findViewById(R.id.btnSaveChanges);
        btnLogout = rootView.findViewById(R.id.btnLogout);
        btnCancel = rootView.findViewById(R.id.btnCancel);

        loadUserData();


        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch event is within the bounds of the drawableEnd
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (etPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        // Switch to hidden password
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pw_visibility_off, 0);
                    } else {
                        // Switch to visible password
                        etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pw_visibility, 0);
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

                // Disable editing after saving
                toggleEditing(false);

                // Show Edit button, hide Save button
                btnEditCredentials.setVisibility(View.VISIBLE);
                btnSaveChanges.setVisibility(View.GONE);
            }
        });

        // Logout Button Listener
        btnLogout.setOnClickListener(v -> {
            SharedPrefsManager.clearUserData(requireContext());
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return rootView;
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

        // Optionally change background to make it visually clear
        etUsername.setBackgroundResource(isEnabled ? android.R.drawable.edit_text : android.R.color.transparent);
        etPassword.setBackgroundResource(isEnabled ? android.R.drawable.edit_text : android.R.color.transparent);
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
}
