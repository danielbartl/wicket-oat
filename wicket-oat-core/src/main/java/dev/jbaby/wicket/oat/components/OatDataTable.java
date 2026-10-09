package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.table.OatSelectionColumn;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A styled Wicket DataTable wrapper for the Oat library: sortable headers (with an
 * arrow and {@code aria-sort} for the sort order), paging, and optionally
 * <ul>
 * <li>a {@linkplain #setToolbar toolbar} above the table, e.g. for a search field and a
 * "New" button,</li>
 * <li>an {@linkplain #setEmptyState empty state} shown in place of the rows,</li>
 * <li>row selection: give it an {@link OatSelectionColumn}, and a bar above the table
 * shows how many rows are selected, with the {@linkplain #addBulkAction bulk actions}
 * for them.</li>
 * </ul>
 * It renders its own tag with a markup id, so {@code target.add(table)} re-renders it,
 * e.g. after an action changed its rows. Columns for common cells - badges, links,
 * numbers, dates, row actions - are in {@code dev.jbaby.wicket.oat.components.table}.
 */
public class OatDataTable<T, S> extends Panel {

    private static final String EMPTY_STATE_ID = "msg";
    private static final String TOOLBAR_ID = "toolbar";

    private final DataTable<T, S> table;
    private final OatSelectionColumn<T, S> selectionColumn;
    private final List<BulkAction<T>> bulkActions = new ArrayList<>();
    private final WebMarkupContainer bulkBar;
    private NoRecordsToolbar emptyStateToolbar;

    @SuppressWarnings("unchecked")
    public OatDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
        super(id);
        setOutputMarkupId(true);

        table = new DataTable<>("table", columns, dataProvider, rowsPerPage);
        table.setOutputMarkupId(true);

        // Standard Oat toolbars
        table.addTopToolbar(new HeadersToolbar<>(table, dataProvider) {
            @Override
            protected WebMarkupContainer newSortableHeader(String headerId, S property, ISortStateLocator<S> locator) {
                WebMarkupContainer header = super.newSortableHeader(headerId, property, locator);
                // Tell screen readers how the column is sorted, as the arrow (wicket-oat.css) shows it
                header.add(AttributeModifier.replace("aria-sort", (IModel<String>) () -> {
                    SortOrder order = locator.getSortState().getPropertySortOrder(property);
                    return order == SortOrder.ASCENDING ? "ascending" : order == SortOrder.DESCENDING ? "descending" : null;
                }));
                return header;
            }
        });
        table.addBottomToolbar(new NavigationToolbar(table) {
            @Override
            protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
                return new OatPagingNavigator(navigatorId, table);
            }
        });
        add(table);

        selectionColumn = (OatSelectionColumn<T, S>) columns.stream()
                .filter(OatSelectionColumn.class::isInstance)
                .findFirst().orElse(null);

        add(new WebMarkupContainer(TOOLBAR_ID).setVisible(false));

        bulkBar = new WebMarkupContainer("bulkActions") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(selectionColumn != null && !selectionColumn.getSelection().getObject().isEmpty());
            }
        };
        bulkBar.setOutputMarkupPlaceholderTag(true);
        add(bulkBar);
        bulkBar.add(new Label("count", new StringResourceModel("OatDataTable.selected", this)
                .setParameters((IModel<Integer>) () -> selectionColumn == null ? 0 : selectionColumn.getSelection().getObject().size())
                .setDefaultValue("selected")));
        bulkBar.add(new ListView<>("actions", Model.ofList(bulkActions)) {
            @Override
            protected void populateItem(ListItem<BulkAction<T>> item) {
                BulkAction<T> action = item.getModelObject();
                item.add(new OatButton("action", action.label()) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        action.onClick().accept(target, List.copyOf(selectionColumn.getSelection().getObject()));
                    }
                }.setSize(ButtonBehavior.Size.SMALL).setVariant(action.variant()));
            }
        });
        bulkBar.add(new OatButton("clear", new StringResourceModel("OatDataTable.clearSelection", this).setDefaultValue("Clear selection")) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                clearSelection(target);
            }
        }.setSize(ButtonBehavior.Size.SMALL).setStyle(ButtonBehavior.Style.GHOST));
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

    /**
     * Sets a toolbar shown above the table, e.g. a panel with a search field and a "New"
     * button. The factory receives the component id to use; the component is rendered
     * on a {@code <div>}. Calling this again replaces the previous toolbar.
     * <p>
     * To filter the rows, have the data provider read the filter's model, and in the
     * field's Ajax handler go back to the first page and re-render the table:
     * {@code table.getTable().setCurrentPage(0); target.add(table);}
     */
    public OatDataTable<T, S> setToolbar(SerializableFunction<String, ? extends Component> toolbarFactory) {
        Component toolbar = toolbarFactory.apply(TOOLBAR_ID);
        if (toolbar == null || !TOOLBAR_ID.equals(toolbar.getId())) {
            throw new IllegalArgumentException("The toolbar must use the id passed to the factory (\""
                    + TOOLBAR_ID + "\"), but was " + (toolbar == null ? "null" : "\"" + toolbar.getId() + "\""));
        }
        replace(toolbar);
        return this;
    }

    /**
     * Adds a button to the bar shown while rows are selected; {@code onClick} receives a
     * copy of the selected rows. The selection is kept afterwards, so a confirmation can
     * still be cancelled: call {@link #clearSelection} once the action is done.
     *
     * @throws IllegalStateException if the table has no {@link OatSelectionColumn}
     */
    public OatDataTable<T, S> addBulkAction(String label, SerializableBiConsumer<AjaxRequestTarget, List<T>> onClick) {
        return addBulkAction(Model.of(label), OatVariant.DEFAULT, onClick);
    }

    /**
     * Adds a button to the bar shown while rows are selected, e.g. with
     * {@code OatVariant.DANGER} for a destructive action.
     *
     * @see #addBulkAction(String, SerializableBiConsumer)
     */
    public OatDataTable<T, S> addBulkAction(IModel<String> label, OatVariant variant,
                                            SerializableBiConsumer<AjaxRequestTarget, List<T>> onClick) {
        if (selectionColumn == null) {
            throw new IllegalStateException("Bulk actions need an OatSelectionColumn among the table's columns");
        }
        bulkActions.add(new BulkAction<>(label, variant, onClick));
        return this;
    }

    /** Clears the selection and re-renders the table, e.g. after a bulk action. */
    public OatDataTable<T, S> clearSelection(AjaxRequestTarget target) {
        if (selectionColumn != null) {
            selectionColumn.getSelection().getObject().clear();
        }
        target.add(this);
        return this;
    }

    /** Keeps the selection bar in step with the checkboxes. */
    @Override
    public void onEvent(IEvent<?> event) {
        if (event.getPayload() instanceof OatSelectionColumn.SelectionChanged changed && changed.getColumn() == selectionColumn) {
            changed.getTarget().add(bulkBar);
        }
    }

    public DataTable<T, S> getTable() {
        return table;
    }

    private record BulkAction<T>(IModel<String> label, OatVariant variant,
                                 SerializableBiConsumer<AjaxRequestTarget, List<T>> onClick) implements Serializable {
    }
}
