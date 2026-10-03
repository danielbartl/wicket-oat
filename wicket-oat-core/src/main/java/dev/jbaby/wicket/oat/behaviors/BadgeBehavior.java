package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Badge styling ({@code data-variant}, optional
 * {@code outline} class) to any component.
 */
public class BadgeBehavior extends Behavior {

    private final IModel<OatVariant> variantModel;
    private boolean outline = false;

    public BadgeBehavior() {
        this(OatVariant.DEFAULT);
    }

    public BadgeBehavior(OatVariant variant) {
        this(Model.of(variant));
    }

    public BadgeBehavior(IModel<OatVariant> variantModel) {
        this.variantModel = variantModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        tag.append("class", "badge", " ");
        OatVariant variant = variantModel.getObject();
        if (variant != null && variant.getValue() != null) {
            tag.put("data-variant", variant.getValue());
        }
        if (outline) {
            tag.append("class", "outline", " ");
        }
    }

    public BadgeBehavior setOutline(boolean outline) {
        this.outline = outline;
        return this;
    }

    public boolean isOutline() {
        return outline;
    }
}
