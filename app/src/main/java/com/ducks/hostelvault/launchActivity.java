package com.ducks.hostelvault;

import static com.ducks.hostelvault.R.layout.activity_launch;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class launchActivity extends AppCompatActivity {
    Button launchLogin, launchRegister;
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
        setContentView(activity_launch);

        // Initialize helper classes
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();
        // Finding views
        launchLogin = findViewById(R.id.launchLogin);
        launchRegister = findViewById(R.id.launchRegister);

        // Set up button click listeners
        setupListeners();
    }

    // Set up button click listeners
    private void setupListeners() {
        // Redirect to login page
        launchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperClass.startNewActivity(launchActivity.this, loginActivity.class);
            }
        });

        // Redirect to signup page
        launchRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperClass.startNewActivity(launchActivity.this, signupActivity.class);
            }
        });
    }
    // Check if user is logged in and redirect to home activity
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseHelper.getCurrentUser() != null) {
            if(firebaseHelper.getCurrentUser().isEmailVerified())
                helperClass.startFreshActivity(this, homeActivity.class);
        }
    }
}
