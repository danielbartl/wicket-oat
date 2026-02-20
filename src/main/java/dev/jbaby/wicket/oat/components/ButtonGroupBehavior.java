package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Behavior that applies Oat's Button Group styling.
 * It adds class="buttons" and role="group".
 */
public class ButtonGroupBehavior extends Behavior {

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.append("class", "buttons", " ");
        tag.put("role", "group");
    }
}
