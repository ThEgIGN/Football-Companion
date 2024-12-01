package com.example.footballapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.footballapplication.authentication.LoginActivity;
import com.example.footballapplication.databinding.ActivityMainBinding;
import com.example.footballapplication.matches.HomeFragment;
import com.example.footballapplication.news.NewsFragment;
import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.example.footballapplication.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public static final int POST_NOTIFICATIONS_PERMISSION_CODE = 80;

    private final HomeFragment homeFragment = HomeFragment.newInstance();
    private final NewsFragment newsFragment = NewsFragment.newInstance();
    private final ProfileFragment profileFragment = ProfileFragment.newInstance();
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment activeFragment;
    private FirebaseAuth mAuth;

    private boolean newsOpened;
    private boolean pictureUpdated;

    @Override
    public void onStart() {
        super.onStart();
        // Fail-safe, this should never happen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean smallPhone = ScreenSizeHelper.lockOrientationForSmallPhones(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNotificationChannel();

        newsOpened = false;
        pictureUpdated = false;

        mAuth = FirebaseAuth.getInstance();

        // In order for fragments not to be recreated every time user navigates left/right,
        // they are first created, hidden and then only "Home" fragment is shown
        activeFragment = homeFragment;

        if (!smallPhone) {
            // User is using tablet in landscape mode
            createAndHideFragmentsForTablets();

            assert binding.leftNavigationView != null;
            binding.leftNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.home && activeFragment != homeFragment) {
                    replaceFragment(homeFragment, true);
                } else if (itemId == R.id.profile && activeFragment != profileFragment) {
                    replaceFragment(profileFragment, true);
                    if (!pictureUpdated) {
                        profileFragment.updatePicture();
                        pictureUpdated = true;
                    }
                }
                return true;
            });
        } else {
            // User is using anything else
            createAndHideFragmentsForSmartphones();
            // By navigating, user is simply hiding current fragment and revealing already created new fragment
            assert binding.bottomNavigationView != null;
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.home && activeFragment != homeFragment) {
                    replaceFragment(homeFragment, false);
                } else if (itemId == R.id.news && activeFragment != newsFragment) {
                    replaceFragment(newsFragment, false);
                    if (!newsOpened) {
                        newsFragment.getNews();
                        newsOpened = true;
                    }
                } else if (itemId == R.id.profile && activeFragment != profileFragment) {
                    replaceFragment(profileFragment, false);
                    if (!pictureUpdated) {
                        profileFragment.updatePicture();
                        pictureUpdated = true;
                    }
                }

                return true;
            });
        }
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "footballCompanionNotificationChannel";
            String description = "Channel for matches notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("football_companion", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createAndHideFragmentsForSmartphones() {
        fragmentManager.beginTransaction().add(R.id.frameLayout, profileFragment, "3").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, newsFragment, "2").hide(newsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout, homeFragment, "1").commit();
    }

    private void createAndHideFragmentsForTablets() {
        fragmentManager.beginTransaction().add(R.id.profileFrame, profileFragment, "3").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.matchesFrame, homeFragment, "2").commit();
        fragmentManager.beginTransaction().add(R.id.newsFrame, newsFragment, "1").commit();
    }

    private void replaceFragment(Fragment fragment, boolean tablet) {
        if (!tablet) {
            fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        } else {
            if (fragment == profileFragment) {
                fragmentManager.beginTransaction().hide(homeFragment).hide(newsFragment).show(profileFragment).commit();
            } else {
                fragmentManager.beginTransaction().hide(profileFragment).show(homeFragment).show(newsFragment).commit();
            }
        }
        activeFragment = fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.permission_granted_notifications), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_notifications), Toast.LENGTH_SHORT).show();
            }
        }
    }
}