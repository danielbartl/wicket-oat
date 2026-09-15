package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonGroupBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * A container component that renders its items as Oat Pagination, reusing the
 * button-group markup: {@code <nav aria-label="Pagination"><menu class="buttons">}
 * with each page link wrapped in an {@code <li>}. The consumer's populateItem is
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
        add(AttributeModifier.append("aria-label", "Pagination"));

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
}
