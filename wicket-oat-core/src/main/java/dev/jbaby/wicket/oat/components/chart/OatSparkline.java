package dev.jbaby.wicket.oat.components.chart;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import java.util.List;
import java.util.Locale;

/**
 * A small line without axes showing a value's recent trend, e.g. in an
 * {@code OatStatCard} ({@code card.setTrend(...)}) or a table cell:
 * <pre>{@code
 * add(new OatSparkline("trend", () -> reports.dailyOrders()));
 * }</pre>
 * on {@code <span wicket:id="trend"></span>}. The line is drawn in a quiet color with
 * the latest value as a dot in the accent color. It is decorative - hidden from screen
 * readers - so show the value it ends at as text nearby.
 */
public class OatSparkline extends WebComponent {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 32;

    public OatSparkline(String id, IModel<? extends List<? extends Number>> values) {
        super(id, values);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-sparkline", " ");
        tag.put("aria-hidden", "true");
        if (tag.isOpenClose()) {
            tag.setType(org.apache.wicket.markup.parser.XmlTag.TagType.OPEN);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        List<? extends Number> values = (List<? extends Number>) getDefaultModelObject();
        replaceComponentTagBody(markupStream, openTag, values == null || values.size() < 2 ? "" : svg(values));
    }

    private static String svg(List<? extends Number> values) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (Number value : values) {
            if (value != null) {
                min = Math.min(min, value.doubleValue());
                max = Math.max(max, value.doubleValue());
            }
        }
        if (min == Double.MAX_VALUE) {
            return "";
        }
        double range = max - min == 0 ? 1 : max - min;
        StringBuilder points = new StringBuilder();
        double lastX = 0;
        double lastY = 0;
        for (int i = 0; i < values.size(); i++) {
            Number value = values.get(i);
            if (value == null) {
                continue;
            }
            lastX = i * (double) WIDTH / (values.size() - 1);
            // 3px clear above and below, so the line and dot aren't cut off
            lastY = 3 + (1 - (value.doubleValue() - min) / range) * (HEIGHT - 6);
            points.append(points.length() > 0 ? " " : "").append(num(lastX)).append(',').append(num(lastY));
        }
        return "<svg class=\"oat-sparkline-svg\" viewBox=\"0 0 " + WIDTH + " " + HEIGHT + "\" preserveAspectRatio=\"none\">"
                + "<polyline class=\"oat-sparkline-line\" points=\"" + points + "\" vector-effect=\"non-scaling-stroke\"/></svg>"
                + "<svg class=\"oat-sparkline-svg\" width=\"100%\" height=\"" + HEIGHT + "\">"
                + "<circle class=\"oat-dot-1\" cx=\"" + num(lastX) + "%\" cy=\"" + num(lastY) + "\" r=\"3\"/></svg>";
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
