package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A sidebar navigation entry for {@link OatAppLayout}: either a link - a label and the
 * Wicket {@link Page} it opens, optionally with {@link PageParameters} - or a
 * {@linkplain #group group} of links under a heading, which the sidebar shows as a
 * collapsible section, open while one of its pages is shown. Entries for pages the
 * current user isn't authorized to instantiate are left out, and so is a group left
 * without entries.
 * <p>
 * A link can show a count or short text next to its label, such as unread messages,
 * with {@link #withBadge}:
 * <pre>{@code
 * MenuItem.group("Sales",
 *         MenuItem.of("Orders", OrdersPage.class).withBadge(() -> orders.countOpen()),
 *         MenuItem.of("Invoices", InvoicesPage.class))
 * }</pre>
 *
 * @param label the label; use a {@code ResourceModel} to translate it
 * @param pageClass the page to link to, or {@code null} for a group
 * @param parameters the page parameters, or {@code null}
 * @param badge the badge's content, or {@code null} for none
 * @param items a group's entries; empty for a link
 */
public record MenuItem(
        IModel<String> label,
        Class<? extends Page> pageClass,
        PageParameters parameters,
        IModel<?> badge,
        List<MenuItem> items
) implements Serializable {

    public MenuItem {
        Objects.requireNonNull(label, "label");
        items = items == null ? List.of() : List.copyOf(items);
        if (items.isEmpty()) {
            Objects.requireNonNull(pageClass, "pageClass: a menu item needs a page, or entries if it is a group");
        } else {
            if (pageClass != null) {
                throw new IllegalArgumentException("A group has entries instead of a page of its own");
            }
            for (MenuItem item : items) {
                if (item.isGroup()) {
                    throw new IllegalArgumentException("Groups can't be nested: " + item.label().getObject());
                }
            }
        }
    }

    /** A link to a page, with its parameters (or {@code null}). */
    public MenuItem(IModel<String> label, Class<? extends Page> pageClass, PageParameters parameters) {
        this(label, pageClass, parameters, null, List.of());
    }

    public MenuItem(String label, Class<? extends Page> pageClass) {
        this(Model.of(label), pageClass, null);
    }

    public static MenuItem of(String label, Class<? extends Page> pageClass) {
        return new MenuItem(Model.of(label), pageClass, null);
    }

    public static MenuItem of(String label, Class<? extends Page> pageClass, PageParameters parameters) {
        return new MenuItem(Model.of(label), pageClass, parameters);
    }

    public static MenuItem of(IModel<String> label, Class<? extends Page> pageClass) {
        return new MenuItem(label, pageClass, null);
    }

    public static MenuItem of(IModel<String> label, Class<? extends Page> pageClass, PageParameters parameters) {
        return new MenuItem(label, pageClass, parameters);
    }

    /** A collapsible section of links under a heading. Groups can't contain groups. */
    public static MenuItem group(String label, MenuItem... items) {
        return group(Model.of(label), List.of(items));
    }

    /** A collapsible section of links under a heading. Groups can't contain groups. */
    public static MenuItem group(IModel<String> label, MenuItem... items) {
        return group(label, List.of(items));
    }

    /** A collapsible section of links under a heading. Groups can't contain groups. */
    public static MenuItem group(IModel<String> label, List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A group needs at least one entry");
        }
        return new MenuItem(label, null, null, null, items);
    }

    /**
     * This link with a badge after its label, e.g. a count. The badge is read on every
     * render and hidden while its content is {@code null}, empty or zero, so
     * {@code withBadge(() -> inbox.unreadCount())} shows only when there's something unread.
     */
    public MenuItem withBadge(IModel<?> badge) {
        if (isGroup()) {
            throw new IllegalStateException("Only links have badges, not groups");
        }
        return new MenuItem(label, pageClass, parameters, badge, items);
    }

    /** This link with a fixed badge after its label, e.g. "New". */
    public MenuItem withBadge(String badge) {
        return withBadge(Model.of(badge));
    }

    /** Whether this is a group of entries rather than a link. */
    public boolean isGroup() {
        return !items.isEmpty();
    }
}
