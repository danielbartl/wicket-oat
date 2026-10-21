package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.components.OatSubmitButton;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }

    /** A required field, a button whose handlers add nothing, and one that adds the form. */
    public static class ErrorsPage extends WebPage implements IMarkupResourceStreamProvider {

        public final OatTextField<String> name;
        public final Form<Void> form;

        public ErrorsPage() {
            form = new Form<>("form");
            form.setOutputMarkupId(true);
            add(form);
            form.add(name = new OatTextField<String>("name", "Name", Model.of("")).setRequired(true));
            form.add(OatSubmitButton.onSubmit("save", Model.of("Save"), target -> {
            }));
            form.add(OatSubmitButton.onSubmit("saveAndAdd", "Save", target -> target.add(form), target -> target.add(form)));
            add(new AjaxLink<Void>("elsewhere") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body>"
                    + "<form wicket:id='form'><div wicket:id='name'></div>"
                    + "<button wicket:id='save'></button><button wicket:id='saveAndAdd'></button></form>"
                    + "<a wicket:id='elsewhere'>elsewhere</a>"
                    + "</body></html>");
        }
    }

    private void submit(String button, String name) {
        tester.newFormTester("form").setValue("name:input", name);
        tester.executeAjaxEvent("form:" + button, "click");
    }

    private String feedbackId(ErrorsPage page) {
        return page.name.get("feedback").getMarkupId();
    }

    @Test
    void anErrorAppearsWithoutTheHandlerAddingTheField() {
        ErrorsPage page = tester.startPage(new ErrorsPage());

        submit("save", "");

        tester.assertComponentOnAjaxResponse("form:name:feedback");
        String response = tester.getLastResponseAsString();
        assertThat(response).contains("&#039;Name&#039; is required.")
                // The input isn't re-rendered, so it keeps its focus and what the user typed
                .doesNotContain("<component id=\"" + page.name.getMarkupId() + "\"")
                .doesNotContain("<component id=\"" + page.name.getInput().getMarkupId() + "\"")
                .contains("[aria-describedby~=\"" + feedbackId(page) + "\"]")
                .contains("(document.getElementById('" + page.name.getMarkupId() + "'),true)");
        assertThat(page.name.getInput().getFeedbackMessages().first(FeedbackMessage.ERROR).isRendered()).isTrue();
    }

    @Test
    void aFixedErrorGoesAwayWithoutTheHandlerAddingTheField() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        submit("save", "");

        submit("save", "Ada");

        tester.assertComponentOnAjaxResponse("form:name:feedback");
        assertThat(tester.getLastResponseAsString()).doesNotContain("is required.")
                .contains("(document.getElementById('" + page.name.getMarkupId() + "'),false)");
    }

    @Test
    void theSameErrorAgainIsNotSentAgainButCountsAsShown() {
        ErrorsPage page = tester.startPage(new ErrorsPage());
        submit("save", "");
        tester.cleanupFeedbackMessages();

        submit("save", "");

        assertThat(tester.getLastResponseAsString()).doesNotContain("<component id=\"" + feedbackId(page) + "\"")
                .doesNotContain("aria-describedby~=");
        assertThat(page.name.getInput().getFeedbackMessages().first(FeedbackMessage.ERROR).isRendered()).isTrue();
    }

    @Test
    void aFieldWhoseFormIsAddedIsRenderedOnceWithIt() {
        ErrorsPage page = tester.startPage(new ErrorsPage());

        submit("saveAndAdd", "");

        tester.assertComponentOnAjaxResponse("form");
        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain("<component id=\"" + feedbackId(page) + "\"");
        assertThat(response.split("is required\\.", -1)).hasSize(2);
        assertThat(response).contains("data-field=\"error\"");
    }

    @Test
    void anotherAjaxRequestLeavesTheFieldAlone() {
        tester.startPage(new ErrorsPage());

        tester.clickLink("elsewhere");

        assertThat(tester.getLastResponseAsString()).doesNotContain("<component").doesNotContain("aria-describedby~=");
    }
}
