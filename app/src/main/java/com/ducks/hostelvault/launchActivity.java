package com.ducks.hostelvault;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class launchActivity extends AppCompatActivity {
    Button launchLogin, lauchRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // finding views
        launchLogin = findViewById(R.id.launchLogin);
        lauchRegister = findViewById(R.id.launchRegister);

        // redirect to login page
        launchLogin();

        // redirect to register
        lauchRegister();
    }

    // function to redirect on login page
    private void launchLogin(){
        launchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(launchActivity.this,loginActivity.class);
            }
        });
    }

    // function to redirect on signup page
    private void lauchRegister(){
        lauchRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(launchActivity.this,signupActivity.class);
            }
        });
    }

    // start new activity launch
    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        startActivity(intent);
    }
}