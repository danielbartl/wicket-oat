package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;
import java.util.Objects;

/**
 * A breadcrumb trail: the levels above a page, from the top, each a link, separated by
 * "/". The last level is marked {@code aria-current="page"}, unless the trail ends with
 * the current page as text ({@link #setCurrent}). On a {@code <nav>}, named "Breadcrumb"
 * ({@code OatBreadcrumb.label}, translated); set another name with {@link #setLabel} or
 * an {@code aria-label} in the markup, e.g. for a second trail on the page:
 * <pre>{@code
 * <nav wicket:id="breadcrumb"></nav>
 *
 * add(OatBreadcrumb.of("breadcrumb", Model.ofList(folders), (item, folder) ->
 *         item.add(new BookmarkablePageLink<>("link", FolderPage.class, folder.parameters())
 *                 .add(new Label("label", folder.name())))));
 * }</pre>
 * A long trail can leave levels out ({@link #setMaxItems}): it shows the first, a "…"
 * button whose menu has the levels in between, and the last ones. A trail without levels
 * isn't shown. The levels are read once per request, and an item's model finds its level
 * by its index among all of them, as a {@code ListView}'s does.
 *
 * @param <T> the type of the list items
 */
public abstract class OatBreadcrumb<T> extends Panel {

    private final IModel<List<T>> levels;
    private IModel<String> current;
    private IModel<String> label;
    private int maxItems = Integer.MAX_VALUE;

    public OatBreadcrumb(String id, IModel<List<T>> model) {
        super(id);
        levels = OncePerRequestModel.of(Objects.requireNonNull(model));

        WebMarkupContainer list = new WebMarkupContainer("list");
        add(list);

        // When collapsed: the first level, "…" with the ones in between, and the last ones.
        // Each is a view of all the levels, from the index of its first one.
        list.add(new Trail("items") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setViewSize(isCollapsed() ? 1 : Integer.MAX_VALUE);
            }
        });

        WebMarkupContainer more = new WebMarkupContainer("more") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isCollapsed());
            }
        };
        list.add(more);
        WebMarkupContainer menu = new WebMarkupContainer("menu");
        menu.setOutputMarkupId(true);
        more.add(menu);
        WebMarkupContainer button = new WebMarkupContainer("button");
        button.add(AttributeModifier.replace("popovertarget", (IModel<String>) menu::getMarkupId));
        button.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatBreadcrumb.more", this).setDefaultValue("Show more levels")));
        button.add(new OatIcon("icon", "ellipsis"));
        more.add(button);
        menu.add(new ListView<>("hidden", levels) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setStartIndex(1);
                setViewSize(firstOfTail() - 1);
            }

            @Override
            protected void populateItem(ListItem<T> item) {
                OatBreadcrumb.this.populateItem(item);
                item.get("link").add(AttributeModifier.replace("role", "menuitem"));
            }
        });

        list.add(new Trail("tail") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isCollapsed());
                if (isCollapsed()) {
                    setStartIndex(firstOfTail());
                }
            }
        });

        WebMarkupContainer currentItem = new WebMarkupContainer("currentItem") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(hasCurrent());
            }
        };
        list.add(currentItem);
        currentItem.add(new Label("current", (IModel<String>) () -> current != null ? current.getObject() : null));
    }

    /** Levels of the trail, each a link and a separator. */
    private class Trail extends ListView<T> {

        Trail(String id) {
            super(id, levels);
        }

        @Override
        protected void populateItem(ListItem<T> item) {
            item.add(new Label("separator", "/"));
            OatBreadcrumb.this.populateItem(item);

            // The trail's last level, when it doesn't end with the current page as text
            boolean last = !hasCurrent() && item.getIndex() == levels().size() - 1;
            if (last) {
                item.get("link").add(AttributeModifier.replace("aria-current", "page"));
            }
            item.get("separator").setVisible(!last);
        }
    }

    private List<T> levels() {
        List<T> list = levels.getObject();
        return list != null ? list : List.of();
    }

    private boolean hasCurrent() {
        return current != null && current.getObject() != null;
    }

    /** Whether some levels are left out, as there are more than {@link #setMaxItems}. */
    private boolean isCollapsed() {
        return levels().size() + (hasCurrent() ? 1 : 0) > maxItems;
    }

    /** The index of the first level shown after "…". */
    private int firstOfTail() {
        int shownAfter = maxItems - 1 - (hasCurrent() ? 1 : 0);
        return levels().size() - shownAfter;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(!levels().isEmpty());
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (label != null) {
            tag.put("aria-label", label.getObject());
        } else if (tag.getAttribute("aria-label") == null) {
            tag.put("aria-label", getString("OatBreadcrumb.label"));
        }
    }

    /**
     * Populate the item's "link" (e.g. a {@code Link} or {@code BookmarkablePageLink})
     * wrapping a "label" child. The separator is managed automatically. A level left out
     * of a long trail is populated the same way, for the "…" menu.
     */
    protected abstract void populateItem(ListItem<T> item);

    /**
     * Ends the trail with the current page as text, e.g. its title, marked
     * {@code aria-current="page"}, rather than marking the last level. While the text is
     * {@code null}, the last level is marked instead.
     */
    public OatBreadcrumb<T> setCurrent(IModel<String> current) {
        this.current = OncePerRequestModel.of(current);
        return this;
    }

    /** The {@code <nav>}'s name for screen readers, instead of "Breadcrumb". */
    public OatBreadcrumb<T> setLabel(IModel<String> label) {
        this.label = label;
        return this;
    }

    public OatBreadcrumb<T> setLabel(String label) {
        return setLabel(Model.of(label));
    }

    /**
     * Shows at most this many levels, counting the current page: with more, the trail
     * shows the first, a "…" button whose menu has the levels in between, and the last
     * {@code maxItems - 1}. All of them by default.
     *
     * @param maxItems at least 2
     */
    public OatBreadcrumb<T> setMaxItems(int maxItems) {
        if (maxItems < 2) {
            throw new IllegalArgumentException("A breadcrumb shows at least its first and last level, not " + maxItems);
        }
        this.maxItems = maxItems;
        return this;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        levels.detach();
        if (current != null) {
            current.detach();
        }
        if (label != null) {
            label.detach();
        }
    }

    /**
     * A breadcrumb trail, without a subclass: {@code populateItem} receives each list item and its
     * object.
     */
    public static <T> OatBreadcrumb<T> of(String id, IModel<List<T>> model, SerializableBiConsumer<ListItem<T>, T> populateItem) {
        Objects.requireNonNull(populateItem);
        return new OatBreadcrumb<T>(id, model) {
            @Override
            protected void populateItem(ListItem<T> item) {
                populateItem.accept(item, item.getModelObject());
            }
        };
    }
}
