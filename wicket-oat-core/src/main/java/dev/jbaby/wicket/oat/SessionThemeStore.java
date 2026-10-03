package dev.jbaby.wicket.oat;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * Keeps the theme in the Wicket session's metadata, so it works with any session
 * class and lasts as long as the session.
 */
public class SessionThemeStore implements OatThemeStore {

    private static final MetaDataKey<String> KEY = new MetaDataKey<>() {};

    @Override
    public String load() {
        return Session.exists() ? Session.get().getMetaData(KEY) : null;
    }

    @Override
    public void save(String theme) {
        Session session = Session.get();
        session.setMetaData(KEY, theme);
        if (session.isTemporary()) {
            session.bind();
        }
    }
}
