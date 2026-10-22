package dev.jbaby.wicket.oat.components;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OatFilterTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    /** A status filter above what it shows, optionally inside a page's form. */
    public static class FilterPage extends WebPage implements IMarkupResourceStreamProvider {

        public final Model<String> status = Model.of("open");
        public final List<String> statuses = new ArrayList<>(List.of("open", "paid", "overdue"));
        public final OatFilter<String> filter;
        public int changes;
        private final boolean nested;

        public FilterPage(boolean nested) {
            this.nested = nested;
            Label shown = new Label("shown", status);
            shown.setOutputMarkupId(true);
            add(shown);

            filter = OatFilter.onChange("status", status, Model.ofList(statuses), target -> {
                changes++;
                target.add(shown);
            });
            filter.setChoiceRenderer(new ChoiceRenderer<String>() {
                @Override
                public Object getDisplayValue(String status) {
                    return status.substring(0, 1).toUpperCase(Locale.ROOT) + status.substring(1);
                }

                @Override
                public String getIdValue(String status, int index) {
                    return status;
                }
            });
            if (nested) {
                Form<Void> outer = new Form<>("outer");
                add(outer);
                outer.add(filter);
            } else {
                add(filter);
            }
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            String filterTag = "<div wicket:id='status'></div>";
            return new StringResourceStream("<html><body>"
                    + (nested ? "<form wicket:id='outer'>" + filterTag + "</form>" : filterTag)
                    + "<span wicket:id='shown'></span></body></html>");
        }
    }

    /** Clicks a chip, as the browser sends it: the input set to the chip's value. */
    private void choose(OatFilter<?> filter, String value) {
        String path = filter.getPageRelativePath() + ":form:choice";
        FormComponent<?> choice = (FormComponent<?>) tester.getComponentFromLastRenderedPage(path);
        tester.getRequest().getPostParameters().setParameterValue(choice.getInputName(), value);
        tester.executeAjaxEvent(path, "change");
    }

    private List<TagTester> chips() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "class", "oat-filter-chip", false);
    }

    private List<String> texts() {
        return chips().stream().map(chip -> chip.getValue().replaceAll("<[^>]+>", "").trim()).toList();
    }

    private List<String> values() {
        return chips().stream().map(chip -> chip.getChild("type", "radio").getAttribute("value")).toList();
    }

    private List<String> checked() {
        return chips().stream().map(chip -> chip.getChild("type", "radio").getAttribute("checked")).toList();
    }

    @Test
    void theChoicesAreChipsWithTheChosenOneChecked() {
        tester.startPage(new FilterPage(false));

        assertThat(texts()).containsExactly("Open", "Paid", "Overdue");
        assertThat(values()).containsExactly("open", "paid", "overdue");
        assertThat(checked()).containsExactly("checked", null, null);
        TagTester group = tester.getTagByWicketId("choice");
        assertThat(group.getName()).isEqualTo("fieldset");
        // Named by the id when the .properties files don't name it
        assertThat(group.getAttribute("aria-label")).isEqualTo("status");
        // Enter on a chip is kept from submitting the form
        assertThat(tester.getLastResponseAsString()).contains("wicket-oat");
    }

    @Test
    void choosingAChipSetsTheModelAndRunsOnChange() {
        FilterPage page = tester.startPage(new FilterPage(false));

        choose(page.filter, "overdue");

        assertThat(page.status.getObject()).isEqualTo("overdue");
        assertThat(page.changes).isEqualTo(1);
        tester.assertComponentOnAjaxResponse("shown");
        // The filter itself isn't re-rendered, so the chip keeps the focus
        assertThat(tester.getLastResponseAsString()).doesNotContain("oat-filter-chip");
    }

    @Test
    void aChipStandsForItsValueAlsoWhenTheChoicesChanged() {
        FilterPage page = tester.startPage(new FilterPage(false));
        // Another status comes first by the time "Paid" is clicked
        page.statuses.add(0, "cancelled");

        choose(page.filter, "paid");

        assertThat(page.status.getObject()).isEqualTo("paid");
    }

    @Test
    void aFirstChipCanChooseNoValue() {
        FilterPage page = new FilterPage(false);
        page.filter.setNullValid(true);
        page.status.setObject(null);
        tester.startPage(page);

        assertThat(texts()).containsExactly("All", "Open", "Paid", "Overdue");
        assertThat(values().get(0)).isEmpty();
        assertThat(checked().get(0)).isEqualTo("checked");

        choose(page.filter, "open");
        assertThat(page.status.getObject()).isEqualTo("open");
        choose(page.filter, "");
        assertThat(page.status.getObject()).isNull();
        assertThat(page.changes).isEqualTo(2);
    }

    @Test
    void withoutAllNoValueCanBeChosen() {
        FilterPage page = tester.startPage(new FilterPage(false));

        // As a request made up, or one from before "All" was turned off, would
        choose(page.filter, "");
        choose(page.filter, "no-such-status");

        assertThat(page.status.getObject()).isEqualTo("open");
        assertThat(page.changes).isZero();
    }

    @Test
    void itWorksInsideAPagesForm() {
        FilterPage page = tester.startPage(new FilterPage(true));

        choose(page.filter, "paid");

        assertThat(page.status.getObject()).isEqualTo("paid");
        assertThat(page.changes).isEqualTo(1);
    }

    @Test
    void theChoicesAreReadOncePerRequestAndFollowedOnEveryRender() {
        AtomicInteger reads = new AtomicInteger();
        List<String> statuses = new ArrayList<>(List.of("open", "paid"));
        OatFilter<String> filter = new OatFilter<>("status", Model.of("open"), (IModel<List<String>>) () -> {
            reads.incrementAndGet();
            return statuses;
        });
        tester.startComponentInPage(filter);
        assertThat(reads).hasValue(1);

        statuses.add("overdue");
        tester.startComponentInPage(filter);
        assertThat(chips()).hasSize(3);
    }

    @Test
    void nullAmongTheChoicesIsLeftOut() {
        tester.startComponentInPage(new OatFilter<>("status", Model.of("open"), Model.ofList(Arrays.asList(null, "open", "paid"))));

        assertThat(texts()).containsExactly("open", "paid");
    }

    /** A value without equals(), loaded again for each request, as an entity often is. */
    public static class Category implements Serializable {

        private final String code;

        public Category(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    @Test
    void valuesWithoutEqualsAreCheckedByTheirId() {
        OatFilter<Category> filter = new OatFilter<>("category", Model.of(new Category("books")),
                (IModel<List<Category>>) () -> List.of(new Category("music"), new Category("books")));
        filter.setChoiceRenderer(new ChoiceRenderer<>("code", "code"));
        tester.startComponentInPage(filter);

        assertThat(checked()).containsExactly(null, "checked");
    }

    /** An order whose status a filter edits through the form's CompoundPropertyModel. */
    public static class Order implements Serializable {

        private String status = "open";

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @Test
    void withoutAModelItUsesTheOneInheritedByItsId() {
        Order order = new Order();
        WebMarkupContainer form = new WebMarkupContainer("order", new CompoundPropertyModel<>(order));
        OatFilter<String> filter = new OatFilter<>("status", null, Model.ofList(List.of("open", "paid")));
        form.add(filter);
        tester.startComponentInPage(form, Markup.of("<div wicket:id='order'><div wicket:id='status'></div></div>"));
        assertThat(checked()).containsExactly("checked", null);

        choose(filter, "paid");

        assertThat(order.getStatus()).isEqualTo("paid");
    }

    @Test
    void theGroupCanBeNamedAndAllIsTranslated() {
        tester.getSession().setLocale(Locale.GERMAN);
        FilterPage page = new FilterPage(false);
        page.filter.setNullValid(true).setLabel(Model.of("Status"));
        tester.startPage(page);

        assertThat(tester.getTagByWicketId("choice").getAttribute("aria-label")).isEqualTo("Status");
        assertThat(texts().get(0)).isEqualTo("Alle");
    }

    @Test
    void aResourceModelNameIsLookedUpForTheFilter() {
        FilterPage page = new FilterPage(false);
        // In OatFilterTest$FilterPage.properties
        page.filter.setLabel(new ResourceModel("orders.status"));
        tester.startPage(page);

        assertThat(tester.getTagByWicketId("choice").getAttribute("aria-label")).isEqualTo("Order status");
    }
}
