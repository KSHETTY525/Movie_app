package com.example.movie_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView greeting;
    EditText searchInput;
    DBHelper dbHelper;
    MovieAdapter adapter;
    ArrayList<Movie> movieList;
    Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        greeting = findViewById(R.id.greeting);
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        String username = preferences.getString("username", "User");
        greeting.setText("Hello " + username + "!");

        toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        movieList = new ArrayList<>();

        dbHelper.importMoviesFromCSV(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadMovies("");

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                loadMovies(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void loadMovies(String keyword) {
        movieList.clear();
        Cursor cursor = keyword.isEmpty() ? dbHelper.getAllMovies() : dbHelper.searchMovies(keyword);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("movie_name"));
                String year = cursor.getString(cursor.getColumnIndexOrThrow("year"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                String language = cursor.getString(cursor.getColumnIndexOrThrow("language"));

                movieList.add(new Movie(id, name, year, genre, language));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new MovieAdapter(this, movieList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_view_watched) {
            // Replace with your actual activity for watched movies
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_logout) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
