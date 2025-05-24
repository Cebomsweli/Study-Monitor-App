package com.example.studytrackerapp;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button createAccountButton, loginButton, backButton;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "StudyTrackerPrefs";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Bind views
        initializeViews();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        createAccountButton = findViewById(R.id.createAccountButton);
        loginButton = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setClickListeners() {
        createAccountButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v -> navigateToLogin());
        backButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        // Clear all existing preferences before creating new account
        clearAllAppPreferences();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleSuccessfulRegistration();
                    } else {
                        handleRegistrationFailure(task.getException());
                    }
                });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void clearAllAppPreferences() {
        // Clear default SharedPreferences
        sharedPreferences.edit().clear().apply();

        // Clear any named SharedPreferences
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();

        // Clear specific preferences if needed
        /*
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_DARK_MODE);
        editor.apply();
        */
    }

    private void handleSuccessfulRegistration() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(verifyTask -> {
                        if (verifyTask.isSuccessful()) {
                            showVerificationSuccess();
                            navigateToLoginAfterSignOut();
                        } else {
                            showVerificationFailure(verifyTask.getException());
                        }
                    });
        }
    }

    private void showVerificationSuccess() {
        Toast.makeText(this,
                "Account created. Please check your email for verification.",
                Toast.LENGTH_LONG).show();
    }

    private void showVerificationFailure(Exception exception) {
        Toast.makeText(this,
                "Verification email failed: " + (exception != null ? exception.getMessage() : "Unknown error"),
                Toast.LENGTH_LONG).show();
    }

    private void navigateToLoginAfterSignOut() {
        mAuth.signOut();
        navigateToLogin();
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void handleRegistrationFailure(Exception exception) {
        Toast.makeText(this,
                "Registration failed: " + (exception != null ? exception.getMessage() : "Unknown error"),
                Toast.LENGTH_LONG).show();
    }
}