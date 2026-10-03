package dev.jbaby.wicket.oat;

import java.io.Serializable;

/**
 * Keeps the current user's theme choice, as a {@code data-theme} value.
 *
 * @see OatSettings#setThemeStore(OatThemeStore)
 */
public interface OatThemeStore extends Serializable {

    /** The stored theme value for the current user, or {@code null} if none. */
    String load();

    /** Stores the current user's theme value; {@code null} clears it. */
    void save(String theme);
}
