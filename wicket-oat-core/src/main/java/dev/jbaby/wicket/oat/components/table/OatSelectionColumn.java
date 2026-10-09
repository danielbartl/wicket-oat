package dev.jbaby.wicket.oat.components.table;

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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A data table column of checkboxes for selecting rows, with one in the header that
 * selects or clears every row on the current page. The selected rows are kept in the
 * given collection, across pages and sorting:
 * <pre>{@code
 * IModel<Set<Invoice>> selected = new SetModel<>(new HashSet<>());
 * columns.add(0, new OatSelectionColumn<>(selected));
 * }</pre>
 * Rows are compared with {@code equals}, so the row type needs a meaningful one (a
 * record, or an entity comparing ids). In an {@code OatDataTable}, a selection shows a
 * bar with its count and the table's {@linkplain dev.jbaby.wicket.oat.components.OatDataTable#addBulkAction
 * bulk actions}. Every change also sends a {@link SelectionChanged} event, bubbling up
 * from the table, for components of your own that show the selection.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatSelectionColumn<T, S> extends AbstractColumn<T, S> implements IStyledColumn<T, S> {

    private final IModel<? extends Collection<T>> selection;

    /** @param selection the selected rows; checking a box adds its row, unchecking removes it */
    public OatSelectionColumn(IModel<? extends Collection<T>> selection) {
        super(Model.of(""));
        this.selection = selection;
    }

    /** The selected rows. */
    public IModel<? extends Collection<T>> getSelection() {
        return selection;
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
                List<T> rows = currentPageRows(this);
                return !rows.isEmpty() && selection.getObject().containsAll(rows);
            }

            @Override
            protected void apply(boolean checked) {
                List<T> rows = currentPageRows(this);
                if (checked) {
                    rows.stream().filter(row -> !selection.getObject().contains(row)).forEach(selection.getObject()::add);
                } else {
                    selection.getObject().removeAll(rows);
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
                return selection.getObject().contains(rowModel.getObject());
            }

            @Override
            protected void apply(boolean checked) {
                T row = rowModel.getObject();
                if (checked && !selection.getObject().contains(row)) {
                    selection.getObject().add(row);
                } else if (!checked) {
                    selection.getObject().remove(row);
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

    /** The rows on the table's current page, as its data provider returns them. */
    @SuppressWarnings("unchecked")
    private List<T> currentPageRows(Component inTable) {
        DataTable<T, S> table = inTable.findParent(DataTable.class);
        long first = table.getCurrentPage() * table.getItemsPerPage();
        long count = Math.min(table.getItemsPerPage(), table.getRowCount() - first);
        List<T> rows = new ArrayList<>();
        if (count > 0) {
            Iterator<? extends T> iterator = table.getDataProvider().iterator(first, count);
            iterator.forEachRemaining(rows::add);
        }
        return rows;
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

        /** The column whose selection changed; {@link #getSelection()} has the rows. */
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
