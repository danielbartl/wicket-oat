package dev.jbaby.wicket.oat.components.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * The column headers of {@code OatDataTable} and {@code OatTableTree}: an
 * {@link AjaxFallbackHeadersToolbar}, sorting over Ajax with plain links as the fallback,
 * whose sortable headers tell screen readers the sort order ({@code aria-sort}), as the
 * arrow ({@code wicket-oat.css}) shows it. For a {@code DataTable} of your own:
 * {@code table.addTopToolbar(new OatHeadersToolbar<>(table, provider))}.
 *
 * @param <S> the sort property type
 */
public class OatHeadersToolbar<S> extends AjaxFallbackHeadersToolbar<S> {

    public OatHeadersToolbar(DataTable<?, S> table, ISortStateLocator<S> stateLocator) {
        super(table, stateLocator);
    }

    @Override
    protected WebMarkupContainer newSortableHeader(String headerId, S property, ISortStateLocator<S> locator) {
        WebMarkupContainer header = super.newSortableHeader(headerId, property, locator);
        header.add(AttributeModifier.replace("aria-sort", (IModel<String>) () -> {
            SortOrder order = locator.getSortState().getPropertySortOrder(property);
            return order == SortOrder.ASCENDING ? "ascending" : order == SortOrder.DESCENDING ? "descending" : null;
        }));
        return header;
    }
}
