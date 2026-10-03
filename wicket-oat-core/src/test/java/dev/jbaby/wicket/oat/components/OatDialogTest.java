package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class OatDialogTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    /** An edit dialog with a required field, opened from the server, optionally inside an outer form. */
    public static class DialogPage extends WebPage implements IMarkupResourceStreamProvider {

        public final Model<String> name = Model.of("Ada");
        public final OatTextField<String> nameField;
        public final OatDialog dialog;
        public int confirmed;
        public String rejectName;
        private final boolean nested;

        public DialogPage(boolean nested) {
            this.nested = nested;
            MarkupContainer parent = this;
            if (nested) {
                Form<Void> outer = new Form<>("outer");
                add(outer);
                parent = outer;
            }

            dialog = new OatDialog("dialog", Model.of("Edit")) {
                @Override
                protected void onConfirm(AjaxRequestTarget target) {
                    if (name.getObject().equals(rejectName)) {
                        nameField.getField().error("That name is taken.");
                        return;
                    }
                    confirmed++;
                }
            };
            nameField = new OatTextField<String>("name", "Name", name).setRequired(true);
            Fragment body = new Fragment(OatDialog.BODY_ID, "fields", this);
            body.add(nameField);
            dialog.setBody(body);
            parent.add(dialog);

            add(new AjaxLink<Void>("open") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.open(target);
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            String dialogTag = "<div wicket:id='dialog'></div>";
            return new StringResourceStream("<html><body>"
                    + (nested ? "<form wicket:id='outer'>" + dialogTag + "</form>" : dialogTag)
                    + "<a wicket:id='open'>Edit</a>"
                    + "<wicket:fragment wicket:id='fields'><div wicket:id='name'></div></wicket:fragment>"
                    + "</body></html>");
        }
    }

    @Test
    void theDialogsFormIsADirectChildOfTheDialogElement() {
        tester.startPage(new DialogPage(false));

        TagTester form = tester.getTagByWicketId("dialog").getChild("form");
        assertThat(form).isNotNull();
        assertThat(form.getAttribute("class")).isEqualTo("oat-dialog-form");
        // Without a trigger label there is no trigger button
        assertThat(tester.getLastResponseAsString()).doesNotContain("command=\"show-modal\"");
    }

    @Test
    void openReRendersTheContentAndShowsTheDialog() {
        DialogPage page = tester.startPage(new DialogPage(false));
        page.name.setObject("Grace");

        tester.clickLink("open");

        tester.assertComponentOnAjaxResponse("dialog:dialog:form");
        String response = tester.getLastResponseAsString();
        assertThat(response).contains("value=\"Grace\"");
        assertThat(response).contains(".showModal()");
    }

    @Test
    void confirmWithValidInputRunsOnConfirmAndCloses() {
        DialogPage page = tester.startPage(new DialogPage(false));

        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("body:name:container:field", "Grace");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        assertThat(page.confirmed).isEqualTo(1);
        assertThat(page.name.getObject()).isEqualTo("Grace");
        assertThat(tester.getLastResponseAsString()).contains(".close()");
    }

    @Test
    void confirmWithInvalidInputStaysOpenAndShowsTheErrors() {
        DialogPage page = tester.startPage(new DialogPage(false));

        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("body:name:container:field", "");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        assertThat(page.confirmed).isZero();
        tester.assertComponentOnAjaxResponse("dialog:dialog:form");
        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain(".close()");
        assertThat(response).contains("data-field=\"error\"").contains("&#039;Name&#039; is required.");
    }

    @Test
    void anErrorReportedInOnConfirmKeepsTheDialogOpen() {
        DialogPage page = tester.startPage(new DialogPage(false));
        page.rejectName = "Taken";

        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("body:name:container:field", "Taken");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain(".close()");
        assertThat(response).contains("That name is taken.");
    }

    @Test
    void openDiscardsInputLeftFromAFailedConfirm() {
        tester.startPage(new DialogPage(false));
        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("body:name:container:field", "");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");
        tester.cleanupFeedbackMessages();

        tester.clickLink("open");

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("value=\"Ada\"").doesNotContain("is required");
    }

    @Test
    void insideAnotherFormTheDialogsFormRendersAsANestedFormDiv() {
        DialogPage page = tester.startPage(new DialogPage(true));

        TagTester nested = tester.getTagByWicketId("form");
        assertThat(nested.getName()).isEqualTo("div");
        assertThat(nested.getAttribute("class")).isEqualTo("oat-dialog-form");

        // Confirming submits just the dialog's (nested) form
        FormTester formTester = tester.newFormTester("outer:dialog:dialog:form");
        formTester.setValue("body:name:container:field", "Grace");
        tester.executeAjaxEvent("outer:dialog:dialog:form:confirm", "click");
        assertThat(page.confirmed).isEqualTo(1);
        assertThat(page.name.getObject()).isEqualTo("Grace");
    }

    @Test
    void buttonLabelsDefaultFromResourcesAndCanBeSet() {
        OatDialog dialog = new OatDialog("dialog", "Delete item");
        tester.startComponentInPage(dialog);
        tester.assertLabel("dialog:dialog:form:cancel:cancelLabel", "Cancel");
        tester.assertLabel("dialog:dialog:form:confirm:confirmLabel", "Confirm");

        dialog = new OatDialog("dialog", "Delete item")
                .setCancelLabel(Model.of("Keep it"))
                .setConfirmLabel(Model.of("Delete"))
                .setConfirmVariant(OatVariant.DANGER);
        tester.startComponentInPage(dialog);
        tester.assertLabel("dialog:dialog:form:cancel:cancelLabel", "Keep it");
        tester.assertLabel("dialog:dialog:form:confirm:confirmLabel", "Delete");
        assertThat(tester.getTagByWicketId("confirm").getAttribute("data-variant")).isEqualTo("danger");
    }

    @Test
    void setBodyRejectsTheWrongId() {
        OatDialog dialog = new OatDialog("dialog", "Title");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> dialog.setBody(new Label("content", "Hi")))
                .withMessageContaining("OatDialog.BODY_ID");
    }
}
