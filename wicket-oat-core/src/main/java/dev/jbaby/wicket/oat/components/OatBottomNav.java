package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import java.util.List;

/**
 * A tab bar fixed to the bottom of a phone's screen, for an app's three to five main
 * pages: each tab an icon above a short label, the current page's tab marked
 * ({@code aria-current="page"}) and drawn in the primary color. One item can be the
 * bar's main action, such as "Add", shown as a round button ({@link BottomNavItem#asPrimary}).
 * <pre>{@code
 * add(new OatBottomNav("tabs", List.of(
 *         BottomNavItem.of("Today", TodayPage.class).withIcon(Icon.named("calendar")),
 *         BottomNavItem.of("Add", AddPage.class).withIcon(Icon.named("plus")).asPrimary(),
 *         BottomNavItem.of("Team", TeamPage.class).withIcon(Icon.named("users")))));
 * }</pre>
 * on {@code <nav wicket:id="tabs"></nav>}. {@link OatMobileLayout} is a base page with
 * the bar already in place.
 * <p>
 * The bar shows on screens up to 768px wide, where Oat's grid switches to phones too;
 * {@link #setAlwaysVisible} keeps it on wider ones. Its rules are in {@code wicket-oat.css}.
 * Content under it needs room at the bottom, which {@code OatMobileLayout} leaves; in a
 * layout of your own, give it
 * {@code padding-block-end: calc(var(--oat-bottom-nav-height) + env(safe-area-inset-bottom, 0px))}.
 * The safe area (the iPhone's home indicator) needs {@code viewport-fit=cover} in the
 * page's viewport meta tag.
 * <p>
 * The {@code <nav>}'s name for screen readers is "Main" ({@code OatBottomNav.label},
 * translated); set another with {@link #setLabel} or an {@code aria-label} in the markup.
 * Tabs for pages the current user isn't authorized to open are left out.
 */
public class OatBottomNav extends Panel {

    private IModel<String> label;
    private boolean alwaysVisible;

    public OatBottomNav(String id, List<BottomNavItem> items) {
        this(id, Model.ofList(items));
    }

    /** @param items the tabs, read on every render */
    public OatBottomNav(String id, IModel<? extends List<BottomNavItem>> items) {
        super(id);
        add(new ListView<BottomNavItem>("items", items) {
            @Override
            protected void populateItem(ListItem<BottomNavItem> item) {
                populateTab(item);
            }
        });
    }

    /** The {@code <nav>}'s name for screen readers; {@code null} for the markup's or "Main". */
    public OatBottomNav setLabel(IModel<String> label) {
        this.label = label;
        return this;
    }

    public OatBottomNav setLabel(String label) {
        return setLabel(Model.of(label));
    }

    /** Whether the bar also shows on screens wider than 768px; {@code false} by default. */
    public OatBottomNav setAlwaysVisible(boolean alwaysVisible) {
        this.alwaysVisible = alwaysVisible;
        return this;
    }

    public boolean isAlwaysVisible() {
        return alwaysVisible;
    }

    private void populateTab(ListItem<BottomNavItem> item) {
        BottomNavItem tab = item.getModelObject();
        if (tab.primary() && tab.icon() == null) {
            throw new IllegalStateException("The primary item \"" + tab.label().getObject()
                    + "\" shows only its icon, so it needs one");
        }

        AbstractLink link = newLink("link", tab);
        link.add(newIcon("icon", tab.icon()));
        link.add(new Label("label", tab.label()).setVisible(!tab.primary()));
        // An explicit model even without a badge, so it never inherits a page's CompoundPropertyModel
        link.add(new Label("badge", tab.badge() != null ? tab.badge() : new Model<Integer>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                Object count = getDefaultModelObject();
                setVisible(count instanceof Number number && number.intValue() != 0);
            }
        });
        if (tab.primary()) {
            // Oat's button, round; the label is only announced, as the button shows only its icon
            link.add(new AttributeAppender("class", Model.of("button oat-fab"), " "));
            link.add(AttributeModifier.replace("aria-label", tab.label()));
        }
        // The primary item is an action, never the current tab
        link.add(AttributeModifier.replace("aria-current", (IModel<String>) () -> !tab.primary() && isActive(tab) ? "page" : null));
        item.add(link);

        // Leave out pages the current user may not open
        item.setVisible(getApplication().getSecuritySettings().getAuthorizationStrategy()
                .isInstantiationAuthorized(tab.pageClass()));
    }

    /**
     * The link of a tab: a {@link BookmarkablePageLink} to the item's page by default.
     * Override it to return another link, such as an {@code AjaxLink}, on an {@code <a>};
     * the bar adds the icon, label, badge and {@code aria-current} to it.
     */
    protected AbstractLink newLink(String id, BottomNavItem item) {
        return new BookmarkablePageLink<Void>(id, item.pageClass(), item.parameters());
    }

    /**
     * Whether a tab is the current page's: the item's own rule if it has one
     * ({@link BottomNavItem#withActive}), otherwise the same page class and, if the item
     * has parameters, the same parameters.
     */
    protected boolean isActive(BottomNavItem item) {
        Page page = getPage();
        if (item.active() != null) {
            return item.active().test(page);
        }
        return CurrentPage.isLinkedBy(page, item.pageClass(), item.parameters());
    }

    private static Component newIcon(String id, BottomNavItem.Icon icon) {
        if (icon == null) {
            return new WebMarkupContainer(id).setVisible(false);
        }
        return switch (icon.kind()) {
            case NAMED -> new OatIcon(id, icon.value());
            case SPRITE -> new Label(id, "<svg aria-hidden=\"true\" focusable=\"false\"><use href=\"#"
                    + Strings.escapeMarkup(icon.value()) + "\"/></svg>").setEscapeModelStrings(false);
            // The app's own markup, trusted as documented on Icon.svg
            case SVG -> new Label(id, icon.value()).setEscapeModelStrings(false);
        };
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        checkComponentTag(tag, "nav");
        super.onComponentTag(tag);
        tag.append("class", "oat-bottom-nav", " ");
        if (label != null) {
            tag.put("aria-label", label.getObject());
        } else if (tag.getAttribute("aria-label") == null) {
            tag.put("aria-label", getString("OatBottomNav.label"));
        }
        if (alwaysVisible) {
            tag.put("data-always-visible", "");
        } else {
            tag.remove("data-always-visible");
        }
    }

    @Override
    protected void onDetach() {
        if (label != null) {
            label.detach();
        }
        super.onDetach();
    }
}
