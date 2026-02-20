package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

/**
 * Behavior that applies Oat's Accordion styling and logic.
 * In Oat, an accordion is simply a sequence of &lt;details&gt; elements.
 * This behavior can be added to a ListView or any component that renders items.
 * If added to a ListView, it will automatically manage the "name" attribute
 * for all child items to ensure they behave as an exclusive group (only one open at a time).
 */
public class AccordionBehavior extends Behavior {

    private boolean exclusive = true;
    private String name;

    public AccordionBehavior() {
    }

    public AccordionBehavior(boolean exclusive) {
        this.exclusive = exclusive;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        // Ensure the component renders a tag that can contain children if it's a ListView
        if (component instanceof ListView) {
            component.setOutputMarkupId(true);
        }
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        
        // If this is a ListItem, and the parent has an AccordionBehavior, we apply the group name
        if (component instanceof ListItem) {
            applyToListItem((ListItem<?>) component, tag);
        }
    }

    private void applyToListItem(ListItem<?> item, ComponentTag tag) {
        ListView<?> listView = item.findParent(ListView.class);
        if (listView != null) {
            AccordionBehavior behavior = getBehavior(listView);
            if (behavior != null && behavior.isExclusive()) {
                tag.put("name", behavior.getGroupName(listView));
            }
        }
    }

    private AccordionBehavior getBehavior(Component component) {
        return component.getBehaviors(AccordionBehavior.class).stream().findFirst().orElse(null);
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public AccordionBehavior setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
        return this;
    }

    protected String getGroupName(Component component) {
        if (name == null) {
            name = "acc-" + component.getMarkupId();
        }
        return name;
    }
}
