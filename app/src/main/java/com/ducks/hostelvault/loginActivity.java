package com.ducks.hostelvault;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class loginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private TextView forgotPwd;
    private Button loginButton;
    private CheckBox rememberMe;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private HelperClass helperClass;


    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration overrideConfiguration = new Configuration(newBase.getResources().getConfiguration());
        overrideConfiguration.fontScale = 1.0f;  // Set fontScale to 1.0 to avoid scaling
        Context context = newBase.createConfigurationContext(overrideConfiguration);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        editTextEmail = findViewById(R.id.loginEt_email);
        editTextPassword = findViewById(R.id.loginET_pwd);
        forgotPwd = findViewById(R.id.forgotpwd);
        loginButton = findViewById(R.id.button);
        rememberMe = findViewById(R.id.rememberME);
        progressBar = findViewById(R.id.progressbarSignin);

        // Initialize helper classes
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();

        // Launch login function
        launchLogin();
        setupForgotPwd();
    }

    private void launchLogin() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (validateFields(email, password)) {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(email, password);
                }
            }
        });
    }

    private boolean validateFields(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters long");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        firebaseHelper.signInUser(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseHelper.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        helperClass.startNewActivity(loginActivity.this, homeActivity.class);
                        firebaseHelper.getUserNameToast(firebaseHelper.getCurrentUser().getUid(),loginActivity.this);
                    } else {
                        helperClass.customToast(loginActivity.this, "Email not verified. Please verify your email.");
                        helperClass.startNewActivity(loginActivity.this, verifyEmailActivity.class);
//                        firebaseHelper.signOutUser();
                    }
                } else {
                    handleFirebaseAuthException(task.getException());
                }
            }
        });
    }

    private void setupForgotPwd() {
        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperClass.startNewActivity(loginActivity.this, forgotPasswrd.class);
            }
        });
    }

    private void handleFirebaseAuthException(Exception exception) {
        Log.e("FirebaseAuthError", "Error during login: ", exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {
            helperClass.customToast(this, "User does not exist. Please sign up.");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            helperClass.customToast(this, "Invalid credentials. Please check your email and password.");
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            helperClass.customToast(this, "This email is already registered. Try logging in.");
        } else {
            helperClass.customToast(this, "An error occurred. Please try again.");
        }
    }


}
