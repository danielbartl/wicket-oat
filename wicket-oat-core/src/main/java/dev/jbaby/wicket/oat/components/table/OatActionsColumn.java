package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatDropdown;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A data table column with a menu of actions for each row - Edit, Duplicate, Delete -
 * behind a "⋯" button:
 * <pre>{@code
 * OatActionsColumn<Invoice, String> actions = new OatActionsColumn<>();
 * actions.addAction("Edit", (target, invoice) -> editDialog.open(target, invoice));
 * actions.addAction("Mark as paid", (target, invoice) -> { invoices.markPaid(invoice); target.add(table); })
 *         .setVisibleWhen(invoice -> invoice.status() == Status.OPEN);
 * actions.addAction("Delete", (target, invoice) -> confirm.ask(target, "Delete invoice?", null, t -> ...))
 *         .setVariant(OatVariant.DANGER);
 * columns.add(actions);
 * }</pre>
 * Each action runs over Ajax with the row and closes the menu. Actions can be shown
 * only for some rows ({@link RowAction#setVisibleWhen}); a row without any shows no
 * button. The button is named by the {@code OatActionsColumn.actions} resource
 * ("Actions") for screen readers.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatActionsColumn<T, S> extends AbstractColumn<T, S> implements IStyledColumn<T, S> {

    private final List<RowAction<T>> actions = new ArrayList<>();

    /** A column without a header text. */
    public OatActionsColumn() {
        this(Model.of(""));
    }

    public OatActionsColumn(IModel<String> displayModel) {
        super(displayModel);
    }

    /**
     * Adds an action to every row's menu.
     *
     * @return the action, to set its variant or the rows it shows for
     */
    public RowAction<T> addAction(String label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        return addAction(Model.of(label), onClick);
    }

    /**
     * Adds an action to every row's menu.
     *
     * @return the action, to set its variant or the rows it shows for
     */
    public RowAction<T> addAction(IModel<String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        RowAction<T> action = new RowAction<>(label, onClick);
        actions.add(action);
        return action;
    }

    @Override
    public String getCssClass() {
        return "align-right";
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        IModel<List<RowAction<T>>> visibleActions = () -> actions.stream()
                .filter(action -> action.isVisibleFor(rowModel.getObject()))
                .toList();

        OatDropdown<RowAction<T>> menu = new OatDropdown<>(componentId, Model.of("⋯"), visibleActions) {
            @Override
            protected void populateItem(ListItem<RowAction<T>> menuItem) {
                RowAction<T> action = menuItem.getModelObject();
                menuItem.add(new Label("label", action.label));
                if (action.variant != null && action.variant != OatVariant.DEFAULT) {
                    menuItem.add(AttributeModifier.replace("data-variant", action.variant.name().toLowerCase()));
                }
                menuItem.add(AjaxEventBehavior.onEvent("click", target -> {
                    action.onClick.accept(target, rowModel.getObject());
                    close(target);
                }));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!visibleActions.getObject().isEmpty());
            }
        };
        menu.getTrigger().add(AttributeModifier.replace("class", "ghost small icon"));
        menu.getTrigger().add(AttributeModifier.replace("aria-label",
                new StringResourceModel("OatActionsColumn.actions", menu).setDefaultValue("Actions")));
        item.add(menu);
    }

    /**
     * An entry in each row's menu.
     *
     * @param <T> the row type
     */
    public static final class RowAction<T> implements Serializable {

        private final IModel<String> label;
        private final SerializableBiConsumer<AjaxRequestTarget, T> onClick;
        private OatVariant variant;
        private SerializableFunction<T, Boolean> visibleWhen;

        RowAction(IModel<String> label, SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
            this.label = label;
            this.onClick = onClick;
        }

        /** E.g. {@code DANGER} for a destructive action, shown in red. */
        public RowAction<T> setVariant(OatVariant variant) {
            this.variant = variant;
            return this;
        }

        /** Shows the action only for rows this returns {@code true} for. */
        public RowAction<T> setVisibleWhen(SerializableFunction<T, Boolean> visibleWhen) {
            this.visibleWhen = visibleWhen;
            return this;
        }

        boolean isVisibleFor(T row) {
            return visibleWhen == null || Boolean.TRUE.equals(visibleWhen.apply(row));
        }
    }
}
