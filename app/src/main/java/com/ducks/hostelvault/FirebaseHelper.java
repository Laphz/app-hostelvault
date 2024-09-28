package com.ducks.hostelvault;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final String TAG = "FirebaseHelper";

    public FirebaseHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
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

    // Store user data in Firestore inside the specific hostel/room
    public void storeUserDataInRoom(String hostelCode, String roomNumber, String subRoomNumber, String name, String email, String phone, OnCompleteListener<Void> onCompleteListener) {
        CollectionReference hostelsCollection = firestore.collection("hostels");

        // Query the hostel by its unique code
        hostelsCollection.whereEqualTo("hostel_code", hostelCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (!querySnapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String hostelId = document.getId();
                        // Reference to the specific room where user data should be stored
                        hostelsCollection.document(hostelId)
                                .collection("rooms")
                                .document(roomNumber)
                                .collection(subRoomNumber)
                                .document("user_info")
                                .set(constructUserData(name, email, phone))
                                .addOnCompleteListener(onCompleteListener)
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to store user data in room: ", e));
                    }
                } else {
                    Log.e(TAG, "Hostel with the provided code not found.");
                }
            } else {
                Log.e(TAG, "Failed to query hostel by code: ", task.getException());
            }
        });
    }

    // Helper function to construct user data map
    private Map<String, Object> constructUserData(String name, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        return userData;
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
