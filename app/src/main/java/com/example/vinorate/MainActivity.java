package com.example.vinorate;

import android.os.Bundle;
import android.util.Log;

import com.example.vinorate.Models.User;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.Utilities.ImageLoader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.vinorate.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseManager firebaseManager;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = new FirebaseManager();
        auth = FirebaseAuth.getInstance();

        setupBottomNavigation();

        ImageLoader.initImageLoader(this);

        setupAuthStateListener();
    }

    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_for_you,
                R.id.navigation_search,
                R.id.navigation_wishlist,
                R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                // User is signed in
                Log.d("MainActivity", "User signed in: " + firebaseUser.getUid());
                handleUserSignIn(firebaseUser);
            } else {
                // User is signed out
                Log.d("MainActivity", "User signed out");
                // Handle the case where the user is not signed in, maybe redirect to login activity
            }
        };
    }

    private void handleUserSignIn(FirebaseUser firebaseUser) {
        DatabaseReference userRef = FirebaseManager.getUsersRef().child(firebaseUser.getUid());

        // Check if user already exists
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // User doesn't exist, save it
                    User user = new User();
                    user.setId(firebaseUser.getUid());
                    if (firebaseUser.getDisplayName() != null) {
                        user.setName(firebaseUser.getDisplayName());
                    }
                    firebaseManager.saveUserToFirebase(user);
                } else {
                    // User exists, no need to save again
                    Log.d("MainActivity", "User already exists: " + firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error checking user existence: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }
}

