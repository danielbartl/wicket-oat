package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonGroupBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import java.util.Objects;

/**
 * A container component that renders its items as Oat Pagination, reusing the
 * button-group markup: {@code <nav aria-label="Pagination"><menu class="buttons">}
 * with each page link wrapped in an {@code <li>}. The {@code <nav>} is named by the
 * {@code OatPagination.label} resource, unless the markup gives it an {@code aria-label}. The consumer's populateItem is
 * responsible for styling the current page (e.g. dropping {@code .outline} and
 * adding {@code aria-current="page"}), since only the data model knows which
 * item is current.
 *
 * <pre>
 * &lt;nav wicket:id="pagination"&gt;
 *     &lt;menu wicket:id="menu"&gt;
 *         &lt;li wicket:id="items"&gt;
 *             &lt;a wicket:id="link" class="button outline small"&gt;&lt;span wicket:id="label"&gt;&lt;/span&gt;&lt;/a&gt;
 *         &lt;/li&gt;
 *     &lt;/menu&gt;
 * &lt;/nav&gt;
 * </pre>
 *
 * @param <T> the type of the list items
 */
public abstract class OatPagination<T> extends WebMarkupContainer {

    public OatPagination(String id, IModel<List<T>> model) {
        super(id);

        WebMarkupContainer menu = new WebMarkupContainer("menu");
        menu.add(new ButtonGroupBehavior());
        add(menu);

        menu.add(new ListView<>("items", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatPagination.this.populateItem(item);
            }
        });
    }

    protected abstract void populateItem(ListItem<T> item);

    /** Names the {@code <nav>} {@code OatPagination.label} ("Pagination"), unless the markup does. */
    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (tag.getAttribute("aria-label") == null) {
            tag.put("aria-label", getString("OatPagination.label"));
        }
    }

    /**
     * A row of page links, without a subclass: {@code populateItem} receives each list item and its
     * object.
     */
    public static <T> OatPagination<T> of(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatPagination<T>(id, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}
