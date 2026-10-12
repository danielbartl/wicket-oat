package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.OatDensityBehavior;
import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * The {@code <html>} tag of a layout page ({@link OatAppLayout}, {@link OatMobileLayout}):
 * the current user's theme, the configured density and the page's language.
 */
final class LayoutHtml extends TransparentWebMarkupContainer {

    LayoutHtml(String id) {
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
