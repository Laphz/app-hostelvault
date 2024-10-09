package com.ducks.hostelvault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseHelper {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final String TAG = "FirebaseHelper";
    private String roomId;
    private HelperClass helperClass = new HelperClass();


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

    public void checkHostelAndRoom(String hostelerId, String hostelId, String roomNum, RoomSizeCallback callback) {

        // Fetch the hostel by ID
        fetchHostel(firestore, hostelId, (document) -> {
            if (document != null && document.exists()) {
                Map<String, Object> rooms = (Map<String, Object>) document.get("rooms");
                if (rooms != null && rooms.containsKey(roomNum)) {
                    handleExistingRoom(firestore, hostelId, roomNum, rooms, hostelerId, callback);
                } else {
                    handleNewRoom(firestore, hostelId, roomNum, rooms, hostelerId, callback);
                }
            } else {
                // Hostel does not exist
                Log.d("Hostel Check", "No such hostel document.");
                callback.onCallback(0);
            }
        }, (e) -> {
            // Error handling
            Log.w("Firestore Data", "Error getting hostel document.", e);
            callback.onCallback(null);
        });
    }

    public void fetchHostel(FirebaseFirestore db, String hostelId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection("hostels").document(hostelId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    private void handleExistingRoom(FirebaseFirestore db, String hostelId, String roomNum, Map<String, Object> rooms, String hostelerId, RoomSizeCallback callback) {
        Map<String, Object> roomInfo = (Map<String, Object>) rooms.get(roomNum);
        if (roomInfo != null) {
            // Generate room ID and add hosteler
            roomId = roomNum + "." + (roomInfo.size() + 1);
            roomInfo.put(roomId, hostelerId);
            updateHostelRooms(db, hostelId, rooms, callback, roomInfo.size());
        } else {
            callback.onCallback(0);
        }
    }

    private void handleNewRoom(FirebaseFirestore db, String hostelId, String roomNum, Map<String, Object> rooms, String hostelerId, RoomSizeCallback callback) {
        // Create new room and add hosteler
        if (rooms == null) {
            rooms = new HashMap<>();
        }
        roomId = roomNum + ".1";
        Map<String, Object> newRoomInfo = new HashMap<>();
        newRoomInfo.put(roomId, hostelerId);
        rooms.put(roomNum, newRoomInfo);

        updateHostelRooms(db, hostelId, rooms, callback, 0);
    }

    private void updateHostelRooms(FirebaseFirestore db, String hostelId, Map<String, Object> rooms, RoomSizeCallback callback, int roomSize) {
        db.collection("hostels").document(hostelId)
                .update("rooms", rooms)
                .addOnSuccessListener(aVoid -> callback.onCallback(roomSize))
                .addOnFailureListener(e -> {
                    Log.e("Firestore Update", "Error updating hostel rooms", e);
                    callback.onCallback(-1);
                });
    }

    // Store Hosteler data
    public void storeHostelerData(String hostelerId, String name, String email, String mobile, String hostelId, String roomNum) {
        checkHostelAndRoom(hostelerId, hostelId, roomNum, new RoomSizeCallback() {
            @Override
            public void onCallback(Integer roomSize) {
                if (roomSize != null) {

                    Map<String, Object> hostelerData = new HashMap<>();
                    hostelerData.put("name", name);
                    hostelerData.put("email", email);
                    hostelerData.put("mobile", mobile);
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

    // get user name toast
    public void getUserNameToast(String userId, Context currentActivity) {
        firestore.collection("hostelers").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("name");
                                helperClass.customToast(currentActivity, "Logged in successfully!!\nWelcome " + username + " \uD83D\uDE03");
                            } else {
                                helperClass.customToast(currentActivity, "User does not exist \uD83E\uDD7A");
                            }
                        } else {
                            helperClass.customToast(currentActivity, "Something went wrong \uD83E\uDD7A");
                        }
                    }
                });
    }

    // get userName
    public void getUserName(String userId, final userNameCallback callback) {
        firestore.document("hostelers/" + userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("name");
                                callback.onCallback(username);
                            } else {
                                Log.d("FirebaseHelper", "User not found");
                                callback.onCallback(null);
                            }
                        } else {
                            Log.d("FirebaseHelper", "Error getting documents: ", task.getException());
                            callback.onCallback(null); // Handle errors
                        }
                    }
                });
    }

    // Callback interface
    public interface userNameCallback {
        void onCallback(String userName);
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

    // fetch hostel_id from uid
    public void getHostelId(String userUid, final hostelIdCallback callback) {
        firestore.document("hostelers/" + userUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String hostel_id = document.getString("hostel_id");
                    callback.onCallback(hostel_id); // Pass hostel_id to callback
                } else {
                    Log.d("FirebaseHelper", "User not found");
                    callback.onCallback(null); // Handle case where the document doesn't exist
                }
            } else {
                Log.d("FirebaseHelper", "Error getting documents: ", task.getException());
                callback.onCallback(null); // Handle errors
            }
        });
    }

    // Callback interface
    public interface hostelIdCallback {
        void onCallback(String hostelId);
    }

    // Reset the user's password
    public void resetPassword(String email, OnCompleteListener<Void> onCompleteListener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send password reset email: ", e));
    }

    // checking weather the user exists in logs/hostelId
    public void checkUserInLogs(String userUid, CheckUserCallback callback) {
        getHostelId(userUid, new hostelIdCallback() {
            @Override
            public void onCallback(String hostelId) {
                if (hostelId != null) {
                    DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference(hostelId + "/logs").child(userUid);
                    statusRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot statusSnapshot = task.getResult();
                                // Check if the user ID exists in the logs
                                if (statusSnapshot.exists()) {
                                    // User exists in logs, invoke the callback with true
                                    callback.onResult(true);
                                } else {
                                    // User does not exist, invoke the callback with false
                                    callback.onResult(false);
                                }
                            } else {
                                Log.e("FetchUserStatus", "Error getting user status: " + task.getException());
                                // Handle the error case
                                callback.onResult(false); // Assuming user does not exist in case of error
                            }
                        }
                    });
                } else {
                    // Handle case where hostelId is null
                    callback.onResult(false);
                }
            }
        });
    }

    public interface CheckUserCallback {
        void onResult(boolean exists);
    }

    // Method to check if a hostel exists
    public Task<Boolean> doesHostelExist(String hostelId) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        firestore.collection("hostels").document(hostelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        taskCompletionSource.setResult(true);  // Hostel exists
                    } else {
                        taskCompletionSource.setResult(false); // Hostel does not exist
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }


    // Method to check if the user is authenticated
    public void checkUserAuthentication(Context activity) {
        FirebaseUser user = getCurrentUser();

        if (user != null) {
            // User is logged in
            if (user.isEmailVerified()) {
                // Redirect to home activity
                helperClass.startFreshActivity(activity, homeActivity.class);
            } else {
                // Optionally handle unverified email
                Log.d("FirebaseHelper", "Email not verified for user: " + user.getEmail());
            }
        } else {
            // User not logged in, redirect to login activity
            firebaseAuth.signOut();
        }
    }

    // not working maa chuda rha bsdk
    public void checkUserState(Context currentActivity) {
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(currentActivity, launchActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    currentActivity.startActivity(intent);
                    if (currentActivity instanceof Activity) {
                        ((Activity) currentActivity).finish();
                    }
                }
            }
        });
    }


    public void fetchAndStoreData(String hostelId, String userUid, String defaultWhere, String defaultCheckIn, String defaultCheckOut, DatabaseReference statusRef, String userName, Context context) {
        // Fetch current date in correct format
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String currentDate = sdf.format(date);

        // Initialize Firebase references
        DatabaseReference realtimeRef = FirebaseDatabase.getInstance().getReference(hostelId + "/logs/" + userUid);

        realtimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Extract data from the Realtime Database
                    String fetchedWhere = snapshot.child("where").getValue(String.class);
                    String fetchedCheckOut = snapshot.child("check_out").getValue(String.class);

                    // Use provided defaults if values are missing
                    String where = fetchedWhere != null ? fetchedWhere : defaultWhere;
                    String checkOut = fetchedCheckOut != null ? fetchedCheckOut : defaultCheckOut;

                    // Create record data map
                    Map<String, Object> recordData = createRecordData(userUid, where, defaultCheckIn, checkOut,statusRef, userName,context);

                    // Store record in Firestore with the desired structure
                    storeRecordInFirestore(userUid,hostelId, currentDate, recordData, snapshot,statusRef, userName,context);
                } else {
                    Log.d("FirebaseHelper", "No entry exists at the given path in Realtime Database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseHelper", "Error fetching data from Realtime Database", error.toException());
            }
        });
    }

    private Map<String, Object> createRecordData(String userUid, String where, String checkIn, String checkOut,DatabaseReference statusRef, String userName,Context context) {
        Map<String, Object> recordData = new HashMap<>();
        Map<String, Object> currentFieldNo = new HashMap<>();

        // Add fields to the currentFieldNo map
        currentFieldNo.put("userUid", userUid);
        currentFieldNo.put("where", where);
        currentFieldNo.put("check_in", checkIn);
        currentFieldNo.put("check_out", checkOut);

        // Use the fieldNo as the key and store the currentFieldNo map under it
        recordData.put(String.valueOf(recordData.size() + 1), currentFieldNo);

        return recordData;
    }


    private void storeRecordInFirestore(String userUid, String hostelId, String currentDate, Map<String, Object> recordData, DataSnapshot snapshot,DatabaseReference statusRef, String userName,Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the document for the current date
        DocumentReference recordsForDateRef = db.collection("hostels")
                .document(hostelId)
                .collection("records")
                .document(currentDate);

        // Fetch the existing data for this date
        recordsForDateRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Document exists, retrieve the existing data
                Map<String, Object> existingData = task.getResult().getData();

                // Find the next available number (1, 2, 3, etc.)
                int nextRecordNumber = existingData.size() + 1;

                // Add the new data under the next available number
                existingData.put(String.valueOf(nextRecordNumber), recordData.get("1")); // recordData has one entry with key "1"

                // Update the document with the new entry
                recordsForDateRef.update(existingData)
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Data successfully updated for the date: " + currentDate))
                        .addOnFailureListener(e -> Log.e("FirebaseHelper", "Error updating data in Firestore", e));
            } else {
                // Document doesn't exist, set the new data
                recordsForDateRef.set(recordData)
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Data successfully added to the records for date: " + currentDate))
                        .addOnFailureListener(e -> Log.e("FirebaseHelper", "Error adding data to Firestore", e));
            }
        }).addOnFailureListener(e -> Log.e("FirebaseHelper", "Error fetching Firestore document", e));

        deleteUserNode(userUid, statusRef, userName,context);

    }


    // Update the updateRecord method to match the new structure
    public void updateRecord(String hostelId, String currentDate, String recordId, Map<String, Object> updatedData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("hostels")
                .document(hostelId)
                .collection("records")
                .document(currentDate)
                .collection("entries")
                .document(recordId); // Use the recordId obtained from addOnSuccessListener

        docRef.update(updatedData)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "Document updated successfully."))
                .addOnFailureListener(e -> Log.e("FirebaseHelper", "Error updating document: " + e.getMessage()));
    }

    // Delete the user's status entry from the database
    private void deleteUserNode(String userUid, DatabaseReference statusRef, String userName, Context context) {
        statusRef.removeValue().addOnCompleteListener(deleteTask -> {
            if (deleteTask.isSuccessful()) {
                // Optionally, you can show a message after deletion
                helperClass.customToast(context, "User: " + userName + " is now IN!");
            } else {
                logError("Failed to delete status entry: " + deleteTask.getException());
                helperClass.customToast(context, "Failed to delete status. Please try again.");
            }
        });
        helperClass.startFreshActivity(context,homeActivity.class);
    }

    // Log error and show message
    private void logError(String message) {
        Log.e("QRScanner", message);
    }


}