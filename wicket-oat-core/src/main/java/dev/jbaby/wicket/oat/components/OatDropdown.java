package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * A self-contained dropdown menu using the {@code ot-dropdown} web component
 * (JS vendored in oat.min.js) and the native popover API. Each menu item is
 * rendered with {@code role="menuitem"} inside the popover {@code <menu>}.
 *
 * @param <T> the type of the menu items
 */
public abstract class OatDropdown<T> extends Panel {

    private final WebMarkupContainer menu;

    public OatDropdown(String id, String triggerLabel, IModel<List<T>> model) {
        this(id, Model.of(triggerLabel), model);
    }

    public OatDropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model) {
        super(id);
        setRenderBodyOnly(true);

        menu = new WebMarkupContainer("menu");
        menu.setOutputMarkupId(true);
        add(menu);

        WebMarkupContainer trigger = new WebMarkupContainer("trigger");
        trigger.add(new Label("triggerLabel", triggerLabel));
        trigger.add(AttributeModifier.replace("popovertarget", (IModel<String>) menu::getMarkupId));
        add(trigger);

        menu.add(new ListView<>("items", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatDropdown.this.populateItem(item);
            }
        });
    }

    /** Populate a menu item; the item's root tag should carry {@code role="menuitem"}. */
    protected abstract void populateItem(ListItem<T> item);
}
