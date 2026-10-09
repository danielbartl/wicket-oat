package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.behaviors.ContextMenuBehavior;
import dev.jbaby.wicket.oat.components.chart.OatChart;
import dev.jbaby.wicket.oat.components.chart.OatLineChart;
import jakarta.servlet.http.Cookie;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class TierBComponentsTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            protected void init() {
                super.init();
                WicketOats.install(this);
            }
        });
        tester.getSession().setLocale(Locale.US);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    private String html() {
        return tester.getLastResponseAsString();
    }

    // --- Context menu ---

    @Test
    void contextMenuPassesItsEntriesAsData() {
        List<String> ran = new ArrayList<>();
        ContextMenuBehavior menu = Oat.Behaviors.contextMenu()
                .addAction("Open \"draft\" <now>", target -> ran.add("open"))
                .addAction(Model.of("Delete"), OatVariant.DANGER, target -> ran.add("delete"));
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.add(menu);
        tester.startComponentInPage(row, Markup.of("<div wicket:id='row' tabindex='0'></div>"));

        TagTester tag = tester.getTagByWicketId("row");
        assertThat(tag.getAttribute("data-oat-context-menu"))
                .isEqualTo("[{\"label\":\"Open \\\"draft\\\" <now>\"},{\"label\":\"Delete\",\"variant\":\"danger\"}]");
        assertThat(html()).contains("data-oat-context-menu=\"[{&quot;label&quot;:");
        assertThat(tag.getAttribute("data-oat-context-url")).isNotEmpty();

        tester.getRequest().getPostParameters().setParameterValue("action", "1");
        tester.executeBehavior(menu);
        assertThat(ran).containsExactly("delete");

        tester.getRequest().getPostParameters().setParameterValue("action", "7");
        tester.executeBehavior(menu);
        assertThat(ran).containsExactly("delete");
    }

    /** A page with a context menu, to see its header contributions. */
    public static class ContextMenuPage extends WebPage implements IMarkupResourceStreamProvider {
        public ContextMenuPage() {
            add(new WebMarkupContainer("row").add(Oat.Behaviors.contextMenu().addAction("Open", target -> { })));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><head></head><body><div wicket:id='row'></div></body></html>");
        }
    }

    @Test
    void contextMenuAddsItsScript() {
        tester.startPage(ContextMenuPage.class);
        assertThat(html()).contains("wicket-oat.js");
    }

    // --- Load-more list ---

    private OatLoadMoreList<Integer> numbers(int count) {
        List<Integer> items = IntStream.rangeClosed(1, count).boxed().toList();
        return Oat.Components.loadMoreList("numbers", new ListDataProvider<>(items), 20,
                (id, number) -> new Label(id, () -> "Item " + number.getObject()));
    }

    @Test
    void loadMoreListShowsABatchAndAppendsTheNext() {
        OatLoadMoreList<Integer> list = numbers(45);
        tester.startComponentInPage(list);
        assertThat(tags("role", "listitem")).hasSize(20);
        assertThat(html()).contains("Load more (20 of 45)").doesNotContain("Item 21<");

        tester.clickLink("numbers:more");
        String response = html();
        assertThat(response).contains("Item 21").contains("Item 40").doesNotContain(">Item 1<");
        assertThat(response).contains("appendChild(b)").contains("Load more (40 of 45)");

        tester.clickLink("numbers:more");
        assertThat(html()).contains("Item 45");
        tester.assertInvisible("numbers:more");
    }

    @Test
    void loadMoreListCanLoadOnScroll() {
        tester.startComponentInPage(numbers(5));
        tester.assertInvisible("numbers:more");

        tester.startComponentInPage(numbers(30).setLoadOnScroll(true));
        assertThat(tester.getTagByWicketId("more").getAttribute("data-oat-load-on-scroll")).isNotNull();
        assertThatIllegalArgumentException().isThrownBy(() -> new OatLoadMoreList<>("x", new ListDataProvider<>(List.of(1)), 0, Label::new));
    }

    // --- Menu bar ---

    @Test
    void menuBarHasButtonsLinksAndMenus() {
        List<String> ran = new ArrayList<>();
        OatMenuBar bar = Oat.Components.menuBar("commands").addAction("Save", target -> ran.add("save"))
                .addLink("Home", ParamLayoutPage.SecretPage.class);
        bar.addMenu("Export").addAction("CSV", target -> ran.add("csv"))
                .addAction(Model.of("Delete all"), OatVariant.DANGER, target -> ran.add("delete"));
        tester.startComponentInPage(bar, Markup.of("<nav wicket:id='commands' aria-label='Invoice'></nav>"));

        assertThat(tester.getTagByWicketId("button").getAttribute("class")).isEqualTo("ghost small");
        assertThat(tester.getTagByWicketId("link").getAttribute("class")).isEqualTo("button ghost small");
        assertThat(tags("role", "menuitem")).hasSize(2);
        assertThat(tags("role", "menuitem").get(1).getAttribute("data-variant")).isEqualTo("danger");

        tester.clickLink("commands:items:1:button");
        tester.executeAjaxEvent("commands:items:3:dropdown:menu:items:0", "click");
        assertThat(ran).containsExactly("save", "csv");
        assertThat(html()).contains("hidePopover()");
    }

    // --- Split layout ---

    @Test
    void splitLayoutSetsItsStartingSplitWithClasses() {
        tester.startComponentInPage(Oat.Components.splitLayout("split", id -> new Label(id, "List"), id -> new Label(id, "Message"))
                .setSplit(30).setOrientation(OatSplitLayout.Orientation.VERTICAL));
        assertThat(tester.getTagByWicketId("split").getAttribute("class")).isEqualTo("oat-split oat-split-vertical");
        assertThat(tester.getTagByWicketId("firstPane").getAttribute("class"))
                .isEqualTo("oat-split-pane oat-split-first oat-split-first-vertical oat-pane-v-30");
        assertThat(tester.getTagByWicketId("first").getValue()).isEqualTo("List");
        assertThat(html()).doesNotContain("style=");
        assertThatIllegalArgumentException().isThrownBy(() -> new OatSplitLayout("s", Label::new, Label::new).setSplit(35));
    }

    // --- Cookie consent ---

    @Test
    void cookieConsentAsksUntilAnswered() {
        List<Boolean> answers = new ArrayList<>();
        tester.startComponentInPage(Oat.Components.cookieConsent("cookies").onAnswer(answers::add));
        assertThat(html()).contains("We use cookies").contains(">Accept<").contains(">Only necessary<");
        assertThat(OatCookieConsent.isAnswered()).isFalse();

        tester.clickLink("cookies:accept");
        assertThat(answers).containsExactly(true);
        Cookie cookie = tester.getLastResponse().getCookies().stream()
                .filter(c -> OatCookieConsent.COOKIE.equals(c.getName())).findFirst().orElseThrow();
        assertThat(cookie.getValue()).isEqualTo("accepted");
        assertThat(cookie.getMaxAge()).isEqualTo(365 * 24 * 3600);
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void cookieConsentReadsTheAnswer() {
        tester.getRequest().addCookie(new Cookie(OatCookieConsent.COOKIE, "necessary"));
        tester.startComponentInPage(new OatCookieConsent("cookies"));
        tester.assertInvisible("cookies");
        assertThat(OatCookieConsent.isAnswered()).isTrue();
        assertThat(OatCookieConsent.isAccepted()).isFalse();
    }

    // --- Icons ---

    @Test
    void iconsAreInlineSvg() {
        tester.startComponentInPage(Oat.Components.icon("icon", "pencil"), Markup.of("<svg wicket:id='icon'></svg>"));
        TagTester svg = tester.getTagByWicketId("icon");
        assertThat(svg.getAttribute("class")).isEqualTo("oat-icon");
        assertThat(svg.getAttribute("viewBox")).isEqualTo("0 0 24 24");
        assertThat(svg.getAttribute("aria-hidden")).isEqualTo("true");
        assertThat(svg.getValue()).startsWith("<path d=\"");

        tester.startComponentInPage(new OatIcon("icon", "trash-2").setLabel("Delete"), Markup.of("<span wicket:id='icon'></span>"));
        assertThat(html()).contains("<svg class=\"oat-icon\"").contains("role=\"img\" aria-label=\"Delete\"");
        assertThat(OatIcon.names()).hasSize(64).contains("plus", "search", "chart-line");
        assertThatIllegalArgumentException().isThrownBy(() -> new OatIcon("icon", "no-such-icon"));

        OatIcon.register("brand", "<circle cx=\"12\" cy=\"12\" r=\"10\"/>");
        tester.startComponentInPage(new OatIcon("icon", "brand"), Markup.of("<svg wicket:id='icon'></svg>"));
        assertThat(tester.getTagByWicketId("icon").getValue()).contains("<circle");
    }

    // --- Charts ---

    record Month(String label, Integer revenue, Integer lastYear) implements Serializable {
    }

    private static final List<Month> MONTHS = List.of(new Month("Jan", 1200, 1000), new Month("Feb", 3000, 2500),
            new Month("Mar", null, 2800), new Month("Apr", 2400, 2600));

    @Test
    void barChartScalesBarsToTheLargestValue() {
        tester.startComponentInPage(Oat.Components.barChart("bars", Model.ofList(MONTHS), Month::label, Month::revenue)
                .setTitle("Revenue"));
        assertThat(html()).contains("<figcaption").contains(">Revenue</figcaption>");
        assertThat(html()).contains("width=\"100%\"><title>Feb: 3,000").contains("width=\"40%\"><title>Jan: 1,200");
        assertThat(tags("class", "oat-bar-value")).extracting(TagTester::getValue).containsExactly("1,200", "3,000", "", "2,400");
        assertThat(tags("wicket:id", "label")).extracting(TagTester::getValue).containsExactly("Jan", "Feb", "Mar", "Apr");
        assertThat(tester.getTagByWicketId("plot").getAttribute("role")).isEqualTo("img");
        assertThat(tester.getTagByWicketId("plot").getAttribute("aria-label")).isEqualTo("Revenue");
    }

    @Test
    void columnChartLabelsTheHighestAndLastColumn() {
        tester.startComponentInPage(Oat.Components.columnChart("columns", Model.ofList(MONTHS), Month::label, Month::revenue));
        String svg = tester.getTagByWicketId("svg").getValue();
        assertThat(svg).contains(">3,000</text>").contains(">2,400</text>").doesNotContain(">1,200</text>");
        assertThat(svg).contains("<title>Feb: 3,000</title>").contains("<title>Mar: </title>");
        assertThat(tags("wicket:id", "yTick").size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void lineChartDrawsUpToThreeSeriesWithGaps() {
        OatLineChart<Month> chart = Oat.Components.lineChart("lines", Model.ofList(MONTHS), Month::label)
                .addSeries("2026", Month::revenue).addSeries("2025", Month::lastYear);
        chart.setCategoryHeader(Model.of("Month"));
        tester.startComponentInPage(chart);
        String svg = tester.getTagByWicketId("svg").getValue();
        // 2026 has a gap in March, so two runs; 2025 one
        assertThat(svg.split("<polyline class=\"oat-stroke-1\"").length - 1).isEqualTo(2);
        assertThat(svg.split("<polyline class=\"oat-stroke-2\"").length - 1).isEqualTo(1);
        assertThat(svg).contains("vector-effect=\"non-scaling-stroke\"").contains("2026: 3,000").doesNotContain("oat-area-");
        assertThat(tags("wicket:id", "legend")).hasSize(2);
        assertThat(tester.getTagByWicketId("categoryHeader").getValue()).isEqualTo("Month");

        chart.addSeries("2024", Month::lastYear);
        assertThatIllegalStateException().isThrownBy(() -> chart.addSeries("2023", Month::lastYear));
    }

    @Test
    void aSingleLineGetsAnAreaAndItsLastValue() {
        tester.startComponentInPage(Oat.Components.lineChart("lines", Model.ofList(MONTHS), Month::label).addSeries("2025", Month::lastYear));
        String svg = tester.getTagByWicketId("svg").getValue();
        assertThat(svg).contains("<polygon class=\"oat-area-1\"").contains(">2,600</text>");
        assertThat(tags("wicket:id", "legend")).isEmpty();
    }

    record Region(String name, int revenue) implements Serializable {
    }

    @Test
    void donutChartFoldsSmallPartsIntoOther() {
        List<Region> regions = Arrays.asList(new Region("North", 40), new Region("South", 25), new Region("East", 15),
                new Region("West", 10), new Region("Central", 5), new Region("Islands", 3), new Region("Overseas", 2));
        tester.startComponentInPage(Oat.Components.donutChart("regions", Model.ofList(regions), Region::name, Region::revenue));
        assertThat(tags("class", "oat-donut-name")).extracting(TagTester::getValue)
                .containsExactly("North", "South", "East", "West", "Central", "Other");
        assertThat(tags("wicket:id", "share")).extracting(TagTester::getValue).containsExactly("40%", "25%", "15%", "10%", "5%", "5%");
        String ring = tester.getTagByWicketId("ring").getValue();
        assertThat(ring.split("oat-donut-segment").length - 1).isEqualTo(6);
        assertThat(ring).contains(">100</text>").contains("<title>Other: 5 (5%)</title>");
        // The table still has every region
        assertThat(tags("wicket:id", "category")).hasSize(7);
    }

    @Test
    void chartsHaveADataTable() {
        tester.startComponentInPage(Oat.Components.columnChart("columns", Model.ofList(MONTHS), Month::label, Month::revenue)
                .setValueFormat((value, locale) -> "€" + value));
        assertThat(tester.getTagByWicketId("data").getName()).isEqualTo("details");
        assertThat(tester.getTagByWicketId("summary").getValue()).isEqualTo("Data");
        assertThat(tags("wicket:id", "text")).extracting(TagTester::getValue).contains("€1200", "€3000", "€2400");
    }

    @Test
    void ticksAreRoundNumbers() {
        assertThat(TickAccess.of(0, 128400, 4)).containsExactly(0, 50000, 100000, 150000);
        assertThat(TickAccess.of(0, 3000, 4)).containsExactly(0, 1000, 2000, 3000);
        assertThat(TickAccess.of(-40, 90, 4)).containsExactly(-50, 0, 50, 100);
    }

    /** Exposes the protected tick calculation. */
    static final class TickAccess extends OatChart<Object> {
        private TickAccess() {
            super("x", Model.ofList(List.of()), Object::toString);
        }

        static double[] of(double min, double max, int intervals) {
            return OatChart.ticks(min, max, intervals);
        }

        @Override
        protected String chartClass() {
            return "";
        }
    }

    @Test
    void sparklineAndStatCardTrend() {
        tester.startComponentInPage(Oat.Components.sparkline("trend", Model.ofList(List.of(3, 5, 4, 8))), Markup.of("<span wicket:id='trend'></span>"));
        TagTester spark = tester.getTagByWicketId("trend");
        assertThat(spark.getAttribute("aria-hidden")).isEqualTo("true");
        assertThat(spark.getValue()).contains("<polyline").contains("<circle class=\"oat-dot-1\" cx=\"100.00%\"");

        tester.startComponentInPage(new OatStatCard("orders", "Orders", Model.of(8)).setTrend(Model.ofList(List.of(3, 5, 4, 8))),
                Markup.of("<article wicket:id='orders'></article>"));
        assertThat(html()).contains("oat-sparkline");
        tester.startComponentInPage(new OatStatCard("orders", "Orders", Model.of(8)), Markup.of("<article wicket:id='orders'></article>"));
        tester.assertInvisible("orders:trend");
    }
}
