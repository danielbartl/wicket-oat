package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A simple container component that renders as an Oat Alert.
 *
 * <pre>
 * &lt;div wicket:id="alert"&gt;
 *     OatAlert message goes here.
 * &lt;/div&gt;
 * </pre>
 */
public class OatAlert extends WebMarkupContainer {

    public OatAlert(String id) {
        this(id, AlertBehavior.Variant.DEFAULT);
    }

    public OatAlert(String id, AlertBehavior.Variant variant) {
        super(id);
        add(new AlertBehavior(variant));
    }

    public OatAlert(String id, IModel<AlertBehavior.Variant> variantModel) {
        super(id);
        add(new AlertBehavior(variantModel));
    }
}
