package dev.jbaby.wicket.oat.components.chart;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;

/**
 * Columns comparing a value across a few ordered categories - revenue per month, orders
 * per weekday:
 * <pre>{@code
 * add(new OatColumnChart<>("revenue", () -> reports.monthly(), Month::label, Month::revenue)
 *         .setTitle("Revenue per month"));
 * }</pre>
 * Columns start at zero; the highest and the last are labelled with their value, the
 * others show it on hover and in the table. One series, in the palette's first color;
 * for several series over time use an {@link OatLineChart}.
 *
 * @param <T> the item type, one per column
 */
public class OatColumnChart<T> extends OatXYChart<T> {

    public OatColumnChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category,
                          SerializableFunction<T, ? extends Number> value) {
        super(id, data, category);
        addSeriesInternal(new StringResourceModel("OatChart.value", this).setDefaultValue("Value"), value);
    }

    @Override
    protected String plot(double[] ticks) {
        List<T> items = items();
        int count = items.size();
        SerializableFunction<T, ? extends Number> value = series().get(0).value();
        int highest = -1;
        for (int i = 0; i < count; i++) {
            Number v = value.apply(items.get(i));
            if (v != null && (highest < 0 || v.doubleValue() > value.apply(items.get(highest)).doubleValue())) {
                highest = i;
            }
        }
        StringBuilder svg = new StringBuilder(open("oat-xy-svg")).append(grid(ticks));
        double zero = y(0, ticks);
        for (int i = 0; i < count; i++) {
            T item = items.get(i);
            Number v = value.apply(item);
            double top = v == null ? zero : y(Math.max(0, v.doubleValue()), ticks);
            double height = zero - top;
            // Each column in its own slot; 24px wide while they fit, a share of the slot otherwise
            svg.append("<svg x=\"").append(num(i * 100.0 / count)).append("%\" width=\"").append(num(100.0 / count))
                    .append("%\" height=\"").append(HEIGHT).append("\" overflow=\"visible\">");
            String x = count <= 8 ? "50%\" transform=\"translate(-12 0)" : "20%";
            String width = count <= 8 ? "24" : "60%";
            if (height > 0) {
                svg.append("<rect class=\"oat-fill-1\" x=\"").append(x).append("\" width=\"").append(width)
                        .append("\" y=\"").append(num(top)).append("\" height=\"").append(num(height)).append("\" rx=\"4\"/>");
                if (height > 4) {
                    // Square at the baseline
                    svg.append("<rect class=\"oat-fill-1\" x=\"").append(x).append("\" width=\"").append(width)
                            .append("\" y=\"").append(num(zero - 4)).append("\" height=\"4\"/>");
                }
            }
            if (v != null && (i == highest || i == count - 1)) {
                // The last column's label ends at its right edge, so it stays inside the plot
                boolean last = i == count - 1;
                svg.append("<text class=\"oat-chart-value\" x=\"50%\" y=\"").append(num(Math.max(12, top - 8)))
                        .append(last ? "\" text-anchor=\"end\" dx=\"" + (count <= 8 ? "12" : "0") + "\">" : "\" text-anchor=\"middle\">")
                        .append(escape(format(v))).append("</text>");
            }
            svg.append("</svg>");
        }
        for (int i = 0; i < count; i++) {
            svg.append(band(i, count, items.get(i)));
        }
        return svg.append("</svg>").toString();
    }

    @Override
    protected String chartClass() {
        return "oat-column-chart";
    }
}
