package dev.jbaby.wicket.oat.app;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * The examples' user, signed in for the session by demo / demo on the sign-in page. A
 * real application keeps its user in its session or security framework.
 */
final class DemoUser {

    static final String EMAIL = "ada@example.com";

    private static final MetaDataKey<String> NAME = new MetaDataKey<>() {
    };

    private DemoUser() {
    }

    static void signIn() {
        Session session = Session.get();
        session.bind();
        session.setMetaData(NAME, "Ada Lovelace");
    }

    /** The signed-in user's name, or {@code null} while nobody is signed in. */
    static String name() {
        return Session.get().getMetaData(NAME);
    }
}
