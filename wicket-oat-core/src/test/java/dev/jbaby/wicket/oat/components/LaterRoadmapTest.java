package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.form.DateRange;
import dev.jbaby.wicket.oat.components.form.OatAutoCompleteField;
import dev.jbaby.wicket.oat.components.form.OatDateRangeField;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaterRoadmapTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    private FormTester startInForm(org.apache.wicket.Component field) {
        Form<Void> form = new Form<>("form");
        form.add(field);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='" + field.getId() + "'></div></form>"));
        return tester.newFormTester("form");
    }

    // --- Date range ---

    @Test
    void dateRangeShowsTwoDateInputs() {
        startInForm(new OatDateRangeField("period", "Period",
                Model.of(new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)))));

        assertThat(tester.getTagByWicketId("label").getName()).isEqualTo("legend");
        TagTester from = tester.getTagByWicketId("from");
        TagTester to = tester.getTagByWicketId("to");
        assertThat(from.getAttribute("type")).isEqualTo("date");
        assertThat(from.getAttribute("value")).isEqualTo("2026-01-01");
        assertThat(to.getAttribute("value")).isEqualTo("2026-03-31");
        assertThat(from.getAttribute("aria-label")).isEqualTo("From");
        assertThat(to.getAttribute("aria-label")).isEqualTo("To");
        assertThat(from.getAttribute("aria-describedby")).isEqualTo(tester.getTagByWicketId("feedback").getAttribute("id"));
    }

    @Test
    void dateRangeAcceptsAWholeOrOpenRange() {
        Model<DateRange> model = new Model<>();
        OatDateRangeField field = new OatDateRangeField("period", "Period", model);
        FormTester form = startInForm(field);
        form.setValue("period:container:field:from", "2026-01-01");
        form.setValue("period:container:field:to", "2026-03-31");
        form.submit();
        assertThat(model.getObject()).isEqualTo(new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)));

        form = tester.newFormTester("form");
        form.setValue("period:container:field:from", "2026-02-01");
        form.setValue("period:container:field:to", "");
        form.submit();
        assertThat(model.getObject()).isEqualTo(new DateRange(LocalDate.of(2026, 2, 1), null));
        assertThat(model.getObject().contains(LocalDate.of(2030, 1, 1))).isTrue();
        assertThat(model.getObject().contains(LocalDate.of(2026, 1, 31))).isFalse();

        form = tester.newFormTester("form");
        form.setValue("period:container:field:from", "");
        form.submit();
        assertThat(model.getObject()).isNull();
    }

    @Test
    void dateRangeRejectsAnEndBeforeItsStart() {
        Model<DateRange> model = new Model<>();
        OatDateRangeField field = new OatDateRangeField("period", "Period", model);
        FormTester form = startInForm(field);
        form.setValue("period:container:field:from", "2026-03-01");
        form.setValue("period:container:field:to", "2026-02-01");
        form.submit();

        assertThat(model.getObject()).isNull();
        assertThat(tester.getTagByWicketId("feedback").getValue()).isEqualTo("The end of &#039;Period&#039; must not be before its start.");
        assertThat(tester.getTagByWicketId("from").getAttribute("aria-invalid")).isEqualTo("true");
    }

    @Test
    void aRequiredDateRangeNeedsBothEnds() {
        OatDateRangeField field = new OatDateRangeField("period", "Period", new Model<>()).setRequired(true);
        FormTester form = startInForm(field);
        form.setValue("period:container:field:from", "2026-03-01");
        form.submit();
        assertThat(field.getField().hasErrorMessage()).isTrue();
    }

    // --- Autocomplete ---

    record Customer(long id, String name) implements Serializable {
    }

    private static final List<Customer> CUSTOMERS = List.of(new Customer(1, "ACME GmbH"), new Customer(2, "Acme Ltd"), new Customer(3, "Globex"));

    private static List<Customer> search(String text) {
        return CUSTOMERS.stream().filter(c -> c.name().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))).toList();
    }

    @Test
    void autoCompleteSuggestsMatchesFromTheServer() {
        OatAutoCompleteField<Customer> field = new OatAutoCompleteField<Customer>("customer", "Customer", new Model<>())
                .setChoices(LaterRoadmapTest::search).setDisplay(Customer::name).setMaxChoices(1);
        startInForm(field);
        TagTester input = tester.getTagByWicketId("field");
        assertThat(input.getAttribute("autocomplete")).isEqualTo("off");
        assertThat(input.getAttribute("role")).isEqualTo("combobox"); // Wicket's own ARIA support

        tester.getRequest().getPostParameters().setParameterValue("q", "acme");
        tester.executeBehavior(field.getField().getBehaviors(AutoCompleteBehavior.class).get(0));
        assertThat(tester.getLastResponseAsString()).contains("<li textvalue=\"ACME GmbH\"").doesNotContain("Acme Ltd");
    }

    @Test
    void autoCompleteStoresTheChosenObject() {
        Model<Customer> model = new Model<>();
        OatAutoCompleteField<Customer> field = new OatAutoCompleteField<Customer>("customer", "Customer", model)
                .setChoices(LaterRoadmapTest::search).setDisplay(Customer::name);
        FormTester form = startInForm(field);
        form.setValue("customer:container:field", "acme ltd");
        form.submit();
        assertThat(model.getObject()).isEqualTo(CUSTOMERS.get(1));

        // Shown again by its display text
        tester.startPage(tester.getLastRenderedPage());
        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("Acme Ltd");
    }

    @Test
    void autoCompleteRejectsTextThatIsNoSuggestion() {
        Model<Customer> model = Model.of(CUSTOMERS.get(2));
        OatAutoCompleteField<Customer> field = new OatAutoCompleteField<Customer>("customer", "Customer", model)
                .setChoices(LaterRoadmapTest::search).setDisplay(Customer::name);
        FormTester form = startInForm(field);
        form.setValue("customer:container:field", "Initech");
        form.submit();

        assertThat(model.getObject()).isEqualTo(CUSTOMERS.get(2));
        assertThat(tester.getTagByWicketId("feedback").getValue()).isEqualTo("Choose &#039;Customer&#039; from the suggestions.");
    }

    @Test
    void autoCompleteCanAcceptFreeText() {
        Model<String> model = new Model<>();
        OatAutoCompleteField<String> field = Oat.Components.autoCompleteField("city", "City", model)
                .setChoices(text -> List.of("Vienna", "Villach")).setFreeText(text -> text);
        FormTester form = startInForm(field);
        form.setValue("city:container:field", "Graz");
        form.submit();
        assertThat(model.getObject()).isEqualTo("Graz");
        assertThatIllegalArgumentException().isThrownBy(() -> field.setMaxChoices(0));
    }

    // --- Timeline ---

    record Event(String title, LocalDateTime at, String details, OatVariant variant) implements Serializable {
    }

    @Test
    void timelineShowsEventsInOrder() {
        List<Event> events = List.of(
                new Event("Invoice sent", LocalDateTime.of(2026, 10, 1, 9, 30), "By Jordan", null),
                new Event("Payment received", LocalDateTime.of(2026, 10, 9, 14, 5), null, OatVariant.SUCCESS),
                new Event("Note", null, "  ", null));
        tester.startComponentInPage(Oat.Components.timeline("history", Model.ofList(events), Event::title, Event::at)
                .setDescription(Event::details).setVariant(Event::variant));

        assertThat(tags("wicket:id", "title")).extracting(TagTester::getValue).containsExactly("Invoice sent", "Payment received", "Note");
        List<TagTester> times = tags("wicket:id", "time");
        assertThat(times).hasSize(2);
        assertThat(times.get(0).getName()).isEqualTo("time");
        assertThat(times.get(0).getAttribute("datetime")).isEqualTo("2026-10-01T09:30");
        assertThat(times.get(0).getValue()).isEqualTo("Oct 1, 2026, 9:30 AM");
        assertThat(tags("wicket:id", "description")).extracting(TagTester::getValue).containsExactly("By Jordan");
        assertThat(tags("wicket:id", "events")).extracting(e -> e.getAttribute("data-variant")).containsExactly(null, "success", null);
    }

    // --- Wizard ---

    @Test
    void wizardGoesThroughItsStepsValidatingEach() {
        Model<String> name = new Model<>();
        Model<String> email = new Model<>();
        List<String> finished = new ArrayList<>();
        OatWizard wizard = Oat.Components.wizard("wizard", target -> finished.add(name.getObject() + " " + email.getObject()))
                .addStep("Name", id -> new OatTextField<>(id, "Name", name).setRequired(true))
                .addStep("Email", id -> new OatTextField<>(id, "Email", email).setRequired(true))
                .addStep("Review", id -> new org.apache.wicket.markup.html.basic.Label(id, "All set"));
        tester.startComponentInPage(wizard);

        List<TagTester> steps = tags("wicket:id", "steps");
        assertThat(steps).extracting(s -> s.getAttribute("data-state")).containsExactly("current", "upcoming", "upcoming");
        assertThat(steps.get(0).getAttribute("aria-current")).isEqualTo("step");
        tester.assertInvisible("wizard:form:back");
        tester.assertInvisible("wizard:form:finish");

        // Next with the required field empty stays and shows the error
        tester.executeAjaxEvent("wizard:form:next", "click");
        assertThat(wizard.getCurrentStep()).isZero();
        assertThat(tester.getLastResponseAsString()).contains("data-field=\"error\"");

        FormTester form = tester.newFormTester("wizard:form");
        form.setValue("body:container:field", "Ada");
        tester.executeAjaxEvent("wizard:form:next", "click");
        assertThat(wizard.getCurrentStep()).isEqualTo(1);
        assertThat(tags("wicket:id", "steps")).extracting(s -> s.getAttribute("data-state")).containsExactly("done", "current", "upcoming");

        // Back to a finished step by its number, then forward again
        tester.clickLink(wizard.get("steps:0:link"));
        assertThat(wizard.getCurrentStep()).isZero();
        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("Ada");
        tester.executeAjaxEvent("wizard:form:next", "click");
        form = tester.newFormTester("wizard:form");
        form.setValue("body:container:field", "ada@example.com");
        tester.executeAjaxEvent("wizard:form:next", "click");
        assertThat(wizard.getCurrentStep()).isEqualTo(2);
        tester.assertInvisible("wizard:form:next");

        tester.executeAjaxEvent("wizard:form:finish", "click");
        assertThat(finished).containsExactly("Ada ada@example.com");
    }

    @Test
    void wizardOnNextCanKeepTheUserOnAStep() {
        OatWizard wizard = new OatWizard("wizard") {
            @Override
            protected void onNext(int step, org.apache.wicket.ajax.AjaxRequestTarget target) {
                error("Not yet");
            }

            @Override
            protected void onFinish(org.apache.wicket.ajax.AjaxRequestTarget target) {
            }
        };
        wizard.addStep("One", id -> new org.apache.wicket.markup.html.basic.Label(id, "1"))
                .addStep("Two", id -> new org.apache.wicket.markup.html.basic.Label(id, "2"));
        tester.startComponentInPage(wizard);
        tester.executeAjaxEvent("wizard:form:next", "click");
        assertThat(wizard.getCurrentStep()).isZero();
    }

    @Test
    void wizardIsValidated() {
        assertThatThrownBy(() -> tester.startComponentInPage(Oat.Components.wizard("w", target -> { })))
                .isInstanceOf(IllegalStateException.class);
        OatWizard wizard = Oat.Components.wizard("w", target -> { }).addStep("One", id -> new org.apache.wicket.markup.html.basic.Label(id));
        assertThatThrownBy(() -> wizard.setCurrentStep(1)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
