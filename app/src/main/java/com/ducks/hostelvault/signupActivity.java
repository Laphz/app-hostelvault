package com.ducks.hostelvault;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signupActivity extends AppCompatActivity {

    // variable
    EditText editTextname,editTextemail,editTextpasswrd,editTextConfPasswrd;
    Button signupbtn;
    TextView redirectToLogin;
    EditText[] editTextArray;
    ProgressBar progressBar;

    // start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // input fields views
        editTextname = findViewById(R.id.et_name);
        editTextemail = findViewById(R.id.et_email);
        editTextConfPasswrd = findViewById(R.id.et_confpasswrd);
        editTextpasswrd = findViewById(R.id.et_passwrd);
        signupbtn = findViewById(R.id.signup_btn);
        redirectToLogin = findViewById(R.id.redirectToLogin);
        progressBar = findViewById(R.id.progressbar);
        signupbtn.setBackgroundResource(R.drawable.btn_bg);

        signupLaunch();
        redirectToLogin();

    }

    // signup btn functionality
    private void signupLaunch() {
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Regret 🙂 getting the text from edit text
                 String name = editTextname.getText().toString();
                 String email = editTextemail.getText().toString();
                 String passwrd = editTextpasswrd.getText().toString();
                 String conPasswrd = editTextConfPasswrd.getText().toString();

                editTextArray  = new EditText[]{editTextname, editTextemail, editTextpasswrd, editTextConfPasswrd};
             String[] array = new String[]{editTextname.getText().toString(), editTextemail.getText().toString(), editTextpasswrd.getText().toString(), editTextConfPasswrd.getText().toString()};
             if(checkInput(editTextArray)){// checking empty field
                 signupUser(name,email,passwrd);
             }
            }
        });
    }

    // redirect to signin page
    private void redirectToLogin(){
        redirectToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewActivity(signupActivity.this, loginActivity.class);
            }
        });
    }

    // function for checking empty fields and valid input
    private Boolean checkInput(EditText[] editTextArray){
        // checking for empty fields
        for(EditText value: editTextArray ) {
            if (TextUtils.isEmpty(value.getText().toString())) {
                customToast("Field is required!");
                value.setError("Field is required!");
                value.requestFocus();
                return false;
            }
        }

        // email validation
        if(!Patterns.EMAIL_ADDRESS.matcher(editTextemail.getText().toString()).matches()){
            customToast("Enter valid Email.");
            editTextemail.setError("Enter valid Email.");
            editTextemail.requestFocus();
            return false;
        }

        // passwrd length must be atleast 6
        if (editTextpasswrd.getText().toString().length() < 6) {
            customToast("Password must be at least 6 characters.");
            editTextpasswrd.setError("Password must be at least 6 characters.");
            editTextpasswrd.requestFocus();
            return false;
        }

        // passwrd and confirm passwrd should match
        if (!(editTextpasswrd.getText().toString().equals(editTextConfPasswrd.getText().toString()))) {
            customToast("Password does not match.");
            editTextConfPasswrd.setError("Password does not match.");
            editTextConfPasswrd.requestFocus();
            return false;
        }
        return true;
    }

    // signup email authentication
    private void signupUser(String name, String email, String passwrd ){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,passwrd).addOnCompleteListener(signupActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            customToast("Register Successfully!");
                            // progress bar
                            progressBar.setVisibility(View.VISIBLE);

                            // storing current user
                            FirebaseUser user = auth.getCurrentUser();

                            // storing data in realtime database
                            storeData(name,email,auth,user);
                        }

                        // expection handling
                        else{
                            // progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthWeakPasswordException e){
                                editTextpasswrd.setError("Your password is weak.");
                                editTextpasswrd.requestFocus();

                            }
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextemail.setError("Already Registered with this email.");
                                editTextemail.requestFocus();
                            }
                            catch (FirebaseAuthUserCollisionException e){
                                editTextemail.setError("Already Registered with this email.");
                                editTextemail.requestFocus();
                            }
                            catch (FirebaseAuthEmailException e){
                                editTextemail.setError("Invalid Email!");
                            }
                            catch (Exception e){
                                customToast(e.getMessage());
                            }

                        }
                    }
                });
    }

    // storing data in realtime database
    private void storeData(String name , String email, FirebaseAuth auth, FirebaseUser user){
        readWriteUserDetails readWriteUserDetails = new readWriteUserDetails(name,email);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users");
        ref.child(auth.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    auth.signOut();

                    // verifying email
                    user.sendEmailVerification();
                    customToast("Registered successfully! Welcome \n" + name + " !\uD83D\uDE03");


                    // starting  home activity
                    startNewActivity(signupActivity.this, loginActivity.class);
                }
                else{
                    customToast("Something went wrong, you fucked up \n" + name + " !\uD83E\uDD7A");
                }

            }
        });

    }

    // start new activity
    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // custom toast function
    private void customToast(String message) {
        Toast.makeText(signupActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}