package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.components.OatAppLayout;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * Central facade for configuring Wicket Oat UI in a WebApplication.
 * <p>
 * The resource references are public so they can be swapped with Wicket's own
 * mechanism, e.g. to serve Oat from a CDN:
 * {@code addResourceReplacement(WicketOats.OAT_JS, new UrlResourceReference(Url.parse("https://.../oat.min.js")))}.
 */
public class WicketOats {

    /** Oat's stylesheet. */
    public static final CssResourceReference OAT_CSS = new CssResourceReference(OatAppLayout.class, "oat.min.css");
    /** Oat's web components and {@code ot.toast()}. */
    public static final JavaScriptResourceReference OAT_JS = new JavaScriptResourceReference(OatAppLayout.class, "oat.min.js");

    /**
     * Wicket Oat's own script, for context menus and loading more items on scroll. The
     * components that need it render it themselves; it isn't added to every page.
     */
    public static final JavaScriptResourceReference WICKET_OAT_JS = new JavaScriptResourceReference(OatAppLayout.class, "wicket-oat.js");
    /** The built-in themes beyond Oat's light/dark default. */
    public static final CssResourceReference THEMES_CSS = new CssResourceReference(OatAppLayout.class, "themes.css");
    /** Wicket Oat's own component rules. */
    public static final CssResourceReference WICKET_OAT_CSS = new CssResourceReference(OatAppLayout.class, "wicket-oat.css");

    /**
     * Installs Wicket Oat UI into the given application.
     * This registers global CSS/JS contributors and adds the one Content Security
     * Policy source Oat needs on top of Wicket's strict default.
     * 
     * @param app the application to configure
     * @return the application's {@link OatSettings}, to configure themes
     */
    public static OatSettings install(WebApplication app) {
        // Add Oat's CSS/JS to every page, unless the application opted out
        app.getHeaderContributorListeners().add(response -> {
            if (OatSettings.get(app).isAddResources()) {
                renderResources(response);
            }
        });

        // Oat works under Wicket's strict, nonce-based CSP: the library renders no inline
        // scripts or style attributes, and oat.min.js only sets styles through the CSSOM
        // (element.style.top = ...), which CSP doesn't restrict. The one addition is
        // img-src data:, for the SVG icons oat.min.css embeds as data: URLs (checkbox
        // tick, radio dot, select arrow). Anything else, such as image hosts for your own
        // content, is up to the application to add.
        app.getCspSettings().blocking().add(CSPDirective.IMG_SRC, "data:");

        // Busy state for Ajax buttons with an AjaxBusyBehavior (once, even if installed twice:
        // a second listener would make every click look like a repeated one)
        boolean busyListenerAdded = false;
        for (AjaxRequestTarget.IListener listener : app.getAjaxRequestTargetListeners()) {
            busyListenerAdded |= listener instanceof AjaxBusyBehavior.Listener;
        }
        if (!busyListenerAdded) {
            app.getAjaxRequestTargetListeners().add(new AjaxBusyBehavior.Listener());
        }

        return OatSettings.get(app);
    }

    /**
     * Renders Oat's CSS and JS. {@link #install} does this on every page; call it from a
     * page's {@code renderHead} after {@code OatSettings.setAddResources(false)} to add
     * them only where they're used.
     */
    public static void renderResources(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(OAT_CSS));
        response.render(CssHeaderItem.forReference(THEMES_CSS));
        response.render(CssHeaderItem.forReference(WICKET_OAT_CSS));
        response.render(JavaScriptHeaderItem.forReference(OAT_JS));
    }
}
