package com.example.movie;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive, menu-driven command-line interface for managing movies.
 */
public class MovieManagerCli {

    private static final String HEADER = String.format(
            "%-4s %-30s %-20s %-12s %4s  %4s  %s",
            "ID", "Title", "Director", "Genre", "Year", "Rate", "Status");

    private final MovieRepository repository;
    private final Scanner scanner;

    public MovieManagerCli(MovieRepository repository, Scanner scanner) {
        this.repository = repository;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("=====================================");
        System.out.println("        🎬  Movie Manager");
        System.out.println("=====================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Choose an option").trim();
            System.out.println();
            switch (choice) {
                case "1" -> listMovies();
                case "2" -> addMovie();
                case "3" -> updateMovie();
                case "4" -> deleteMovie();
                case "5" -> toggleWatched();
                case "6" -> searchMovies();
                case "7" -> listSorted();
                case "0", "q", "Q" -> running = false;
                default -> System.out.println("Unknown option: '" + choice + "'. Please try again.");
            }
            System.out.println();
        }
        System.out.println("Goodbye! 👋");
    }

    private void printMenu() {
        System.out.println("-------------------------------------");
        System.out.printf("Movies in collection: %d%n", repository.count());
        System.out.println("-------------------------------------");
        System.out.println("  1) List all movies");
        System.out.println("  2) Add a movie");
        System.out.println("  3) Update a movie");
        System.out.println("  4) Delete a movie");
        System.out.println("  5) Mark watched / unwatched");
        System.out.println("  6) Search movies");
        System.out.println("  7) List sorted");
        System.out.println("  0) Quit");
    }

    /* ---------------------------- Actions --------------------------- */

    private void listMovies() {
        printTable(repository.findAll());
    }

    private void addMovie() {
        System.out.println("-- Add a new movie --");
        String title = requireNonBlank("Title");
        if (title == null) {
            return;
        }
        String director = prompt("Director");
        String genre = prompt("Genre");
        int year = promptInt("Release year", 1888, 2100);
        double rating = promptDouble("Rating (0.0 - 10.0)", 0.0, 10.0);
        boolean watched = promptYesNo("Watched already?");

        Movie created = repository.add(title, director, genre, year, rating, watched);
        System.out.println("\nAdded movie #" + created.getId() + ": " + created.getTitle());
    }

    private void updateMovie() {
        System.out.println("-- Update a movie --");
        if (repository.count() == 0) {
            System.out.println("No movies to update yet.");
            return;
        }
        int id = promptInt("Enter the ID of the movie to update", 1, Integer.MAX_VALUE);
        Optional<Movie> existing = repository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("No movie found with ID " + id + ".");
            return;
        }
        Movie movie = existing.get();
        System.out.println("Editing: " + movie);
        System.out.println("Leave a field blank to keep its current value.");

        String title = emptyToNull(prompt("Title [" + movie.getTitle() + "]"));
        String director = emptyToNull(prompt("Director [" + movie.getDirector() + "]"));
        String genre = emptyToNull(prompt("Genre [" + movie.getGenre() + "]"));
        Integer year = optionalInt(prompt("Year [" + movie.getYear() + "]"));
        Double rating = optionalDouble(prompt("Rating [" + movie.getRating() + "]"));
        Boolean watched = optionalYesNo(prompt("Watched? (y/n) [" + (movie.isWatched() ? "y" : "n") + "]"));

        boolean updated = repository.update(id, title, director, genre, year, rating, watched);
        System.out.println(updated ? "\nMovie updated." : "\nUpdate failed.");
    }

    private void deleteMovie() {
        System.out.println("-- Delete a movie --");
        if (repository.count() == 0) {
            System.out.println("No movies to delete.");
            return;
        }
        int id = promptInt("Enter the ID of the movie to delete", 1, Integer.MAX_VALUE);
        Optional<Movie> existing = repository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("No movie found with ID " + id + ".");
            return;
        }
        if (promptYesNo("Really delete \"" + existing.get().getTitle() + "\"?")) {
            repository.delete(id);
            System.out.println("Deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private void toggleWatched() {
        System.out.println("-- Mark watched / unwatched --");
        if (repository.count() == 0) {
            System.out.println("No movies yet.");
            return;
        }
        int id = promptInt("Enter the ID of the movie", 1, Integer.MAX_VALUE);
        Optional<Movie> existing = repository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("No movie found with ID " + id + ".");
            return;
        }
        boolean newState = !existing.get().isWatched();
        repository.setWatched(id, newState);
        System.out.println("\"" + existing.get().getTitle() + "\" is now "
                + (newState ? "watched." : "unwatched."));
    }

    private void searchMovies() {
        System.out.println("-- Search movies --");
        String term = prompt("Search term (title, director or genre)");
        List<Movie> results = repository.search(term);
        System.out.println();
        if (results.isEmpty()) {
            System.out.println("No matches for \"" + term + "\".");
        } else {
            System.out.println(results.size() + " match(es):");
            printTable(results);
        }
    }

    private void listSorted() {
        System.out.println("-- List sorted --");
        System.out.println("  1) Title");
        System.out.println("  2) Year");
        System.out.println("  3) Rating (high to low)");
        System.out.println("  4) Director");
        String choice = prompt("Sort by").trim();
        MovieRepository.SortBy sortBy = switch (choice) {
            case "1" -> MovieRepository.SortBy.TITLE;
            case "2" -> MovieRepository.SortBy.YEAR;
            case "3" -> MovieRepository.SortBy.RATING;
            case "4" -> MovieRepository.SortBy.DIRECTOR;
            default -> null;
        };
        System.out.println();
        if (sortBy == null) {
            System.out.println("Invalid sort option.");
            return;
        }
        printTable(repository.findAllSorted(sortBy));
    }

    /* ------------------------- Presentation ------------------------- */

    private void printTable(List<Movie> movies) {
        if (movies.isEmpty()) {
            System.out.println("(no movies to show)");
            return;
        }
        System.out.println(HEADER);
        System.out.println("-".repeat(HEADER.length()));
        for (Movie m : movies) {
            System.out.println(m);
        }
    }

    /* --------------------------- Input I/O -------------------------- */

    private String prompt(String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine();
    }

    private String requireNonBlank(String label) {
        String value = prompt(label);
        if (value == null || value.isBlank()) {
            System.out.println(label + " cannot be empty. Aborting.");
            return null;
        }
        return value.trim();
    }

    private int promptInt(String label, int min, int max) {
        while (true) {
            String raw = prompt(label).trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("'" + raw + "' is not a valid whole number.");
            }
        }
    }

    private double promptDouble(String label, double min, double max) {
        while (true) {
            String raw = prompt(label).trim();
            try {
                double value = Double.parseDouble(raw);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("'" + raw + "' is not a valid number.");
            }
        }
    }

    private boolean promptYesNo(String label) {
        while (true) {
            String raw = prompt(label + " (y/n)").trim().toLowerCase();
            if (raw.equals("y") || raw.equals("yes")) {
                return true;
            }
            if (raw.equals("n") || raw.equals("no")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static Integer optionalInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double optionalDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean optionalYesNo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim().toLowerCase();
        if (v.equals("y") || v.equals("yes")) {
            return true;
        }
        if (v.equals("n") || v.equals("no")) {
            return false;
        }
        return null;
    }
}
