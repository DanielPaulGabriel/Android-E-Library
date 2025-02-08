package mdad.localdata.androide_library;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterUsername, etRegisterPassword;
    private Button btnRegister;
    //private ImageButton btnBack;
    private TextView tvLogin, tvRegister;

    private static final String REGISTER_URL = Constants.REGISTER_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Add app icon to Register Header
        SpannableString spannable = new SpannableString("  Register"); // Space for icon
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher_lib);
        drawable.setBounds(0, 0,100 ,100 );
        ImageSpan imageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
        spannable.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tvRegister.setText(spannable);

        etRegisterPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch event is within the bounds of the drawableEnd
                if (event.getRawX() >= (etRegisterPassword.getRight() - etRegisterPassword.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (etRegisterPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        // Switch to hidden password
                        etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etRegisterPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_not_visible_grey, 0);
                    } else {
                        // Switch to visible password
                        etRegisterPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etRegisterPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visible_grey, 0);
                    }
                    // Move the cursor to the end of the text
                    etRegisterPassword.setSelection(etRegisterPassword.getText().length());
                    return true;
                }
            }
            return false;
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registerUser(){
        final String username = etRegisterUsername.getText().toString().trim();
        final String password = etRegisterPassword.getText().toString().trim();

        // Input validation
        if (username.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.isEmpty()) {
            etRegisterUsername.setError("Username is required");
            return;
        }
        if (password.isEmpty()) {
            etRegisterPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            return;
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("success")) {
                                showSuccessDialog("Registration Successful!"); // Show animated alert

                                // Delay transition to the next activity
                                new Handler().postDelayed(() -> {
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }, 2000);

                            } else {
                                Toast.makeText(RegisterActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
    private void showSuccessDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        // Find the Lottie animation view
        LottieAnimationView lottieSuccess = dialogView.findViewById(R.id.lottieSuccess);
        TextView tvSuccessMessage = dialogView.findViewById(R.id.tvSuccessMessage);
        tvSuccessMessage.setText(msg);
        lottieSuccess.setVisibility(View.VISIBLE);
        lottieSuccess.playAnimation();

        // Show the dialog
        alertDialog.show();

        // Automatically dismiss the dialog after 2 seconds
        new Handler().postDelayed(() -> {
            alertDialog.dismiss();
        }, 2000);
    }
}