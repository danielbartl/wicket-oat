package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import java.time.Duration;

/**
 * Renders the page first and loads slow content - a report, a chart, figures from a
 * remote service - over Ajax right after, showing Oat skeleton placeholders until it
 * arrives. Implement {@link #getLazyLoadComponent} to create the content:
 * <pre>{@code
 * add(new OatLazyLoadPanel<>("revenue") {
 *     @Override
 *     public Component getLazyLoadComponent(String id) {
 *         return new RevenuePanel(id, reports.revenueThisYear());
 *     }
 * });
 * }</pre>
 * or use {@code Oat.Components.lazyLoad("revenue", id -> new RevenuePanel(id, ...))}.
 * <p>
 * The placeholder is three skeleton lines; {@link #setPlaceholder} changes the count to
 * resemble the content, or uses Oat's square skeleton box, e.g. for a picture. Like Wicket's
 * {@link AjaxLazyLoadPanel}, which this extends, it can also wait for work running in
 * the background: override {@link #isContentReady()}, and it checks again every
 * {@link #getUpdateInterval()} until the content is ready.
 *
 * @param <T> the type of the lazily loaded component
 */
public abstract class OatLazyLoadPanel<T extends Component> extends AjaxLazyLoadPanel<T> {

    private SkeletonBehavior.Shape placeholderShape = SkeletonBehavior.Shape.LINE;
    private int placeholderCount = 3;

    public OatLazyLoadPanel(String id) {
        super(id);
    }

    public OatLazyLoadPanel(String id, IModel<?> model) {
        super(id, model);
    }

    /**
     * The skeleton shown while loading: {@code count} lines or boxes.
     *
     * @throws IllegalArgumentException if {@code count} is less than 1
     */
    public OatLazyLoadPanel<T> setPlaceholder(SkeletonBehavior.Shape shape, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1: " + count);
        }
        this.placeholderShape = shape;
        this.placeholderCount = count;
        return this;
    }

    /** Oat skeletons, as set with {@link #setPlaceholder}. */
    @Override
    public Component getLoadingComponent(String id) {
        return new Placeholder(id, placeholderShape, placeholderCount);
    }

    /**
     * Loads the content right after the page is shown (0.1 seconds), instead of the 1
     * second {@link AjaxLazyLoadPanel} waits. With {@link #isContentReady()} overridden,
     * this is also how often it checks; override this to check less often.
     */
    @Override
    public Duration getUpdateInterval() {
        return Duration.ofMillis(100);
    }

    /** A number of skeleton lines or boxes. */
    static final class Placeholder extends Panel {

        Placeholder(String id, SkeletonBehavior.Shape shape, int count) {
            super(id);
            RepeatingView skeletons = new RepeatingView("skeleton");
            for (int i = 0; i < count; i++) {
                skeletons.add(new OatSkeleton(skeletons.newChildId(), shape));
            }
            add(skeletons);
        }
    }
}
