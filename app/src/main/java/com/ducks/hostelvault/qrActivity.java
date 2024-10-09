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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class qrActivity extends AppCompatActivity {

    private CodeScanner scanner;
    private static final int CAMERA_REQUEST_CODE = 101;
    private DatabaseReference ref;
    HelperClass helperClass;
    FirebaseHelper firebaseHelper;
    User user;

    // Constants for database paths
    private static final String ADMIN_DB_PATH = "qrs";
    private static final String STATUS_DB_PATH = "logs";

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
        getHostelIdAndCheckQr(userUid, scannedValue);
    }

    private void getHostelIdAndCheckQr(String userUid, String scannedValue) {
        firebaseHelper.getHostelId(userUid, hostelId -> {
            if (hostelId == null) {
                helperClass.customToast(qrActivity.this, "Hostel not found login again");
                helperClass.startFreshActivity(qrActivity.this, loginActivity.class);
            } else {
                checkQrCode(scannedValue, hostelId, userUid);
            }
        });
    }

    private void checkQrCode(String scannedValue, String hostelId, String userUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(hostelId);
        ref.orderByValue().equalTo(scannedValue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    retrieveUserNameAndUpdateStatus(userUid, hostelId);
                } else {
                    helperClass.customToast(qrActivity.this, "QR not found!");
                    helperClass.startFreshActivity(qrActivity.this, homeActivity.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                logError("Database error: " + error.getMessage());
                helperClass.customToast(qrActivity.this, "Something went wrong. Please try again.");
            }
        });
    }
    private void retrieveUserNameAndUpdateStatus(String userUid, String hostelId) {
        firebaseHelper.getUserName(userUid, userName -> {
            if (userName != null) {
                updateStatus(userName, userUid, hostelId);
            } else {
                helperClass.customToast(qrActivity.this, "User not found.");
            }
        });
    }

    // Method to update status in the database
    private void updateStatus(String userName, String userUid, String hostelId) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference( hostelId + "/" +STATUS_DB_PATH + "/" + userUid);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date());

        firebaseHelper.checkUserInLogs(userUid, new FirebaseHelper.CheckUserCallback() {
            @Override
            public void onResult(boolean exists) {
                if (!exists) {
                    // User does not exist, show the reason dialog
                    showReasonDialog(userName, userUid, statusRef, formattedTimestamp);
                } else {
                    // User exists, delete their status entry
                    firebaseHelper.fetchAndStoreData(hostelId,userUid,null,formattedTimestamp,null, statusRef, userName,qrActivity.this);

                }
            }
        });
    }

    private void showReasonDialog(String userName, String userUid, DatabaseReference statusRef, String formattedTimestamp) {
        Dialog dialog = new Dialog(qrActivity.this);
        dialog.setContentView(R.layout.reason_dialog);
        dialog.setCancelable(false);

        EditText reason = dialog.findViewById(R.id.reason);
        Button submitBtn = dialog.findViewById(R.id.reasonSubmit);

        submitBtn.setOnClickListener(v -> {
            String reasonText = reason.getText().toString().trim();

            if (!TextUtils.isEmpty(reasonText)) {
                dialog.dismiss(); // Close the dialog when the reason is provided

                // Prepare data to update in the database
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("where", reasonText); // Update the status
                updateData.put("check_out", formattedTimestamp); // Add formatted timestamp

                // Update the status in the database
                updateStatusInDatabase(statusRef, userName, updateData);
            } else {
                reason.setError("Reason Required"); // Display error message when reason is empty
            }
        });

        dialog.show();
    }

    private void updateStatusInDatabase(DatabaseReference statusRef, String userName, Map<String, Object> updateData) {
        statusRef.updateChildren(updateData).addOnCompleteListener(updateTask -> {
            if (updateTask.isSuccessful()) {
                helperClass.customToast(qrActivity.this, "User: " + userName + " is now OUT!");
                helperClass.startFreshActivity(qrActivity.this, homeActivity.class);
            } else {
                logError("Failed to update status: " + updateTask.getException());
                helperClass.customToast(qrActivity.this, "Failed to change status. Please try again.");
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