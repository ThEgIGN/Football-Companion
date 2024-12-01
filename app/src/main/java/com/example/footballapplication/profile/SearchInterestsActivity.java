package com.example.footballapplication.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footballapplication.orientation.ScreenSizeHelper;
import com.example.footballapplication.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class SearchInterestsActivity extends AppCompatActivity {

    private TextView textTerms;
    private EditText editTextTerm;

    private String currentTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenSizeHelper.lockOrientationForSmallPhones(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_interests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textTerms = findViewById(R.id.profile_interests_terms);
        currentTerms = getIntent().getStringExtra("interests");
        textTerms.setText(currentTerms);
        editTextTerm = findViewById(R.id.profile_interests_text);
        Button addTerm = findViewById(R.id.profile_interests_button_add);
        Button removeTerm = findViewById(R.id.profile_interests_button_remove);
        ImageButton backButton = findViewById(R.id.profile_interests_back_button);

        addTerm.setOnClickListener(view -> {
            String term = editTextTerm.getText().toString().strip();
            if (TextUtils.isEmpty(term)) {
                editTextTerm.setError(getString(R.string.required_field));
                return;
            }
            //noinspection deprecation
            String capitalizedTerm = WordUtils.capitalize(term);
            if (TextUtils.isEmpty(currentTerms)) {
                currentTerms = capitalizedTerm;
            } else {
                int countMatching = StringUtils.countMatches(currentTerms, capitalizedTerm);
                if (countMatching == 0) {
                    currentTerms += ", " + capitalizedTerm;
                } else {
                    editTextTerm.setError(getString(R.string.term_already_added));
                    return;
                }
            }
            textTerms.setText(currentTerms);
        });

        removeTerm.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(currentTerms)) {
                int countLastSeparator = currentTerms.lastIndexOf(",");
                if (countLastSeparator == -1) {
                    currentTerms = "";
                } else {
                    currentTerms = currentTerms.substring(0, countLastSeparator);
                }
                textTerms.setText(currentTerms);
            }
        });

        backButton.setOnClickListener(view -> handleBack());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("currentTerms", currentTerms);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentTerms = savedInstanceState.getString("currentTerms");
        textTerms.setText(currentTerms);
    }

    private void handleBack() {
        Intent intent = new Intent(this, ProfileFragment.class);
        intent.putExtra("interests", currentTerms);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}