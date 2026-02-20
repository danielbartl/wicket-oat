package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.AppLayout;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Central facade for configuring Wicket Oat UI in a WebApplication.
 */
public class WicketOats {

    private static final PackageResourceReference OAT_CSS = new PackageResourceReference(AppLayout.class, "oat.min.css");
    private static final PackageResourceReference OAT_JS = new PackageResourceReference(AppLayout.class, "oat.min.js");
    private static final PackageResourceReference THEMES_CSS = new PackageResourceReference(AppLayout.class, "themes.css");

    /**
     * Installs Wicket Oat UI into the given application.
     * This registers global CSS/JS contributors and configures the required CSP settings.
     * 
     * @param app the application to configure
     */
    public static void install(WebApplication app) {
        // Register global header contributors
        app.getHeaderContributorListeners().add(response -> {
            response.render(CssHeaderItem.forReference(OAT_CSS));
            response.render(CssHeaderItem.forReference(THEMES_CSS));
            response.render(JavaScriptHeaderItem.forReference(OAT_JS));
        });

        // Configure CSP for Oat UI compatibility
        // Oat UI (especially Toast) uses inline styles and dynamic scripts.
        // unsafeInline() removes nonces and allows 'unsafe-inline' for style-src and script-src.
        app.getCspSettings().blocking().unsafeInline();
    }
}
