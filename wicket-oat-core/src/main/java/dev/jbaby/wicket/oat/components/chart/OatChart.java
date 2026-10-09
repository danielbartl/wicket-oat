package dev.jbaby.wicket.oat.components.chart;

import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Base class for Wicket Oat's charts: simple, server-rendered SVG charts that follow the
 * theme (light or dark) and need no JavaScript. Every chart is a {@code <figure>} with
 * an optional title, a legend when it shows two or more series, and a "Data" disclosure
 * holding the values as a table - for screen readers, keyboard users and anyone who
 * wants the exact numbers. Marks show their value in a tooltip on hover.
 * <p>
 * Colors come from a fixed, colorblind-safe palette in {@code wicket-oat.css}
 * ({@code --oat-chart-1} to {@code --oat-chart-8}, stepped for light and dark), used in
 * order; values and labels stay in the theme's text colors. Numbers are formatted in the
 * user's locale; {@link #setValueFormat} changes how, e.g. for amounts.
 *
 * @param <T> the type of the data items, one per category (bar, month, region, ...)
 */
public abstract class OatChart<T> extends Panel {

    private final WebMarkupContainer figure;
    private final IModel<? extends List<T>> data;
    private final SerializableFunction<T, String> category;
    private final List<Series<T>> series = new ArrayList<>();
    private IModel<String> title = new Model<>();
    private IModel<String> categoryHeader;
    private SerializableBiFunction<Number, Locale, String> valueFormat;

    /**
     * @param data the items, one per category, in the order to show them
     * @param category an item's category label
     */
    protected OatChart(String id, IModel<? extends List<T>> data, SerializableFunction<T, String> category) {
        super(id);
        this.data = Objects.requireNonNull(data);
        this.category = Objects.requireNonNull(category);
        setOutputMarkupId(true);

        figure = new WebMarkupContainer("figure");
        figure.add(AttributeModifier.append("class", chartClass()));
        add(figure);
        figure.add(new Label("title", (IModel<String>) () -> title.getObject()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(title.getObject() != null);
            }
        });

        WebMarkupContainer legendList = new WebMarkupContainer("legendList") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(showsLegend());
            }
        };
        figure.add(legendList);
        legendList.add(new ListView<>("legend", (IModel<List<Integer>>) () -> IntStream.range(0, series.size()).boxed().toList()) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                int index = item.getModelObject();
                item.add(new WebMarkupContainer("key").add(AttributeModifier.replace("class", "oat-chart-key oat-key-" + (index + 1))));
                item.add(new Label("name", series.get(index).name()));
            }
        });

        // The values as a table: the accessible equivalent of the chart
        WebMarkupContainer table = new WebMarkupContainer("data");
        figure.add(table);
        table.add(new Label("summary", new StringResourceModel("OatChart.data", this).setDefaultValue("Data")));
        table.add(new Label("categoryHeader", (IModel<String>) () -> categoryHeader != null ? categoryHeader.getObject()
                : new StringResourceModel("OatChart.category", this).setDefaultValue("Category").getObject()));
        table.add(new ListView<>("seriesHeader", (IModel<List<Series<T>>>) () -> series) {
            @Override
            protected void populateItem(ListItem<Series<T>> item) {
                item.add(new Label("name", item.getModelObject().name()));
            }
        });
        table.add(new ListView<T>("row", (IModel<List<T>>) this::items) {
            @Override
            protected void populateItem(ListItem<T> item) {
                T row = item.getModelObject();
                item.add(new Label("category", category.apply(row)));
                item.add(new ListView<>("value", (IModel<List<Series<T>>>) () -> series) {
                    @Override
                    protected void populateItem(ListItem<Series<T>> cell) {
                        cell.add(new Label("text", format(cell.getModelObject().value().apply(row))));
                    }
                });
            }
        });
    }

    /** The {@code <figure>}; a subclass adds its plot to it, as its markup is inside it. */
    protected WebMarkupContainer figure() {
        return figure;
    }

    /** A CSS class for the chart type, e.g. {@code oat-bar-chart}. */
    protected abstract String chartClass();

    /** Whether to show the legend: by default, for two or more series. */
    protected boolean showsLegend() {
        return series.size() >= 2;
    }

    /** Adds a series of values, one per item. */
    protected void addSeriesInternal(IModel<String> name, SerializableFunction<T, ? extends Number> value) {
        series.add(new Series<>(name, value));
    }

    /** The chart's title, shown above it and naming it for screen readers. */
    public OatChart<T> setTitle(IModel<String> title) {
        this.title = title != null ? title : new Model<>();
        return this;
    }

    public OatChart<T> setTitle(String title) {
        return setTitle(Model.of(title));
    }

    /** The heading of the table's first column, e.g. "Month" ({@code OatChart.category}, "Category", by default). */
    public OatChart<T> setCategoryHeader(IModel<String> header) {
        this.categoryHeader = header;
        return this;
    }

    /**
     * How values are shown in tooltips, labels and the table; by default the locale's
     * number format, e.g. {@code (value, locale) -> money(value, locale)} for amounts.
     */
    public OatChart<T> setValueFormat(SerializableBiFunction<Number, Locale, String> valueFormat) {
        this.valueFormat = valueFormat;
        return this;
    }

    /** The items. */
    protected List<T> items() {
        List<T> items = data.getObject();
        return items == null ? List.of() : items;
    }

    protected List<Series<T>> series() {
        return series;
    }

    protected String categoryOf(T item) {
        return category.apply(item);
    }

    /** The title, or the given fallback, as a name for the plot. */
    protected String plotName() {
        String text = title.getObject();
        return text != null ? text : new StringResourceModel("OatChart.chart", this).setDefaultValue("Chart").getObject();
    }

    /** A value in the chart's format; {@code null} shows as an empty string. */
    protected String format(Number value) {
        if (value == null) {
            return "";
        }
        if (valueFormat != null) {
            return valueFormat.apply(value, getLocale());
        }
        NumberFormat format = NumberFormat.getNumberInstance(getLocale());
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }

    /** An axis tick, short: {@code 12K}, {@code 1.5M}. */
    protected String formatTick(double value) {
        NumberFormat format = NumberFormat.getCompactNumberInstance(getLocale(), NumberFormat.Style.SHORT);
        format.setMaximumFractionDigits(1);
        return format.format(value);
    }

    /** Text for SVG and HTML, escaped. */
    protected static String escape(String text) {
        return Strings.escapeMarkup(text == null ? "" : text).toString();
    }

    /** A number for SVG attributes: no locale, few digits. */
    protected static String num(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
    }

    /** Ticks for an axis from {@code min} to {@code max}: round steps of 1, 2 or 5 times a power of ten. */
    protected static double[] ticks(double min, double max, int intervals) {
        double low = Math.min(0, min);
        double high = Math.max(0, max);
        if (high == low) {
            high = low + 1;
        }
        double raw = (high - low) / intervals;
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double step = magnitude;
        for (double factor : new double[] {1, 2, 2.5, 5, 10}) {
            step = factor * magnitude;
            if (step >= raw) {
                break;
            }
        }
        double start = Math.floor(low / step) * step;
        int count = (int) Math.ceil((high - start) / step - 1e-9);
        double[] ticks = new double[count + 1];
        for (int i = 0; i <= count; i++) {
            ticks[i] = start + i * step;
        }
        return ticks;
    }

    @Override
    protected void onDetach() {
        data.detach();
        title.detach();
        series.forEach(s -> s.name().detach());
        super.onDetach();
    }

    /** A named series of values, one per item. */
    protected record Series<T>(IModel<String> name, SerializableFunction<T, ? extends Number> value) implements Serializable {
    }
}
