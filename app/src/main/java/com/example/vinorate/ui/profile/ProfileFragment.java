package com.example.vinorate.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.UserReviewAdapter;
import com.example.vinorate.Activitys.LoginActivity;
import com.example.vinorate.Models.Review;
import com.example.vinorate.R;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.Utilities.ImageLoader;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ImageView profile_IMG_image;
    private TextView profile_LBL_name;
    private TextView profile_LBL_email;
    private TextView profile_LBL_phone;
    private RecyclerView profile_recycler_reviews;
    private UserReviewAdapter reviewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile_IMG_image = view.findViewById(R.id.profile_IMG_image);
        profile_LBL_name = view.findViewById(R.id.profile_LBL_name);
        profile_LBL_email = view.findViewById(R.id.profile_LBL_email);
        profile_LBL_phone = view.findViewById(R.id.profile_LBL_phone);
        Button profile_BTN_signOut = view.findViewById(R.id.profile_BTN_signout);
        profile_recycler_reviews = view.findViewById(R.id.profile_recycler_reviews);
        profile_recycler_reviews.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUserProfile();

        loadUserReviews();

        profile_BTN_signOut.setOnClickListener(v -> signOut());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (isAdded() && !isDetached() && getActivity() != null) {
                ImageLoader.getInstance().load(String.valueOf(user.getPhotoUrl()), profile_IMG_image);
            }
            profile_LBL_name.setText(user.getDisplayName());
            profile_LBL_email.setText(user.getEmail());
            profile_LBL_phone.setText(user.getPhoneNumber());
        }
    }

    private void loadUserReviews() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseManager.getUsersRef().child(user.getUid()).child("reviews").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Review> userReviews = new ArrayList<>();
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Review review = reviewSnapshot.getValue(Review.class);
                        if (review != null) {
                            userReviews.add(review);
                        }
                    }
                    reviewAdapter = new UserReviewAdapter(userReviews);
                    profile_recycler_reviews.setAdapter(reviewAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener(task -> {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    requireActivity().finish();
                });
    }
}
