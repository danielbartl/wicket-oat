package dev.jbaby.wicket.oat;

import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;

import java.time.Duration;

/**
 * Keeps the theme in a cookie, so it survives the session, and caches it in the
 * session. The default {@link OatThemeStore}.
 */
public class CookieThemeStore extends SessionThemeStore {

    private final String cookieName;
    private final Duration maxAge;

    /** An {@code oat-theme} cookie kept for a year. */
    public CookieThemeStore() {
        this("oat-theme", Duration.ofDays(365));
    }

    public CookieThemeStore(String cookieName, Duration maxAge) {
        this.cookieName = cookieName;
        this.maxAge = maxAge;
    }

    @Override
    public String load() {
        String theme = super.load();
        return theme != null ? theme : cookies().load(cookieName);
    }

    @Override
    public void save(String theme) {
        super.save(theme);
        if (theme == null) {
            cookies().remove(cookieName);
        } else {
            cookies().save(cookieName, theme);
        }
    }

    private CookieUtils cookies() {
        CookieDefaults defaults = new CookieDefaults();
        defaults.setMaxAge((int) maxAge.toSeconds());
        defaults.setHttpOnly(true);
        defaults.setSameSite(CookieDefaults.SameSite.Lax);
        return new CookieUtils(defaults);
    }
}
