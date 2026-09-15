package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.OatAppLayout;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
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

        // Configure CSP for Oat UI compatibility.
        // In Oat 0.8, the ot-dropdown web component positions its popover menu by
        // setting element.style.top/left directly from JS, which requires 'unsafe-inline'
        // for style-src; Oat's other dynamic components (toast, tabs, taginput, upload,
        // sidebar) only toggle classes/attributes and don't need it themselves.
        // unsafeInline() removes nonces and allows 'unsafe-inline' for style-src and script-src.
        app.getCspSettings().blocking().unsafeInline();
        
        app.getCspSettings().blocking()
                .add(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.IMG_SRC, "https://i.pravatar.cc")
                .add(CSPDirective.IMG_SRC, "https://images.unsplash.com");
    }
}
