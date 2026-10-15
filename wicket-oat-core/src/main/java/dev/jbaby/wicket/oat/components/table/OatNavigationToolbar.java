package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.components.OatAjaxPagingNavigator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * The paging row of {@code OatDataTable} and {@code OatTableTree}: a
 * {@link NavigationToolbar} with an {@link OatAjaxPagingNavigator}, which re-renders the
 * table over Ajax, with plain links as the fallback. It gives the table a markup id for
 * that. For a {@code DataTable} of your own:
 * {@code table.addBottomToolbar(new OatNavigationToolbar(table))}.
 */
public class OatNavigationToolbar extends NavigationToolbar {

    public OatNavigationToolbar(DataTable<?, ?> table) {
        super(table);
        table.setOutputMarkupId(true);
    }

    @Override
    protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
        return new OatAjaxPagingNavigator(navigatorId, table) {
            @Override
            protected void onAjaxEvent(AjaxRequestTarget target) {
                target.add(table);
            }
        };
    }
}
