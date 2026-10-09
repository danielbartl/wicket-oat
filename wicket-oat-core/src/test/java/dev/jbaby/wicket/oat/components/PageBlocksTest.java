package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageBlocksTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    // --- Page header ---

    @Test
    void pageHeaderShowsTitleBreadcrumbAndActions() {
        OatPageHeader header = Oat.Components.pageHeader("header", "Invoices")
                .setSubtitle("23 open")
                .setBreadcrumb(List.of(MenuItem.of("Home", LayoutAndNavigationTestPages.Home.class)));
        header.add(new Label("new", "New invoice"));
        tester.startComponentInPage(header, Markup.of("<header wicket:id='header'><span wicket:id='new'></span></header>"));

        assertThat(tester.getTagByWicketId("header").getAttribute("class")).isEqualTo("oat-page-header");
        tester.assertLabel("header:title", "Invoices");
        tester.assertLabel("header:subtitle", "23 open");
        assertThat(tester.getTagByWicketId("breadcrumb").getAttribute("aria-label")).isEqualTo("Breadcrumb");
        assertThat(tester.getTagByWicketId("link").getName()).isEqualTo("a");
        TagTester current = tester.getTagByWicketId("current");
        assertThat(current.getValue()).isEqualTo("Invoices");
        assertThat(current.getAttribute("aria-current")).isEqualTo("page");
        assertThat(tester.getTagByWicketId("new").getValue()).isEqualTo("New invoice");
    }

    @Test
    void pageHeaderLeavesOutWhatIsNotSet() {
        tester.startComponentInPage(new OatPageHeader("orders"), Markup.of("<header wicket:id='orders'></header>"));

        tester.assertLabel("orders:title", "orders"); // looked up by id, falling back to it
        tester.assertInvisible("orders:subtitle");
        tester.assertInvisible("orders:breadcrumb");
    }

    @Test
    void pageHeaderIsValidated() {
        OatPageHeader header = new OatPageHeader("h", "Title");
        assertThatIllegalArgumentException().isThrownBy(() -> header.setBreadcrumb(List.of(
                MenuItem.group("Group", MenuItem.of("Home", LayoutAndNavigationTestPages.Home.class)))));
        assertThatThrownBy(() -> tester.startComponentInPage(header, Markup.of("<div wicket:id='h'></div>")))
                .hasMessageContaining("header");
    }

    // --- Stat card ---

    @Test
    void statCardShowsTheFigureAndItsChange() {
        tester.startComponentInPage(Oat.Components.statCard("revenue", "Revenue", Model.of(48250))
                .setChange(Model.of(12.5)).setHint(Model.of("vs. last month")),
                Markup.of("<article wicket:id='revenue'></article>"));

        assertThat(tester.getTagByWicketId("revenue").getAttribute("class")).isEqualTo("card oat-stat");
        tester.assertLabel("revenue:label", "Revenue");
        tester.assertLabel("revenue:value", "48,250");
        TagTester change = tester.getTagByWicketId("change");
        assertThat(change.getValue()).isEqualTo("+12.5%");
        assertThat(change.getAttribute("data-variant")).isEqualTo("success");
        tester.assertLabel("revenue:footer:hint", "vs. last month");
    }

    @Test
    void statCardColorsTheChangeByWhatIsBetter() {
        assertThat(change(-3.25, true)).containsExactly("-3.2%", "danger");
        assertThat(change(8, false)).containsExactly("+8%", "danger");
        assertThat(change(-8, false)).containsExactly("-8%", "success");
        assertThat(change(0, true)).containsExactly("0%", "secondary");

        tester.getSession().setLocale(Locale.GERMANY);
        assertThat(change(12.5, true)).containsExactly("+12,5 %", "success");
    }

    private List<String> change(double percent, boolean higherIsBetter) {
        tester.startComponentInPage(new OatStatCard("s", "Costs", Model.of(1)).setChange(Model.of(percent), higherIsBetter),
                Markup.of("<article wicket:id='s'></article>"));
        TagTester change = tester.getTagByWicketId("change");
        return List.of(change.getValue(), change.getAttribute("data-variant"));
    }

    @Test
    void statCardWithoutChangeOrHintHasNoFooterAndKeepsItsBody() {
        OatStatCard card = new OatStatCard("s", "Users", Model.of(7));
        card.add(new Label("extra", "Details"));
        tester.startComponentInPage(card, Markup.of("<article wicket:id='s'><a wicket:id='extra'></a></article>"));

        tester.assertInvisible("s:footer");
        assertThat(tester.getTagByWicketId("extra").getValue()).isEqualTo("Details");
    }

    // --- Description list ---

    public static class Customer implements Serializable {
        public String name = "ACME GmbH";
        public String vatId;
        public java.time.LocalDate since = java.time.LocalDate.of(2019, 3, 1);
    }

    @Test
    void descriptionListShowsLabelsAndValues() {
        OatDescriptionList details = Oat.Components.descriptionList("details", new CompoundPropertyModel<>(new Customer()))
                .addProperty("name")
                .addProperty("vatId")
                .addProperty("since")
                .addItem("Revenue", Model.of(1234))
                .addItem(Model.of("Status"), id -> new OatBadge(id, "Active", OatVariant.SUCCESS));
        tester.startComponentInPage(details, Markup.of("<dl wicket:id='details'></dl>"));

        assertThat(tester.getTagByWicketId("details").getAttribute("class")).isEqualTo("oat-dl");
        assertThat(tags("wicket:id", "term")).extracting(TagTester::getValue).containsExactly("name", "vatId", "since", "Revenue", "Status");
        assertThat(tags("wicket:id", "term")).allMatch(t -> "dt".equals(t.getName()));
        List<TagTester> values = tags("wicket:id", "value");
        assertThat(values).extracting(TagTester::getValue).containsExactly("ACME GmbH", "—", "Mar 1, 2019", "1,234", "Active");
        assertThat(values.get(4).getAttribute("class")).isEqualTo("badge");
    }

    @Test
    void descriptionListIsValidated() {
        OatDescriptionList list = new OatDescriptionList("d");
        assertThatIllegalArgumentException().isThrownBy(() -> list.addItem(Model.of("x"), id -> new Label("other")));
        assertThatThrownBy(() -> tester.startComponentInPage(new OatDescriptionList("d").addItem("a", Model.of("b")),
                Markup.of("<div wicket:id='d'></div>"))).hasMessageContaining("dl");
    }

    // --- Split button ---

    @Test
    void splitButtonRunsTheMainActionOrAMenuEntry() {
        List<String> clicked = new ArrayList<>();
        OatSplitButton save = Oat.Components.splitButton("save", "Save", target -> clicked.add("save"))
                .setVariant(OatVariant.SECONDARY).setSize(ButtonBehavior.Size.SMALL);
        save.addAction("Save as draft", target -> clicked.add("draft"));
        save.addAction("Discard", target -> clicked.add("discard")).setVariant(OatVariant.DANGER);
        tester.startComponentInPage(save);

        TagTester primary = tester.getTagByWicketId("primary");
        assertThat(primary.getValue()).isEqualTo("Save");
        assertThat(primary.getAttribute("data-variant")).isEqualTo("secondary");
        TagTester trigger = tester.getTagByWicketId("trigger");
        assertThat(trigger.getAttribute("data-variant")).isEqualTo("secondary");
        assertThat(trigger.getAttribute("class")).isEqualTo("small");
        assertThat(trigger.getAttribute("aria-label")).isEqualTo("More actions");
        List<TagTester> items = tags("role", "menuitem");
        assertThat(items).hasSize(2);
        assertThat(items.get(1).getAttribute("data-variant")).isEqualTo("danger");

        tester.executeAjaxEvent("save:primary", "click");
        tester.executeAjaxEvent("save:menu:menu:items:1", "click");
        assertThat(clicked).containsExactly("save", "discard");
        assertThat(tester.getLastResponseAsString()).contains("hidePopover()");
    }

    // --- Popover ---

    @Test
    void popoverHoldsAnyContent() {
        OatPopover popover = Oat.Components.popover("filters", "Filters", id -> new Label(id, "Only overdue"));
        tester.startComponentInPage(popover);

        TagTester content = tester.getTagByWicketId("popover");
        assertThat(content.getAttribute("popover")).isNotNull();
        assertThat(tester.getTagByWicketId("trigger").getAttribute("popovertarget")).isEqualTo(content.getAttribute("id"));
        assertThat(tester.getTagByWicketId("content").getValue()).isEqualTo("Only overdue");
        assertThat(popover.getContent().getOutputMarkupId()).isTrue();
        assertThatIllegalArgumentException().isThrownBy(() -> new OatPopover("p", "x", id -> new Label("other")));
    }

    // --- Suggestions ---

    @Test
    void fieldsCanSuggestValues() {
        OatTextField<String> city = new OatTextField<String>("city", "City", Model.of("")).setSuggestions(List.of("Berlin", "Vienna"));
        tester.startComponentInPage(city);

        TagTester datalist = tester.getTagByWicketId("suggestions");
        assertThat(datalist.getName()).isEqualTo("datalist");
        assertThat(tester.getTagByWicketId("field").getAttribute("list")).isEqualTo(datalist.getAttribute("id"));
        assertThat(tags("wicket:id", "option")).extracting(o -> o.getAttribute("value")).containsExactly("Berlin", "Vienna");
    }

    @Test
    void fieldsWithoutSuggestionsHaveNoDatalist() {
        tester.startComponentInPage(new OatTextField<>("city", "City", Model.of("")));
        assertThat(tester.getLastResponseAsString()).doesNotContain("datalist");
        assertThat(tester.getTagByWicketId("field").getAttribute("list")).isNull();
    }
}
