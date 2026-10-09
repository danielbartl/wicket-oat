package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatStatCard;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Wicket Oat's server-rendered SVG charts: stat cards with a trend, a column chart, a
 * line chart with two series, a bar chart and a donut chart. Each chart has a "Data"
 * table and follows the theme (light or dark).
 */
public class ReportsPage extends BasePage {

    public record Month(String label, int revenue, int lastYear, int orders) implements Serializable {
    }

    public record Customer(String name, int revenue) implements Serializable {
    }

    public record Region(String name, int revenue) implements Serializable {
    }

    private static final List<Month> MONTHS = List.of(
            new Month("Jan", 84_200, 71_300, 412), new Month("Feb", 79_800, 69_900, 398),
            new Month("Mar", 92_400, 78_100, 455), new Month("Apr", 96_100, 80_400, 471),
            new Month("May", 101_700, 86_200, 502), new Month("Jun", 98_300, 90_800, 488),
            new Month("Jul", 104_900, 88_700, 517), new Month("Aug", 97_600, 84_300, 479),
            new Month("Sep", 112_300, 95_600, 548), new Month("Oct", 118_400, 99_100, 571),
            new Month("Nov", 121_900, 104_200, 590), new Month("Dec", 128_400, 111_800, 623));

    private static final List<Customer> CUSTOMERS = List.of(
            new Customer("ACME GmbH", 182_400), new Customer("Globex", 151_900), new Customer("Initech", 118_300),
            new Customer("Umbrella Corporation", 96_700), new Customer("Stark Industries", 74_200), new Customer("Wayne Enterprises", 52_800));

    private static final List<Region> REGIONS = List.of(
            new Region("DACH", 512_300), new Region("Benelux", 241_800), new Region("Nordics", 198_400),
            new Region("France", 132_600), new Region("Iberia", 78_900), new Region("Italy", 41_200),
            new Region("UK & Ireland", 30_700));

    public ReportsPage() {
        int revenue = MONTHS.stream().mapToInt(Month::revenue).sum();
        int orders = MONTHS.stream().mapToInt(Month::orders).sum();

        add(new OatStatCard("revenue", "Revenue this year", (IModel<String>) () -> euros(revenue, getLocale()))
                .setChange(Model.of(16.4))
                .setTrend(Model.ofList(MONTHS.stream().map(Month::revenue).toList()))
                .setHint(Model.of("vs. last year")));
        add(new OatStatCard("orders", "Orders", Model.of(orders))
                .setChange(Model.of(9.1))
                .setTrend(Model.ofList(MONTHS.stream().map(Month::orders).toList()))
                .setHint(Model.of("vs. last year")));
        add(new OatStatCard("average", "Average order", (IModel<String>) () -> euros(revenue / orders, getLocale()))
                .setChange(Model.of(6.7))
                .setTrend(Model.ofList(MONTHS.stream().map(m -> m.revenue() / m.orders()).toList()))
                .setHint(Model.of("vs. last year")));

        add(Oat.Components.columnChart("monthly", Model.ofList(MONTHS), Month::label, Month::revenue)
                .setTitle("Revenue per month")
                .setCategoryHeader(Model.of("Month"))
                .setValueFormat(ReportsPage::euros));
        add(Oat.Components.lineChart("trend", Model.ofList(MONTHS), Month::label)
                .addSeries("2026", Month::revenue)
                .addSeries("2025", Month::lastYear)
                .setTitle("Revenue, this year and last")
                .setCategoryHeader(Model.of("Month"))
                .setValueFormat(ReportsPage::euros));
        add(Oat.Components.barChart("customers", Model.ofList(CUSTOMERS), Customer::name, Customer::revenue)
                .setTitle("Top customers")
                .setCategoryHeader(Model.of("Customer"))
                .setValueFormat(ReportsPage::euros));
        add(Oat.Components.donutChart("regions", Model.ofList(REGIONS), Region::name, Region::revenue)
                .setTitle("Revenue by region")
                .setCategoryHeader(Model.of("Region"))
                .setValueFormat(ReportsPage::euros));
    }

    /** Whole euros in the user's locale: € 128,400. */
    private static String euros(Number value, Locale locale) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(Currency.getInstance("EUR"));
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }
}
