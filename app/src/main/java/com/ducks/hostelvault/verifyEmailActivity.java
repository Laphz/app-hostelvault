package com.ducks.hostelvault;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class verifyEmailActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private HelperClass helperClass;
    private FirebaseHelper mAuth;
    private ProgressBar progressBar;
    private Button verifyBtn, resendEmailBtn;
    private Handler handler;
    private final int EMAIL_CHECK_INTERVAL = 5000; // 5 seconds

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
        setContentView(R.layout.activity_verify_email);

        helperClass = new HelperClass();
        mAuth = new FirebaseHelper();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No user is currently signed in.", Toast.LENGTH_SHORT).show();
            redirectToHomeActivity();
            return;
        }

        progressBar = findViewById(R.id.progressBar);
        verifyBtn = findViewById(R.id.verifybtn);
//        resendEmailBtn = findViewById(R.id.resendEmailBtn);

        // Set onClickListener to check email verification
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailVerificationAndOpenEmail();
            }
        });

        // Set onClickListener to resend verification email
//        resendEmailBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resendVerificationEmail();
//            }
//        });

        // Set up the handler to check email verification periodically
        handler = new Handler();
        startEmailVerificationCheck();
    }

    // Start the periodic email verification check
    private void startEmailVerificationCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkEmailVerification();
                handler.postDelayed(this, EMAIL_CHECK_INTERVAL);
            }
        }, EMAIL_CHECK_INTERVAL);
    }

    // Check if the user's email is verified periodically
    private void checkEmailVerification() {
        currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(verifyEmailActivity.this, "Email verified!", Toast.LENGTH_SHORT).show();
                        redirectToHomeActivity();
                    }
                } else {
                    // Handle failure to reload user
                    Toast.makeText(verifyEmailActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Check email verification when user clicks verify button
    private void checkEmailVerificationAndOpenEmail() {
        currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(verifyEmailActivity.this, "Email verified!", Toast.LENGTH_SHORT).show();
                        redirectToHomeActivity();
                    } else {
                        Toast.makeText(verifyEmailActivity.this, "Email not yet verified. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        helperClass.openEmailApp(verifyEmailActivity.this);
                    }
                } else {
                    Toast.makeText(verifyEmailActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Resend verification email
//    private void resendVerificationEmail() {
//        progressBar.setVisibility(View.VISIBLE);
//        verifyBtn.setEnabled(false);
//        resendEmailBtn.setEnabled(false);
//
//        currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                progressBar.setVisibility(View.GONE);
//                verifyBtn.setEnabled(true);
//                resendEmailBtn.setEnabled(true);
//
//                if (task.isSuccessful()) {
//                    Toast.makeText(verifyEmailActivity.this, "Verification email resent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(verifyEmailActivity.this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    // Redirect to the login activity
    private void redirectToHomeActivity() {
        handler.removeCallbacksAndMessages(null); // Stop email check
        helperClass.startFreshActivity(verifyEmailActivity.this, homeActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
