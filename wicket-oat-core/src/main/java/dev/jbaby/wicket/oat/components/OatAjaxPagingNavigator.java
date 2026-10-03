package dev.jbaby.wicket.oat.components;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

/**
 * The Ajax counterpart of {@link OatPagingNavigator}: an {@link AjaxPagingNavigator}
 * rendered as Oat pagination. As with {@code AjaxPagingNavigator}, the pageable (or a
 * parent of it) must have {@code setOutputMarkupId(true)} so it can be re-rendered.
 */
public class OatAjaxPagingNavigator extends AjaxPagingNavigator {

    public OatAjaxPagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
    }

    public OatAjaxPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
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
        return new AjaxPagingNavigation(id, pageable, labelProvider) {
            @Override
            protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex) {
                return PageLinkBehavior.pageNumber(super.newPagingNavigationLink(id, pageable, pageIndex));
            }
        };
    }
}
