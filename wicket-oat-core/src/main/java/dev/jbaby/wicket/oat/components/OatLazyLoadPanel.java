package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import java.time.Duration;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import java.util.Objects;

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
 * {@link #getUpdateInterval()} (a second) until the content is ready. All lazy-load panels
 * of a page share one timer, which takes the shortest interval of those still waiting.
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
     * Starts the page's timer, unless a panel has already, so that it loads the content
     * right after the page is shown (0.1 seconds) instead of after the second
     * {@link AjaxLazyLoadPanel} waits. Content that isn't ready then is checked again every
     * {@link #getUpdateInterval()}, a second as in Wicket.
     */
    @Override
    protected void initTimer() {
        if (getPage().getBehaviors(AjaxLazyLoadTimer.class).isEmpty()) {
            AjaxLazyLoadTimer timer = new FirstLoadTimer();
            getPage().add(timer);
            // Not rendered with the page when this panel is added over Ajax
            getRequestCycle().find(AjaxRequestTarget.class).ifPresent(timer::restart);
        }
    }

    /**
     * Wicket's timer for the page's lazy-load panels, first going off after 0.1 seconds.
     * Each time it goes off it waits the shortest {@link #getUpdateInterval()} of the panels
     * still waiting, so one panel checking often makes the whole page send requests.
     */
    static final class FirstLoadTimer extends AjaxLazyLoadTimer {

        FirstLoadTimer() {
            setUpdateInterval(Duration.ofMillis(100));
        }
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

    /**
     * A panel loading its content over Ajax right after the page, without a subclass:
     * {@code content} creates it with the id it is given.
     */
    public static OatLazyLoadPanel<Component> of(String id, SerializableFunction<String, ? extends Component> content) {
        Objects.requireNonNull(content);
        return new OatLazyLoadPanel<>(id) {
            @Override
            public Component getLazyLoadComponent(String markupId) {
                return content.apply(markupId);
            }
        };
    }
}
