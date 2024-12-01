package com.example.footballapplication.authentication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footballapplication.MainActivity;
import com.example.footballapplication.map.MapActivity;
import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.example.footballapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

@SuppressWarnings("SpellCheckingInspection")
public class LoginActivity extends AppCompatActivity {

    private EditText textEmail, textPass;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    public static final String PREFERENCE_LANGUAGE = "com.example.footballapplication.language";
    private boolean keepSplashScreen = true;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        keepSplashScreen = true;
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ScreenSizeHelper.lockOrientationForSmallPhones(this);

        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);
        getLanguageFromPreferences();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        TextView textClickRegister = findViewById(R.id.auth_login_click_register);
        progressBar = findViewById(R.id.auth_login_progress_bar);
        textEmail = findViewById(R.id.auth_login_email);
        textPass = findViewById(R.id.auth_login_password);
        Button buttonLogin = findViewById(R.id.auth_login_button);

        textClickRegister.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(view -> {
            String email = String.valueOf(textEmail.getText());
            String pass = String.valueOf(textPass.getText());

            if (TextUtils.isEmpty(email)) {
                textEmail.setError(getString(R.string.required_field));
                return;
            }

            if (TextUtils.isEmpty(pass)) {
                textPass.setError(getString(R.string.required_field));
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void updateAppLanguage(String language) {
        // language should be "en" or "sr"
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        keepSplashScreen = false;
    }

    private void getLanguageFromPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_LANGUAGE,Context.MODE_PRIVATE);
        String language = sharedPref.getString("language", "en");
        updateAppLanguage(language);
    }
}