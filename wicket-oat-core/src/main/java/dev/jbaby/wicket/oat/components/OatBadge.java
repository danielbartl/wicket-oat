package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A simple label component that renders as an Oat Badge.
 */
public class OatBadge extends Label {

    private final BadgeBehavior behavior;

    public OatBadge(String id, String label) {
        this(id, Model.of(label), OatVariant.DEFAULT);
    }

    public OatBadge(String id, String label, OatVariant variant) {
        this(id, Model.of(label), variant);
    }

    public OatBadge(String id, IModel<?> model) {
        this(id, model, OatVariant.DEFAULT);
    }

    public OatBadge(String id, IModel<?> model, OatVariant variant) {
        super(id, model);
        this.behavior = new BadgeBehavior(variant);
        add(behavior);
    }

    public OatBadge(String id, IModel<?> model, IModel<OatVariant> variantModel) {
        super(id, model);
        this.behavior = new BadgeBehavior(variantModel);
        add(behavior);
    }

    public OatBadge setOutline(boolean outline) {
        behavior.setOutline(outline);
        return this;
    }
}
