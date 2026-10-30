package com.example.movie.gui;

import com.example.movie.Movie;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A modal dialog for creating or editing a {@link Movie}.
 *
 * <p>Use {@link #showDialog(Frame, Movie)} to display it. It returns the entered
 * values as a {@link Result}, or {@code null} if the user cancelled.
 */
public class MovieDialog extends JDialog {

    /** Simple value holder for the dialog's form fields. */
    public record Result(String title, String director, String genre,
                         int year, double rating, boolean watched) {
    }

    private final JTextField titleField = new JTextField(20);
    private final JTextField directorField = new JTextField(20);
    private final JTextField genreField = new JTextField(20);
    private final JSpinner yearSpinner =
            new JSpinner(new SpinnerNumberModel(2000, 1888, 2100, 1));
    private final JSpinner ratingSpinner =
            new JSpinner(new SpinnerNumberModel(5.0, 0.0, 10.0, 0.1));
    private final JCheckBox watchedCheck = new JCheckBox("Watched");

    private Result result;

    private MovieDialog(Frame owner, Movie existing) {
        super(owner, existing == null ? "Add Movie" : "Edit Movie", true);
        buildUi();
        if (existing != null) {
            populate(existing);
        }
        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog.
     *
     * @param owner    parent frame (for centering/modality)
     * @param existing the movie to edit, or {@code null} to add a new one
     * @return the entered values, or {@code null} if cancelled
     */
    public static Result showDialog(Frame owner, Movie existing) {
        MovieDialog dialog = new MovieDialog(owner, existing);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(form, c, row++, "Title:", titleField);
        addRow(form, c, row++, "Director:", directorField);
        addRow(form, c, row++, "Genre:", genreField);
        addRow(form, c, row++, "Year:", yearSpinner);
        addRow(form, c, row++, "Rating (0-10):", ratingSpinner);

        // Watched checkbox spanning the value column.
        c.gridx = 1;
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        form.add(watchedCheck, c);

        JButton ok = new JButton("Save");
        ok.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(ok);

        getRootPane().setDefaultButton(ok);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        JLabel l = new JLabel(label, SwingConstants.RIGHT);
        panel.add(l, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(field, c);
    }

    private void populate(Movie m) {
        titleField.setText(m.getTitle());
        directorField.setText(m.getDirector());
        genreField.setText(m.getGenre());
        // Clamp year/rating into the spinner's allowed range defensively.
        yearSpinner.setValue(clamp(m.getYear(), 1888, 2100));
        ratingSpinner.setValue(clampRating(m.getRating()));
        watchedCheck.setSelected(m.isWatched());
    }

    private void onSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocusInWindow();
            return;
        }
        int year = ((Number) yearSpinner.getValue()).intValue();
        double rating = ((Number) ratingSpinner.getValue()).doubleValue();

        result = new Result(
                title,
                directorField.getText().trim(),
                genreField.getText().trim(),
                year,
                Math.round(rating * 10.0) / 10.0,
                watchedCheck.isSelected());
        dispose();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampRating(double value) {
        return Math.max(0.0, Math.min(10.0, value));
    }
}
