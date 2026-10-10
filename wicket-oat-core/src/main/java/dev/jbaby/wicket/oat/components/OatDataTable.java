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

import dev.jbaby.wicket.oat.components.table.OatCsvExport;
import dev.jbaby.wicket.oat.components.table.OatRowDetailsColumn;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
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
 * <li>an {@link OatRowDetailsColumn} to expand a row into a full-width panel below it,</li>
 * <li>a {@linkplain #setColumnChooser column chooser}, letting users hide columns,</li>
 * <li>a {@linkplain #setCsvExport CSV export} of all rows, in the current sort order.</li>
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
    private final OatRowDetailsColumn<T, S> detailsColumn;
    /** Indexes of the columns the user hid with the column chooser. */
    private final Set<Integer> hiddenColumns = new HashSet<>();
    private boolean columnChooser;
    private String csvFileName;
    private final List<BulkAction<T>> bulkActions = new ArrayList<>();
    private final WebMarkupContainer bulkBar;
    private NoRecordsToolbar emptyStateToolbar;

    @SuppressWarnings("unchecked")
    public OatDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, long rowsPerPage) {
        super(id);
        setOutputMarkupId(true);

        detailsColumn = (OatRowDetailsColumn<T, S>) columns.stream()
                .filter(OatRowDetailsColumn.class::isInstance)
                .findFirst().orElse(null);

        table = new RowsTable("table", columns, dataProvider, rowsPerPage);
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

            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                // The header cells are created anew for every render; hide those of hidden columns
                visitChildren(Item.class, (Item<?> item, IVisit<Void> visit) -> {
                    Component header = item.get("header");
                    if (header != null && header.getBehaviors(HiddenColumnClass.class).isEmpty()) {
                        header.add(new HiddenColumnClass(item.getIndex()));
                    }
                    visit.dontGoDeeper();
                });
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
        add(newTools("tools"));

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

    /**
     * Shows a "Columns" button above the table (on the toolbar's right) whose popover
     * lets users hide and show columns. Columns without a header text, such as the
     * selection and actions columns, are always shown. The choice lasts as long as the
     * page.
     */
    public OatDataTable<T, S> setColumnChooser(boolean columnChooser) {
        this.columnChooser = columnChooser;
        return this;
    }

    /**
     * Shows an "Export CSV" button above the table that downloads all rows - every
     * page, in the current sort order - as a CSV file with the given name, e.g.
     * {@code "invoices.csv"}. The file has the columns that have a header text and a
     * value: {@code LambdaColumn}, {@code PropertyColumn} and the Oat columns built on
     * them (any {@code IExportableColumn}), including hidden ones. {@code null} removes
     * the button. See {@link OatCsvExport} for how values are written.
     */
    public OatDataTable<T, S> setCsvExport(String fileName) {
        this.csvFileName = fileName;
        return this;
    }

    /** Hides or shows a column, by its index in the column list. */
    public OatDataTable<T, S> setColumnVisible(int index, boolean visible) {
        if (index < 0 || index >= table.getColumns().size()) {
            throw new IndexOutOfBoundsException("No column " + index + "; the table has " + table.getColumns().size());
        }
        if (visible) {
            hiddenColumns.remove(index);
        } else {
            hiddenColumns.add(index);
        }
        return this;
    }

    public boolean isColumnVisible(int index) {
        return !hiddenColumns.contains(index);
    }

    private Component newTools(String id) {
        WebMarkupContainer tools = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(columnChooser || csvFileName != null);
            }
        };
        OatPopover chooser = new OatPopover("columns", new StringResourceModel("OatDataTable.columns", this).setDefaultValue("Columns"),
                contentId -> {
                    Fragment list = new Fragment(contentId, "columnsFragment", OatDataTable.this);
                    list.add(new ListView<>("column", (IModel<List<Integer>>) this::choosableColumns) {
                        @Override
                        protected void populateItem(ListItem<Integer> item) {
                            int index = item.getModelObject();
                            AjaxCheckBox box = new AjaxCheckBox("check", new IModel<>() {
                                @Override
                                public Boolean getObject() {
                                    return isColumnVisible(index);
                                }

                                @Override
                                public void setObject(Boolean visible) {
                                    setColumnVisible(index, Boolean.TRUE.equals(visible));
                                }
                            }) {
                                @Override
                                protected void onUpdate(AjaxRequestTarget target) {
                                    target.add(table);
                                }
                            };
                            item.add(box);
                            item.add(new Label("name", headerText(table.getColumns().get(index))));
                        }
                    });
                    return list;
                }) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(columnChooser);
            }
        };
        chooser.getTrigger().add(AttributeModifier.replace("class", "outline small"));
        tools.add(chooser);

        Link<Void> export = new Link<>("export") {
            @Override
            public void onClick() {
                OatCsvExport.download(getRequestCycle(), table.getDataProvider(), table.getColumns(), csvFileName, getLocale());
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(csvFileName != null);
            }
        };
        export.setBody(new StringResourceModel("OatDataTable.export", this).setDefaultValue("Export CSV"));
        export.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE).setSize(ButtonBehavior.Size.SMALL));
        tools.add(export);
        return tools;
    }

    /** The columns the chooser offers: those with a header text. */
    private List<Integer> choosableColumns() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < table.getColumns().size(); i++) {
            if (!Strings.isEmpty(headerText(table.getColumns().get(i)))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    private static String headerText(IColumn<?, ?> column) {
        if (column instanceof AbstractColumn<?, ?> abstractColumn && abstractColumn.getDisplayModel() != null) {
            Object text = abstractColumn.getDisplayModel().getObject();
            return text == null ? null : text.toString();
        }
        return null;
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

    /** Adds {@code oat-column-hidden} to a column's cells while the user hides it. */
    private final class HiddenColumnClass extends Behavior {

        private final int index;

        HiddenColumnClass(int index) {
            this.index = index;
        }

        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            if (hiddenColumns.contains(index)) {
                tag.append("class", "oat-column-hidden", " ");
            }
        }
    }

    /**
     * The table, with each row in a {@code <tbody>} of its own, so a row can have a
     * details row below it ({@link OatRowDetailsColumn}).
     */
    private final class RowsTable extends DataTable<T, S> {

        RowsTable(String id, List<? extends IColumn<T, S>> columns, IDataProvider<T> provider, long rowsPerPage) {
            super(id, columns, provider, rowsPerPage);
        }

        @Override
        protected Item<IColumn<T, S>> newCellItem(String id, int index, IModel<IColumn<T, S>> model) {
            Item<IColumn<T, S>> item = super.newCellItem(id, index, model);
            item.add(new HiddenColumnClass(index));
            return item;
        }

        @Override
        protected Item<T> newRowItem(String id, int index, IModel<T> model) {
            return new Row<>(id, index, model, detailsColumn,
                    () -> getColumns().size() - (int) hiddenColumns.stream().filter(i -> i < getColumns().size()).count());
        }
    }

    /**
     * A row of the table: its cells, and its details row while an
     * {@link OatRowDetailsColumn} has it expanded.
     *
     * @param <T> the row type
     */
    public static final class Row<T> extends Item<T> {

        private final WebMarkupContainer details;

        Row(String id, int index, IModel<T> model, OatRowDetailsColumn<T, ?> column, IModel<Integer> colspan) {
            super(id, index, model);
            details = new WebMarkupContainer("details") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    boolean expanded = column != null && column.isExpanded(model.getObject());
                    setVisible(expanded);
                    if (expanded && get("cell").get(OatRowDetailsColumn.DETAILS_ID) == null) {
                        ((MarkupContainer) get("cell")).add(column.newDetails(OatRowDetailsColumn.DETAILS_ID, model));
                    }
                }
            };
            details.setOutputMarkupPlaceholderTag(true);
            WebMarkupContainer cell = new WebMarkupContainer("cell");
            cell.add(AttributeModifier.replace("colspan", colspan));
            details.add(cell);
            add(details);
        }

        /** The details row, e.g. to re-render it: {@code target.add(row.getDetails())}. */
        public WebMarkupContainer getDetails() {
            return details;
        }
    }

    private record BulkAction<T>(IModel<String> label, OatVariant variant,
                                 SerializableBiConsumer<AjaxRequestTarget, List<T>> onClick) implements Serializable {
    }
}
