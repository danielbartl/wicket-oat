package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Badge styling.
 * It adds the "badge" class and an optional variant class.
 */
public class BadgeBehavior extends Behavior {

    public enum Variant implements Serializable {
        DEFAULT(null),
        SECONDARY("secondary"),
        OUTLINE("outline"),
        SUCCESS("success"),
        WARNING("warning"),
        DANGER("danger");

        private final String className;

        Variant(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    private final IModel<Variant> variantModel;

    public BadgeBehavior() {
        this(Variant.DEFAULT);
    }

    public BadgeBehavior(Variant variant) {
        this(Model.of(variant));
    }

    public BadgeBehavior(IModel<Variant> variantModel) {
        this.variantModel = variantModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.append("class", "badge", " ");

        Variant variant = variantModel.getObject();
        if (variant != null && variant.getClassName() != null) {
            tag.append("class", variant.getClassName(), " ");
        }
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        if (variantModel != null) {
            variantModel.detach();
        }
    }
}
