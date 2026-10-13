package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A data table column of checkboxes for selecting rows, with one in the header that
 * selects or clears every row on the current page. The selection is kept in the given
 * collection, across pages and sorting, either as the rows themselves or by key:
 * <pre>{@code
 * // The rows themselves, compared with equals: for records and other small values
 * IModel<Set<Invoice>> selected = new SetModel<>(new HashSet<>());
 * columns.add(0, new OatSelectionColumn<>(selected));
 *
 * // By key, e.g. for entities: the page keeps only their ids, not the entities
 * IModel<Set<Long>> selectedIds = new SetModel<>(new HashSet<>());
 * columns.add(0, new OatSelectionColumn<>(selectedIds, Invoice::getId));
 * }</pre>
 * In an {@code OatDataTable}, a selection shows a bar with its count and the table's
 * {@linkplain dev.jbaby.wicket.oat.components.OatDataTable#addBulkAction bulk actions},
 * which get the selected rows ({@link #getSelectedRows}). Every change also sends a
 * {@link SelectionChanged} event, bubbling up from the table, for components of your own
 * that show the selection.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatSelectionColumn<T, S> extends AbstractColumn<T, S> implements IStyledColumn<T, S> {

    private final IModel<? extends Collection<?>> selection;
    /** A row's key in the selection; the row itself unless selecting by key. */
    private final SerializableFunction<? super T, ?> key;
    private final boolean byKey;

    /** @param selection the selected rows; checking a box adds its row, unchecking removes it */
    public OatSelectionColumn(IModel<? extends Collection<T>> selection) {
        this(selection, row -> row, false);
    }

    /**
     * Selects rows by key, e.g. an entity's id, so the selection - and the page - keeps
     * only the keys. {@link #getSelectedRows} looks the rows up through the data provider.
     *
     * @param selection the keys of the selected rows
     * @param key a row's key, e.g. {@code Invoice::getId}
     */
    public <K> OatSelectionColumn(IModel<? extends Collection<K>> selection, SerializableFunction<? super T, ? extends K> key) {
        this(selection, key, true);
    }

    private OatSelectionColumn(IModel<? extends Collection<?>> selection, SerializableFunction<? super T, ?> key, boolean byKey) {
        super(Model.of(""));
        this.selection = Objects.requireNonNull(selection);
        this.key = Objects.requireNonNull(key);
        this.byKey = byKey;
    }

    /** The selection as it is kept: the selected rows, or their keys when selecting by key. */
    public IModel<? extends Collection<?>> getSelection() {
        return selection;
    }

    public boolean isSelected(T row) {
        return selected().contains(key.apply(row));
    }

    /**
     * The selected rows. When selecting by key, they are looked up through the data
     * provider, so they are current, in the provider's order (the table's sort order);
     * that reads every row once, so for a very large table read the keys from
     * {@link #getSelection()} instead.
     */
    public List<T> getSelectedRows(IDataProvider<T> provider) {
        Collection<Object> selected = selected();
        if (!byKey) {
            @SuppressWarnings("unchecked")
            List<T> rows = (List<T>) List.copyOf(selected);
            return rows;
        }
        List<T> rows = new ArrayList<>();
        if (!selected.isEmpty()) {
            provider.iterator(0, provider.size()).forEachRemaining(row -> {
                if (selected.contains(key.apply(row))) {
                    rows.add(row);
                }
            });
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> selected() {
        return (Collection<Object>) selection.getObject();
    }

    @Override
    public String getCssClass() {
        return "oat-select";
    }

    @Override
    public Component getHeader(String componentId) {
        return new CheckCell(componentId, true) {
            @Override
            protected boolean isChecked() {
                List<Object> keys = currentPageKeys(this);
                return !keys.isEmpty() && selected().containsAll(keys);
            }

            @Override
            protected void apply(boolean checked) {
                List<Object> keys = currentPageKeys(this);
                if (checked) {
                    keys.stream().filter(k -> !selected().contains(k)).forEach(selected()::add);
                } else {
                    selected().removeAll(keys);
                }
            }

            @Override
            protected void refresh(AjaxRequestTarget target) {
                target.add(findParent(DataTable.class)); // every row's checkbox changed
            }
        };
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new CheckCell(componentId, false) {
            @Override
            protected boolean isChecked() {
                return isSelected(rowModel.getObject());
            }

            @Override
            protected void apply(boolean checked) {
                Object rowKey = key.apply(rowModel.getObject());
                if (checked && !selected().contains(rowKey)) {
                    selected().add(rowKey);
                } else if (!checked) {
                    selected().remove(rowKey);
                }
            }

            @Override
            protected void refresh(AjaxRequestTarget target) {
                // The header's "select all" box may have changed with this row
                findParent(DataTable.class).visitChildren(CheckCell.class, (cell, visit) -> {
                    if (((CheckCell) cell).header) {
                        target.add(cell);
                        visit.stop();
                    }
                });
            }
        });
    }

    @Override
    public void detach() {
        super.detach();
        selection.detach();
    }

    /** The keys of the rows on the table's current page, as its data provider returns them. */
    @SuppressWarnings("unchecked")
    private List<Object> currentPageKeys(Component inTable) {
        DataTable<T, S> table = inTable.findParent(DataTable.class);
        long first = table.getCurrentPage() * table.getItemsPerPage();
        long count = Math.min(table.getItemsPerPage(), table.getRowCount() - first);
        List<Object> keys = new ArrayList<>();
        if (count > 0) {
            Iterator<? extends T> iterator = table.getDataProvider().iterator(first, count);
            iterator.forEachRemaining(row -> keys.add(key.apply(row)));
        }
        return keys;
    }

    /**
     * Sent, bubbling up from the table, whenever the selection changes; add your own
     * components to {@link #getTarget()} to update them.
     */
    public static final class SelectionChanged {

        private final AjaxRequestTarget target;
        private final OatSelectionColumn<?, ?> column;

        SelectionChanged(AjaxRequestTarget target, OatSelectionColumn<?, ?> column) {
            this.target = target;
            this.column = column;
        }

        public AjaxRequestTarget getTarget() {
            return target;
        }

        /** The column whose selection changed; {@link #getSelection()} has the rows or their keys. */
        public OatSelectionColumn<?, ?> getColumn() {
            return column;
        }
    }

    /** A checkbox updating the selection over Ajax, named for screen readers by a resource. */
    abstract class CheckCell extends Panel {

        final boolean header;

        CheckCell(String id, boolean header) {
            super(id);
            this.header = header;
            setOutputMarkupId(true);
            AjaxCheckBox box = new AjaxCheckBox("check", new IModel<>() {
                @Override
                public Boolean getObject() {
                    return isChecked();
                }

                @Override
                public void setObject(Boolean checked) {
                    apply(Boolean.TRUE.equals(checked));
                }
            }) {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    refresh(target);
                    send(this, Broadcast.BUBBLE, new SelectionChanged(target, OatSelectionColumn.this));
                }
            };
            box.add(AttributeModifier.replace("aria-label", header
                    ? new StringResourceModel("OatDataTable.selectAll", this).setDefaultValue("Select all rows on this page")
                    : new StringResourceModel("OatDataTable.selectRow", this).setDefaultValue("Select row")));
            add(box);
        }

        /** Whether the box is checked, from the selection. */
        protected abstract boolean isChecked();

        /** Updates the selection after the box was checked or unchecked. */
        protected abstract void apply(boolean checked);

        /** Re-renders what the change affects in the table. */
        protected abstract void refresh(AjaxRequestTarget target);
    }
}
