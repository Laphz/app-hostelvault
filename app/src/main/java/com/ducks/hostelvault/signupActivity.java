package com.ducks.hostelvault;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signupActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone, editTextRoomNum, editTextPassword, editTextConfirmPassword;
    private Spinner editTextHostelName;
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
        editTextPhone = findViewById(R.id.et_phn);
        editTextRoomNum = findViewById(R.id.et_roomnum);
        editTextHostelName = findViewById(R.id.et_hostelname);

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
        String phoneNum = editTextPhone.getText().toString();
        String roomNum = editTextRoomNum.getText().toString();
        String hostelName = editTextHostelName.getSelectedItem().toString();

        String hostelcode = hostelName.split("-")[0];

        // Define sub-room number, you may want to create this based on your logic
        String subRoomNumber = "1"; // Change this based on your app logic

        EditText[] inputFields = {editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone, editTextRoomNum};

        if (!inputValidator.validateInputs(email, password, confirmPassword, phoneNum, roomNum, hostelName, inputFields)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.signUpUser(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                    user user = new user(name, email, phoneNum);

                    // Store user data in the specified room and sub-room
                    firebaseHelper.storeUserDataInRoom(hostelcode, roomNum, subRoomNumber, user, task1 -> {
                        if (task1.isSuccessful()) {
                            firebaseHelper.sendEmailVerification(firebaseUser);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(signupActivity.this, "Sign-up successful! Verification email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            helperClass.showErrorToast(signupActivity.this, task1.getException().getMessage());
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    helperClass.showErrorToast(signupActivity.this, task.getException().getMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
