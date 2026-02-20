package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A simple label component that renders as an Oat Badge.
 */
public class Badge extends Label {

    public Badge(String id, String label) {
        this(id, Model.of(label), BadgeBehavior.Variant.DEFAULT);
    }

    public Badge(String id, String label, BadgeBehavior.Variant variant) {
        this(id, Model.of(label), variant);
    }

    public Badge(String id, IModel<?> model) {
        this(id, model, BadgeBehavior.Variant.DEFAULT);
    }

    public Badge(String id, IModel<?> model, BadgeBehavior.Variant variant) {
        super(id, model);
        add(new BadgeBehavior(variant));
    }

    public Badge(String id, IModel<?> model, IModel<BadgeBehavior.Variant> variantModel) {
        super(id, model);
        add(new BadgeBehavior(variantModel));
    }
}
