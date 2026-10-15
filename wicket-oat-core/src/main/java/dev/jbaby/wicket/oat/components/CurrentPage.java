package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/** The rule for a navigation link pointing at the current page, shared by the sidebar, the bottom bar and the menu bar. */
final class CurrentPage {

    private CurrentPage() {
    }

    /** The same page class and, if the link has parameters, the same parameters. */
    static boolean isLinkedBy(Page page, Class<? extends Page> pageClass, PageParameters parameters) {
        return page.getClass().equals(pageClass)
                && (parameters == null || parameters.equals(page.getPageParameters()));
    }
}
