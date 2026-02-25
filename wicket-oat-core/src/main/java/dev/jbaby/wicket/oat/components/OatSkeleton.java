package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A simple container component that renders as an Oat Skeleton.
 */
public class OatSkeleton extends WebMarkupContainer {

    public OatSkeleton(String id) {
        this(id, SkeletonBehavior.Shape.LINE);
    }

    public OatSkeleton(String id, SkeletonBehavior.Shape shape) {
        super(id);
        add(new SkeletonBehavior(shape));
    }
}
