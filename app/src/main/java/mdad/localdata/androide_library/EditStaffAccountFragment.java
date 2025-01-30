package mdad.localdata.androide_library;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditStaffAccountFragment extends Fragment {
    private EditText etUsername,etPassword;
    private Button btnSubmit, btnCancel;
    private static final String ARG_STAFF_ID = "staffId";
    private static final String ARG_USERNAME = "username";
    private int staffId;
    private String username;
    private static final String UPDATE_STAFF_DETAILS_URL = Constants.UPDATE_STAFF_DETAILS_URL;

    public EditStaffAccountFragment() {
        // Required empty public constructor
    }

    public static EditStaffAccountFragment newInstance(int staffId, String username) {
        EditStaffAccountFragment fragment = new EditStaffAccountFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STAFF_ID, staffId);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            staffId = getArguments().getInt(ARG_STAFF_ID);
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_staff_account, container, false);
        etUsername = rootView.findViewById(R.id.etUsername);
        etPassword = rootView.findViewById(R.id.etPassword);
        btnSubmit = rootView.findViewById(R.id.btnSubmit);
        btnCancel = rootView.findViewById(R.id.btnCancel);

        etUsername.setText(username);
        btnSubmit.setOnClickListener(v -> updateStaffDetails());
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

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

        return rootView;
    }
    private void updateStaffDetails() {
        System.out.println("Staff ID:"+staffId);
        System.out.println("Username:"+username);
        String updatedUsername = etUsername.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (updatedUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Username cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }
        }

        // Send update request to the server
        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_STAFF_DETAILS_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Staff details updated successfully!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
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
                params.put("user_id", String.valueOf(staffId));
                params.put("username", updatedUsername);
                params.put("role","staff");
                if (!newPassword.isEmpty()) {
                    params.put("password", newPassword); // Include new password if provided
                }
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }


}