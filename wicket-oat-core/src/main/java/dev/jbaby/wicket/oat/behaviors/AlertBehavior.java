package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Alert styling.
 * It adds role="alert" and an optional data-variant attribute.
 */
public class AlertBehavior extends Behavior {

    public enum Variant implements Serializable {
        DEFAULT(null),
        SUCCESS("success"),
        WARNING("warning"),
        ERROR("error"),
        DANGER("danger");

        private final String value;

        Variant(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private IModel<Variant> variantModel;

    public AlertBehavior() {
        this(Variant.DEFAULT);
    }

    public AlertBehavior(Variant variant) {
        this(Model.of(variant));
    }

    public AlertBehavior(IModel<Variant> variantModel) {
        this.variantModel = variantModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.put("role", "alert");

        Variant variant = variantModel.getObject();
        if (variant != null && variant.getValue() != null) {
            tag.put("data-variant", variant.getValue());
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
