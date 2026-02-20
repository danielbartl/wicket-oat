package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A simple container component that renders as an Oat Alert.
 *
 * <pre>
 * &lt;div wicket:id="alert"&gt;
 *     Alert message goes here.
 * &lt;/div&gt;
 * </pre>
 */
public class Alert extends WebMarkupContainer {

    public Alert(String id) {
        this(id, AlertBehavior.Variant.DEFAULT);
    }

    public Alert(String id, AlertBehavior.Variant variant) {
        super(id);
        add(new AlertBehavior(variant));
    }

    public Alert(String id, IModel<AlertBehavior.Variant> variantModel) {
        super(id);
        add(new AlertBehavior(variantModel));
    }
}
