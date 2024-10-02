package com.ducks.hostelvault;

import android.content.Context;
import android.content.Intent;
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

public class homeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static final float END_SCALE = 0.7f;
    ImageButton redirectToQr;
    TextView status;
    ImageView indicator;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    ConstraintLayout contentView;
    FirebaseAuth auth = FirebaseAuth.getInstance();

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



        // Fetch the user's username and status
        fetchUserNameAndStatus();

        // open scanner
        redirectToQr();;

        // show drawer
        showNavigation();




    }

    /*-----------------------------------------------------------Drawer Functions--------------------------------------------------------------*/

    // show navigation
    private void showNavigation(){
        // Navigation drawer
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.settings);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawerLayout.isDrawerVisible(GravityCompat.START)){drawerLayout.closeDrawer(GravityCompat.START);}

                else {drawerLayout.openDrawer(GravityCompat.START);}

            }
        });
        animateNavigationDrawer();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerVisible(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.home:
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            case R.id.settings:
//                startNewActivity(homeActivity.this,settingsActivity.class);
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            case R.id.aboutus:
//                startNewActivity(homeActivity.this,aboutUsActivity.class);
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            case R.id.logout:
//                logout();
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            default:
//                return false;
//
//        }
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.settings) {
            startNewActivity(homeActivity.this, comingSoon.class);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.aboutus) {
            startNewActivity(homeActivity.this, comingSoon.class);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.logout) {
            logout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return false;
        }
    }

//     animation of drawer
    private void animateNavigationDrawer() {
        drawerLayout.setScrimColor(Color.TRANSPARENT); // No background overlay (optional)

        // Add a listener for drawer slide events
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // 3D Rotation effect
                final float rotationAngle = -10 * slideOffset; // Rotate content by 10 degrees based on slide offset
                contentView.setPivotX(contentView.getWidth() * 0.5f); // Set pivot to the center of the view
                contentView.setPivotY(contentView.getHeight() * 0.5f); // Set pivot point for Y-axis rotation
                contentView.setRotationY(rotationAngle); // Rotate the content view along the Y-axis

                // Fade effect (content fades out as the drawer opens)
                contentView.setAlpha(1 - (slideOffset * 0.5f)); // Slight fade-out effect

                // Slide effect
                contentView.setTranslationX(drawerView.getWidth() * slideOffset * 0.5f); // Content moves 50% of drawer width
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Optionally, do something when the drawer is fully opened
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Optionally, do something when the drawer is fully closed
                contentView.setRotationY(0); // Reset rotation when the drawer is closed
                contentView.setAlpha(1.0f);  // Reset the transparency to fully visible
            }
        });
    }

    /*---------------------------------------------------------Status Functions-----------------------------------------------------------*/
    // Fetch user name and status
    private void fetchUserNameAndStatus() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId);

        userRef.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getValue(String.class);
                    if (userName != null) {
                        fetchUserStatusByUsername(userName);
                    } else {
                        status.setText("User name not found.");
                    }
                } else {
                    Log.e("FetchUserName", "Error getting user name: " + task.getException());
                    status.setText("Error fetching user name");
                }
            }
        });
    }

    // Fetch user status using the username
    private void fetchUserStatusByUsername(String userName) {
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Status").child(userName);

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
        if ("IN".equals(userStatus)) {
            indicator.setImageResource(R.drawable.baseline_circle_green_24);
            status.setText("You are IN!!");
        } else {
            indicator.setImageResource(R.drawable.baseline_circle_red_24);
            status.setText("You are OUT!!");
        }
    }

    private void logout() {
        auth.signOut();
        startFreshActivity(homeActivity.this, loginActivity.class);

    }

    private void startNewActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        startActivity(intent);
    }

    private void startFreshActivity(Context currentActivity, Class<?> newActivity) {
        Intent intent = new Intent(currentActivity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void customToast(String message) {
        Toast.makeText(homeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void redirectToQr(){
        redirectToQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startNewActivity(homeActivity.this, qrActivity.class);
            }
        });
    }


}
