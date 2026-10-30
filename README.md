# 🎬 Movie Manager

A simple command-line application to manage a personal movie collection, written in Java.

List, add, update, delete, search, and sort your movies. Each movie tracks a
title, director, genre, release year, rating, and whether you've watched it.
Your collection is saved to a JSON file so it persists between runs.

## Features

- **List** all movies in a formatted table
- **Add** a new movie with input validation
- **Update** any field of an existing movie (partial updates supported)
- **Delete** a movie (with confirmation)
- **Mark watched / unwatched**
- **Search** across title, director, and genre (case-insensitive)
- **Sort** by title, year, rating, or director
- **File-based persistence** — data stored in a human-readable JSON file

> **No external dependencies.** The app uses a small built-in JSON
> reader/writer, so it builds and runs with nothing but a JDK — no libraries to
> download.

## Requirements

- Java 21 or newer (a JDK, not just a JRE)
- Maven 3.8+ is *optional* (only if you prefer the Maven workflow)

## Build & run (JDK only — recommended)

A `build.sh` script builds and tests using only `javac`/`java`/`jar`:

```bash
./build.sh        # compile, run tests, build the runnable JAR
./build.sh test   # compile and run tests only
./build.sh run    # build then launch the app
```

The runnable JAR is written to `dist/movie-manager.jar`:

```bash
java -jar dist/movie-manager.jar
```

## Build & run (Maven — optional)

```bash
mvn clean package
java -jar target/movie-manager.jar
```

By default the collection is stored in `movies.json` in the current directory.
You can choose a different location:

```bash
java -Dmovies.file=/path/to/my-movies.json -jar target/movie-manager.jar
```

You can also run directly through Maven during development:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.movie.Main
```

## Usage

When launched, you get an interactive menu:

```
=====================================
        🎬  Movie Manager
=====================================
-------------------------------------
Movies in collection: 0
-------------------------------------
  1) List all movies
  2) Add a movie
  3) Update a movie
  4) Delete a movie
  5) Mark watched / unwatched
  6) Search movies
  7) List sorted
  0) Quit
```

Type the number of the option you want and follow the prompts.

## Tests

With the JDK-only workflow:

```bash
./build.sh test
```

Or with Maven:

```bash
mvn test
```

Tests live in `src/test/java/com/example/movie/RepositoryTests.java` and use
plain Java assertions (no JUnit required), so they run anywhere a JDK is present.

## Project structure

```
movie-manager/
├── pom.xml                         # Optional Maven build
├── build.sh                        # JDK-only build/test/run script
├── README.md
├── LICENSE
└── src
    ├── main/java/com/example/movie
    │   ├── Main.java               # Entry point
    │   ├── Movie.java              # Domain model
    │   ├── Json.java               # Tiny built-in JSON reader/writer
    │   ├── MovieRepository.java    # CRUD + search/sort + JSON persistence
    │   └── MovieManagerCli.java    # Interactive menu
    └── test/java/com/example/movie
        └── RepositoryTests.java    # Dependency-free test runner
```

## License

Released under the [MIT License](LICENSE).
