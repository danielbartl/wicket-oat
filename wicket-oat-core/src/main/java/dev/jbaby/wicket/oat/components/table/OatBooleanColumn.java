package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * A data table column showing a yes/no value as a read-only checkbox, e.g. whether a
 * customer is active. Screen readers announce it as a checked or unchecked checkbox
 * named after the column. A {@code null} value leaves the cell empty.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 */
public class OatBooleanColumn<T, S> extends LambdaColumn<T, S> {

    /** An unsortable column. */
    public OatBooleanColumn(IModel<String> displayModel, SerializableFunction<T, Boolean> value) {
        this(displayModel, null, value);
    }

    /** @param sortProperty the property to sort by, or {@code null} if unsortable */
    public OatBooleanColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, Boolean> value) {
        super(displayModel, sortProperty, value::apply);
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new CheckCell(componentId, getDataModel(rowModel), getDisplayModel()));
    }

    /** A disabled checkbox, checked for {@code true}. */
    static final class CheckCell extends Panel {

        CheckCell(String id, IModel<?> value, IModel<String> name) {
            super(id, value); // the panel's model, so it is detached with it
            WebMarkupContainer box = new WebMarkupContainer("box") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(value.getObject() != null);
                }
            };
            box.add(AttributeModifier.replace("checked", (IModel<String>) () -> Boolean.TRUE.equals(value.getObject()) ? "checked" : null));
            box.add(AttributeModifier.replace("aria-label", name));
            add(box);
        }
    }
}
