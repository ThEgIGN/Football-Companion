package com.example.footballapplication.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footballapplication.BuildConfig;
import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.example.footballapplication.R;
import com.example.footballapplication.profile.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText textName, textEmail, textPass;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenSizeHelper.lockOrientationForSmallPhones(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        TextView textClickLogin = findViewById(R.id.auth_register_click_login);
        progressBar = findViewById(R.id.auth_register_progress_bar);
        textName = findViewById(R.id.auth_register_name);
        textEmail = findViewById(R.id.auth_register_email);
        textPass = findViewById(R.id.auth_register_password);
        Button buttonRegister = findViewById(R.id.auth_register_button);

        textClickLogin.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonRegister.setOnClickListener(view -> {
            String email = String.valueOf(textEmail.getText());
            String pass = String.valueOf(textPass.getText());
            name = String.valueOf(textName.getText());

            if (TextUtils.isEmpty(name)) {
                textName.setError(getString(R.string.required_field));
                return;
            }

            if (TextUtils.isEmpty(email)) {
                textEmail.setError(getString(R.string.required_field));
                return;
            }

            if (TextUtils.isEmpty(pass)) {
                textPass.setError(getString(R.string.required_field));
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                addDataToDatabase(user.getUid());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void addDataToDatabase(String uid) {
        Profile profile = new Profile(name, "");
        DatabaseReference reference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE).getReference().child("Users");
        reference.child(uid).setValue(profile).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }
}