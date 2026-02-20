package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Spinner styling.
 * It adds aria-busy="true" and data-spinner for size.
 */
public class SpinnerBehavior extends Behavior {

    public enum Size implements Serializable {
        SMALL("small"),
        DEFAULT(null),
        LARGE("large");

        private final String value;

        Size(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final IModel<Size> sizeModel;

    public SpinnerBehavior() {
        this(Size.DEFAULT);
    }

    public SpinnerBehavior(Size size) {
        this(Model.of(size));
    }

    public SpinnerBehavior(IModel<Size> sizeModel) {
        this.sizeModel = sizeModel;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.put("aria-busy", "true");

        Size size = sizeModel.getObject();
        if (size != null && size.getValue() != null) {
            tag.put("data-spinner", size.getValue());
        }
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        if (sizeModel != null) {
            sizeModel.detach();
        }
    }
}
