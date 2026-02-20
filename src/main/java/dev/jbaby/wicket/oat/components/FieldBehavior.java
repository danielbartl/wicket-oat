package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Behavior that applies Oat's Field styling.
 * It adds data-field attribute.
 */
public class FieldBehavior extends Behavior {

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.put("data-field", "");
    }
}
