package com.example.movie.gui;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Window;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Light and dark themes for the Swing UI, applied purely through
 * {@link UIManager} colour defaults so no external look-and-feel library is
 * required (keeping the project dependency-free).
 *
 * <p>The selected theme can be changed at runtime via
 * {@link #apply(Theme)} followed by a UI refresh, and is remembered across runs
 * through a small preference file.
 */
public enum Theme {

    LIGHT("Light"),
    DARK("Dark");

    private final String label;

    Theme(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Returns the other theme — handy for a toggle action. */
    public Theme opposite() {
        return this == DARK ? LIGHT : DARK;
    }

    /* --------------------------- Palettes --------------------------- */

    // Dark palette
    private static final Color D_BG = new Color(0x2B2B2B);
    private static final Color D_BG_ALT = new Color(0x3C3F41);
    private static final Color D_FG = new Color(0xE0E0E0);
    private static final Color D_SELECTION = new Color(0x2F65CA);
    private static final Color D_SELECTION_FG = Color.WHITE;
    private static final Color D_GRID = new Color(0x4A4A4A);
    private static final Color D_BORDER = new Color(0x555555);

    // Light palette
    private static final Color L_BG = new Color(0xF2F2F2);
    private static final Color L_BG_ALT = Color.WHITE;
    private static final Color L_FG = new Color(0x1E1E1E);
    private static final Color L_SELECTION = new Color(0x3875D7);
    private static final Color L_SELECTION_FG = Color.WHITE;
    private static final Color L_GRID = new Color(0xD0D0D0);
    private static final Color L_BORDER = new Color(0xBBBBBB);

    /**
     * Applies this theme to the global {@link UIManager} defaults. Call
     * {@link javax.swing.SwingUtilities#updateComponentTreeUI(java.awt.Component)}
     * on open windows afterwards for the change to take effect on live UI.
     */
    public void install() {
        if (this == DARK) {
            applyPalette(D_BG, D_BG_ALT, D_FG, D_SELECTION, D_SELECTION_FG, D_GRID, D_BORDER);
        } else {
            applyPalette(L_BG, L_BG_ALT, L_FG, L_SELECTION, L_SELECTION_FG, L_GRID, L_BORDER);
        }
    }

    private static void applyPalette(Color bg, Color bgAlt, Color fg,
                                     Color selection, Color selectionFg,
                                     Color grid, Color border) {
        // Backgrounds
        put("Panel.background", bg);
        put("OptionPane.background", bg);
        put("ToolBar.background", bg);
        put("MenuBar.background", bg);
        put("Menu.background", bg);
        put("MenuItem.background", bg);
        put("Viewport.background", bgAlt);
        put("ScrollPane.background", bg);

        put("TextField.background", bgAlt);
        put("FormattedTextField.background", bgAlt);
        put("Spinner.background", bgAlt);
        put("ComboBox.background", bgAlt);
        put("List.background", bgAlt);
        put("Table.background", bgAlt);
        put("Table.focusCellBackground", bgAlt);

        put("Button.background", bg);
        put("CheckBox.background", bg);
        put("Label.background", bg);
        put("TableHeader.background", bg);

        // Foregrounds (text)
        for (String key : new String[]{
                "Panel.foreground", "OptionPane.foreground", "OptionPane.messageForeground",
                "Label.foreground", "Button.foreground", "CheckBox.foreground",
                "TextField.foreground", "FormattedTextField.foreground", "Spinner.foreground",
                "ComboBox.foreground", "List.foreground", "Table.foreground",
                "TableHeader.foreground", "ToolBar.foreground", "Menu.foreground",
                "MenuItem.foreground", "MenuBar.foreground", "TitledBorder.titleColor"}) {
            put(key, fg);
        }

        // Caret so the text cursor is visible on dark fields.
        put("TextField.caretForeground", fg);
        put("FormattedTextField.caretForeground", fg);

        // Selection
        put("Table.selectionBackground", selection);
        put("Table.selectionForeground", selectionFg);
        put("List.selectionBackground", selection);
        put("List.selectionForeground", selectionFg);
        put("ComboBox.selectionBackground", selection);
        put("ComboBox.selectionForeground", selectionFg);
        put("TextField.selectionBackground", selection);
        put("TextField.selectionForeground", selectionFg);

        // Grid / borders
        put("Table.gridColor", grid);
        put("TitledBorder.border", border);
    }

    private static void put(String key, Color color) {
        UIManager.put(key, new ColorUIResource(color));
    }

    /**
     * Applies a theme and immediately repaints every open window so the change
     * is visible without restarting.
     */
    public static void apply(Theme theme) {
        theme.install();
        for (Window window : Window.getWindows()) {
            javax.swing.SwingUtilities.updateComponentTreeUI(window);
        }
    }

    /* ------------------------- Persistence -------------------------- */

    /**
     * Loads the saved theme preference, defaulting to {@link #DARK} if none is
     * stored or the file cannot be read.
     */
    public static Theme load(Path prefsFile) {
        try {
            if (prefsFile != null && Files.exists(prefsFile)) {
                String content = Files.readString(prefsFile, StandardCharsets.UTF_8).trim();
                for (String line : content.split("\\R")) {
                    if (line.startsWith("theme=")) {
                        String value = line.substring("theme=".length()).trim().toUpperCase(Locale.ROOT);
                        if (value.equals("LIGHT")) {
                            return LIGHT;
                        }
                        if (value.equals("DARK")) {
                            return DARK;
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // Fall through to default.
        }
        return DARK;
    }

    /** Persists the theme preference. Failures are silently ignored. */
    public void save(Path prefsFile) {
        if (prefsFile == null) {
            return;
        }
        try {
            if (prefsFile.getParent() != null) {
                Files.createDirectories(prefsFile.getParent());
            }
            Files.writeString(prefsFile, "theme=" + name() + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save theme preference", e);
        }
    }
}
