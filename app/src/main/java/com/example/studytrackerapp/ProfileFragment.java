package com.example.studytrackerapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String PROFILE_PREFS = "user_profile";
    private EditText etFirstName, etLastName, etCourse, etAge, etStudentId, etYear;
    private ImageView ivProfile;
    private Uri profileImageUri;
    private Uri cameraImageUri;

    // ActivityResultLaunchers
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        profileImageUri = selectedImage;
                        ivProfile.setImageURI(selectedImage);
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(getContext(), "Permission required to access images", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    profileImageUri = cameraImageUri;
                    ivProfile.setImageURI(cameraImageUri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etCourse = view.findViewById(R.id.et_course);
        etAge = view.findViewById(R.id.et_age);
        etStudentId = view.findViewById(R.id.et_student_id);
        etYear = view.findViewById(R.id.et_year);
        ivProfile = view.findViewById(R.id.iv_profile);
        ImageView btnEditPhoto = view.findViewById(R.id.btn_edit_photo);
        Button btnSave = view.findViewById(R.id.btn_save);

        loadProfileData();

        // Set click listeners
        btnEditPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Profile Picture")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"},
                        (dialog, which) -> {
                            if (which == 0) {
                                dispatchTakePictureIntent();
                            } else {
                                checkAndRequestImagePermission();
                            }
                        })
                .show();
    }

    private void checkAndRequestImagePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        } else {
            openImagePicker();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile
                );
                cameraLauncher.launch(cameraImageUri);
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void loadProfileData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE);

        etFirstName.setText(prefs.getString("first_name", ""));
        etLastName.setText(prefs.getString("last_name", ""));
        etCourse.setText(prefs.getString("course", ""));
        etAge.setText(prefs.getString("age", ""));
        etStudentId.setText(prefs.getString("student_id", ""));
        etYear.setText(prefs.getString("year", ""));

        String imageUri = prefs.getString("profile_image", null);
        if (imageUri != null) {
            profileImageUri = Uri.parse(imageUri);
            ivProfile.setImageURI(profileImageUri);
        }
    }

    private void saveProfile() {
        // Get all field values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String course = etCourse.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String year = etYear.getText().toString().trim();

        // Validate required fields
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
                .edit();

        editor.putString("first_name", firstName);
        editor.putString("last_name", lastName);
        editor.putString("course", course);
        editor.putString("age", age);
        editor.putString("student_id", studentId);
        editor.putString("year", year);

        if (profileImageUri != null) {
            editor.putString("profile_image", profileImageUri.toString());
        }

        editor.apply();

        // Show success message
        Snackbar.make(requireView(), "Profile saved successfully", Snackbar.LENGTH_SHORT)
                .setAction("UNDO", v -> revertChanges())
                .show();
    }

    private void revertChanges() {
        loadProfileData();
    }
}