package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Behavior that applies Oat's input group styling to a {@code <fieldset>}: its
 * {@code <input>}s, {@code <select>}s and buttons are joined into one control, with any
 * direct {@code <label>} or {@code <legend>} children shown as text addons.
 * It adds class="group".
 */
public class InputGroupBehavior extends Behavior {

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.append("class", "group", " ");
    }
}
