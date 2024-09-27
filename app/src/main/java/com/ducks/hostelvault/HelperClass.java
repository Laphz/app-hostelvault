package com.ducks.hostelvault;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class HelperClass {
    public void showAlertEmailVerification(Context CurrentActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(CurrentActivity);
        builder.setTitle("Email Not Verified!");
        builder.setMessage("Your email is not verified.\nPlease continue to verify the email.");
        // opening email for verification
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CurrentActivity.startActivity(intent);
            }
        });

        // create alter dialog
        AlertDialog alertDialog = builder.create();

        // show alter dialog
        alertDialog.show();
    }

    // start a fresh activity
    private void startFreshActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
        if (!(currentActivity instanceof Activity)) {
            ((Activity) currentActivity).finish();
        }
    }

    // start a new activity
    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        currentActivity.startActivity(intent);
    }
}
