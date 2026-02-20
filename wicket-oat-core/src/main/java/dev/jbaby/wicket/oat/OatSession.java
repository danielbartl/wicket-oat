package dev.jbaby.wicket.oat;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

public class OatSession extends WebSession {

    private OatTheme theme = OatTheme.DARK;

    public OatSession(Request request) {
        super(request);
    }

    public static OatSession get() {
        return (OatSession) Session.get();
    }

    public OatTheme getTheme() {
        return theme;
    }

    public void setTheme(OatTheme theme) {
        this.theme = theme;
        dirty();
    }
}
