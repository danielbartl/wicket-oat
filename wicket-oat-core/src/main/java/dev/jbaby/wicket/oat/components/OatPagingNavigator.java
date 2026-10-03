package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * A {@link PagingNavigator} for any {@link IPageable} (a {@code DataView},
 * {@code PageableListView}, {@code DataTable}, ...) rendered as Oat pagination: a
 * group of small buttons with first/previous, the page numbers, and next/last. The
 * current page is a solid button marked {@code aria-current="page"}. For Ajax paging
 * use {@link OatAjaxPagingNavigator}.
 *
 * <pre>
 * add(new OatPagingNavigator("navigator", dataView));
 *
 * &lt;div wicket:id="navigator"&gt;&lt;/div&gt;
 * </pre>
 */
public class OatPagingNavigator extends PagingNavigator {

    public OatPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
    }

    public OatPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }

    @Override
    protected AbstractLink newPagingNavigationIncrementLink(String id, IPageable pageable, int increment) {
        return PageLinkBehavior.step(super.newPagingNavigationIncrementLink(id, pageable, increment));
    }

    @Override
    protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, int pageNumber) {
        return PageLinkBehavior.step(super.newPagingNavigationLink(id, pageable, pageNumber));
    }

    @Override
    protected PagingNavigation newNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        return new PagingNavigation(id, pageable, labelProvider) {
            @Override
            protected AbstractLink newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
                return PageLinkBehavior.pageNumber(super.newPagingNavigationLink(id, pageable, pageIndex));
            }
        };
    }
}
