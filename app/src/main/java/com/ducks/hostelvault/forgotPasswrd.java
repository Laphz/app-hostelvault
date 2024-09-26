package com.ducks.hostelvault;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPasswrd extends AppCompatActivity {

    Button resetbtn;
    EditText email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwrd);
        resetbtn = findViewById(R.id.resetbtn);
        email = findViewById(R.id.resetemail);

        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resetEmail = email.getText().toString();
                if(checkField(resetEmail)){
                    resetPwd(resetEmail);
                }

            }
        });

    }

    // reset pwd
    private void resetPwd(String email){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    customToast("Reset Link is sent on your email");
                    startNewActivity(forgotPasswrd.this, loginActivity.class);
                }
                else{
                    customToast("Something went wrong!");
                }
            }
        });
    }
    // checking input field
    private boolean checkField(String email){

        if(TextUtils.isEmpty(email)){
            customToast("Please enter registered email.");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            customToast("Please enter a valid email");
            return false;
        }
        return true;
    }

    // custom toast function
    private void customToast(String message) {
        Toast.makeText(forgotPasswrd.this, message, Toast.LENGTH_SHORT).show();
    }

    // start new activity launch
    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}