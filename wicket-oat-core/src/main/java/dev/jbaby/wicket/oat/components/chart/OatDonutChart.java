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

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * How a whole divides into a few parts - revenue by region, tickets by channel - as a
 * ring with the total in the middle and a legend giving each part's value and share:
 * <pre>{@code
 * add(new OatDonutChart<>("regions", () -> reports.byRegion(), Region::name, Region::revenue)
 *         .setTitle("Revenue by region"));
 * }</pre>
 * At most {@value #MAX_SEGMENTS} segments, in the palette's colors; past that, the
 * smallest parts are combined into "Other" ({@code OatChart.other}). Use it for a share
 * at a glance; to compare parts of similar size, an {@link OatBarChart} reads better.
 * Values must not be negative.
 *
 * @param <T> the item type, one per part
 */
public class OatDonutChart<T> extends OatChart<T> {

    public static final int MAX_SEGMENTS = 6;

    public OatDonutChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category,
                         SerializableFunction<T, ? extends Number> value) {
        super(id, data, category);
        addSeriesInternal(new StringResourceModel("OatChart.value", this).setDefaultValue("Value"), value);

        WebMarkupContainer plot = new WebMarkupContainer("plot");
        plot.add(AttributeModifier.replace("aria-label", (IModel<String>) this::plotName));
        figure().add(plot);
        plot.add(new Label("ring", (IModel<String>) this::ring).setEscapeModelStrings(false));
        plot.add(new ListView<>("part", (IModel<List<Part>>) this::parts) {
            @Override
            protected void populateItem(ListItem<Part> item) {
                Part part = item.getModelObject();
                item.add(new WebMarkupContainer("key").add(AttributeModifier.replace("class", "oat-chart-key oat-key-" + part.slot())));
                item.add(new Label("name", part.name()));
                item.add(new Label("value", format(part.value())));
                item.add(new Label("share", share(part.value(), total())));
            }
        });
    }

    /** The parts, largest first is up to the data; past {@link #MAX_SEGMENTS} the smallest become "Other". */
    private List<Part> parts() {
        List<Part> parts = new ArrayList<>();
        for (T item : items()) {
            Number value = series().get(0).value().apply(item);
            parts.add(new Part(categoryOf(item), value == null ? 0 : Math.max(0, value.doubleValue()), 0));
        }
        if (parts.size() > MAX_SEGMENTS) {
            List<Part> bySize = new ArrayList<>(parts);
            bySize.sort(Comparator.comparingDouble(Part::value).reversed());
            List<Part> kept = bySize.subList(0, MAX_SEGMENTS - 1);
            double other = bySize.subList(MAX_SEGMENTS - 1, bySize.size()).stream().mapToDouble(Part::value).sum();
            List<Part> folded = new ArrayList<>(parts.stream().filter(kept::contains).toList());
            folded.add(new Part(new StringResourceModel("OatChart.other", this).setDefaultValue("Other").getObject(), other, 0));
            parts = folded;
        }
        List<Part> numbered = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            numbered.add(new Part(parts.get(i).name(), parts.get(i).value(), i + 1));
        }
        return numbered;
    }

    private double total() {
        return parts().stream().mapToDouble(Part::value).sum();
    }

    private String share(double value, double total) {
        NumberFormat percent = NumberFormat.getPercentInstance(getLocale());
        percent.setMaximumFractionDigits(1);
        return percent.format(total <= 0 ? 0 : value / total);
    }

    /** The ring: one circle per part, its stroke dashed to the part's share, a 2px gap between parts. */
    private String ring() {
        List<Part> parts = parts();
        double total = total();
        StringBuilder svg = new StringBuilder("<svg class=\"oat-donut-svg\" viewBox=\"0 0 120 120\" aria-hidden=\"true\">")
                .append("<circle class=\"oat-donut-track\" cx=\"60\" cy=\"60\" r=\"48\" pathLength=\"100\"/>");
        double offset = 0;
        double gap = parts.size() > 1 ? 0.6 : 0;
        for (Part part : parts) {
            double length = total <= 0 ? 0 : part.value() / total * 100;
            if (length > 0) {
                double dash = Math.max(0.01, length - gap);
                svg.append("<circle class=\"oat-donut-segment oat-stroke-").append(part.slot())
                        .append("\" cx=\"60\" cy=\"60\" r=\"48\" pathLength=\"100\" stroke-dasharray=\"").append(num(dash))
                        .append(' ').append(num(100 - dash)).append("\" stroke-dashoffset=\"").append(num(-offset))
                        .append("\" transform=\"rotate(-90 60 60)\"><title>")
                        .append(escape(part.name() + ": " + format(part.value()) + " (" + share(part.value(), total) + ")"))
                        .append("</title></circle>");
            }
            offset += length;
        }
        // Sized to fit the 80-unit hole: about 0.6em per character, at most 18
        String text = format(total);
        double size = Math.min(18, 70 / (0.6 * Math.max(1, text.length())));
        svg.append("<text class=\"oat-donut-total\" x=\"60\" y=\"60\" text-anchor=\"middle\" dominant-baseline=\"central\" font-size=\"")
                .append(num(size)).append("\">").append(escape(text)).append("</text>");
        return svg.append("</svg>").toString();
    }

    @Override
    protected boolean showsLegend() {
        return false; // its own legend, with values and shares
    }

    @Override
    protected String chartClass() {
        return "oat-donut-chart";
    }

    private record Part(String name, double value, int slot) implements Serializable {
    }
}
