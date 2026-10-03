package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationLink;

/**
 * Styles a paging navigator's links as small Oat buttons. A link that wouldn't change
 * the page is the current page for a page-number link (solid, {@code aria-current}),
 * and inactive for first/previous/next/last ({@code aria-disabled}). Shared by
 * {@link OatPagingNavigator} and {@link OatAjaxPagingNavigator}.
 */
final class PageLinkBehavior extends Behavior {

    private final boolean pageNumber;

    private PageLinkBehavior(boolean pageNumber) {
        this.pageNumber = pageNumber;
    }

    /** Styles a page-number link. */
    static <L extends AbstractLink> L pageNumber(L link) {
        link.add(new PageLinkBehavior(true));
        return link;
    }

    /** Styles a first/previous/next/last link. */
    static <L extends AbstractLink> L step(L link) {
        link.add(new PageLinkBehavior(false));
        return link;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        boolean current = linksToCurrentPage(component);
        tag.put("class", current && pageNumber ? "button small" : "button outline small");
        if (current) {
            tag.put(pageNumber ? "aria-current" : "aria-disabled", pageNumber ? "page" : "true");
        }
    }

    private static boolean linksToCurrentPage(Component link) {
        if (link instanceof PagingNavigationLink<?> pageLink) {
            return pageLink.linksTo(link.getPage());
        }
        return link instanceof PagingNavigationIncrementLink<?> incrementLink && incrementLink.linksTo(link.getPage());
    }
}
