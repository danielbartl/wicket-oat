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
        public int closed;
        public int outerSubmitted;
        public String rejectName;
        public boolean reopenOnConfirm;
        private final boolean nested;

        public DialogPage(boolean nested) {
            this.nested = nested;
            MarkupContainer parent = this;
            if (nested) {
                Form<Void> outer = new Form<Void>("outer") {
                    @Override
                    protected void onSubmit() {
                        outerSubmitted++;
                    }
                };
                add(outer);
                parent = outer;
            }

            dialog = new OatDialog("dialog", Model.of("Edit")) {
                @Override
                protected void onConfirm(AjaxRequestTarget target) {
                    if (name.getObject().equals(rejectName)) {
                        nameField.getInput().error("That name is taken.");
                        return;
                    }
                    confirmed++;
                    if (reopenOnConfirm) {
                        open(target); // e.g. for the next record
                    }
                }

                @Override
                protected void onClose(AjaxRequestTarget target) {
                    closed++;
                }
            };
            nameField = new OatTextField<String>("name", "Name", name).setRequired(true);
            Fragment body = new Fragment(OatDialog.CONTENT_ID, "fields", this);
            body.add(nameField);
            dialog.setContent(body);
            parent.add(dialog);

            add(new AjaxLink<Void>("open") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.open(target);
                }
            });
            add(new AjaxLink<Void>("openOnce") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.open(new Label(OatDialog.CONTENT_ID, "Just this once"), target);
                }
            });
            add(new AjaxLink<Void>("refresh") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    target.add(dialog);
                }
            });
            add(new AjaxLink<Void>("close") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.close(target);
                }
            });
            add(new AjaxLink<Void>("closeAndOpen") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.close(target);
                    dialog.open(target); // e.g. the next record
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            String dialogTag = "<div wicket:id='dialog'></div>";
            return new StringResourceStream("<html><body>"
                    + (nested ? "<form wicket:id='outer'>" + dialogTag + "</form>" : dialogTag)
                    + "<a wicket:id='open'>Edit</a><a wicket:id='openOnce'>Once</a>"
                    + "<a wicket:id='refresh'>Refresh</a><a wicket:id='close'>Close</a><a wicket:id='closeAndOpen'>Next</a>"
                    + "<wicket:fragment wicket:id='fields'><div wicket:id='name'></div></wicket:fragment>"
                    + "</body></html>");
        }
    }

    @Test
    void theDialogsFormIsADirectChildOfTheDialogElement() {
        tester.startPage(new DialogPage(false));

        TagTester form = TagTester.createTagByName(tester.getLastResponseAsString(), "dialog").getChild("form");
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

        assertThat(page.dialog.isOpen()).isTrue();
        tester.assertComponentOnAjaxResponse("dialog");
        String response = tester.getLastResponseAsString();
        assertThat(response).contains("value=\"Grace\"");
        assertThat(response).contains(".showModal()");
    }

    @Test
    void closedAndOpenedAgainInOneRequestItStaysOpen() {
        DialogPage page = tester.startPage(new DialogPage(false));
        tester.clickLink("open");

        tester.clickLink("closeAndOpen");

        assertThat(page.closed).isEqualTo(1);
        assertThat(page.dialog.isOpen()).isTrue();
        // The browser isn't told to close it after showing it again
        assertThat(tester.getLastResponseAsString()).contains(".showModal()").doesNotContain(".close()");
    }

    @Test
    void thePanelRendersItsOwnTagSoItCanBeReRendered() {
        tester.startPage(new DialogPage(false));

        TagTester panel = tester.getTagByWicketId("dialog");
        assertThat(panel.getName()).isEqualTo("div");
        assertThat(panel.getAttribute("class")).isEqualTo("oat-dialog");
        assertThat(panel.getAttribute("id")).isNotBlank();
    }

    @Test
    void reRenderingAnOpenDialogKeepsItOpen() {
        tester.startPage(new DialogPage(false));
        tester.clickLink("refresh");
        tester.assertComponentOnAjaxResponse("dialog");
        assertThat(tester.getLastResponseAsString()).doesNotContain(".showModal()");

        tester.clickLink("open");
        tester.clickLink("refresh");
        assertThat(tester.getLastResponseAsString()).contains(".showModal()");
    }

    @Test
    void closingInTheBrowserTellsTheServer() {
        DialogPage page = tester.startPage(new DialogPage(false));
        // Not for a close the server asked for, nor for a dialog replaced by a re-render,
        // which is checked on the element the event fired on, not on the one with its id now
        assertThat(tester.getLastResponseAsString()).contains("data-oat-closing")
                .contains("var d=attrs.event&&attrs.event.target;if(!d||!d.isConnected)");
        tester.clickLink("open");

        // Cancel, Escape or a click on the backdrop
        tester.executeAjaxEvent("dialog:dialog", "close");

        assertThat(page.dialog.isOpen()).isFalse();
        assertThat(page.closed).isEqualTo(1);
    }

    @Test
    void closeRunsOnCloseOnceAndOnlyWhenOpen() {
        DialogPage page = tester.startPage(new DialogPage(false));
        tester.clickLink("close");
        assertThat(page.closed).isZero();

        tester.clickLink("open");
        tester.clickLink("close");
        tester.clickLink("close");

        assertThat(page.closed).isEqualTo(1);
        assertThat(page.dialog.isOpen()).isFalse();
        // Marked, so the browser doesn't report this close back
        assertThat(tester.getLastResponseAsString()).contains("setAttribute('data-oat-closing','')").contains(".close()");
    }

    @Test
    void contentOpenedWithTheDialogIsRemovedWhenItCloses() {
        DialogPage page = tester.startPage(new DialogPage(false));
        tester.clickLink("openOnce");
        tester.assertLabel("dialog:dialog:form:content", "Just this once");

        tester.executeAjaxEvent("dialog:dialog", "close");

        assertThat(page.dialog.getForm().get(OatDialog.CONTENT_ID)).isNotInstanceOf(Label.class);
        tester.assertComponentOnAjaxResponse("dialog:dialog:form");
    }

    @Test
    void theTriggerTellsTheServerTheDialogIsOpen() {
        OatDialog dialog = new OatDialog("dialog", "Open", "Title");
        tester.startComponentInPage(dialog);
        assertThat(dialog.isOpen()).isFalse();

        tester.executeAjaxEvent("dialog:trigger", "click");

        assertThat(dialog.isOpen()).isTrue();
    }

    @Test
    void confirmWithValidInputRunsOnConfirmAndCloses() {
        DialogPage page = tester.startPage(new DialogPage(false));

        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("content:name:input", "Grace");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        assertThat(page.confirmed).isEqualTo(1);
        assertThat(page.closed).isEqualTo(1);
        assertThat(page.dialog.isOpen()).isFalse();
        assertThat(page.name.getObject()).isEqualTo("Grace");
        assertThat(tester.getLastResponseAsString()).contains(".close()");
    }

    @Test
    void openingItAgainInOnConfirmKeepsItOpen() {
        DialogPage page = tester.startPage(new DialogPage(false));
        page.reopenOnConfirm = true;
        tester.clickLink("open");

        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        assertThat(page.confirmed).isEqualTo(1);
        assertThat(page.closed).isZero();
        assertThat(page.dialog.isOpen()).isTrue();
        assertThat(tester.getLastResponseAsString()).contains(".showModal()").doesNotContain(".close()");
    }

    @Test
    void confirmWithInvalidInputStaysOpenAndShowsTheErrors() {
        DialogPage page = tester.startPage(new DialogPage(false));

        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("content:name:input", "");
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
        formTester.setValue("content:name:input", "Taken");
        tester.executeAjaxEvent("dialog:dialog:form:confirm", "click");

        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain(".close()");
        assertThat(response).contains("That name is taken.");
    }

    @Test
    void openDiscardsInputLeftFromAFailedConfirm() {
        tester.startPage(new DialogPage(false));
        FormTester formTester = tester.newFormTester("dialog:dialog:form");
        formTester.setValue("content:name:input", "");
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
        formTester.setValue("content:name:input", "Grace");
        tester.executeAjaxEvent("outer:dialog:dialog:form:confirm", "click");
        assertThat(page.confirmed).isEqualTo(1);
        assertThat(page.name.getObject()).isEqualTo("Grace");
    }

    @Test
    void insideAnotherFormConfirmIsNoSubmitButtonOfThatForm() {
        // Else Enter in a field of the page's form could click it, as its first submit button
        tester.startPage(new DialogPage(true));
        assertThat(tester.getTagByWicketId("confirm").getAttribute("type")).isEqualTo("button");
    }

    @Test
    void inItsOwnFormConfirmIsASubmitButton() {
        tester.startPage(new DialogPage(false));
        assertThat(tester.getTagByWicketId("confirm").getAttribute("type")).isEqualTo("submit");
    }

    /** A dialog on a tag that deployment mode leaves out, opened from the server. */
    public static class ContainerPage extends WebPage implements IMarkupResourceStreamProvider {
        public final OatDialog dialog = new OatDialog("dialog", "Title");

        public ContainerPage() {
            add(dialog);
            add(new AjaxLink<Void>("open") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialog.open(target);
                }
            });
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><wicket:container wicket:id='dialog'></wicket:container>"
                    + "<a wicket:id='open'>Open</a></body></html>");
        }
    }

    @Test
    void onAWicketContainerItOpensAlsoInDeploymentMode() {
        tester.getApplication().getMarkupSettings().setStripWicketTags(true);
        ContainerPage page = tester.startPage(ContainerPage.class);
        String dialogId = page.dialog.get("dialog").getMarkupId();
        assertThat(tester.getLastResponseAsString()).doesNotContain("wicket:container").contains("id=\"" + dialogId + "\"");

        tester.clickLink("open");

        // The <dialog> inside, by an id the page has
        tester.assertComponentOnAjaxResponse(page.dialog.get("dialog"));
        assertThat(tester.getLastResponseAsString()).contains(".showModal()");
    }

    @Test
    void submittingTheOuterFormLeavesTheClosedDialogsFieldsOut() {
        DialogPage page = tester.startPage(new DialogPage(true));

        // The dialog's required field is empty, but the dialog isn't what's submitted
        FormTester formTester = tester.newFormTester("outer");
        formTester.setValue("dialog:dialog:form:content:name:input", "");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(page.outerSubmitted).isEqualTo(1);
        assertThat(page.name.getObject()).isEqualTo("Ada");
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
    void setContentRejectsTheWrongId() {
        OatDialog dialog = new OatDialog("dialog", "Title");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> dialog.setContent(new Label("body", "Hi")))
                .withMessageContaining("OatDialog.CONTENT_ID");
    }
}
