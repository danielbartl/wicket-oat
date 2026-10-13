package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import java.util.Objects;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxEventBehavior;

/**
 * A self-contained dropdown menu using the {@code ot-dropdown} web component
 * (JS vendored in oat.min.js) and the native popover API. Each menu item is
 * rendered with {@code role="menuitem"} inside the popover {@code <menu>}.
 *
 * @param <T> the type of the menu items
 */
public abstract class OatDropdown<T> extends Panel {

    private final WebMarkupContainer menu;
    private final WebMarkupContainer trigger;

    public OatDropdown(String id, String triggerLabel, IModel<List<T>> model) {
        this(id, Model.of(triggerLabel), model);
    }

    public OatDropdown(String id, IModel<String> triggerLabel, IModel<List<T>> model) {
        super(id);
        setRenderBodyOnly(true);

        menu = new WebMarkupContainer("menu");
        menu.setOutputMarkupId(true);
        add(menu);

        trigger = new WebMarkupContainer("trigger");
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

    /** The button that opens the menu, e.g. to add an {@code aria-label} or change its style. */
    public WebMarkupContainer getTrigger() {
        return trigger;
    }

    /** Closes the menu, e.g. after handling a click on one of its items. */
    public OatDropdown<T> close(IPartialPageRequestHandler target) {
        target.appendJavaScript("(function(m){if(m&&m.matches(':popover-open'))m.hidePopover();})(document.getElementById('"
                + menu.getMarkupId() + "'))");
        return this;
    }

    /**
     * A dropdown menu without a subclass: {@code populateItem} receives each menu item and
     * its object, to add the item's content.
     */
    public static <T> OatDropdown<T> of(String id, String triggerLabel, IModel<List<T>> model,
                                        SerializableBiConsumer<ListItem<T>, T> populateItem) {
        return of(id, Model.of(triggerLabel), model, populateItem);
    }

    /** A dropdown menu without a subclass; {@code populateItem} fills each menu item. */
    public static <T> OatDropdown<T> of(String id, IModel<String> triggerLabel, IModel<List<T>> model,
                                        SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatDropdown<T>(id, triggerLabel, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }

    /**
     * A menu of actions: each object is an item labelled by {@code label}; clicking it runs
     * {@code onClick} over Ajax and closes the menu. The menu's markup gives each item a
     * {@code label}.
     */
    public static <T> OatDropdown<T> of(String id, String triggerLabel, IModel<List<T>> model,
                                        SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        return of(id, Model.of(triggerLabel), model, label, onClick);
    }

    /** A menu of actions, each labelled by {@code label} and running {@code onClick} when clicked. */
    public static <T> OatDropdown<T> of(String id, IModel<String> triggerLabel, IModel<List<T>> model,
                                        SerializableFunction<T, String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(onClick);
        return new OatDropdown<T>(id, triggerLabel, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                item.add(new Label("label", label.apply(item.getModelObject())));
                item.add(AjaxEventBehavior.onEvent("click", target -> {
                    onClick.accept(target, item.getModelObject());
                    close(target);
                }));
            }
        };
    }
}
