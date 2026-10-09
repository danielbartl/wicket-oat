package dev.jbaby.wicket.oat.components.chart;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Base class for charts with a value axis and categories along the bottom
 * ({@link OatColumnChart}, {@link OatLineChart}). The plot is SVG; the axis labels are
 * HTML text beside and below it, so they stay readable on any screen. Each category is
 * a hover band whose tooltip lists its values.
 *
 * @param <T> the item type, one per category
 */
public abstract class OatXYChart<T> extends OatChart<T> {

    /** The plot's height in pixels. */
    protected static final int HEIGHT = 192;

    /** At most this many category labels; others are left out (they stay in tooltips and the table). */
    private static final int MAX_LABELS = 8;

    protected OatXYChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category) {
        super(id, data, category);
        WebMarkupContainer plot = new WebMarkupContainer("plot");
        plot.add(AttributeModifier.replace("aria-label", (IModel<String>) this::plotName));
        figure().add(plot);

        plot.add(new ListView<>("yTick", (IModel<List<String>>) () -> {
            double[] ticks = ticks();
            List<String> labels = new ArrayList<>();
            for (int i = ticks.length - 1; i >= 0; i--) {
                labels.add(formatTick(ticks[i]));
            }
            return labels;
        }) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("text", item.getModel()));
            }
        });
        plot.add(new Label("svg", (IModel<String>) this::svg).setEscapeModelStrings(false));
        plot.add(new ListView<>("xLabel", (IModel<List<String>>) () -> {
            List<T> items = items();
            int every = Math.max(1, (int) Math.ceil(items.size() / (double) MAX_LABELS));
            return IntStream.range(0, items.size())
                    .mapToObj(i -> i % every == 0 ? categoryOf(items.get(i)) : "")
                    .toList();
        }) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("text", item.getModel()));
            }
        });
    }

    /** The plot's SVG: grid, marks and hover bands. */
    protected abstract String plot(double[] ticks);

    private String svg() {
        return plot(ticks());
    }

    /** Ticks covering every value and zero. */
    protected double[] ticks() {
        double min = 0;
        double max = 0;
        for (T item : items()) {
            for (Series<T> s : series()) {
                Number value = s.value().apply(item);
                if (value != null) {
                    min = Math.min(min, value.doubleValue());
                    max = Math.max(max, value.doubleValue());
                }
            }
        }
        return ticks(min, max, 4);
    }

    /** The y coordinate of a value, in pixels from the top. */
    protected static double y(double value, double[] ticks) {
        double low = ticks[0];
        double high = ticks[ticks.length - 1];
        return HEIGHT - (value - low) / (high - low) * HEIGHT;
    }

    /** Hairline gridlines at the ticks, the zero line one step stronger. */
    protected static String grid(double[] ticks) {
        StringBuilder svg = new StringBuilder();
        for (double tick : ticks) {
            double y = Math.min(HEIGHT - 0.5, Math.max(0.5, y(tick, ticks)));
            svg.append("<line class=\"").append(tick == 0 ? "oat-chart-baseline" : "oat-chart-grid")
                    .append("\" x1=\"0\" x2=\"100%\" y1=\"").append(num(y)).append("\" y2=\"").append(num(y)).append("\"/>");
        }
        return svg.toString();
    }

    /** A category's hover band with a tooltip of its values, on top of the marks. */
    protected String band(int index, int count, T item) {
        StringBuilder tip = new StringBuilder(categoryOf(item));
        for (Series<T> s : series()) {
            Number value = s.value().apply(item);
            tip.append(series().size() > 1 ? "\n" + s.name().getObject() + ": " : ": ").append(format(value));
        }
        return "<rect class=\"oat-chart-hit\" x=\"" + num(index * 100.0 / count) + "%\" y=\"0\" width=\""
                + num(100.0 / count) + "%\" height=\"" + HEIGHT + "\"><title>" + escape(tip.toString()) + "</title></rect>";
    }

    /** Opens the plot's SVG, sized to the plot area. */
    protected static String open(String cssClass) {
        return "<svg class=\"" + Objects.requireNonNull(cssClass) + "\" width=\"100%\" height=\"" + HEIGHT + "\" aria-hidden=\"true\">";
    }
}
