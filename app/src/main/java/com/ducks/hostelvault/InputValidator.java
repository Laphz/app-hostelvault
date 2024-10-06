package com.ducks.hostelvault;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    FirebaseHelper firebaseHelper = new FirebaseHelper();

    public interface OnValidationListener {
        void onValidationSuccess();
        void onValidationFailed();
    }

    public void validateInputs(String email, String password, String confirmPassword, String phoneNum, String hostelId, EditText[] inputFields, OnValidationListener listener) {
        boolean isValid = true;

        // Valid mobile num
        String mobileRegex = "[6-9][0-9]{9}";  // First num should be in range [6,9] and other 9 can be [0,9]
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(phoneNum);

        // Check if required fields are empty
        for (int i = 0; i < inputFields.length; i++) {
            if (TextUtils.isEmpty(inputFields[i].getText().toString())) {
                showError(inputFields[i], "Required Field!");
                isValid = false;
            }
        }

        // Check hostel existence
        firebaseHelper.doesHostelExist(hostelId).addOnSuccessListener(exists -> {
            if (!exists) {
                Log.d("HostelCheck", "Hostel does not exist");
                showError(inputFields[5], "Hostel ID does not exist!");
                listener.onValidationFailed();
            } else {
                Log.d("HostelCheck", "Hostel exists");
                // Proceed with other validations
                if (phoneNum.length() != 10) {
                    showError(inputFields[4], "Length should be 10!");
                    listener.onValidationFailed();
                    return;
                }

                if (!mobileMatcher.find()) {
                    showError(inputFields[4], "Please enter a valid Mobile Number");
                    listener.onValidationFailed();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showError(inputFields[1], "Please provide a valid Email.");
                    listener.onValidationFailed();
                    return;
                }

                if (password.length() < 6) {
                    showError(inputFields[2], "Password must be 6 characters!");
                    listener.onValidationFailed();
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    showError(inputFields[3], "Confirm Password is required!");
                    listener.onValidationFailed();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    showError(inputFields[3], "Passwords do not match!");
                    listener.onValidationFailed();
                    return;
                }

                // If all validations pass
                listener.onValidationSuccess();
            }
        }).addOnFailureListener(e -> {
            // Handle failure, e.g., connection issues
            Log.e("HostelCheck", "Error: ", e);
            listener.onValidationFailed();
        });
    }

    private void showError(EditText inputField, String message) {
        inputField.setError(message);
        inputField.requestFocus();
    }
}
