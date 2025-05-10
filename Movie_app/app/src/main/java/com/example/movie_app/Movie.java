package com.example.movie_app;
public class Movie {
    private int id;
    private String movieName;
    private String year;
    private String genre;
    private String language; // Optional

    public Movie(int id, String movieName, String year, String genre, String language) {
        this.id = id;
        this.movieName = movieName;
        this.year = year;
        this.genre = genre;
        this.language = language;
    }

    // Getters
    public int getId() { return id; }
    public String getMovieName() { return movieName; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getLanguage() { return language; }
}