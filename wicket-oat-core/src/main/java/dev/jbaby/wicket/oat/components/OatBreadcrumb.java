package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.BreadcrumbBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * A container component that renders its items as an Oat Breadcrumb trail.
 * The last item is automatically marked {@code aria-current="page"} and its
 * trailing separator is hidden.
 *
 * <pre>
 * &lt;nav wicket:id="breadcrumb"&gt;
 *     &lt;ol wicket:id="list"&gt;
 *         &lt;li wicket:id="items"&gt;
 *             &lt;a wicket:id="link" class="unstyled"&gt;&lt;span wicket:id="label"&gt;&lt;/span&gt;&lt;/a&gt;
 *             &lt;span wicket:id="separator" aria-hidden="true"&gt;/&lt;/span&gt;
 *         &lt;/li&gt;
 *     &lt;/ol&gt;
 * &lt;/nav&gt;
 * </pre>
 *
 * @param <T> the type of the list items
 */
public abstract class OatBreadcrumb<T> extends WebMarkupContainer {

    public OatBreadcrumb(String id, IModel<List<T>> model) {
        super(id);
        add(new BreadcrumbBehavior());

        WebMarkupContainer list = new WebMarkupContainer("list");
        list.add(AttributeModifier.append("class", "unstyled hstack"));
        list.add(AttributeModifier.append("style", "font-size: var(--text-7)"));
        add(list);

        list.add(new ListView<>("items", model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                item.add(new Label("separator", "/"));
                OatBreadcrumb.this.populateItem(item);

                boolean last = item.getIndex() == getViewSize() - 1;
                if (last) {
                    item.get("link").add(AttributeModifier.replace("aria-current", "page"));
                }
                item.get("separator").setVisible(!last);
            }
        });
    }

    /**
     * Populate the item's "link" (e.g. a {@code Link} or {@code BookmarkablePageLink})
     * wrapping a "label" child. The separator is managed automatically.
     */
    protected abstract void populateItem(ListItem<T> item);
}
