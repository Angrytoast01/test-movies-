package com.example.movie.gui;

import com.example.movie.Movie;
import com.example.movie.MovieRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;

/**
 * The main Swing window for the Movie Manager.
 *
 * <p>Shows the collection in a table with a toolbar to add, edit and delete
 * movies, a live search box, and a sort selector. All operations delegate to a
 * shared {@link MovieRepository}, so GUI and CLI stay in sync via the same JSON
 * file.
 */
public class MovieManagerGui extends JFrame {

    private final MovieRepository repository;
    private final MovieTableModel tableModel = new MovieTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<SortOption> sortCombo = new JComboBox<>(SortOption.values());
    private final JLabel statusLabel = new JLabel();
    private final Path prefsFile;
    private Theme currentTheme;

    /** Pairs a human-readable label with a repository sort key. */
    private enum SortOption {
        NONE("None", null),
        TITLE("Title", MovieRepository.SortBy.TITLE),
        YEAR("Year", MovieRepository.SortBy.YEAR),
        RATING("Rating (high first)", MovieRepository.SortBy.RATING),
        DIRECTOR("Director", MovieRepository.SortBy.DIRECTOR);

        private final String label;
        private final MovieRepository.SortBy key;

        SortOption(String label, MovieRepository.SortBy key) {
            this.label = label;
            this.key = key;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public MovieManagerGui(MovieRepository repository, Theme theme, Path prefsFile) {
        super("🎬 Movie Manager");
        this.repository = repository;
        this.currentTheme = theme;
        this.prefsFile = prefsFile;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refresh();

        setSize(760, 460);
        setMinimumSize(new Dimension(620, 320));
        setLocationRelativeTo(null);
    }

    /* ---------------------------- Menu bar -------------------------- */

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenu themeMenu = new JMenu("Theme");

        JRadioButtonMenuItem lightItem = new JRadioButtonMenuItem("Light");
        JRadioButtonMenuItem darkItem = new JRadioButtonMenuItem("Dark");
        ButtonGroup group = new ButtonGroup();
        group.add(lightItem);
        group.add(darkItem);
        if (currentTheme == Theme.DARK) {
            darkItem.setSelected(true);
        } else {
            lightItem.setSelected(true);
        }
        lightItem.addActionListener(e -> switchTheme(Theme.LIGHT));
        darkItem.addActionListener(e -> switchTheme(Theme.DARK));
        themeMenu.add(lightItem);
        themeMenu.add(darkItem);
        viewMenu.add(themeMenu);

        // Quick toggle: Ctrl/Cmd + D flips between light and dark.
        JMenuItemToggle toggle = new JMenuItemToggle(lightItem, darkItem);
        JMenuItem toggleItem = new JMenuItem("Toggle Light / Dark");
        int shortcutMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        toggleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcutMask));
        toggleItem.addActionListener(e -> {
            switchTheme(currentTheme.opposite());
            toggle.sync(currentTheme);
        });
        viewMenu.add(toggleItem);

        menuBar.add(viewMenu);
        return menuBar;
    }

    /** Small helper to keep the radio items in sync when toggling via shortcut. */
    private record JMenuItemToggle(JRadioButtonMenuItem light, JRadioButtonMenuItem dark) {
        void sync(Theme theme) {
            if (theme == Theme.DARK) {
                dark.setSelected(true);
            } else {
                light.setSelected(true);
            }
        }
    }

    private void switchTheme(Theme theme) {
        if (theme == currentTheme) {
            return;
        }
        currentTheme = theme;
        Theme.apply(theme);
        theme.save(prefsFile);
    }

    /* ------------------------------ UI ------------------------------ */

    private JToolBar buildToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> onAdd());

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> onEdit());

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> onDelete());

        bar.add(addBtn);
        bar.add(editBtn);
        bar.add(deleteBtn);
        bar.addSeparator();

        bar.add(new JLabel("Search: "));
        searchField.setMaximumSize(new Dimension(220, 28));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });
        bar.add(searchField);

        bar.add(Box.createHorizontalStrut(12));
        bar.add(new JLabel("Sort: "));
        sortCombo.setMaximumSize(new Dimension(160, 28));
        sortCombo.addActionListener(e -> refresh());
        bar.add(sortCombo);

        bar.add(Box.createHorizontalGlue());
        return bar;
    }

    private JScrollPane buildTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(false); // sorting handled via repository

        // Sensible column widths.
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(220);  // Title
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Director
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Genre
        table.getColumnModel().getColumn(4).setPreferredWidth(60);   // Year
        table.getColumnModel().getColumn(5).setPreferredWidth(60);   // Rating
        table.getColumnModel().getColumn(6).setPreferredWidth(70);   // Watched

        // Double-click a row to edit it.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onEdit();
                }
            }
        });

        // Toggling the "Watched" checkbox in the table persists immediately.
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                int row = e.getFirstRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    Movie m = tableModel.getMovieAt(row);
                    boolean watched = (Boolean) tableModel.getValueAt(row, 6);
                    if (m.isWatched() != watched) {
                        repository.setWatched(m.getId(), watched);
                        updateStatus();
                    }
                }
            }
        });

        return new JScrollPane(table);
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    /* ---------------------------- Actions --------------------------- */

    private void onAdd() {
        MovieDialog.Result r = MovieDialog.showDialog(this, null);
        if (r != null) {
            repository.add(r.title(), r.director(), r.genre(), r.year(), r.rating(), r.watched());
            refresh();
        }
    }

    private void onEdit() {
        Movie selected = selectedMovie();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a movie to edit.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MovieDialog.Result r = MovieDialog.showDialog(this, selected);
        if (r != null) {
            repository.update(selected.getId(), r.title(), r.director(), r.genre(),
                    r.year(), r.rating(), r.watched());
            refresh();
        }
    }

    private void onDelete() {
        Movie selected = selectedMovie();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a movie to delete.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getTitle() + "\"?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            repository.delete(selected.getId());
            refresh();
        }
    }

    private Movie selectedMovie() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return tableModel.getMovieAt(row);
    }

    /* ---------------------------- Helpers --------------------------- */

    /** Rebuilds the table view from the repository, applying search + sort. */
    private void refresh() {
        String term = searchField.getText();
        List<Movie> view;

        SortOption sort = (SortOption) sortCombo.getSelectedItem();
        if (sort != null && sort.key != null) {
            // Sort the full collection, then filter by the search term.
            view = repository.findAllSorted(sort.key);
            if (term != null && !term.isBlank()) {
                String needle = term.toLowerCase();
                view = view.stream().filter(m -> matches(m, needle)).toList();
            }
        } else {
            view = repository.search(term);
        }

        tableModel.setMovies(view);
        updateStatus();
    }

    private static boolean matches(Movie m, String lowerNeedle) {
        return contains(m.getTitle(), lowerNeedle)
                || contains(m.getDirector(), lowerNeedle)
                || contains(m.getGenre(), lowerNeedle);
    }

    private static boolean contains(String s, String lowerNeedle) {
        return s != null && s.toLowerCase().contains(lowerNeedle);
    }

    private void updateStatus() {
        int shown = tableModel.getRowCount();
        int total = repository.count();
        if (shown == total) {
            statusLabel.setText(total + " movie" + (total == 1 ? "" : "s"));
        } else {
            statusLabel.setText("Showing " + shown + " of " + total + " movies");
        }
    }

    /* ----------------------------- Launch --------------------------- */

    /**
     * Launches the GUI on the Swing event-dispatch thread.
     *
     * @param repository the shared movie repository
     * @param prefsFile  file used to remember the theme choice (may be null)
     */
    public static void launch(MovieRepository repository, Path prefsFile) {
        SwingUtilities.invokeLater(() -> {
            // Use the cross-platform (Metal) look and feel: its colours are fully
            // driven by UIManager defaults, which lets our themes recolour every
            // component — including on platforms whose native L&F ignores them.
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to whatever default is available.
            }

            Theme theme = Theme.load(prefsFile);
            theme.install();

            new MovieManagerGui(repository, theme, prefsFile).setVisible(true);
        });
    }
}
