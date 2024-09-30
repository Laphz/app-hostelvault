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

    // store the data
    public void storeData(String uid, String name, String email, String mobileNumber, String hostelCode) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore reference to the 'hostels' collection to get rooms by hostel code
        CollectionReference hostelsRef = db.collection("hostels");

        // Query to find the hostel by its code
        hostelsRef.whereEqualTo("hostel_id", hostelCode).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Assume only one hostel matches this code
                        DocumentSnapshot hostelSnapshot = task.getResult().getDocuments().get(0);
                        String hostelId = hostelSnapshot.getId();

                        // Get the rooms map from the hostel document
                        Map<String, Object> rooms = (Map<String, Object>) hostelSnapshot.get("rooms");

                        if (rooms != null && !rooms.isEmpty()) {
                            // Find the first available room or use a specific room number logic
                            String roomid = null;

                            // Iterate through the rooms map
                            for (String roomNumber : rooms.keySet()) {
                                Map<String, Object> subRooms = (Map<String, Object>) rooms.get(roomNumber);

                                // Check if there is available sub-room in the room map
                                if (subRooms != null && subRooms.size() < 3) { // Assuming 3 sub-rooms per room
                                    roomid = roomNumber;
                                    break;
                                }
                            }

                            if (roomid != null) {
                                // Prepare user data to store in Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("mobileNumber", mobileNumber);
                                userData.put("hostel_code", hostelCode);
                                userData.put("roomid", roomid);  // Store the room ID in Firestore

                                // Firestore reference to the 'users' collection
                                DocumentReference userRef = db.collection("users").document(uid);

                                // Store user data in Firestore
                                userRef.set(userData)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data stored successfully."))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error storing user data: ", e));
                            } else {
                                Log.e("Firestore", "No available room found.");
                            }
                        } else {
                            Log.e("Firestore", "No rooms available in the hostel.");
                        }
                    } else {
                        Log.e("Firestore", "Hostel not found with the given hostel code.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error finding hostel: ", e));
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
