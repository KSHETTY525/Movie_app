package com.example.movie_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "MovieTrackerDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        db.execSQL("CREATE TABLE Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT)");

        // Movies Table
        db.execSQL("CREATE TABLE Movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_name TEXT, " +
                "year   TEXT, " +
                "language TEXT, " +
                "genre TEXT)");

        // WatchedMovies Table
        db.execSQL("CREATE TABLE WatchedMovies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user TEXT, " +
                "movie_id INTEGER, " +
                "rating INTEGER, " +
                "review TEXT, " +
                "FOREIGN KEY(movie_id) REFERENCES Movies(id), " +
                "FOREIGN KEY(user) REFERENCES Users(username))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WatchedMovies");
        db.execSQL("DROP TABLE IF EXISTS Movies");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    // Register a new user
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("Users", null, values);
        return result != -1;
    }

    // Check login credentials
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE username=? AND password=?",
                new String[]{username, password});
        boolean result = cursor.moveToFirst();
        cursor.close();
        return result;
    }

    // Add movie to Movies table
    public void insertMovie(String movie_name, String language, String year, String genre) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Movie Name", movie_name);
        values.put("Year", year);
        values.put("Genre", genre);
        values.put("Language", language);
        db.insert("Movies", null, values);
    }

    // Get all movies
    public Cursor getAllMovies() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Movies", null);
    }

    // Search movies by keyword or director
    public Cursor searchMovies(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Movies WHERE movie_name LIKE ? OR genre LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
    }

    // Mark movie as watched
    public boolean markMovieWatched(String username, int movieId, int rating, String review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user", username);
        values.put("movie_id", movieId);
        values.put("rating", rating);
        values.put("review", review);
        long result = db.insert("WatchedMovies", null, values);
        return result != -1;
    }

    // Get watched movies for a user
    public Cursor getWatchedMovies(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT M.movie_name, M.language, M.year, M.genre, W.rating, W.review " +
                "FROM WatchedMovies W JOIN Movies M ON W.movie_id = M.id " +
                "WHERE W.user = ?", new String[]{username});
    }

    // Delete movie from watched list
    public void deleteWatchedMovie(String username, int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("WatchedMovies", "user=? AND movie_id=?", new String[]{username, String.valueOf(movieId)});
    }

    public void importMoviesFromCSV(Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            InputStream is = context.getAssets().open("movies.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            db.beginTransaction();
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length == 4) {
                    ContentValues values = new ContentValues();
                    values.put("movie_name", tokens[0].trim());
                    values.put("year", tokens[1].trim());
                    values.put("language", tokens[2].trim());
                    values.put("genre", tokens[3].trim());
                    db.insert("Movies", null, values);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

