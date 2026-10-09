package dev.jbaby.wicket.oat;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An Oat theme: the {@code data-theme} value its CSS is keyed on, plus a label and an
 * optional icon for theme pickers. The built-in themes are constants; a custom theme
 * defined in your own CSS is just a new instance registered with
 * {@link OatSettings#addTheme(OatTheme)}:
 *
 * <pre>
 * WicketOats.install(this).addTheme(new OatTheme("brand", "Brand"));
 * </pre>
 *
 * The current user's theme is read with {@link #current()} and changed with
 * {@link #setCurrent(OatTheme)}; where it is kept is up to the configured
 * {@link OatThemeStore}.
 *
 * @param value the {@code data-theme} value: lowercase letters, digits and dashes
 * @param label a human-readable name
 * @param icon a short symbol (e.g. an emoji) shown by {@code OatThemeSwitcher}, or {@code null} to show the label
 */
public record OatTheme(String value, String label, String icon) implements Serializable {

    private static final Pattern VALUE = Pattern.compile("[a-z0-9-]+");

    public static final OatTheme DARK = new OatTheme("dark", "Dark", "🌙");
    public static final OatTheme LIGHT = new OatTheme("light", "Light", "☀️");
    public static final OatTheme MIDNIGHT = new OatTheme("midnight", "Midnight", "🌌");
    public static final OatTheme NORD = new OatTheme("nord", "Nord", "❄️");
    public static final OatTheme EVERFOREST = new OatTheme("everforest", "Everforest", "🌲");
    public static final OatTheme TOKYO_NIGHT = new OatTheme("tokyo-night", "Tokyo Night", "🗼");
    public static final OatTheme ROSE_PINE_DAWN = new OatTheme("rose-pine-dawn", "Rose Pine Dawn", "🌸");
    public static final OatTheme ROYAL = new OatTheme("royal", "Royal", "👑");
    public static final OatTheme CLAY = new OatTheme("clay", "Clay", "🏺");
    public static final OatTheme CATPPUCCIN_MOCHA = new OatTheme("catppuccin-mocha", "Catppuccin Mocha", "☕");
    public static final OatTheme CATPPUCCIN_LATTE = new OatTheme("catppuccin-latte", "Catppuccin Latte", "🥛");
    public static final OatTheme MATERIAL = new OatTheme("material", "Material Design", "🎨");
    public static final OatTheme DAISY = new OatTheme("daisy", "Daisy UI Dark", "🌼");
    public static final OatTheme ULTRAVIOLET = new OatTheme("ultraviolet", "Ultraviolet", "🔮");
    public static final OatTheme HALLOWEEN = new OatTheme("halloween", "Halloween", "🎃");
    public static final OatTheme XMAS = new OatTheme("xmas", "Christmas", "🎄");
    public static final OatTheme WIREFRAME = new OatTheme("wireframe", "Wireframe", "📐");
    public static final OatTheme BUSINESS = new OatTheme("business", "Business", "💼");
    public static final OatTheme BUSINESS_DARK = new OatTheme("business-dark", "Business Dark", "🗂️");

    private static final List<OatTheme> BUILT_INS = List.of(DARK, LIGHT, MIDNIGHT, NORD, EVERFOREST,
            TOKYO_NIGHT, ROSE_PINE_DAWN, ROYAL, CLAY, CATPPUCCIN_MOCHA, CATPPUCCIN_LATTE, MATERIAL, DAISY,
            ULTRAVIOLET, HALLOWEEN, XMAS, WIREFRAME, BUSINESS, BUSINESS_DARK);

    public OatTheme {
        if (value == null || !VALUE.matcher(value).matches()) {
            throw new IllegalArgumentException("A theme value must be lowercase letters, digits and dashes, but was " + value);
        }
        if (label == null) {
            label = value;
        }
    }

    /** A theme without an icon; theme pickers show its label. */
    public OatTheme(String value, String label) {
        this(value, label, null);
    }

    /** The themes Oat ships with, in the order {@code OatThemeSwitcher} shows them. */
    public static List<OatTheme> builtIns() {
        return BUILT_INS;
    }

    /** The {@code data-theme} value; same as {@link #value()}. */
    public String getValue() {
        return value;
    }

    /**
     * The current user's theme: the stored choice if it is a registered theme,
     * otherwise the configured default, which may be {@code null} (follow the browser).
     */
    public static OatTheme current() {
        OatSettings settings = OatSettings.get();
        OatTheme stored = settings.findTheme(settings.getThemeStore().load());
        return stored != null ? stored : settings.getDefaultTheme();
    }

    /** Stores the current user's theme; {@code null} goes back to the default. */
    public static void setCurrent(OatTheme theme) {
        OatSettings.get().getThemeStore().save(theme == null ? null : theme.value());
    }
}
