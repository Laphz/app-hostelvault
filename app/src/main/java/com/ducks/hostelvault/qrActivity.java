package com.ducks.hostelvault;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

public class qrActivity extends AppCompatActivity {

    private CodeScanner scanner;
    private static final int CAMERA_REQUEST_CODE = 101;
    private DatabaseReference ref;
    HelperClass helperClass;
    FirebaseHelper firebaseHelper;
    User user;

    // Constants for database paths
    private static final String ADMIN_DB_PATH = "qrs";
    private static final String STATUS_DB_PATH = "status";

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
        setContentView(R.layout.activity_qr);

        // helper class
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();



        checkCameraPermission();
        setupCodeScanner();
    }

    // Setting up QR code scanner
    public void setupCodeScanner() {
        scanner = new CodeScanner(this, findViewById(R.id.scanner_view));
        scanner.setCamera(CodeScanner.CAMERA_BACK);
        scanner.setFormats(CodeScanner.ALL_FORMATS);
        scanner.setAutoFocusMode(AutoFocusMode.SAFE);
        scanner.setScanMode(ScanMode.CONTINUOUS);
        scanner.setAutoFocusEnabled(true);
        scanner.setFlashEnabled(false);

        // Callback for when QR code is successfully decoded
        scanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                String value = result.getText();
                runOnUiThread(() -> {
                    scanner.stopPreview();
                    matchQr(value);// Call function to match QR with Firebase database
//                    helperClass.startFreshActivity(qrActivity.this, homeActivity.class);
                });
            }
        });
    }
    // Method to check if QR matches the database and update status
    public void matchQr(String scannedValue) {
        String userUid = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getHostelId(userUid, new FirebaseHelper.hostelIdCallback() {
            @Override
            public void onCallback(String hostelId) {
                if (hostelId != null) {
                    // Once the hostelId is retrieved, proceed with the query
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ADMIN_DB_PATH);
                    Query checkQr = ref.orderByValue().equalTo(scannedValue);

                    checkQr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Handle user status change logic
                                firebaseHelper.getUserName(userUid, new FirebaseHelper.userNameCallback() {
                                    @Override
                                    public void onCallback(String userName) {
                                        if (userName != null) {

                                            // Optionally call a function like changeStatusInDatabase() here
                                            updateStatus(userName,hostelId);
                                        }
                                        else {
                                            helperClass.customToast(qrActivity.this,"User not found.");
                                        }

                                    }
                                });
                            }
                            else {
                                helperClass.customToast(qrActivity.this, "No QR found.");
                                helperClass.startNewActivity(qrActivity.this, homeActivity.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            logError("Database error: " + error.getMessage());
                            helperClass.customToast(qrActivity.this, "Database error. Please try again.");
                        }
                    });
                } else {
                    // Handle the case where hostelId is null
                    helperClass.customToast(qrActivity.this, "Hostel ID not found. Please try again.");
                    helperClass.startNewActivity(qrActivity.this,homeActivity.class);
                }
            }
        });
    }

    // Method to update status in the database
    private void updateStatus(String userName,String hostelId) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference(STATUS_DB_PATH + "/" +hostelId).child(userName);

        // Check if the user already exists in the status node
        statusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot statusSnapshot = task.getResult();

                if (statusSnapshot.exists()) {
                    // User exists, so update the status
                    String currentStatus = statusSnapshot.getValue(String.class);
                    String newStatus = (currentStatus == null || currentStatus.equals("IN")) ? "OUT" : "IN";

                    if (newStatus.equals("OUT")){

                        Dialog dialog = new Dialog(qrActivity.this);
                        dialog.setContentView(R.layout.reason_dialog);
                        dialog.setCancelable(false);
                        EditText reason = dialog.findViewById(R.id.reason);
                        Button sumbitBtn = dialog.findViewById(R.id.reasonSubmit);
                        sumbitBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!TextUtils.isEmpty(reason.getText().toString().trim())){
                                    dialog.dismiss();
                                    statusRef.setValue(newStatus).addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            helperClass.customToast(qrActivity.this, "User: " + userName + " is now " + newStatus + "!");
                                            helperClass.startNewActivity(qrActivity.this, homeActivity.class);
                                        } else {
                                            logError("Failed to update status: " + updateTask.getException());
                                            helperClass.customToast(qrActivity.this, "Failed to change status. Please try again.");
                                        }
                                    });
                                }
                                else {
                                    reason.setError("Reason Required");
                                }
                            }
                        });

                        dialog.show();
                    }
                    else {
                        statusRef.setValue(newStatus).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                helperClass.customToast(qrActivity.this, "User: " + userName + " is now " + newStatus + "!");
                                helperClass.startNewActivity(qrActivity.this, homeActivity.class);
                            } else {
                                logError("Failed to update status: " + updateTask.getException());
                                helperClass.customToast(qrActivity.this, "Failed to change status. Please try again.");
                            }
                        });
                    }

                    // Update the status in the database

                } else {
                    // User does not exist, create a new entry with default status "OUT"
                    statusRef.setValue("OUT").addOnCompleteListener(createTask -> {
                        if (createTask.isSuccessful()) {
                            helperClass.customToast(qrActivity.this, userName + " is now OUT!");
                            helperClass.startNewActivity(qrActivity.this, homeActivity.class);
                        } else {
                            logError("Failed to create new status entry: " + createTask.getException());
                            helperClass.customToast(qrActivity.this, "Failed to set status for new user. Please try again.");
                        }
                    });
                }
            } else {
                logError("Error checking user status: " + task.getException());
                helperClass.customToast(qrActivity.this, "Failed to retrieve user status.");
            }
        });
    }

    public void checkCameraPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest();
        }
    }

    // Request for camera permission
    public void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupCodeScanner();
            } else {
                helperClass.customToast(qrActivity.this,"Camera permission is required to use this app.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner != null) {
            scanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if (scanner != null) {
            scanner.releaseResources();
        }
        super.onPause();
    }

    // Log error and show message
    private void logError(String message) {
        Log.e("QRScanner", message);
    }
}