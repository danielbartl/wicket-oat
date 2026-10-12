package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializablePredicate;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.Objects;

/**
 * A tab of {@link OatBottomNav}: an icon above a short label, linking to a Wicket
 * {@link Page}, optionally with {@link PageParameters}. Each {@code with...} method
 * returns a changed copy:
 * <pre>{@code
 * BottomNavItem.of("Today", TodayPage.class).withIcon(Icon.sprite("calendar")),
 * BottomNavItem.of("Team", TeamPage.class).withIcon(Icon.named("users"))
 *         .withBadge(() -> requests.countOpen())
 *         .withActive(page -> page instanceof TeamPage || page instanceof MemberPage),
 * BottomNavItem.of("Add absence", AddAbsencePage.class).withIcon(Icon.named("plus")).asPrimary()
 * }</pre>
 *
 * @param label the label; use a {@code ResourceModel} to translate it. A primary item
 *              shows no label, but screen readers announce it ({@code aria-label}).
 * @param icon the icon above the label, or {@code null} for none
 * @param pageClass the page to link to
 * @param parameters the page parameters, or {@code null}
 * @param active whether this tab is the current one on a given page, replacing the
 *               default rule (the item's page, and its parameters if it has any); or
 *               {@code null} for the default
 * @param badge a count on the icon, read on every render and hidden while it is
 *              {@code null} or 0; or {@code null} for none
 * @param primary whether this item is the bar's main action, a round button
 */
public record BottomNavItem(
        IModel<String> label,
        Icon icon,
        Class<? extends Page> pageClass,
        PageParameters parameters,
        SerializablePredicate<Page> active,
        IModel<Integer> badge,
        boolean primary
) implements Serializable {

    public BottomNavItem {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(pageClass, "pageClass");
        // A copy, so changing the caller's parameters later doesn't change the link
        parameters = parameters == null ? null : new PageParameters(parameters);
    }

    /** The page parameters (a copy), or {@code null}. */
    @Override
    public PageParameters parameters() {
        return parameters == null ? null : new PageParameters(parameters);
    }

    public static BottomNavItem of(String label, Class<? extends Page> pageClass) {
        return of(Model.of(label), pageClass, null);
    }

    public static BottomNavItem of(String label, Class<? extends Page> pageClass, PageParameters parameters) {
        return of(Model.of(label), pageClass, parameters);
    }

    public static BottomNavItem of(IModel<String> label, Class<? extends Page> pageClass) {
        return of(label, pageClass, null);
    }

    public static BottomNavItem of(IModel<String> label, Class<? extends Page> pageClass, PageParameters parameters) {
        return new BottomNavItem(label, null, pageClass, parameters, null, null, false);
    }

    /** This item with an icon above its label. */
    public BottomNavItem withIcon(Icon icon) {
        return new BottomNavItem(label, icon, pageClass, parameters, active, badge, primary);
    }

    /**
     * This item, the current tab on the pages {@code active} accepts instead of only on
     * its own page, e.g. on pages that belong to the tab without being its target. The
     * rule replaces the default one, so include the item's own page if it should count.
     */
    public BottomNavItem withActive(SerializablePredicate<Page> active) {
        return new BottomNavItem(label, icon, pageClass, parameters, active, badge, primary);
    }

    /**
     * This item with a count on its icon. The count is read on every render and hidden
     * while it is {@code null} or 0, so {@code withBadge(() -> inbox.unreadCount())}
     * shows only when there's something unread.
     */
    public BottomNavItem withBadge(IModel<Integer> badge) {
        return new BottomNavItem(label, icon, pageClass, parameters, active, badge, primary);
    }

    /**
     * This item as the bar's main action, such as "Add": a round button showing only its
     * icon (it needs one), never marked as the current tab. Use one per bar at most.
     */
    public BottomNavItem asPrimary() {
        return new BottomNavItem(label, icon, pageClass, parameters, active, badge, true);
    }

    /**
     * The icon of a {@link BottomNavItem}, drawn in the item's text color, so it follows
     * the current tab's color. Wicket Oat ships no icon set of its own for the bar:
     * reference a symbol of your page's SVG sprite, pass the SVG itself, or use one of
     * {@link OatIcon}'s icons.
     *
     * @param kind how {@code value} is drawn
     * @param value the symbol id, the SVG markup, or the {@code OatIcon} name
     */
    public record Icon(Kind kind, String value) implements Serializable {

        public enum Kind {
            /** A symbol of an SVG sprite on the page: {@code <svg><use href="#id"/></svg>}. */
            SPRITE,
            /** SVG markup written into the page as it is. */
            SVG,
            /** One of {@link OatIcon}'s icons, by name. */
            NAMED
        }

        public Icon {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(value, "value");
            if (kind == Kind.NAMED && !OatIcon.names().contains(value)) {
                throw new IllegalArgumentException("No icon \"" + value + "\"; the icons are " + OatIcon.names());
            }
        }

        /**
         * A symbol of an SVG sprite that your page supplies, e.g.
         * {@code <svg hidden><symbol id="calendar" viewBox="0 0 24 24" fill="none"
         * stroke="currentColor">...</symbol></svg>}. Draw it with {@code currentColor}, so
         * it takes the tab's color.
         *
         * @param symbolId the symbol's {@code id}, without {@code #}
         */
        public static Icon sprite(String symbolId) {
            return new Icon(Kind.SPRITE, symbolId);
        }

        /**
         * An inline SVG, e.g. {@code <svg viewBox="0 0 24 24" fill="none"
         * stroke="currentColor">...</svg>}, drawn with {@code currentColor} so it takes the
         * tab's color. It is written into the page without escaping, so only pass markup
         * you trust, never anything a user entered.
         */
        public static Icon svg(String markup) {
            return new Icon(Kind.SVG, markup);
        }

        /** One of {@link OatIcon}'s icons, e.g. {@code "plus"}; see {@link OatIcon#names()}. */
        public static Icon named(String name) {
            return new Icon(Kind.NAMED, name);
        }
    }
}
