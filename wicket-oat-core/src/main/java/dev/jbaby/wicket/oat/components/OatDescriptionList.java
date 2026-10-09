package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * A read-only view of a record's fields, as label/value pairs - the details page of an
 * invoice, a customer or an order. It renders an HTML description list ({@code <dl>}),
 * with the labels in a column next to the values on wide screens and above them on
 * narrow ones:
 * <pre>{@code
 * OatDescriptionList details = new OatDescriptionList("details", new CompoundPropertyModel<>(invoice));
 * details.addProperty("number");                 // label "number" from the .properties file
 * details.addProperty("customer.name");
 * details.addItem("Total", () -> money.format(invoice.total()));
 * details.addItem(Model.of("Status"), id -> new OatBadge(id, invoice.status().name(), OatVariant.SUCCESS));
 * add(details);
 * }</pre>
 * on {@code <dl wicket:id="details"></dl>}. An empty value ({@code null} or blank) is
 * shown as "—". Numbers and dates are shown as people read them in their locale -
 * {@code 1,234} and {@code Mar 1, 2019} in English - anything else with Wicket's converters.
 */
public class OatDescriptionList extends Panel {

    private static final String EMPTY = "—";

    private final RepeatingView items = new RepeatingView("items");

    public OatDescriptionList(String id) {
        this(id, null);
    }

    /** @param model the record, for {@link #addProperty}; e.g. a {@code CompoundPropertyModel} */
    public OatDescriptionList(String id, IModel<?> model) {
        super(id, model);
        add(items);
    }

    /** Adds a label and its value. */
    public OatDescriptionList addItem(String label, IModel<?> value) {
        return addItem(Model.of(label), value);
    }

    /** Adds a label and its value. */
    public OatDescriptionList addItem(IModel<String> label, IModel<?> value) {
        return addItem(label, id -> new ValueLabel(id, (IModel<Object>) () -> {
            Object object = value.getObject();
            return object == null || object.toString().isBlank() ? EMPTY : object;
        }) {
            @Override
            protected void onDetach() {
                value.detach();
                super.onDetach();
            }
        });
    }

    /**
     * Adds a label and a component as its value, e.g. a badge or a link. The factory
     * receives the id to use; the component is rendered on a {@code <span>}.
     */
    public OatDescriptionList addItem(IModel<String> label, SerializableFunction<String, ? extends Component> value) {
        WebMarkupContainer item = new WebMarkupContainer(items.newChildId());
        item.add(new Label("term", label));
        Component component = value.apply("value");
        if (component == null || !"value".equals(component.getId())) {
            throw new IllegalArgumentException("The value component must use the id passed to the factory (\"value\"), but was "
                    + (component == null ? "null" : "\"" + component.getId() + "\""));
        }
        item.add(component);
        items.add(item);
        return this;
    }

    /**
     * Adds a property of this list's model, e.g. {@code "customer.name"}, labelled by the
     * {@code .properties} entry with the property as its key (falling back to the property).
     */
    public OatDescriptionList addProperty(String property) {
        return addItem(new StringResourceModel(property, this).setDefaultValue(property),
                new PropertyModel<>(this, "defaultModelObject." + property));
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        checkComponentTag(tag, "dl");
        super.onComponentTag(tag);
        tag.append("class", "oat-dl", " ");
    }
}
