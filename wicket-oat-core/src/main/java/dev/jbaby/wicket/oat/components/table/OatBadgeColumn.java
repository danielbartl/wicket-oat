package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * A data table column showing a value as an Oat badge, colored by the row - typically
 * a status:
 * <pre>{@code
 * new OatBadgeColumn<Invoice, String>(Model.of("Status"), "status", Invoice::status,
 *         invoice -> switch (invoice.status()) {
 *             case PAID -> OatVariant.SUCCESS;
 *             case OVERDUE -> OatVariant.DANGER;
 *             default -> OatVariant.DEFAULT;
 *         })
 * }</pre>
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatBadgeColumn<T, S> extends LambdaColumn<T, S> {

    private final SerializableFunction<T, OatVariant> variant;

    /** An unsortable column. */
    public OatBadgeColumn(IModel<String> displayModel, SerializableFunction<T, ?> value,
                          SerializableFunction<T, OatVariant> variant) {
        this(displayModel, null, value, variant);
    }

    /**
     * @param sortProperty the property to sort by, or {@code null} if unsortable
     * @param value the badge's text for a row
     * @param variant the badge's color for a row
     */
    public OatBadgeColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, ?> value,
                          SerializableFunction<T, OatVariant> variant) {
        super(displayModel, sortProperty, value::apply);
        this.variant = variant;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new OatBadge(componentId, getDataModel(rowModel), () -> variant.apply(rowModel.getObject())));
    }
}
