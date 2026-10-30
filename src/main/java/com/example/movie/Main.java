package com.example.movie;

import com.example.movie.gui.MovieManagerGui;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Entry point for the Movie Manager application.
 *
 * <p>By default a Swing GUI is launched. Pass {@code --cli} (or run in a
 * headless environment) to use the interactive command-line interface instead.
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

        if (useCli(args)) {
            runCli(repository);
        } else {
            MovieManagerGui.launch(repository, prefsFileFor(storage));
        }
    }

    /** Places a small theme-preference file next to the data file. */
    private static Path prefsFileFor(Path storage) {
        Path dir = storage.toAbsolutePath().getParent();
        return dir != null ? dir.resolve(".movie-manager.prefs") : Path.of(".movie-manager.prefs");
    }

    private static boolean useCli(String[] args) {
        for (String arg : args) {
            if (arg.equals("--cli") || arg.equals("-c")) {
                return true;
            }
            if (arg.equals("--gui") || arg.equals("-g")) {
                return false;
            }
        }
        // No display available (e.g. a server/CI) -> fall back to the CLI.
        return GraphicsEnvironment.isHeadless();
    }

    private static void runCli(MovieRepository repository) {
        try (Scanner scanner = new Scanner(System.in)) {
            new MovieManagerCli(repository, scanner).run();
        }
    }
}
