# 🎬 Movie Manager

A Java application to manage a personal movie collection, with **both a Swing
GUI and a command-line interface**.

List, add, update, delete, search, and sort your movies. Each movie tracks a
title, director, genre, release year, rating, and whether you've watched it.
Your collection is saved to a JSON file so it persists between runs — and
because both interfaces share the same storage, they stay in sync.

## Features

- **Two interfaces:** a desktop **Swing GUI** and an interactive **CLI**
- **List** all movies (sortable table / formatted text)
- **Add** a new movie with input validation
- **Update** any field of an existing movie
- **Delete** a movie (with confirmation)
- **Mark watched / unwatched** (GUI: toggle the checkbox directly in the table)
- **Search** across title, director, and genre (case-insensitive; live-filtering in the GUI)
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

## Choosing the interface

By default the app launches the **GUI**. If no display is available (e.g. a
headless server), it automatically falls back to the CLI. You can force either
one with a flag:

```bash
java -jar dist/movie-manager.jar          # GUI (default)
java -jar dist/movie-manager.jar --gui    # force the GUI
java -jar dist/movie-manager.jar --cli    # force the CLI
```

### GUI

- A table lists all movies. Click a column header area via the **Sort**
  dropdown to order by title, year, rating, or director.
- **Add / Edit / Delete** buttons in the toolbar. Double-click a row to edit it.
- The **Search** box filters the list as you type (title, director, genre).
- Toggle the **Watched** checkbox directly in a row to update it instantly.

### CLI

The CLI presents an interactive, numbered menu:

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
    │   ├── Main.java               # Entry point (GUI by default, --cli for CLI)
    │   ├── Movie.java              # Domain model
    │   ├── Json.java               # Tiny built-in JSON reader/writer
    │   ├── MovieRepository.java    # CRUD + search/sort + JSON persistence
    │   ├── MovieManagerCli.java    # Interactive CLI menu
    │   └── gui
    │       ├── MovieManagerGui.java  # Main Swing window (table + toolbar)
    │       ├── MovieTableModel.java  # JTable model over the repository
    │       └── MovieDialog.java      # Add/edit form dialog
    └── test/java/com/example/movie
        └── RepositoryTests.java    # Dependency-free test runner
```

## License

Released under the [MIT License](LICENSE).
