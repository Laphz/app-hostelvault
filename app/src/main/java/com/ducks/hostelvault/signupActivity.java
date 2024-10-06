package com.ducks.hostelvault;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class signupActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone, editTextPassword, editTextConfirmPassword, editTextHostelId, editTextRoomNum;
    private FirebaseHelper firebaseHelper;
    private InputValidator inputValidator;
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
        setContentView(R.layout.activity_signup);


        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_passwrd);
        editTextConfirmPassword = findViewById(R.id.et_confpasswrd);
        editTextPhone = findViewById(R.id.et_phn);
        editTextHostelId = findViewById(R.id.et_hostel_id);
        editTextRoomNum = findViewById(R.id.et_room_num);

        firebaseHelper = new FirebaseHelper();
        inputValidator = new InputValidator();
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
        String phoneNum = editTextPhone.getText().toString().trim();
        String hostelId = editTextHostelId.getText().toString().trim();
        String roomNum = editTextRoomNum.getText().toString().trim();

        EditText[] inputFields = {editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone, editTextHostelId, editTextRoomNum};

        // Use the InputValidator's validateInputs method with callbacks
        inputValidator.validateInputs(email, password, confirmPassword, phoneNum, hostelId, inputFields, new InputValidator.OnValidationListener() {
            @Override
            public void onValidationSuccess() {
                // Proceed with registration since inputs are valid and hostel exists
                firebaseHelper.signUpUser(email, password, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                            String uid = firebaseUser.getUid();

                            // Send email verification link
                            firebaseHelper.sendEmailVerification(firebaseUser);

                            helperClass.customToast(signupActivity.this, "Signed Up successfully! Please verify your email.");
                            // Check email verification
                            helperClass.startNewActivity(signupActivity.this, verifyEmailActivity.class);

                            // Store the data
                            firebaseHelper.storeHostelerData(uid, name, email, phoneNum, hostelId, roomNum);
                        } else {
                            helperClass.showErrorToast(signupActivity.this, task.getException().getMessage());
                        }
                    }
                });
            }

            @Override
            public void onValidationFailed() {
                // Handle validation failure
                helperClass.showErrorToast(signupActivity.this, "Validation failed. Please check the input fields.");
            }
        });
    }
}
