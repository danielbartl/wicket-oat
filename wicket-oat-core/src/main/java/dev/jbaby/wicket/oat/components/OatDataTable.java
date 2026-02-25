package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

/**
 * A styled Wicket DataTable wrapper for the Oat library.
 */
public class OatDataTable<T, S> extends Panel {

    private final DataTable<T, S> table;

    public OatDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
        super(id);
        
        table = new DataTable<>("table", columns, dataProvider, rowsPerPage);
        
        // Add standard Oat toolbars
        table.addTopToolbar(new HeadersToolbar<>(table, dataProvider));
        table.addBottomToolbar(new NavigationToolbar(table));
        
        add(table);
    }

    /**
     * Set a component to be shown when the table is empty.
     */
    public OatDataTable<T, S> setEmptyState(Component emptyState) {
        table.addBottomToolbar(new NoRecordsToolbar(table) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                // We wrap our empty state in the cell
                Component msg = get("msg");
                if (msg != null) {
                    msg.replaceWith(emptyState);
                }
            }
        });
        return this;
    }

    public DataTable<T, S> getTable() {
        return table;
    }
}
