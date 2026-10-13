package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.components.OatDataTable;
import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * A column of buttons that expand a row into a details panel below it, spanning the
 * table's width - an order's lines, a log entry's full text, a customer's addresses:
 * <pre>{@code
 * columns.add(0, new OatRowDetailsColumn<>((id, order) -> new OrderLinesPanel(id, order)));
 * }</pre>
 * Use it in an {@link OatDataTable}. The panel is created the first time its row is
 * expanded, with the id it is given ({@link #DETAILS_ID}) and the row's model, and is
 * rendered on a {@code <div>} - wrap components that need another tag, such as an
 * {@code OatDescriptionList} on a {@code <dl>}, in a panel or fragment. Expanded rows
 * stay open across pages, sorting and re-renders. They are remembered by {@code equals},
 * or by key, so the page keeps only, e.g., an entity's id:
 * {@code new OatRowDetailsColumn<>(details, new SetModel<>(new HashSet<>()), Order::getId)}.
 * The button has {@code aria-expanded} and
 * {@code aria-controls} and is named by {@code OatDataTable.showDetails}/{@code hideDetails}.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatRowDetailsColumn<T, S> extends AbstractColumn<T, S> implements IStyledColumn<T, S> {

    /** The id the details panel must use. */
    public static final String DETAILS_ID = "details";

    private final SerializableBiFunction<String, IModel<T>, ? extends Component> details;
    private final IModel<? extends Collection<?>> expanded;
    /** A row's key among the expanded ones; the row itself unless remembered by key. */
    private final SerializableFunction<? super T, ?> key;

    /** @param details creates a row's details panel, with the id and row model it is given */
    public OatRowDetailsColumn(SerializableBiFunction<String, IModel<T>, ? extends Component> details) {
        this(details, Model.ofSet(new HashSet<>()));
    }

    /** @param expanded the expanded rows, e.g. to open some from the start */
    public OatRowDetailsColumn(SerializableBiFunction<String, IModel<T>, ? extends Component> details,
                               IModel<? extends Collection<T>> expanded) {
        this(details, expanded, (SerializableFunction<T, T>) row -> row);
    }

    /**
     * Remembers expanded rows by key, e.g. an entity's id, so the page keeps only the keys.
     *
     * @param expanded the keys of the expanded rows, e.g. to open some from the start
     * @param key a row's key, e.g. {@code Order::getId}
     */
    public <K> OatRowDetailsColumn(SerializableBiFunction<String, IModel<T>, ? extends Component> details,
                                   IModel<? extends Collection<K>> expanded, SerializableFunction<? super T, ? extends K> key) {
        super(Model.of(""));
        this.details = Objects.requireNonNull(details);
        this.expanded = Objects.requireNonNull(expanded);
        this.key = Objects.requireNonNull(key);
    }

    /** The expanded rows, or their keys when remembered by key. */
    public IModel<? extends Collection<?>> getExpanded() {
        return expanded;
    }

    public boolean isExpanded(T row) {
        return row != null && keys().contains(key.apply(row));
    }

    /** Expands or collapses a row; re-render its {@link OatDataTable.Row#getDetails() details} to show it. */
    public void setExpanded(T row, boolean open) {
        Object rowKey = key.apply(row);
        if (open) {
            if (!keys().contains(rowKey)) {
                keys().add(rowKey);
            }
        } else {
            keys().remove(rowKey);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> keys() {
        return (Collection<Object>) expanded.getObject();
    }

    /** Creates a row's details panel. */
    public Component newDetails(String id, IModel<T> rowModel) {
        Component panel = details.apply(id, rowModel);
        if (panel == null || !id.equals(panel.getId())) {
            throw new IllegalArgumentException("The details must use the id passed to the factory (\"" + id + "\"), but was "
                    + (panel == null ? "null" : "\"" + panel.getId() + "\""));
        }
        return panel;
    }

    @Override
    public String getCssClass() {
        return "oat-details-toggle";
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new ToggleCell(componentId, rowModel));
    }

    @Override
    public void detach() {
        expanded.detach();
        super.detach();
    }

    final class ToggleCell extends Panel {

        ToggleCell(String id, IModel<T> rowModel) {
            super(id);
            AjaxLink<Void> toggle = new AjaxLink<>("toggle") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    setExpanded(rowModel.getObject(), !isExpanded(rowModel.getObject()));
                    OatDataTable.Row<?> row = findParent(OatDataTable.Row.class);
                    target.add(this);
                    if (row != null) {
                        target.add(row.getDetails());
                    }
                }
            };
            toggle.setOutputMarkupId(true);
            toggle.add(AttributeModifier.replace("aria-expanded", () -> String.valueOf(isExpanded(rowModel.getObject()))));
            toggle.add(AttributeModifier.replace("aria-controls", () -> {
                OatDataTable.Row<?> row = findParent(OatDataTable.Row.class);
                return row == null ? null : row.getDetails().getMarkupId();
            }));
            toggle.add(AttributeModifier.replace("aria-label", () -> new StringResourceModel(
                    isExpanded(rowModel.getObject()) ? "OatDataTable.hideDetails" : "OatDataTable.showDetails", this)
                    .setDefaultValue(isExpanded(rowModel.getObject()) ? "Hide details" : "Show details").getObject()));
            toggle.add(new OatIcon("icon", "chevron-right"));
            add(toggle);
        }
    }
}
