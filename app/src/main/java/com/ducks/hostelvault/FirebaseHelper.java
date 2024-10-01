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
    private String roomId;

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

    // Define a callback interface
    public interface RoomSizeCallback {
        void onCallback(Integer roomSize); // Callback to handle room size
    }

    // Method to check if a hostel ID and room number exist
    public void checkHostelAndRoom(String hostelerId,String hostelId, String roomNum, RoomSizeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the hostel document by ID
        db.collection("hostels").document(hostelId) // Use the provided hostel ID
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Hostel exists
                                Map<String, Object> rooms = (Map<String, Object>) document.get("rooms");
                                if (rooms != null && rooms.containsKey(roomNum)) {
                                    // Room number exists
                                    Map<String, Object> roomInfo = (Map<String, Object>) rooms.get(roomNum);
                                    if (roomInfo != null) {
                                        // Return the size of the room map
                                        roomId = roomNum + "." + (roomInfo.size() + 1);
                                        roomInfo.put(roomId ,hostelerId);

                                        db.collection("hostels").document(hostelId).update("rooms", rooms)
                                                .addOnSuccessListener(aVoid -> callback.onCallback(0)) // Successfully added, return 0
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firestore Update", "Error updating hostel rooms", e);
                                                    callback.onCallback(-1); // Indicate an error occurred
                                                });
                                        callback.onCallback(roomInfo.size());
                                    } else {
                                        // Room information is not available
                                        callback.onCallback(0);
                                    }
                                } else {
                                    // Room number does not exist
                                    Map<String,Object> newRoomNumber = new HashMap<>();
                                    roomId =  roomNum + ".1";
                                    newRoomNumber.put(roomNum + ".1",hostelerId);
                                    rooms.put(roomNum,newRoomNumber);
                                    db.collection("hostels").document(hostelId).update("rooms", rooms)
                                            .addOnSuccessListener(aVoid -> callback.onCallback(0)) // Successfully added, return 0
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore Update", "Error updating hostel rooms", e);
                                                callback.onCallback(-1); // Indicate an error occurred
                                            });
                                    callback.onCallback(0);
                                }
                            } else {
                                // Hostel does not exist
                                Log.d("Hostel Check", "No such hostel document.");
                                callback.onCallback(0);
                            }
                        } else {
                            // Error getting document
                            Log.w("Firestore Data", "Error getting hostel document.", task.getException());
                            callback.onCallback(null); // Handle error case
                        }
                    }
                });
    }


    // Store Hosteler data
    public void storeHostelerData(String hostelerId, String name, String email, String mobile,String hostelId, String roomNum) {
        checkHostelAndRoom(hostelerId,hostelId, roomNum, new RoomSizeCallback() {
            @Override
            public void onCallback(Integer roomSize) {
                if (roomSize != null) {

                    Map<String, Object> hostelerData = new HashMap<>();
                    hostelerData.put("name", name);
                    hostelerData.put("email", email);
                    hostelerData.put("mobile",mobile);
                    hostelerData.put("hostel_id", hostelId);
                    hostelerData.put("room_id", roomId);

                    firestore.collection("hostelers").document(hostelerId).set(hostelerData)
                            .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Hosteler data successfully written!"))
                            .addOnFailureListener(e -> Log.e("FirebaseHelper", "Error writing hosteler data", e));
                    Log.d("Room Size", "Room Size: " + roomSize);

                } else {
                    Log.d("Room Size", "Error occurred while checking room size.");
                }
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
