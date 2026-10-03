package dev.jbaby.wicket.oat;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Application-wide Wicket Oat settings, returned by {@link WicketOats#install}:
 *
 * <pre>
 * WicketOats.install(this)
 *         .setDefaultTheme(OatTheme.LIGHT)
 *         .addTheme(new OatTheme("brand", "Brand"));
 * </pre>
 */
public class OatSettings implements Serializable {

    private static final MetaDataKey<OatSettings> KEY = new MetaDataKey<>() {};

    private OatTheme defaultTheme = OatTheme.DARK;
    private final List<OatTheme> themes = new ArrayList<>(OatTheme.builtIns());
    private OatThemeStore themeStore = new CookieThemeStore();
    private boolean addResources = true;

    /** The settings of the current application. */
    public static OatSettings get() {
        return get(Application.get());
    }

    public static synchronized OatSettings get(Application application) {
        OatSettings settings = application.getMetaData(KEY);
        if (settings == null) {
            settings = new OatSettings();
            application.setMetaData(KEY, settings);
        }
        return settings;
    }

    public OatTheme getDefaultTheme() {
        return defaultTheme;
    }

    /**
     * The theme for users who haven't chosen one; {@code DARK} by default. {@code null}
     * sets no {@code data-theme}, so Oat follows the browser's light/dark preference.
     */
    public OatSettings setDefaultTheme(OatTheme defaultTheme) {
        this.defaultTheme = defaultTheme;
        return this;
    }

    /** The themes users can choose from, e.g. in {@code OatThemeSwitcher}. */
    public List<OatTheme> getThemes() {
        return Collections.unmodifiableList(themes);
    }

    /** Makes a theme defined in your own CSS selectable. */
    public OatSettings addTheme(OatTheme theme) {
        themes.removeIf(t -> t.value().equals(theme.value()));
        themes.add(Objects.requireNonNull(theme));
        return this;
    }

    /** Replaces the selectable themes, e.g. to offer only light and dark. */
    public OatSettings setThemes(List<OatTheme> themes) {
        this.themes.clear();
        this.themes.addAll(themes);
        return this;
    }

    /** The selectable theme with the given {@code data-theme} value, or {@code null}. */
    public OatTheme findTheme(String value) {
        return value == null ? null : themes.stream().filter(t -> t.value().equals(value)).findFirst().orElse(null);
    }

    public boolean isAddResources() {
        return addResources;
    }

    /**
     * Whether Oat's CSS and JS are added to every page (the default). Turn it off to add
     * them yourself with {@link WicketOats#renderResources} only where Oat is used.
     */
    public OatSettings setAddResources(boolean addResources) {
        this.addResources = addResources;
        return this;
    }

    public OatThemeStore getThemeStore() {
        return themeStore;
    }

    /**
     * Where each user's theme is kept: a cookie, cached in the session, by default
     * ({@link CookieThemeStore}); {@link SessionThemeStore} for the session only, or
     * your own, e.g. backed by a user profile.
     */
    public OatSettings setThemeStore(OatThemeStore themeStore) {
        this.themeStore = Objects.requireNonNull(themeStore);
        return this;
    }
}
