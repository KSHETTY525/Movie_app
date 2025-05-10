package com.example.movie_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button login, goToRegister;
    DBHelper dbHelper;
    SharedPreferences preferences;
    Toolbar toolbar;
    RadioGroup languageGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load saved language before view is created
        applySavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Make sure the correct layout file exists

        // Initialize UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        goToRegister = findViewById(R.id.goToRegister);
        languageGroup = findViewById(R.id.languageGroup);
        toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);

        // Initialize DB helper and preferences
        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("login", Context.MODE_PRIVATE);

        // Set previously selected language on radio buttons
        String currentLang = getSharedPreferences("settings", MODE_PRIVATE).getString("app_lang", "en");
        if (currentLang.equals("hi")) {
            languageGroup.check(R.id.rbHindi);
        } else if (currentLang.equals("mr")) {
            languageGroup.check(R.id.rbMarathi);
        } else {
            languageGroup.check(R.id.rbEnglish);
        }

        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLang;
            if (checkedId == R.id.rbHindi) {
                selectedLang = "hi";
            } else if (checkedId == R.id.rbMarathi) {
                selectedLang = "mr";
            } else {
                selectedLang = "en";
            }

            // Avoid recreation if same language is selected
            String savedLang = getSharedPreferences("settings", MODE_PRIVATE).getString("app_lang", "en");
            if (!savedLang.equals(selectedLang)) {
                SharedPreferences.Editor langEditor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                langEditor.putString("app_lang", selectedLang);
                langEditor.apply();
                recreate();  // Apply language change
            }
        });

        // Auto-login if already logged in
        if (preferences.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();  // Close the login activity so the user can't go back
        }

        // Handle login button click
        login.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Validate inputs
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check login credentials in the database
            if (dbHelper.checkLogin(user, pass)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("logged_in", true);
                editor.putString("username", user);  // Optionally store username
                editor.apply();

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                // Go to the main activity (dashboard)
                startActivity(new Intent(this, MainActivity.class));
                finish();  // Close the login activity so the user can't go back
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to register screen
        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String language = prefs.getString("app_lang", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
