package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.OatDensityBehavior;
import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A page's {@code <html>} tag, set up for Oat: the current user's theme
 * ({@code data-theme}), the configured density ({@code data-density}) and the page's
 * language ({@code lang}, from its locale). {@link OatAppLayout} and
 * {@link OatMobileLayout} use it; a base page of your own adds it the same way:
 * <pre>{@code
 * public abstract class BasePage extends WebPage {
 *     protected BasePage() {
 *         add(new OatHtml("html"));   // <html wicket:id="html">
 *     }
 * }
 * }</pre>
 * It is transparent, so the page's other components stay children of the page, wherever
 * they are in its markup.
 */
public class OatHtml extends TransparentWebMarkupContainer {

    public OatHtml(String id) {
        super(id);
        add(new OatThemeBehavior());
        add(new OatDensityBehavior());
        // The page's language, for screen readers' pronunciation and the browser's hyphenation
        add(AttributeModifier.replace("lang", (IModel<String>) () -> {
            String tag = getLocale().toLanguageTag();
            return "und".equals(tag) ? null : tag;
        }));
    }
}
