package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Sets the {@code data-theme} attribute Oat's themes are keyed on. Without a theme it
 * applies the current user's theme ({@link OatTheme#current()}) - put it on the
 * {@code <html>} tag, as {@code OatAppLayout} does. With a theme it pins a component
 * to that theme, whatever the user chose.
 */
public class OatThemeBehavior extends Behavior {

    private final OatTheme theme;

    /** Applies the current user's theme. */
    public OatThemeBehavior() {
        this(null);
    }

    /** Applies the given theme, or the current user's theme if {@code null}. */
    public OatThemeBehavior(OatTheme theme) {
        this.theme = theme;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        OatTheme applied = theme != null ? theme : OatTheme.current();
        if (applied != null) {
            tag.put("data-theme", applied.value());
        } else {
            tag.remove("data-theme");
        }
    }
}
