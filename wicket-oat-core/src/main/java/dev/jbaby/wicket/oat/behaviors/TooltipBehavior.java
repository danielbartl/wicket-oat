package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * A behavior that applies Oat's Tooltip styling by setting the 'title' attribute.
 * Oat UI automatically styles elements with a 'title' attribute.
 */
public class TooltipBehavior extends Behavior {

    public enum Placement implements Serializable {
        TOP(null),
        BOTTOM("bottom"),
        LEFT("left"),
        RIGHT("right");

        private final String value;
        Placement(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    private final IModel<String> labelModel;
    private Placement placement = Placement.TOP;

    public TooltipBehavior(String label) {
        this(Model.of(label));
    }

    public TooltipBehavior(IModel<String> labelModel) {
        this.labelModel = labelModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        String label = labelModel.getObject();
        if (label != null) {
            tag.put("title", label);
        }
        if (placement != null && placement.getValue() != null) {
            tag.put("data-tooltip-placement", placement.getValue());
        }
    }

    public TooltipBehavior setPlacement(Placement placement) {
        this.placement = placement;
        return this;
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        labelModel.detach();
    }
}
