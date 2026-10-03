package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A simple label component that renders as an Oat Alert.
 * The message is HTML-escaped by default, like any {@link Label}; call
 * {@code setEscapeModelStrings(false)} on the returned instance to render raw HTML.
 */
public class OatAlert extends Label {

    public OatAlert(String id, String message) {
        this(id, Model.of(message), OatVariant.DEFAULT);
    }

    public OatAlert(String id, String message, OatVariant variant) {
        this(id, Model.of(message), variant);
    }

    public OatAlert(String id, IModel<?> model) {
        this(id, model, OatVariant.DEFAULT);
    }

    public OatAlert(String id, IModel<?> model, OatVariant variant) {
        super(id, model);
        add(new AlertBehavior(variant));
    }

    public OatAlert(String id, IModel<?> model, IModel<OatVariant> variantModel) {
        super(id, model);
        add(new AlertBehavior(variantModel));
    }
}
