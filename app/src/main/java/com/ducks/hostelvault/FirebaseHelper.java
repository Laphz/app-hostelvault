package com.ducks.hostelvault;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirebaseHelper {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final String TAG = "FirebaseHelper";

    public FirebaseHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Custom listener interface for hostel ID fetching
    public interface OnHostelCodeFetchedListener {
        void onHostelCodeFetched(String hostelId);
    }

    // Sign up a new user
    public void signUpUser(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    // Get the current user
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    // Send email verification to the user
    public void sendEmailVerification(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email verification sent.");
                        } else {
                            Log.e(TAG, "Failed to send email verification.", task.getException());
                        }
                    });
        }
    }

    // Store Hosteler data
    public void storeHostelerData(String hostelerId, String name, String email, String hostelId, String roomId) {
        Map<String, Object> hostelerData = new HashMap<>();
        hostelerData.put("name", name);
        hostelerData.put("email", email);
        hostelerData.put("hostel_id", hostelId);
        hostelerData.put("room_id", roomId);

        firestore.collection("hostelers").document(hostelerId).set(hostelerData)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Hosteler data successfully written!"))
                .addOnFailureListener(e -> Log.e("FirebaseHelper", "Error writing hosteler data", e));
    }

    // Sign out the user
    public void signOutUser() {
        firebaseAuth.signOut();
    }

    // Login the user
    public void signInUser(String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    // Reset the user's password
    public void resetPassword(String email, OnCompleteListener<Void> onCompleteListener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send password reset email: ", e));
    }
}
