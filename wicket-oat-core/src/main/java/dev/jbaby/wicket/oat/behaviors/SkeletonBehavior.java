package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

import java.io.Serializable;

/**
 * Behavior that applies Oat's Skeleton styling.
 * It adds role="status" and the "skeleton" class, plus a shape (line/box).
 */
public class SkeletonBehavior extends Behavior {

    public enum Shape implements Serializable {
        LINE("line"),
        BOX("box");

        private final String className;

        Shape(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    private final Shape shape;

    public SkeletonBehavior() {
        this(Shape.LINE);
    }

    public SkeletonBehavior(Shape shape) {
        this.shape = shape;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.put("role", "status");
        tag.append("class", "skeleton", " ");
        if (shape != null) {
            tag.append("class", shape.getClassName(), " ");
        }
    }
}
