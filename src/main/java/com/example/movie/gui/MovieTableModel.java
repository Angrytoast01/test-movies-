package com.example.movie.gui;

import com.example.movie.Movie;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link javax.swing.table.TableModel} that displays a list of {@link Movie}
 * objects in a {@link javax.swing.JTable}.
 *
 * <p>The model holds its own snapshot of the rows currently being displayed
 * (which may be a filtered/sorted view). Call {@link #setMovies(List)} to swap
 * in a new view.
 */
public class MovieTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Title", "Director", "Genre", "Year", "Rating", "Watched"
    };

    private List<Movie> movies = new ArrayList<>();

    public void setMovies(List<Movie> movies) {
        this.movies = new ArrayList<>(movies);
        fireTableDataChanged();
    }

    /** Returns the movie displayed on the given view row. */
    public Movie getMovieAt(int rowIndex) {
        return movies.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return movies.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 4 -> Integer.class;      // ID, Year
            case 5 -> Double.class;          // Rating
            case 6 -> Boolean.class;         // Watched
            default -> String.class;         // Title, Director, Genre
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Movie m = movies.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> m.getId();
            case 1 -> m.getTitle();
            case 2 -> m.getDirector();
            case 3 -> m.getGenre();
            case 4 -> m.getYear();
            case 5 -> m.getRating();
            case 6 -> m.isWatched();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // The "Watched" checkbox column is editable directly in the table.
        return columnIndex == 6;
    }
}
