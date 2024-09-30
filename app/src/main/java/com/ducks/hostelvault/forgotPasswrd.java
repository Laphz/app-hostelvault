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
    FirebaseHelper firebaseHelper;
    HelperClass helperClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwrd);
        resetbtn = findViewById(R.id.resetbtn);
        email = findViewById(R.id.resetemail);
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();

        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkField(email.getText().toString())){
                    firebaseHelper.resetPassword(email.getText().toString(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                helperClass.customToast(forgotPasswrd.this,"Reset Link is sent on your email");
                                helperClass.startFreshActivity(forgotPasswrd.this, loginActivity.class);
                            }
                            else{
                                helperClass.customToast(forgotPasswrd.this,"Something went wrong!");
                            }
                        }
                    });
                }
            }
        });

    }

    // checking input field
    private boolean checkField(String email){

        if(TextUtils.isEmpty(email)){
            helperClass.customToast(forgotPasswrd.this,"Please enter registered email.");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            helperClass.customToast(forgotPasswrd.this,"Please enter a valid email");
            return false;
        }
        return true;
    }
}