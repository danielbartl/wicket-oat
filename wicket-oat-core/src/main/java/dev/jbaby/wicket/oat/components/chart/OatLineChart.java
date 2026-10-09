package dev.jbaby.wicket.oat.components.chart;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lines showing how values change over time - revenue this year and last, open tickets
 * per week:
 * <pre>{@code
 * add(new OatLineChart<>("revenue", () -> reports.monthly(), Month::label)
 *         .addSeries("2026", Month::revenue)
 *         .addSeries("2025", Month::revenueLastYear)
 *         .setTitle("Revenue per month"));
 * }</pre>
 * Up to three series, in the palette's first three colors, with a legend from two on;
 * for more, show several charts side by side. A single series gets a light area under
 * its line and its last value labelled. Hovering a category shows all its values; a
 * {@code null} value leaves a gap in the line.
 *
 * @param <T> the item type, one per category (point in time)
 */
public class OatLineChart<T> extends OatXYChart<T> {

    /** More lines than this are hard to tell apart by color. */
    public static final int MAX_SERIES = 3;

    public OatLineChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category) {
        super(id, data, category);
    }

    public OatLineChart<T> addSeries(String name, SerializableFunction<T, ? extends Number> value) {
        return addSeries(Model.of(name), value);
    }

    /** Adds a line; at most {@link #MAX_SERIES}. */
    public OatLineChart<T> addSeries(IModel<String> name, SerializableFunction<T, ? extends Number> value) {
        if (series().size() >= MAX_SERIES) {
            throw new IllegalStateException("An OatLineChart shows at most " + MAX_SERIES
                    + " series; show the others in a chart of their own");
        }
        addSeriesInternal(name, value);
        return this;
    }

    @Override
    protected String plot(double[] ticks) {
        List<T> items = items();
        int count = items.size();
        StringBuilder svg = new StringBuilder("<div class=\"oat-xy-layers\">").append(open("oat-xy-svg")).append(grid(ticks)).append("</svg>");

        // The lines, stretched to the plot's width; strokes keep their 2px
        svg.append("<svg class=\"oat-xy-svg\" viewBox=\"0 0 1000 ").append(HEIGHT).append("\" preserveAspectRatio=\"none\" width=\"100%\" height=\"")
                .append(HEIGHT).append("\" aria-hidden=\"true\">");
        for (int s = series().size() - 1; s >= 0; s--) {
            for (List<double[]> run : runs(items, s, ticks)) {
                if (series().size() == 1 && run.size() > 1) {
                    svg.append("<polygon class=\"oat-area-").append(s + 1).append("\" points=\"")
                            .append(num(run.get(0)[0])).append(',').append(num(y(Math.max(0, ticks[0]), ticks))).append(' ')
                            .append(points(run)).append(' ')
                            .append(num(run.get(run.size() - 1)[0])).append(',').append(num(y(Math.max(0, ticks[0]), ticks)))
                            .append("\"/>");
                }
                svg.append("<polyline class=\"oat-stroke-").append(s + 1).append("\" points=\"").append(points(run))
                        .append("\" vector-effect=\"non-scaling-stroke\"/>");
            }
        }
        svg.append("</svg>");

        // End dots and hover bands, unstretched
        svg.append(open("oat-xy-svg"));
        for (int s = series().size() - 1; s >= 0; s--) {
            for (int i = count - 1; i >= 0; i--) {
                Number value = series().get(s).value().apply(items.get(i));
                if (value != null) {
                    String cx = num((i + 0.5) * 100.0 / count) + "%";
                    double cy = y(value.doubleValue(), ticks);
                    svg.append("<circle class=\"oat-dot-").append(s + 1).append("\" cx=\"").append(cx).append("\" cy=\"")
                            .append(num(cy)).append("\" r=\"4\"/>");
                    if (series().size() == 1) {
                        svg.append("<text class=\"oat-chart-value\" x=\"").append(cx).append("\" y=\"").append(num(Math.max(12, cy - 10)))
                                .append("\" text-anchor=\"end\" dx=\"-6\">").append(escape(format(value))).append("</text>");
                    }
                    break;
                }
            }
        }
        for (int i = 0; i < count; i++) {
            svg.append(band(i, count, items.get(i)));
        }
        return svg.append("</svg></div>").toString();
    }

    /** A series' points in the 1000-wide view box, split into runs at missing values. */
    private List<List<double[]>> runs(List<T> items, int s, double[] ticks) {
        List<List<double[]>> runs = new ArrayList<>();
        List<double[]> run = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Number value = series().get(s).value().apply(items.get(i));
            if (value == null) {
                if (!run.isEmpty()) {
                    runs.add(run);
                    run = new ArrayList<>();
                }
            } else {
                run.add(new double[] {(i + 0.5) * 1000.0 / items.size(), y(value.doubleValue(), ticks)});
            }
        }
        if (!run.isEmpty()) {
            runs.add(run);
        }
        return runs;
    }

    private static String points(List<double[]> run) {
        StringBuilder points = new StringBuilder();
        for (double[] p : run) {
            points.append(points.length() > 0 ? " " : "").append(num(p[0])).append(',').append(num(p[1]));
        }
        return points.toString();
    }

    @Override
    protected String chartClass() {
        return "oat-line-chart";
    }
}
