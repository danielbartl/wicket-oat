package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A simple label component that renders as an Oat Alert.
 */
public class OatAlert extends Label {

    public OatAlert(String id, String message) {
        this(id, Model.of(message), AlertBehavior.Variant.DEFAULT);
    }

    public OatAlert(String id, String message, AlertBehavior.Variant variant) {
        this(id, Model.of(message), variant);
    }

    public OatAlert(String id, IModel<?> model) {
        this(id, model, AlertBehavior.Variant.DEFAULT);
    }

    public OatAlert(String id, IModel<?> model, AlertBehavior.Variant variant) {
        super(id, model);
        add(new AlertBehavior(variant));
        setEscapeModelStrings(false);
    }

    public OatAlert(String id, IModel<?> model, IModel<AlertBehavior.Variant> variantModel) {
        super(id, model);
        add(new AlertBehavior(variantModel));
        setEscapeModelStrings(false);
    }
}
