package com.example.footballapplication.profile;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.footballapplication.BuildConfig;
import com.example.footballapplication.R;
import com.example.footballapplication.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

@SuppressWarnings("SpellCheckingInspection")
public class ProfileFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView textName;
    private ImageView profilePicture;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private Uri updatedImage;
    private Uri firstImage;
    private String currentName;
    private String currentInterests;
    private ActivityResultLauncher<Intent> grabInterests;

    private ActivityResultLauncher<PickVisualMediaRequest> imagePickLauncher;

    private boolean profilePictureDownloaded;
    private boolean profilePictureUpdated;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        progressBar = view.findViewById(R.id.profile_progress_bar);
        profilePicture = view.findViewById(R.id.profile_picture);
        textName = view.findViewById(R.id.profile_name);
        TextView textEmail = view.findViewById(R.id.profile_email);
        Button buttonSearchTerms = view.findViewById(R.id.profile_button_add_interests);
        Button buttonLogout = view.findViewById(R.id.profile_button_logout);
        Button buttonUpdate = view.findViewById(R.id.profile_button_update);
        ImageButton serbianButton = view.findViewById(R.id.profile_serbian_button);
        ImageButton englishButton = view.findViewById(R.id.profile_england_button);

        profilePictureDownloaded = false;
        profilePictureUpdated = false;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        buttonLogout.setOnClickListener(view1 -> {
            try {
                mAuth.signOut();
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } catch (NullPointerException e) {
                Log.i("ProfileFragment", "Null pointer while grabbing activity");
            }
        });

        serbianButton.setOnClickListener(view1 -> updateAppLanguage("sr"));
        englishButton.setOnClickListener(view1 -> updateAppLanguage("en"));

        textEmail.setText(currentUser.getEmail());

        storageReference = FirebaseStorage.getInstance().getReference().child("profile_pic").child(currentUser.getUid());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            firstImage = uri;
            profilePictureDownloaded = true;
            if (!profilePictureUpdated) {
                updatePicture();
            }
        }).addOnFailureListener(e -> Log.i("ProfileFragment", "Storage reference got an error while downloading file. " + e));

        reference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE).getReference().child("Users");
        reference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Profile profile = snapshot.getValue(Profile.class);
                if (profile != null) {
                    currentName = profile.getName();
                    textName.setText(currentName);
                    currentInterests = profile.getSearchStrings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("UpdatingProfileDataError", error.toString());
            }
        });

        grabInterests = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            int result = activityResult.getResultCode();
            Intent data = activityResult.getData();
            if (result == RESULT_OK && data != null) {
                currentInterests = data.getStringExtra("interests");
            }
        });

        buttonSearchTerms.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireActivity(), SearchInterestsActivity.class);
            intent.putExtra("interests", currentInterests);
            grabInterests.launch(intent);
        });

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                updatedImage = uri;
                Glide.with(requireContext()).load(updatedImage).apply(RequestOptions.centerCropTransform()).into(profilePicture);
            }
        });

        buttonUpdate.setOnClickListener(view1 -> {
            progressBar.setVisibility(View.VISIBLE);
            if (firstImage != updatedImage && updatedImage != null) {
                storageReference.putFile(updatedImage).addOnCompleteListener(task -> {
                    firstImage = updatedImage;
                    updateToFireBase();
                });
            } else {
                updateToFireBase();
            }
        });

        profilePicture.setOnClickListener(view1 -> imagePickLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void updateSharedPreferencesWithlanguage(String language) {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences(LoginActivity.PREFERENCE_LANGUAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("language", language);
        editor.apply();
    }

    private void updateAppLanguage(String language) {
        // language should be "en" or "sr"
        updateSharedPreferencesWithlanguage(language);
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        Toast.makeText(requireContext(), getString(R.string.reopen_app_to_see_changes), Toast.LENGTH_SHORT).show();
    }

    private void updateToFireBase() {
        Profile profile = new Profile(currentName, currentInterests);
        reference.child(currentUser.getUid()).setValue(profile).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePicture() {
        Uri imageToLoad;
        if (firstImage != null && profilePictureDownloaded) {
            imageToLoad = firstImage;
            profilePictureUpdated = true;
        } else {
            imageToLoad = Uri.parse("android.resource://com.example.footballapplication/drawable/blank_profile");
        }
        Glide.with(requireContext()).load(imageToLoad).apply(RequestOptions.centerCropTransform()).into(profilePicture);
    }
}