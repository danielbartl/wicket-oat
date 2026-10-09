package dev.jbaby.wicket.oat.components.chart;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;

/**
 * Horizontal bars comparing a value across categories - revenue by customer, tickets by
 * team - with each category's name before its bar and the value after it. Good
 * for long category names and for ranking:
 * <pre>{@code
 * add(new OatBarChart<>("topCustomers", () -> reports.topCustomers(), Row::customer, Row::revenue)
 *         .setTitle("Revenue by customer"));
 * }</pre>
 * Bars start at zero, so values must not be negative (negative ones show as an empty bar).
 * One series, in the palette's first color.
 *
 * @param <T> the item type, one per bar
 */
public class OatBarChart<T> extends OatChart<T> {

    public OatBarChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category,
                       SerializableFunction<T, ? extends Number> value) {
        super(id, data, category);
        addSeriesInternal(new StringResourceModel("OatChart.value", this).setDefaultValue("Value"), value);

        WebMarkupContainer plot = new WebMarkupContainer("plot");
        plot.add(AttributeModifier.replace("aria-label", (IModel<String>) this::plotName));
        figure().add(plot);
        plot.add(new ListView<T>("row", (IModel<List<T>>) this::items) {
            @Override
            protected void populateItem(ListItem<T> item) {
                T row = item.getModelObject();
                item.add(new Label("label", categoryOf(row)));
                item.add(new Label("bar", Model.of(bar(row))).setEscapeModelStrings(false));
                item.add(new Label("value", format(series().get(0).value().apply(row))));
            }
        });
    }

    private String bar(T row) {
        Number value = series().get(0).value().apply(row);
        double max = items().stream().map(series().get(0).value()).filter(v -> v != null)
                .mapToDouble(Number::doubleValue).max().orElse(0);
        double share = value == null || max <= 0 ? 0 : Math.max(0, value.doubleValue()) / max * 100;
        StringBuilder svg = new StringBuilder("<svg class=\"oat-bar-svg\" width=\"100%\" height=\"24\" aria-hidden=\"true\">");
        if (share > 0) {
            String tip = escape(categoryOf(row) + ": " + format(value));
            // Rounded at the data end, square at the baseline
            svg.append("<rect class=\"oat-fill-1\" x=\"0\" y=\"5\" height=\"14\" rx=\"4\" width=\"").append(num(share))
                    .append("%\"><title>").append(tip).append("</title></rect>")
                    .append("<rect class=\"oat-fill-1\" x=\"0\" y=\"5\" height=\"14\" width=\"4\"><title>").append(tip).append("</title></rect>");
        }
        return svg.append("</svg>").toString();
    }

    @Override
    protected String chartClass() {
        return "oat-bar-chart";
    }
}
