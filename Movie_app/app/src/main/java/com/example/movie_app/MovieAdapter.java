package com.example.movie_app; // Replace with your package name

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;
    private DBHelper dbHelper;
    private String loggedInUser;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
        this.dbHelper = new DBHelper(context);

        // Retrieve logged-in user from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        this.loggedInUser = preferences.getString("username", "");
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each movie item
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Set the movie details in the UI components
        holder.titleTextView.setText(movie.getMovieName());
        holder.directorTextView.setText(movie.getLanguage());
        holder.yearTextView.setText(movie.getYear());
        holder.genreTextView.setText(movie.getGenre());

        // Handle rating and review
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            holder.ratingValue = rating;
        });

        holder.addToWatchedButton.setOnClickListener(v -> {
            String review = holder.reviewInput.getText().toString().trim();
            boolean success = dbHelper.markMovieWatched(loggedInUser, movie.getId(), (int) holder.ratingValue, review);
            if (success) {
                Toast.makeText(context, "Movie added to watched list!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to add movie to watched list", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            dbHelper.deleteWatchedMovie(loggedInUser, movie.getId());
            movieList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Movie deleted from watched list!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, directorTextView, yearTextView, genreTextView;
        RatingBar ratingBar;
        EditText reviewInput;
        Button addToWatchedButton, deleteButton;
        float ratingValue = 0;

        @SuppressLint("WrongViewCast")
        public MovieViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movieName);
            directorTextView = itemView.findViewById(R.id.movieLanguage);
            yearTextView = itemView.findViewById(R.id.movieYear);
            genreTextView = itemView.findViewById(R.id.movieGenre);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            reviewInput = itemView.findViewById(R.id.reviewInput);
            addToWatchedButton = itemView.findViewById(R.id.addToWatchedButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}