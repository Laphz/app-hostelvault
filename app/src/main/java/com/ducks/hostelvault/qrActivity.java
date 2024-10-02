
package com.ducks.hostelvault;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
    private SharedPreferences sharedPreferences;
    private static final int CAMERA_REQUEST_CODE = 101;
    private DatabaseReference ref;

    // Constants for database paths
    private static final String ADMIN_DB_PATH = "Admin";
    private static final String REGISTERED_USERS_DB_PATH = "Registered Users";
    private static final String STATUS_DB_PATH = "Status";

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

        // Initializing views
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

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
                    matchQr(value); // Call function to match QR with Firebase database
                });
            }
        });
    }

    // Method to check if QR matches the database and update status
    public void matchQr(String scannedValue) {
        ref = FirebaseDatabase.getInstance().getReference(ADMIN_DB_PATH);
        Query checkQr = ref.orderByChild("qr").equalTo(scannedValue);
        checkQr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = sharedPreferences.getString("userName", "Unknown User");
                    if (userName == null || userName.equals("Unknown User")) {
                        changeStatus();
                        return;
                    }
                } else {
                    customToast("No QR found in the database.");
                    startNewActivity(qrActivity.this, homeActivity.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                logError("Database error: " + error.getMessage());
                customToast("Database error. Please try again.");
            }
        });
    }

    // Changing status in the database
    public void changeStatus() {
        String scannedUserUId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(REGISTERED_USERS_DB_PATH).child(scannedUserUId);

        // Fetch the user's name
        userRef.child("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    if (userName != null) {
                        updateStatus(userName);
                    } else {
                        customToast("User details not found.");
                    }
                } else {
                    customToast("User data not found.");
                }
            } else {
                logError("Error getting user details: " + task.getException());
                customToast("Failed to retrieve user details.");
            }
        });
    }

    // Method to update status in the database
    private void updateStatus(String userName) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference(STATUS_DB_PATH).child(userName);

        // Fetch the current status
        statusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot statusSnapshot = task.getResult();
                String currentStatus = statusSnapshot.getValue(String.class);
                String newStatus = (currentStatus == null || currentStatus.equals("OUT")) ? "IN" : "OUT";

                // Update the status in the database
                statusRef.setValue(newStatus).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        customToast("User : " + userName + " is " + newStatus + "!!");
                        startNewActivity(qrActivity.this, homeActivity.class);
                    } else {
                        logError("Failed to update status: " + updateTask.getException());
                        customToast("Failed to change status. Please try again.");
                    }
                });
            } else {
                logError("Error getting status: " + task.getException());
                customToast("Failed to retrieve current status.");
            }
        });
    }

    // Start a new activity
    public void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        startActivity(intent);
    }

    // Check if camera permission is granted
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
                customToast("Camera permission is required to use this app.");
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

    // Custom toast method
    private void customToast(String message) {
        Toast.makeText(qrActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}