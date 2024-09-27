package com.ducks.hostelvault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ducks.hostelvault.FirebaseHelper;
import com.ducks.hostelvault.loginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class signupActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private inputValidator inputValidator;
    private HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_passwrd);
        editTextConfirmPassword = findViewById(R.id.et_confpasswrd);
        progressBar = findViewById(R.id.progressbar);

        firebaseHelper = new FirebaseHelper();
        inputValidator = new inputValidator();
        helperClass = new HelperClass();

        Button signUpButton = findViewById(R.id.signup_btn);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        EditText[] inputFields = {editTextName, editTextEmail, editTextPassword, editTextConfirmPassword};

        if (!inputValidator.validateInputs(name, email, password, confirmPassword, inputFields)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.signUpUser(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                    user user = new user(name, email);

                    firebaseHelper.storeUserData(firebaseUser.getUid(), user, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseHelper.sendEmailVerification(firebaseUser);
                                firebaseHelper.signOutUser();
                                while (!firebaseUser.isEmailVerified()){
                                    helperClass.showAlertEmailVerification(signupActivity.this);
                                    Toast.makeText(signupActivity.this, "Registration successful! Please check your email to verify your account.", Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(signupActivity.this, loginActivity.class));
                            } else {
                                showErrorToast("Failed to save user data!");
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    showErrorToast(task.getException().getMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showErrorToast(String message) {
        Toast.makeText(signupActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
