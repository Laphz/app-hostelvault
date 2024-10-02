package com.ducks.hostelvault;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class loginActivity extends AppCompatActivity {

    private EditText editTextemail, editTextpassword;
    private TextView redirectToSignUp, forgotPwd;
    private Button login;
    private CheckBox rememberMe;
    private Boolean flag = false;
    private ProgressBar progressBar;
    private ClassNotFoundException task;
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

        // input fields views
        editTextemail = findViewById(R.id.loginEt_email);
        editTextpassword = findViewById(R.id.loginET_pwd);
        redirectToSignUp = findViewById(R.id.loginIntosignup);
        forgotPwd = findViewById(R.id.forgotpwd);
        login = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressbarSignin);
        rememberMe = findViewById(R.id.rememberME);

        // helper classes
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();

        lauchLogin();
        forgotPwd();
        redirectToSignUp();

        // Show and hide password (not in use)
//        ImageView hideShowPwd = findViewById(R.id.password_toggle);
//        hideShowPwd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (editTextpassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
//                    // if passwrd is hidden then it enable show pwd
//                    editTextpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                    // change the image to show pwd
//                    hideShowPwd.setImageResource(R.drawable.show_pwd);
//                } else {
//                    // if passwrd is showen then this will hide pwd
//                    editTextpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                    // change the image to hide pwd
//                    hideShowPwd.setImageResource(R.drawable.hide_pwd);
//                }
//            }
//        });


    }

    // launch login function
    private void lauchLogin(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText[] array = new EditText[]{editTextemail, editTextpassword};
                String userEmail = editTextemail.getText().toString();
                String userPwd = editTextpassword.getText().toString();

                if (checkloginfields(array)) {
                    // progress bar
                    progressBar.setVisibility(View.VISIBLE);
                    login(userEmail, userPwd);
                }
            }
        });
    }

    // forgot password
    private void forgotPwd() {
        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, forgotPasswrd.class);
                startActivity(intent);
            }
        });
    }

    // Redirect to Sign up page
    private void redirectToSignUp() {
        redirectToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this, signupActivity.class);
                startActivity(intent);
            }
        });
    }


    // checking input fields
    private Boolean checkloginfields(EditText[] editTextArray) {
        for (EditText value : editTextArray)
            if (TextUtils.isEmpty(value.getText().toString())) {
                helperClass.customToast(loginActivity.this,"Field is required.");
                value.setError("Field is required.");
                value.requestFocus();
                return false;
            }

        if (!Patterns.EMAIL_ADDRESS.matcher(editTextemail.getText().toString()).matches()) {
            helperClass.customToast(loginActivity.this,"Invalid Email.");
            editTextemail.setError("Enter valid email.");
            editTextemail.requestFocus();
            return false;
        }
        if ((editTextpassword.getText().toString()).length() < 6) {
            helperClass.customToast(loginActivity.this,"Enter valid password contains atleast 6 characters.");
            editTextpassword.setError("Password contains atleast 6 characters.");
            editTextpassword.requestFocus();
            return false;

        }
        return true;
    }

    // login function for user by email
    private void login(String email, String pwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    // Getting the instance of current user
                    FirebaseUser user = auth.getCurrentUser();

                    if (user.isEmailVerified()) {

                        // fetching unique user id by firebase auth
                        String userId = user.getUid();

                        // fetching user name by uid from realtime data
                        FirebaseDatabase.getInstance().getReference("Registered Users").child(userId).child("name").get()
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String username = task.getResult().getValue(String.class);
                                            helperClass.customToast(loginActivity.this,"Logged in successfully!!\n Welcome " + username + " \uD83D\uDE03");
                                            startNewActivity(loginActivity.this,homeActivity.class);
                                        } else {
                                            helperClass.customToast(loginActivity.this,"Something went wrong \uD83E\uDD7A");
                                        }
                                    }
                                });
                    } else {
                        helperClass.customToast(loginActivity.this,"Email is not verified.");
                        auth.signOut();
                        progressBar.setVisibility(View.INVISIBLE);
                        startNewActivity(loginActivity.this, verifyEmailActivity.class);
                    }
                } else {
                    Exception exception = task.getException();
                    if(exception != null){
                        handleFirebaseAuthException(exception);
                    }
                }
            }
        });
    }

    // start new activity launch
    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    // alert dialog box if email is not verified
    private void showAlertbox(){
        // alterbox setup
        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
        builder.setTitle("Unverified Email");
        builder.setMessage("Your Email is not verified. \nPress continue to verify");

        // opening email for verification
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // create alter dialog
        AlertDialog alertDialog = builder.create();

        // show alter dialog
        alertDialog.show();

    }

    // exception handling function
    private void handleFirebaseAuthException(Exception e) {
        progressBar.setVisibility(View.INVISIBLE);
        // Log the exception for debugging purposes
        Log.e("FirebaseAuthError", "Error occurred during login: ", e);

        if (e instanceof FirebaseAuthInvalidUserException) {
            // Handle invalid user and non-registered email together
            helperClass.customToast(loginActivity.this,"This email is not registered or the user doesn't exist. Please sign up.");
        }
        else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // Handle incorrect password or email
            helperClass.customToast(loginActivity.this,"Invalid credentials. Please check your email and password.");
        }
        else if (e instanceof FirebaseAuthUserCollisionException) {
            helperClass.customToast(loginActivity.this,"This email is already registered. Try logging in.");
        }
        else if (e instanceof FirebaseNetworkException) {
            // Handle network error
            helperClass.customToast(loginActivity.this,"Network error. Please check your connection.");
        }
        else {
            // General error
            helperClass.customToast(loginActivity.this,"Something went wrong. Please try again later.");
        }
    }



    // check if user is already logged in if not then start the activity

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            startNewActivity(loginActivity.this, homeActivity.class);
        }
        else {
            // customToast("Please login to continue.");
        }
    }
}