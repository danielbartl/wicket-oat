package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

/**
 * A styled Wicket DataTable wrapper for the Oat library.
 */
public class OatDataTable<T, S> extends Panel {

    private static final String EMPTY_STATE_ID = "msg";

    private final DataTable<T, S> table;
    private NoRecordsToolbar emptyStateToolbar;

    public OatDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
        super(id);
        
        table = new DataTable<>("table", columns, dataProvider, rowsPerPage);
        
        // Add standard Oat toolbars
        table.addTopToolbar(new HeadersToolbar<>(table, dataProvider));
        table.addBottomToolbar(new NavigationToolbar(table));
        
        add(table);
    }

    /**
     * Set the component shown in place of the rows when the table is empty. The factory
     * receives the component id to use, e.g.
     * {@code table.setEmptyState(id -> new OatEmptyState(id, "No users found"))}.
     * Calling this again replaces the previous empty state.
     */
    public OatDataTable<T, S> setEmptyState(SerializableFunction<String, ? extends Component> emptyStateFactory) {
        if (emptyStateToolbar == null) {
            emptyStateToolbar = new NoRecordsToolbar(table);
            table.addBottomToolbar(emptyStateToolbar);
        }
        Component emptyState = emptyStateFactory.apply(EMPTY_STATE_ID);
        if (emptyState == null || !EMPTY_STATE_ID.equals(emptyState.getId())) {
            throw new IllegalArgumentException("The empty state component must use the id passed to the factory (\""
                    + EMPTY_STATE_ID + "\"), but was " + (emptyState == null ? "null" : "\"" + emptyState.getId() + "\""));
        }
        // NoRecordsToolbar nests its "msg" label inside the "td" cell
        ((MarkupContainer) emptyStateToolbar.get("td")).replace(emptyState);
        return this;
    }

    public DataTable<T, S> getTable() {
        return table;
    }
}
