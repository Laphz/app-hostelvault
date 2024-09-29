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

    // Fetch all hostels from FireStore
    public void fetchHostel(OnCompleteListener<List<String>> listener) {
        List<String> hostellist = new ArrayList<>();
        firestore.collection("hostels")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot docx : task.getResult()) {
                            String hostelCode = docx.getString("hostel_code");
                            String hostelName = docx.getString("name");
                            hostellist.add(hostelCode + "-" + hostelName);
                        }
                        listener.onComplete((Task<List<String>>) hostellist);
                    } else {
                        listener.onComplete(null);
                    }
                });
    }

    // Fetch hostel ID by hostel code
    public void fetchHostelIdByCode(String hostelCode, final OnHostelCodeFetchedListener listener) {
        firestore.collection("hostels")
                .whereEqualTo("hostel_code", hostelCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String hostelId = task.getResult().getDocuments().get(0).getId();
                        listener.onHostelCodeFetched(hostelId); // Return the hostelId directly
                    } else {
                        listener.onHostelCodeFetched(null); // Return null if no hostelId is found
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch hostel ID: ", e));
    }

    // store the data
    public void storeData(String name, String email, String mobile, String hostel_id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference hostelRef = db.collection("hostels");
        hostelRef.whereEqualTo("hoste_id", hostel_id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() & !task.getResult().isEmpty()) {
                        DocumentSnapshot hostelSnapshot = task.getResult().getDocuments().get(0);
                        String hostelId = hostelSnapshot.getId();
                        Map<String, Objects> rooms = (Map<String, Objects>) hostelSnapshot.get("rooms");
                    }
                });
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
