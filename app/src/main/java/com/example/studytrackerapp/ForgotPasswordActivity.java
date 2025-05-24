package com.example.studytrackerapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputLayout emailInputLayout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        View successMessage = findViewById(R.id.successMessage);

        // Back button click
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Reset password button click
        findViewById(R.id.resetPasswordButton).setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Reset errors
            emailInputLayout.setError(null);

            // Validate email
            if (TextUtils.isEmpty(email)) {
                emailInputLayout.setError("Email is required");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.setError("Please enter a valid email");
                return;
            }

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Show success message
                            successMessage.setVisibility(View.VISIBLE);
                            emailEditText.setText("");
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//    }
}