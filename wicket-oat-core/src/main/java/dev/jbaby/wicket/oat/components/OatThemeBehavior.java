package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * A behavior that applies the current user's theme preference.
 * It adds the 'data-theme' attribute to the tag.
 */
public class OatThemeBehavior extends Behavior {

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        
        OatTheme theme = OatSession.get().getTheme();
        if (theme != null && theme.getValue() != null) {
            tag.put("data-theme", theme.getValue());
        } else {
            tag.remove("data-theme");
        }
    }
}
