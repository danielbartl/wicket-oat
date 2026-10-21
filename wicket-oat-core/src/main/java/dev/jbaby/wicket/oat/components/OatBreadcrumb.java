package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;
import java.util.Objects;

/**
 * A breadcrumb trail: the levels above a page, from the top, each a link, separated by
 * "/". The last level is marked {@code aria-current="page"}, unless the trail ends with
 * the current page as text ({@link #setCurrent}). On a {@code <nav>}, named by the
 * {@code OatBreadcrumb.label} resource ("Breadcrumb"):
 * <pre>{@code
 * <nav wicket:id="breadcrumb"></nav>
 *
 * add(OatBreadcrumb.of("breadcrumb", Model.ofList(folders), (item, folder) ->
 *         item.add(new BookmarkablePageLink<>("link", FolderPage.class, folder.parameters())
 *                 .add(new Label("label", folder.name())))));
 * }</pre>
 * A long trail can leave levels out ({@link #setMaxItems}): it shows the first, a "…"
 * button whose menu has the levels in between, and the last ones.
 *
 * @param <T> the type of the list items
 */
public abstract class OatBreadcrumb<T> extends Panel {

    private final IModel<List<T>> model;
    private final IModel<List<T>> levels;
    private IModel<String> current;
    private int maxItems = Integer.MAX_VALUE;

    public OatBreadcrumb(String id, IModel<List<T>> model) {
        super(id);
        this.model = Objects.requireNonNull(model);
        // Read once per request, however often the parts of the trail ask
        levels = LoadableDetachableModel.of(() -> {
            List<T> list = model.getObject();
            return list != null ? list : List.of();
        });
        add(AttributeModifier.replace("aria-label", new StringResourceModel("OatBreadcrumb.label", this).setDefaultValue("Breadcrumb")));

        WebMarkupContainer list = new WebMarkupContainer("list");
        add(list);

        // When collapsed: the first level, "…" with the ones in between, and the last ones
        list.add(trail("items", () -> isCollapsed() ? levels.getObject().subList(0, 1) : levels.getObject(), false));

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
        menu.add(new ListView<>("hidden", (IModel<List<T>>) () -> isCollapsed() ? levels.getObject().subList(1, firstOfTail()) : List.of()) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OatBreadcrumb.this.populateItem(item);
                item.get("link").add(AttributeModifier.replace("role", "menuitem"));
            }
        });

        list.add(trail("tail", () -> isCollapsed() ? levels.getObject().subList(firstOfTail(), levels.getObject().size()) : List.of(), true));

        WebMarkupContainer currentItem = new WebMarkupContainer("currentItem") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(current != null);
            }
        };
        list.add(currentItem);
        currentItem.add(new Label("current", (IModel<String>) () -> current != null ? current.getObject() : null));
    }

    private ListView<T> trail(String id, IModel<List<T>> items, boolean tail) {
        return new ListView<>(id, items) {
            @Override
            protected void populateItem(ListItem<T> item) {
                item.add(new Label("separator", "/"));
                OatBreadcrumb.this.populateItem(item);

                // The trail's last level, when it doesn't end with the current page as text
                boolean last = current == null && (tail || !isCollapsed()) && item.getIndex() == getViewSize() - 1;
                if (last) {
                    item.get("link").add(AttributeModifier.replace("aria-current", "page"));
                }
                item.get("separator").setVisible(!last);
            }
        };
    }

    /** Whether some levels are left out, as there are more than {@link #setMaxItems}. */
    private boolean isCollapsed() {
        return levels.getObject().size() + (current != null ? 1 : 0) > maxItems;
    }

    /** The index of the first level shown after "…". */
    private int firstOfTail() {
        int shownAfter = maxItems - 1 - (current != null ? 1 : 0);
        return levels.getObject().size() - shownAfter;
    }

    /**
     * Populate the item's "link" (e.g. a {@code Link} or {@code BookmarkablePageLink})
     * wrapping a "label" child. The separator is managed automatically. A level left out
     * of a long trail is populated the same way, for the "…" menu.
     */
    protected abstract void populateItem(ListItem<T> item);

    /**
     * Ends the trail with the current page as text, e.g. its title, marked
     * {@code aria-current="page"}, rather than marking the last level.
     */
    public OatBreadcrumb<T> setCurrent(IModel<String> current) {
        this.current = current;
        return this;
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
        model.detach();
        if (current != null) {
            current.detach();
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
