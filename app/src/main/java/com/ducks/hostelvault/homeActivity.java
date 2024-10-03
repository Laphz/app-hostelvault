package com.ducks.hostelvault;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class homeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final float END_SCALE = 0.7f;
    ImageButton redirectToQr;
    TextView status;
    ImageView indicator;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    ConstraintLayout contentView;
    FirebaseHelper firebaseHelper;
    HelperClass helperClass;
    User user;

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
        setContentView(R.layout.activity_home);

        redirectToQr = findViewById(R.id.redirectToQr);
        indicator = findViewById(R.id.indicator);
        status = findViewById(R.id.status);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.contentView);
        firebaseHelper = new FirebaseHelper();
        helperClass = new HelperClass();

        // Fetch the user's username and status
        fetchUserNameAndStatus();

        // open scanner
        redirectToQr();

        // show drawer
        showNavigation();



    }

    /*-----------------------------------------------------------Drawer Functions--------------------------------------------------------------*/

    // show navigation
    private void showNavigation() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.settings);

        menuIcon.setOnClickListener(view -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        animateNavigationDrawer();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.settings) {
            helperClass.startNewActivity(homeActivity.this, comingSoon.class);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.aboutus) {
            helperClass.startNewActivity(homeActivity.this, comingSoon.class);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.logout) {
            firebaseHelper.signOutUser();
            helperClass.startNewActivity(homeActivity.this, launchActivity.class);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return false;
        }
    }

    // Animate the drawer
    private void animateNavigationDrawer() {
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float rotationAngle = -10 * slideOffset;
                contentView.setPivotX(contentView.getWidth() * 0.5f);
                contentView.setPivotY(contentView.getHeight() * 0.5f);
                contentView.setRotationY(rotationAngle);
                contentView.setAlpha(1 - (slideOffset * 0.5f));
                contentView.setTranslationX(drawerView.getWidth() * slideOffset * 0.5f);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                contentView.setRotationY(0);
                contentView.setAlpha(1.0f);
            }
        });
    }

    /*---------------------------------------------------------Status Functions-----------------------------------------------------------*/


    // Fetch user name and status
    private void fetchUserNameAndStatus() {

        String userId = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getHostelId(userId, new FirebaseHelper.hostelIdCallback() {
            @Override
            public void onCallback(String hostelId) {
                firebaseHelper.getUserName(userId, new FirebaseHelper.userNameCallback() {
                    @Override
                    public void onCallback(String userName) {
                        if (userName != null) {
                            fetchUserStatusByUsername(userName,hostelId);
                        } else {
                            status.setText("User name not found.");
                        }
                    }
                });
            }
        });

    }

    // Fetch user status using the username
    private void fetchUserStatusByUsername(String userName,String hostelId) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/" + hostelId).child(userName);

        statusRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot statusSnapshot = task.getResult();
                    String userStatus = statusSnapshot.exists() ? statusSnapshot.getValue(String.class) : "Status not found";

                    // Update the UI with the fetched status
                    updateStatusUI(userStatus);
                } else {
                    Log.e("FetchUserStatus", "Error getting user status: " + task.getException());
                    status.setText("Error fetching status");
                }
            }
        });
    }


    private void updateStatusUI(String userStatus) {
        indicator.setImageResource("IN".equals(userStatus) ? R.drawable.baseline_circle_green_24 : R.drawable.baseline_circle_red_24);
        status.setText("IN".equals(userStatus) ? "You are IN!!" : "You are OUT!!");
    }



    private void redirectToQr() {
        redirectToQr.setOnClickListener(v -> helperClass.startFreshActivity(homeActivity.this, qrActivity.class));
    }
}
