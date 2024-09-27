package com.ducks.hostelvault;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

public class inputValidator {

    public boolean validateInputs(String name, String email, String password, String confirmPassword, EditText[] inputFields) {
        if (TextUtils.isEmpty(name)) {
            showError(inputFields[0], "Full name is required!");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            showError(inputFields[1], "Email is required!");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(inputFields[1], "Please provide a valid Email.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showError(inputFields[2], "Password is required!");
            return false;
        }
        if (password.length() < 6) {
            showError(inputFields[2], "Password must be 6 characters!");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            showError(inputFields[3], "Confirm Password is required!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError(inputFields[3], "Passwords do not match!");
            return false;
        }
        return true;
    }

    private void showError(EditText inputField, String message) {
        inputField.setError(message);
        inputField.requestFocus();
    }
}

