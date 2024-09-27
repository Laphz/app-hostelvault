package com.ducks.hostelvault;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private FirebaseAuth auth;
    private DatabaseReference dbReference;

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("Registered Users");
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signUpUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public void storeUserData(String userId, user user, OnCompleteListener<Void> listener) {
        dbReference.child(userId).setValue(user).addOnCompleteListener(listener);
    }

    public void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification();
    }

    public void signOutUser() {
        auth.signOut();
    }

}

