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

    private EditText editTextName, editTextEmail, editTextPhone, editTextPassword, editTextConfirmPassword,editTextHostelId,editTextRoomNum;
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
//        progressBar = findViewById(R.id.progressbar);
        editTextPhone = findViewById(R.id.et_phn);
        editTextHostelId = findViewById(R.id.et_hostel_id);
        editTextRoomNum = findViewById(R.id.et_room_num);

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
        String phoneNum = editTextPhone.getText().toString().trim();
        String hostelId = editTextHostelId.getText().toString().trim();
        String roomNum = editTextRoomNum.getText().toString().trim();





        EditText[] inputFields = {editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone};

        if (!inputValidator.validateInputs(email, password, confirmPassword, phoneNum, inputFields)) {
            return;
        }
//        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.signUpUser(email, password, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                    String uid = firebaseUser.getUid();
                    user user = new user(name, email, phoneNum);

                    // Store user data in the specified room and sub-room
                    firebaseHelper.storeHostelerData(uid,name,email,phoneNum,hostelId,roomNum);


                } else {
                    helperClass.showErrorToast(signupActivity.this, task.getException().getMessage());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
