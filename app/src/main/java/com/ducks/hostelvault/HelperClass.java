package com.ducks.hostelvault;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HelperClass {
    public void showAlertEmailVerification(Context currentActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("Email Not Verified!");
        builder.setMessage("Your email is not verified.\nPlease continue to verify the email.");
        // opening email for verification
        builder.setPositiveButton("Continue", null);
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                CurrentActivity.startActivity(intent);


        // create alter dialog
        AlertDialog alertDialog = builder.create();

        // show alter dialog
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(currentActivity,verifyEmailActivity.class);
            }
        });
    }

    // start a fresh activity
    public void startFreshActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
        if (!(currentActivity instanceof Activity)) {
            ((Activity) currentActivity).finish();
        }
    }

    // start a new activity
    public void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        currentActivity.startActivity(intent);
    }

    // open email
    public void openEmailApp(Context currentActivity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            currentActivity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(currentActivity, "No email app found!", Toast.LENGTH_SHORT).show();
        }
    }
    // custom toast function
    public void customToast(Context currentActivity,String message) {
        Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
    }
    // show error toast
    public void showErrorToast(Context currentActivity,String message) {
        Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
    }

    // prograss bar
    public void progressbar(Context currentActivity){
        SweetAlertDialog dialog = new SweetAlertDialog(currentActivity, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Loading");
        dialog.setCancelable(false);
        dialog.show();
    }


}
