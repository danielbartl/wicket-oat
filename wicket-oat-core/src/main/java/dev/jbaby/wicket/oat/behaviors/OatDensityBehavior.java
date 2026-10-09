package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatDensity;
import dev.jbaby.wicket.oat.OatSettings;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Sets the {@code data-density} attribute Oat's compact density is keyed on. Without a
 * density it applies the configured one ({@link OatSettings#getDensity()}) - put it on the
 * {@code <html>} tag, as {@code OatAppLayout} does. With a density it pins a component to
 * it, e.g. a compact table on an otherwise default page.
 */
public class OatDensityBehavior extends Behavior {

    private final OatDensity density;

    /** Applies the configured density. */
    public OatDensityBehavior() {
        this(null);
    }

    /** Applies the given density, or the configured one if {@code null}. */
    public OatDensityBehavior(OatDensity density) {
        this.density = density;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        OatDensity applied = density != null ? density : OatSettings.get().getDensity();
        if (applied.value() != null) {
            tag.put("data-density", applied.value());
        } else {
            tag.remove("data-density");
        }
    }
}
