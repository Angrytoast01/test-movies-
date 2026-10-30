package com.example.movie;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Stores and manages the movie collection, backed by a JSON file on disk.
 *
 * <p>The collection is loaded into memory on construction and written back to
 * disk after every mutating operation, so data survives between runs.
 */
public class MovieRepository {

    private final Path storageFile;
    private final List<Movie> movies = new ArrayList<>();
    private int nextId = 1;

    public MovieRepository(Path storageFile) {
        this.storageFile = storageFile;
        load();
    }

    /** Field used for sorting movie listings. */
    public enum SortBy {
        TITLE, YEAR, RATING, DIRECTOR
    }

    /* ----------------------------- CRUD ----------------------------- */

    /** Returns an unmodifiable snapshot of all movies. */
    public List<Movie> findAll() {
        return List.copyOf(movies);
    }

    public Optional<Movie> findById(int id) {
        return movies.stream().filter(m -> m.getId() == id).findFirst();
    }

    /** Adds a new movie, assigning it a fresh id, and persists the change. */
    public Movie add(String title, String director, String genre, int year, double rating, boolean watched) {
        Movie movie = new Movie(nextId++, title, director, genre, year, rating, watched);
        movies.add(movie);
        save();
        return movie;
    }

    /**
     * Updates the movie with the given id. Any null/negative argument is treated
     * as "leave unchanged" so callers can do partial updates.
     *
     * @return true if a movie was found and updated
     */
    public boolean update(int id, String title, String director, String genre,
                          Integer year, Double rating, Boolean watched) {
        Optional<Movie> existing = findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        Movie movie = existing.get();
        if (title != null && !title.isBlank()) {
            movie.setTitle(title);
        }
        if (director != null && !director.isBlank()) {
            movie.setDirector(director);
        }
        if (genre != null && !genre.isBlank()) {
            movie.setGenre(genre);
        }
        if (year != null && year > 0) {
            movie.setYear(year);
        }
        if (rating != null && rating >= 0) {
            movie.setRating(rating);
        }
        if (watched != null) {
            movie.setWatched(watched);
        }
        save();
        return true;
    }

    /** Removes the movie with the given id. Returns true if one was removed. */
    public boolean delete(int id) {
        boolean removed = movies.removeIf(m -> m.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean setWatched(int id, boolean watched) {
        return update(id, null, null, null, null, null, watched);
    }

    /* --------------------------- Querying --------------------------- */

    /** Case-insensitive search across title, director and genre. */
    public List<Movie> search(String term) {
        if (term == null || term.isBlank()) {
            return findAll();
        }
        String needle = term.toLowerCase(Locale.ROOT);
        List<Movie> result = new ArrayList<>();
        for (Movie m : movies) {
            if (contains(m.getTitle(), needle)
                    || contains(m.getDirector(), needle)
                    || contains(m.getGenre(), needle)) {
                result.add(m);
            }
        }
        return result;
    }

    /** Returns all movies sorted by the given field. */
    public List<Movie> findAllSorted(SortBy sortBy) {
        Comparator<Movie> comparator = switch (sortBy) {
            case TITLE -> Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
            case DIRECTOR -> Comparator.comparing(Movie::getDirector, String.CASE_INSENSITIVE_ORDER);
            case YEAR -> Comparator.comparingInt(Movie::getYear);
            case RATING -> Comparator.comparingDouble(Movie::getRating).reversed();
        };
        List<Movie> sorted = new ArrayList<>(movies);
        sorted.sort(comparator);
        return sorted;
    }

    public int count() {
        return movies.size();
    }

    private static boolean contains(String haystack, String lowerNeedle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(lowerNeedle);
    }

    /* -------------------------- Persistence ------------------------- */

    private void load() {
        if (!Files.exists(storageFile)) {
            return;
        }
        try {
            String content = Files.readString(storageFile, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return;
            }
            List<Movie> loaded = Json.read(content);
            movies.clear();
            movies.addAll(loaded);
            nextId = movies.stream().mapToInt(Movie::getId).max().orElse(0) + 1;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load movies from " + storageFile, e);
        }
    }

    private void save() {
        try {
            if (storageFile.getParent() != null) {
                Files.createDirectories(storageFile.getParent());
            }
            Files.writeString(storageFile, Json.write(movies), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save movies to " + storageFile, e);
        }
    }
}
