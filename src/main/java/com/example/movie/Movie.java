package com.example.movie;

import java.util.Objects;

/**
 * Represents a single movie in the collection.
 *
 * <p>Fields: title, director, genre, release year, rating (0.0-10.0) and a
 * "watched" flag. Each movie is assigned a unique numeric id by the repository.
 */
public class Movie {

    private int id;
    private String title;
    private String director;
    private String genre;
    private int year;
    private double rating;
    private boolean watched;

    /** No-arg constructor required by Jackson for deserialization. */
    public Movie() {
    }

    public Movie(int id, String title, String director, String genre, int year, double rating, boolean watched) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.watched = watched;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Movie movie)) {
            return false;
        }
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "#%-3d %-30s %-20s %-12s %4d  %4.1f  %s",
                id,
                truncate(title, 30),
                truncate(director, 20),
                truncate(genre, 12),
                year,
                rating,
                watched ? "[watched]" : "[unwatched]");
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "\u2026";
    }
}
