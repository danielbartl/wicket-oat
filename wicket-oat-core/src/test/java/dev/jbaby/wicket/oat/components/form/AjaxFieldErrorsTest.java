package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.components.OatSubmitButton;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.feedback.DefaultCleanupFeedbackMessageFilter;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.StringValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Oat fields show the errors of an Ajax request themselves, without the handler adding
 * them to the AjaxRequestTarget.
 */
class AjaxFieldErrorsTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        // As in production: Wicket removes the messages it showed at the end of a request
        // (WicketTester keeps them)
        tester.getApplication().getApplicationSettings().setFeedbackMessageCleanupFilter(new DefaultCleanupFeedbackMessageFilter());
    }

    /** An amount typed into an inner input, which reports its own conversion error. */
    static class AmountInput extends FormComponentPanel<Integer> implements IMarkupResourceStreamProvider {

        private final TextField<Integer> number = new TextField<>("number", new Model<>(), Integer.class);

        AmountInput(String id, IModel<Integer> model) {
            super(id, model);
            add(number);
        }

        @Override
        public void convertInput() {
            setConvertedInput(number.getConvertedInput());
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<wicket:panel><input wicket:id='number'/></wicket:panel>");
        }
    }

    /**
     * A required field, one validated on its own when it loses the focus, one made of an
     * inner input, buttons whose handlers add nothing, add the form, send the target on or
     * skip validation, and a link outside the form.
     */
    public static class ErrorsPage extends WebPage implements IMarkupResourceStreamProvider {

        public final OatTextField<String> name;
        public final OatTextField<String> code;
        public final OatCustomField<Integer> amount;
        public final Form<Void> form;

        public ErrorsPage() {
            form = new Form<>("form");
            form.setOutputMarkupId(true);
            add(form);
            form.add(name = new OatTextField<String>("name", "Name", Model.of("")).setRequired(true));
            form.add(code = new OatTextField<String>("code", Model.of("Code"), Model.of(""), Model.of("Three letters"))
                    .addValidator(StringValidator.exactLength(3))
                    .add(new AjaxFormComponentUpdatingBehavior("blur") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                        }
                    }));
            form.add(amount = new OatCustomField<>("amount", "Amount", new Model<>(), AmountInput::new));
            form.add(OatSubmitButton.onSubmit("save", Model.of("Save"), target -> {
            }));
            form.add(OatSubmitButton.onSubmit("saveAndAdd", "Save", target -> target.add(form), target -> target.add(form)));
            form.add(OatSubmitButton.onSubmit("saveAndSend", "Save", target -> {
            }, target -> send(this, Broadcast.BREADTH, target)));
            form.add(new AjaxButton("cancel") {
            }.setDefaultFormProcessing(false));
            add(new AjaxLink<Void>("elsewhere") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body>"
                    + "<form wicket:id='form'><div wicket:id='name'></div><div wicket:id='code'></div><div wicket:id='amount'></div>"
                    + "<button wicket:id='save'></button><button wicket:id='saveAndAdd'></button>"
                    + "<button wicket:id='saveAndSend'></button><button wicket:id='cancel'></button></form>"
                    + "<a wicket:id='elsewhere'>elsewhere</a>"
                    + "</body></html>");
        }
    }

    private void submit(String button, String name) {
        tester.newFormTester("form").setValue("name:input", name);
        tester.executeAjaxEvent("form:" + button, "click");
    }

    private void blurCode(String code) {
        tester.newFormTester("form").setValue("code:input", code);
        tester.executeAjaxEvent("form:code:input", "blur");
    }

    private static String update(BaseOatField<?, ?, ?> field, String text, boolean invalid) {
        return "WicketOat.fieldFeedback(\"" + field.getMarkupId() + "\",\"" + field.get("feedback").getMarkupId() + "\",\""
                + text + "\"," + invalid + ")";
    }

    private static String anyUpdate(BaseOatField<?, ?, ?> field) {
        return "WicketOat.fieldFeedback(\"" + field.getMarkupId() + "\"";
    }

    private int count(String text) {
        return tester.getLastResponseAsString().split(Pattern.quote(text), -1).length - 1;
    }

    @Test
    void anErrorAppearsWithoutTheHandlerAddingTheField() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        assertThat(tester.getLastResponseAsString()).contains("OatAppLayout/wicket-oat");

        submit("save", "");

        String response = tester.getLastResponseAsString();
        assertThat(response).contains(update(page.name, "'Name' is required.", true))
                // Nothing is re-rendered, so the input keeps its focus and what the user typed,
                // and the message stays the same live region for screen readers
                .doesNotContain("<component id=\"" + page.name.getMarkupId() + "\"")
                .doesNotContain("<component id=\"" + page.name.getInput().getMarkupId() + "\"")
                .doesNotContain("<component id=\"" + page.name.get("feedback").getMarkupId() + "\"");
        // Marked as shown, so Wicket removed it at the end of the request
        assertThat(page.name.getInput().getFeedbackMessages().isEmpty()).isTrue();
    }

    @Test
    void aFixedErrorGoesAwayWithoutTheHandlerAddingTheField() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        submit("save", "");

        submit("save", "Ada");

        assertThat(tester.getLastResponseAsString()).doesNotContain("is required.")
                .contains(update(page.name, "", false));
    }

    @Test
    void theSameErrorAgainIsNotSentAgainButCountsAsShown() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        submit("save", "");

        submit("save", "");

        assertThat(tester.getLastResponseAsString()).doesNotContain("<component").doesNotContain("WicketOat.fieldFeedback");
        assertThat(page.name.getInput().getFeedbackMessages().isEmpty()).isTrue();
    }

    @Test
    void anotherAjaxRequestLeavesAShownErrorAlone() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        submit("save", "");

        tester.clickLink("elsewhere");
        assertThat(tester.getLastResponseAsString()).doesNotContain("<component").doesNotContain("WicketOat.fieldFeedback");

        submit("save", "Ada");
        assertThat(tester.getLastResponseAsString()).contains(update(page.name, "", false));
    }

    @Test
    void aButtonThatSkipsValidationLeavesAShownErrorAlone() {
        tester.startPage(new ErrorsPage());
        submit("save", "");

        submit("cancel", "Ada");

        assertThat(tester.getLastResponseAsString()).doesNotContain("WicketOat.fieldFeedback");
    }

    @Test
    void aFieldValidatedOnItsOwnShowsItsErrorAndThenItsHint() {
        ErrorsPage page = tester.startPage(new ErrorsPage());

        blurCode("ab");
        assertThat(tester.getLastResponseAsString()).containsPattern(Pattern.quote(anyUpdate(page.code)) + ",\"[^\"]+\",\"[^\"]*Code[^\"]*\",true\\)")
                .doesNotContain(anyUpdate(page.name));

        blurCode("abc");
        assertThat(tester.getLastResponseAsString()).contains(update(page.code, "Three letters", false));
    }

    @Test
    void aFieldWhoseFormIsAddedIsRenderedOnceWithIt() {
        tester.startPage(new ErrorsPage());

        submit("saveAndAdd", "");

        tester.assertComponentOnAjaxResponse("form");
        assertThat(count("is required.")).isEqualTo(1);
        assertThat(tester.getLastResponseAsString()).contains("data-field=\"error\"").doesNotContain("WicketOat.fieldFeedback");
    }

    @Test
    void aTargetSentOnAgainUpdatesTheFieldOnce() {
        ErrorsPage page = tester.startPage(new ErrorsPage());

        submit("saveAndSend", "");

        assertThat(count(anyUpdate(page.name))).isEqualTo(1);
    }

    @Test
    void anInputWithAnErrorOfItsOwnIsRenderedAgain() {
        tester.startPage(new ErrorsPage());

        FormTester form = tester.newFormTester("form");
        form.setValue("name:input", "Ada");
        form.setValue("amount:input:number", "lots");
        tester.executeAjaxEvent("form:save", "click");
        // Only a render marks the inner input invalid, not the amount as a whole
        tester.assertComponentOnAjaxResponse("form:amount");
        assertThat(tester.getTagByWicketId("number").getAttribute("aria-invalid")).isEqualTo("true");

        form = tester.newFormTester("form");
        form.setValue("name:input", "Ada");
        form.setValue("amount:input:number", "5");
        tester.executeAjaxEvent("form:save", "click");
        tester.assertComponentOnAjaxResponse("form:amount");
        assertThat(tester.getTagByWicketId("number").getAttribute("aria-invalid")).isEqualTo("false");

        form = tester.newFormTester("form");
        form.setValue("name:input", "Ada");
        form.setValue("amount:input:number", "6");
        tester.executeAjaxEvent("form:save", "click");
        assertThat(tester.getLastResponseAsString()).doesNotContain("<component");
    }
}
