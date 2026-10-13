package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonGroupBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import java.util.Objects;

/**
 * A container component that renders its items as an Oat Button Group.
 * Oat's connected/grouped button styling (rounded end corners, dividers between
 * buttons) only applies to &lt;li&gt; children of a &lt;menu class="buttons"&gt;, so
 * this repeats an explicit &lt;li&gt; per item rather than accepting loose buttons.
 *
 * <pre>
 * &lt;menu wicket:id="group"&gt;
 *     &lt;li wicket:id="items"&gt;
 *         &lt;button wicket:id="button"&gt;Label&lt;/button&gt;
 *     &lt;/li&gt;
 * &lt;/menu&gt;
 * </pre>
 *
 * @param <T> the type of the list items
 */
public abstract class OatButtonGroup<T> extends WebMarkupContainer {

    public OatButtonGroup(String id, IModel<List<T>> model) {
        super(id);
        add(new ButtonGroupBehavior());
        add(new ListView<>("items", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatButtonGroup.this.populateItem(item);
            }
        });
    }

    protected abstract void populateItem(ListItem<T> item);

    /**
     * A group of buttons, without a subclass: {@code populateItem} receives each list item and its
     * object.
     */
    public static <T> OatButtonGroup<T> of(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatButtonGroup<T>(id, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}
