package dev.jbaby.wicket.oat.behaviors;

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

    public enum Variant implements Serializable {
        DEFAULT(null),
        SECONDARY("secondary"),
        SUCCESS("success"),
        WARNING("warning"),
        DANGER("danger");

        private final String value;
        Variant(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    private final IModel<Variant> variantModel;
    private boolean outline = false;

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
