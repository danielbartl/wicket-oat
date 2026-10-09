package dev.jbaby.wicket.oat;

/**
 * How tightly Oat lays out its components, set app-wide with
 * {@link OatSettings#setDensity(OatDensity)}:
 *
 * <pre>
 * WicketOats.install(this).setDensity(OatDensity.COMPACT);
 * </pre>
 *
 * It is independent of the theme, so any theme can be compact.
 */
public enum OatDensity {

    /** Oat's own spacing. */
    DEFAULT(null),

    /** A tighter spacing scale and smaller body text, for forms and tables in data-heavy apps. */
    COMPACT("compact");

    private final String value;

    OatDensity(String value) {
        this.value = value;
    }

    /** The {@code data-density} value, or {@code null} for {@link #DEFAULT}, which sets none. */
    public String value() {
        return value;
    }
}
