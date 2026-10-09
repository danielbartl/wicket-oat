package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

import java.io.Serializable;

/**
 * Behavior that lays out a component's children in a row ({@link Direction#HORIZONTAL},
 * wrapping and vertically centered) or a column ({@link Direction#VERTICAL}), with Oat's
 * standard gap between them - e.g. a toolbar of buttons, or a stack of cards.
 * It adds class="hstack" or class="vstack".
 */
public class StackBehavior extends Behavior {

    public enum Direction implements Serializable {
        HORIZONTAL("hstack"),
        VERTICAL("vstack");

        private final String className;

        Direction(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    private final Direction direction;

    public StackBehavior() {
        this(Direction.HORIZONTAL);
    }

    public StackBehavior(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);

        tag.append("class", direction.getClassName(), " ");
    }
}
