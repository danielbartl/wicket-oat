package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 * A vertical list of events in order - an order's history, an audit trail, a ticket's
 * activity - each with a marker, a title, its time and an optional description:
 * <pre>{@code
 * add(new OatTimeline<>("history", () -> orders.history(order), Event::title, Event::at)
 *         .setDescription(Event::details)
 *         .setVariant(event -> event.type() == Type.PAYMENT ? OatVariant.SUCCESS : OatVariant.DEFAULT));
 * }</pre>
 * The events are shown in the order given. Times are any {@code java.time} value,
 * shown in the user's locale ({@code Oct 9, 2026, 2:30 PM}) in a {@code <time>} element
 * that carries the machine-readable value. A variant colors an event's marker.
 *
 * @param <T> the event type
 */
public class OatTimeline<T> extends Panel {

    private SerializableFunction<T, ?> description;
    private SerializableFunction<T, OatVariant> variant;

    /**
     * @param events the events, in the order to show them
     * @param title an event's title, e.g. "Invoice sent"
     * @param time when an event happened, or {@code null} to leave it out
     */
    public OatTimeline(String id, IModel<? extends List<T>> events, SerializableFunction<T, ?> title,
                       SerializableFunction<T, ? extends TemporalAccessor> time) {
        super(id);
        add(new ListView<T>("events", events) {
            @Override
            protected void populateItem(ListItem<T> item) {
                T event = item.getModelObject();
                OatVariant eventVariant = variant != null ? variant.apply(event) : null;
                if (eventVariant != null && eventVariant.getValue() != null) {
                    item.add(AttributeModifier.replace("data-variant", eventVariant.getValue()));
                }
                item.add(new Label("title", Model.of(String.valueOf(title.apply(event)))));

                TemporalAccessor when = time.apply(event);
                // java.time values are all serializable, though TemporalAccessor isn't declared so
                ValueLabel timeLabel = new ValueLabel("time", new Model<>((Serializable) when));
                timeLabel.add(AttributeModifier.replace("datetime", when != null ? when.toString() : null));
                timeLabel.setVisible(when != null);
                item.add(timeLabel);

                Object text = description != null ? description.apply(event) : null;
                item.add(new Label("description", Model.of(text != null ? text.toString() : null))
                        .setVisible(text != null && !text.toString().isBlank()));
            }
        });
    }

    /** A line under an event's title, e.g. who did it or what changed. */
    public OatTimeline<T> setDescription(SerializableFunction<T, ?> description) {
        this.description = description;
        return this;
    }

    /** Colors an event's marker, e.g. {@code SUCCESS} for a payment, {@code DANGER} for a failure. */
    public OatTimeline<T> setVariant(SerializableFunction<T, OatVariant> variant) {
        this.variant = variant;
        return this;
    }
}
