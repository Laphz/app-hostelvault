package com.ducks.hostelvault;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    public boolean validateInputs(String email, String password, String confirmPassword, String phoneNum,  EditText[] inputFields) {
        int count = 0;

        // valid mobile num
        String mobileRegex = "[6-9][0-9]{9}";  // first num should be in range [6,9] and other 9 can be [0,9]
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(phoneNum);


        for(EditText value : inputFields){
            if (TextUtils.isEmpty(value.getText().toString())) {
                showError(inputFields[count], "Required Field!");
                return false;
            }
            count++;
        }

        if(phoneNum.length() != 10){
            showError(inputFields[4], "Length of should 10!");
            return false;
        }

        if(!mobileMatcher.find()){
            showError(inputFields[4], "Please enter valid Mobile Number");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(inputFields[1], "Please provide a valid Email.");
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

