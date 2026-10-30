package com.example.movie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A lightweight, dependency-free test runner for {@link MovieRepository} and
 * {@link Json}. It uses plain assertions so it can be compiled and executed with
 * only the JDK (no JUnit required):
 *
 * <pre>{@code
 *   javac -d out src/main/java/com/example/movie/*.java src/test/java/com/example/movie/*.java
 *   java -cp out com.example.movie.RepositoryTests
 * }</pre>
 *
 * Exits with a non-zero status code if any test fails, so it works in CI.
 */
public final class RepositoryTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        run("addAssignsIncrementingIds", RepositoryTests::addAssignsIncrementingIds);
        run("deleteRemovesMovie", RepositoryTests::deleteRemovesMovie);
        run("updatePerformsPartialChanges", RepositoryTests::updatePerformsPartialChanges);
        run("searchIsCaseInsensitiveAcrossFields", RepositoryTests::searchIsCaseInsensitiveAcrossFields);
        run("sortByRatingIsDescending", RepositoryTests::sortByRatingIsDescending);
        run("dataPersistsAcrossInstances", RepositoryTests::dataPersistsAcrossInstances);
        run("jsonRoundTripsSpecialCharacters", RepositoryTests::jsonRoundTripsSpecialCharacters);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /* ------------------------------ Tests --------------------------- */

    private static void addAssignsIncrementingIds() throws IOException {
        MovieRepository repo = newRepo();
        Movie a = repo.add("Inception", "Nolan", "Sci-Fi", 2010, 8.8, true);
        Movie b = repo.add("Heat", "Mann", "Crime", 1995, 8.3, false);
        assertEquals(1, a.getId(), "first id");
        assertEquals(2, b.getId(), "second id");
        assertEquals(2, repo.count(), "count");
    }

    private static void deleteRemovesMovie() throws IOException {
        MovieRepository repo = newRepo();
        Movie a = repo.add("Inception", "Nolan", "Sci-Fi", 2010, 8.8, true);
        assertTrue(repo.delete(a.getId()), "delete existing");
        assertTrue(!repo.delete(a.getId()), "delete missing returns false");
        assertEquals(0, repo.count(), "count after delete");
    }

    private static void updatePerformsPartialChanges() throws IOException {
        MovieRepository repo = newRepo();
        Movie a = repo.add("Inception", "Nolan", "Sci-Fi", 2010, 8.8, false);
        assertTrue(repo.update(a.getId(), null, null, null, null, 9.0, true), "update");
        Movie reloaded = repo.findById(a.getId()).orElseThrow();
        assertEquals("Inception", reloaded.getTitle(), "title unchanged");
        assertEquals(9.0, reloaded.getRating(), "rating changed");
        assertTrue(reloaded.isWatched(), "watched changed");
    }

    private static void searchIsCaseInsensitiveAcrossFields() throws IOException {
        MovieRepository repo = newRepo();
        repo.add("Inception", "Christopher Nolan", "Sci-Fi", 2010, 8.8, true);
        repo.add("Heat", "Michael Mann", "Crime", 1995, 8.3, false);
        assertEquals(1, repo.search("nolan").size(), "search by director");
        assertEquals(1, repo.search("CRIME").size(), "search by genre uppercase");
        assertEquals(2, repo.search("").size(), "empty search returns all");
    }

    private static void sortByRatingIsDescending() throws IOException {
        MovieRepository repo = newRepo();
        repo.add("Low", "X", "Drama", 2000, 5.0, false);
        repo.add("High", "Y", "Drama", 2001, 9.0, false);
        List<Movie> sorted = repo.findAllSorted(MovieRepository.SortBy.RATING);
        assertEquals("High", sorted.get(0).getTitle(), "highest rating first");
        assertEquals("Low", sorted.get(1).getTitle(), "lowest rating last");
    }

    private static void dataPersistsAcrossInstances() throws IOException {
        Path file = Files.createTempFile("movies", ".json");
        Files.deleteIfExists(file);
        MovieRepository first = new MovieRepository(file);
        first.add("Inception", "Nolan", "Sci-Fi", 2010, 8.8, true);

        MovieRepository second = new MovieRepository(file);
        assertEquals(1, second.count(), "reloaded count");
        assertEquals("Inception", second.findAll().get(0).getTitle(), "reloaded title");
        Movie next = second.add("Heat", "Mann", "Crime", 1995, 8.3, false);
        assertEquals(2, next.getId(), "id continues after max persisted id");
        Files.deleteIfExists(file);
    }

    private static void jsonRoundTripsSpecialCharacters() throws IOException {
        MovieRepository repo = newRepo();
        repo.add("Amélie \"quote\" \\slash\\", "Jean-Pierre", "Comedy, Romance", 2001, 8.3, true);
        Movie reloaded = repo.findAll().get(0);
        assertEquals("Amélie \"quote\" \\slash\\", reloaded.getTitle(), "special chars survive round-trip");
        assertEquals("Comedy, Romance", reloaded.getGenre(), "comma in genre survives");
    }

    /* ----------------------------- Helpers -------------------------- */

    private static MovieRepository newRepo() throws IOException {
        Path file = Files.createTempFile("movies", ".json");
        Files.deleteIfExists(file); // start empty
        return new MovieRepository(file);
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }

    private static void run(String name, TestCase test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (AssertionError | Exception e) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + e.getMessage());
        }
    }

    private static void assertTrue(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("expected true: " + what);
        }
    }

    private static void assertEquals(Object expected, Object actual, String what) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(what + " -> expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(double expected, double actual, String what) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError(what + " -> expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
