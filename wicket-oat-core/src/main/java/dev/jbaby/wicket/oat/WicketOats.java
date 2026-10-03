package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.OatAppLayout;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Central facade for configuring Wicket Oat UI in a WebApplication.
 */
public class WicketOats {

    private static final PackageResourceReference OAT_CSS = new PackageResourceReference(OatAppLayout.class, "oat.min.css");
    private static final PackageResourceReference OAT_JS = new PackageResourceReference(OatAppLayout.class, "oat.min.js");
    private static final PackageResourceReference THEMES_CSS = new PackageResourceReference(OatAppLayout.class, "themes.css");
    private static final PackageResourceReference WICKET_OAT_CSS = new PackageResourceReference(OatAppLayout.class, "wicket-oat.css");

    /**
     * Installs Wicket Oat UI into the given application.
     * This registers global CSS/JS contributors and adds the one Content Security
     * Policy source Oat needs on top of Wicket's strict default.
     * 
     * @param app the application to configure
     * @return the application's {@link OatSettings}, to configure themes
     */
    public static OatSettings install(WebApplication app) {
        // Register global header contributors
        app.getHeaderContributorListeners().add(response -> {
            response.render(CssHeaderItem.forReference(OAT_CSS));
            response.render(CssHeaderItem.forReference(THEMES_CSS));
            response.render(CssHeaderItem.forReference(WICKET_OAT_CSS));
            response.render(JavaScriptHeaderItem.forReference(OAT_JS));
        });

        // Oat works under Wicket's strict, nonce-based CSP: the library renders no inline
        // scripts or style attributes, and oat.min.js only sets styles through the CSSOM
        // (element.style.top = ...), which CSP doesn't restrict. The one addition is
        // img-src data:, for the SVG icons oat.min.css embeds as data: URLs (checkbox
        // tick, radio dot, select arrow). Anything else, such as image hosts for your own
        // content, is up to the application to add.
        app.getCspSettings().blocking().add(CSPDirective.IMG_SRC, "data:");

        return OatSettings.get(app);
    }
}
