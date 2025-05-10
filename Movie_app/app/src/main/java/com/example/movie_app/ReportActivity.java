package com.example.movie_app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportActivity extends AppCompatActivity {
    ListView reportList;
    DBHelper dbHelper;
    SharedPreferences preferences;
    Toolbar toolbar;
    Button btnSaveReport;

    ArrayList<String> movieRatings = new ArrayList<>();
    float avgRating = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);

        reportList = findViewById(R.id.reportList);
        btnSaveReport = findViewById(R.id.btnSaveReport);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("login", MODE_PRIVATE);

        String username = preferences.getString("username", null);
        if (username != null) {
            loadWatchedMovies(username);
        }

        btnSaveReport.setOnClickListener(v -> {
            if (!movieRatings.isEmpty()) {
                saveReportToFile(avgRating, movieRatings);
            } else {
                Toast.makeText(this, "No watched movies to save.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWatchedMovies(String username) {
        Cursor cursor = dbHelper.getWatchedMovies(username);
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        movieRatings.clear();
        avgRating = 0;
        int totalRating = 0, count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(0);
                String year = cursor.getString(2);
                String language = cursor.getString(3);
                int rating = cursor.getInt(4);
                String review = cursor.getString(5);

                HashMap<String, String> map = new HashMap<>();
                map.put("title", title);
                map.put("details", "Year: " + year + " | Language: " + language
                        + " | Rating: " + rating + "⭐\nReview: " + review);
                data.add(map);

                movieRatings.add("• " + title + " - " + rating + "⭐");
                totalRating += rating;
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (count > 0) avgRating = (float) totalRating / count;

        SimpleAdapter adapter = new SimpleAdapter(
                this, data, android.R.layout.simple_list_item_2,
                new String[]{"title", "details"},
                new int[]{android.R.id.text1, android.R.id.text2});
        reportList.setAdapter(adapter);
    }

    private void saveReportToFile(float average, ArrayList<String> movieList) {
        StringBuilder report = new StringBuilder();
        report.append("Watched Movies Report:\n\n");
        for (String movie : movieList) {
            report.append(movie).append("\n");
        }
        report.append("\nAverage Rating: ").append(String.format("%.2f", average)).append("⭐");

        // Use the Downloads directory to save the report
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File dir = new File(directoryPath);

        // Ensure the directory exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create the file to store the report
        File file = new File(dir, "watched_movies_report.txt");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(report.toString().getBytes());
            Toast.makeText(this, "Report saved to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
