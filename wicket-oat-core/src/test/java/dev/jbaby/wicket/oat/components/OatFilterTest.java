package dev.jbaby.wicket.oat.components;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            filter.setChoiceRenderer(new ChoiceRenderer<>() {
                @Override
                public Object getDisplayValue(String status) {
                    return status.substring(0, 1).toUpperCase(Locale.ROOT) + status.substring(1);
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

    private String path(FilterPage page) {
        return page.filter.getPageRelativePath() + ":form:group";
    }

    /** Clicks a chip, as the browser sends it: the group's input set to the radio's value. */
    private void choose(FilterPage page, String radioPath) {
        Radio<?> radio = (Radio<?>) tester.getComponentFromLastRenderedPage(path(page) + ":" + radioPath);
        FormTester form = tester.newFormTester(page.filter.getPageRelativePath() + ":form");
        form.setValue("group", radio.getValue());
        tester.executeAjaxEvent(path(page), "change");
    }

    private List<TagTester> chips() {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "class", "oat-filter-chip", false);
    }

    @Test
    void theChoicesAreChipsWithTheChosenOneChecked() {
        tester.startPage(new FilterPage(false));

        assertThat(chips()).extracting(chip -> chip.getChild("wicket:id", "label").getValue())
                .containsExactly("Open", "Paid", "Overdue");
        assertThat(chips()).extracting(chip -> chip.getChild("type", "radio").getAttribute("checked"))
                .containsExactly("checked", null, null);
        TagTester group = tester.getTagByWicketId("group");
        assertThat(group.getName()).isEqualTo("fieldset");
        // Named by the id when the .properties files don't name it
        assertThat(group.getAttribute("aria-label")).isEqualTo("status");
    }

    @Test
    void choosingAChipSetsTheModelAndRunsOnChange() {
        FilterPage page = tester.startPage(new FilterPage(false));

        choose(page, "chips:2:radio");

        assertThat(page.status.getObject()).isEqualTo("overdue");
        assertThat(page.changes).isEqualTo(1);
        tester.assertComponentOnAjaxResponse("shown");
        // The filter itself isn't re-rendered, so the chip keeps the focus
        assertThat(tester.getLastResponseAsString()).doesNotContain("oat-filter-chip");
    }

    @Test
    void aFirstChipCanChooseNoValue() {
        FilterPage page = new FilterPage(false);
        page.filter.setNullValid(true);
        page.status.setObject(null);
        tester.startPage(page);

        assertThat(chips()).extracting(chip -> chip.getChild("wicket:id", "label").getValue())
                .containsExactly("All", "Open", "Paid", "Overdue");
        assertThat(chips().get(0).getChild("type", "radio").getAttribute("checked")).isEqualTo("checked");

        choose(page, "chips:0:radio");
        assertThat(page.status.getObject()).isEqualTo("open");
        choose(page, "none:radio");
        assertThat(page.status.getObject()).isNull();
        assertThat(page.changes).isEqualTo(2);
    }

    @Test
    void itWorksInsideAPagesForm() {
        FilterPage page = tester.startPage(new FilterPage(true));

        choose(page, "chips:1:radio");

        assertThat(page.status.getObject()).isEqualTo("paid");
        assertThat(page.changes).isEqualTo(1);
    }

    @Test
    void theChoicesAreReadOnEveryRender() {
        FilterPage page = tester.startPage(new FilterPage(false));
        page.statuses.add("cancelled");

        tester.startPage(page);

        assertThat(chips()).hasSize(4);
    }

    @Test
    void theGroupCanBeNamedAndAllIsTranslated() {
        tester.getSession().setLocale(Locale.GERMAN);
        FilterPage page = new FilterPage(false);
        page.filter.setNullValid(true).setLabel(Model.of("Status"));
        tester.startPage(page);

        assertThat(tester.getTagByWicketId("group").getAttribute("aria-label")).isEqualTo("Status");
        assertThat(chips().get(0).getChild("wicket:id", "label").getValue()).isEqualTo("Alle");
    }
}
