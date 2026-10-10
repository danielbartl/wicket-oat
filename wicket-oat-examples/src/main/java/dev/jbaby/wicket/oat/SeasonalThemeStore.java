package dev.jbaby.wicket.oat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

/**
 * The demo's theme store: a user's own choice, kept in a cookie as usual, and otherwise a
 * seasonal theme - Halloween on 31 October, Christmas from 10 December (15 days before
 * Christmas) to New Year's Eve. Outside those days it has nothing stored, so the
 * configured default theme applies.
 */
public class SeasonalThemeStore extends CookieThemeStore {

    private final Clock clock;

    public SeasonalThemeStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String load() {
        String chosen = super.load();
        if (chosen != null) {
            return chosen;
        }
        OatTheme seasonal = seasonalTheme(LocalDate.now(clock));
        return seasonal != null ? seasonal.value() : null;
    }

    /** The seasonal theme for a day, or {@code null} outside the seasons. */
    static OatTheme seasonalTheme(LocalDate day) {
        if (day.getMonth() == Month.OCTOBER && day.getDayOfMonth() == 31) {
            return OatTheme.HALLOWEEN;
        }
        if (day.getMonth() == Month.DECEMBER && day.getDayOfMonth() >= 10) {
            return OatTheme.XMAS;
        }
        return null;
    }
}
