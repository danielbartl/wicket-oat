package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.AccordionBehavior;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import java.util.Objects;

/**
 * A specialized ListView that renders its items as an Oat Accordion.
 * The markup for this component should be a &lt;details&gt; tag.
 * Each item in the accordion is an exclusive group by default (only one open at a time).
 *
 * <pre>
 * &lt;details wicket:id="accordion"&gt;
 *     &lt;summary wicket:id="title"&gt;Item Title&lt;/summary&gt;
 *     &lt;div wicket:id="content"&gt;Item Content&lt;/div&gt;
 * &lt;/details&gt;
 * </pre>
 *
 * @param <T> the type of the list items
 */
public abstract class OatAccordion<T> extends ListView<T> {

    private final AccordionBehavior behavior;

    public OatAccordion(String id, IModel<List<T>> model) {
        this(id, model, true);
    }

    public OatAccordion(String id, IModel<List<T>> model, boolean exclusive) {
        super(id, model);
        this.behavior = new AccordionBehavior(exclusive);
        add(behavior);
    }

    @Override
    protected ListItem<T> newItem(int index, IModel<T> itemModel) {
        ListItem<T> item = super.newItem(index, itemModel);
        // Add the same behavior instance to the item so it can contribute the "name" attribute
        item.add(behavior);
        return item;
    }

    public boolean isExclusive() {
        return behavior.isExclusive();
    }

    public OatAccordion<T> setExclusive(boolean exclusive) {
        behavior.setExclusive(exclusive);
        return this;
    }

    /**
     * An accordion of exclusive sections, without a subclass: {@code populateItem}
     * receives each section's list item and object, e.g. to add its {@code title} and
     * {@code content}.
     */
    public static <T> OatAccordion<T> of(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        return of(id, model, true, populateItem);
    }

    /** An accordion without a subclass; {@code exclusive} keeps at most one section open. */
    public static <T> OatAccordion<T> of(String id, IModel<List<T>> model, boolean exclusive,
                                         SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatAccordion<T>(id, model, exclusive) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}
