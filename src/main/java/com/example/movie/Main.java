package com.example.movie;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Entry point for the Movie Manager CLI application.
 *
 * <p>Movies are persisted to a JSON file. The storage location can be
 * overridden with the {@code movies.file} system property, e.g.
 * {@code java -Dmovies.file=/tmp/movies.json -jar movie-manager.jar}.
 */
public final class Main {

    private static final String DEFAULT_STORAGE = "movies.json";

    private Main() {
    }

    public static void main(String[] args) {
        Path storage = Path.of(System.getProperty("movies.file", DEFAULT_STORAGE));
        MovieRepository repository = new MovieRepository(storage);

        try (Scanner scanner = new Scanner(System.in)) {
            new MovieManagerCli(repository, scanner).run();
        }
    }
}
