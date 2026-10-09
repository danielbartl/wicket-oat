package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A modal dialog using the native &lt;dialog&gt; element, with a header, a body and
 * Cancel/Confirm buttons.
 * <ul>
 * <li>Open it from any Ajax handler with {@link #open}, which re-renders its content
 * first, and close it with {@link #close}. With a trigger label it also renders a
 * button that opens it natively, without a server round trip.</li>
 * <li>The body sits inside the dialog's own {@link #getForm() form}, so form fields put
 * in it are validated when Confirm is clicked. On a validation error the dialog stays
 * open and shows the errors; otherwise {@link #onConfirm} runs, and the dialog closes
 * unless an error was reported in it there.</li>
 * <li>Cancel, the Escape key and a click on the backdrop close it natively.</li>
 * </ul>
 *
 * <pre>
 * OatDialog dialog = new OatDialog("dialog", Model.of("Delete item")) {
 *     &#64;Override
 *     protected void onConfirm(AjaxRequestTarget target) { ... }
 * };
 * dialog.setBody(new Label(OatDialog.BODY_ID, "This action cannot be undone."));
 * add(dialog);
 * // elsewhere, e.g. in an AjaxLink: dialog.open(target);
 * </pre>
 *
 * The button labels come from the {@code OatDialog.cancel} and {@code OatDialog.confirm}
 * resource keys, so they can be translated or overridden in your {@code .properties}
 * files, or set per dialog with {@link #setCancelLabel} and {@link #setConfirmLabel}.
 */
public class OatDialog extends Panel {

    /** The component id a body passed to {@link #setBody(Component)} must use. */
    public static final String BODY_ID = "body";

    private final WebMarkupContainer dialog;
    private final Form<Void> form;
    private final Label headerLabel;
    private final Label cancelLabel;
    private final Label confirmLabel;
    private final ButtonBehavior confirmButton;
    private final AjaxButton confirm;

    /**
     * A dialog without a trigger button; open it with {@link #open}.
     */
    public OatDialog(String id, IModel<String> header) {
        this(id, null, header);
    }

    /**
     * A dialog without a trigger button; open it with {@link #open}.
     */
    public OatDialog(String id, String header) {
        this(id, Model.of(header));
    }

    public OatDialog(String id, String triggerLabel, String header) {
        this(id, Model.of(triggerLabel), Model.of(header));
    }

    /**
     * @param triggerLabel the label of a button that opens the dialog, or {@code null} for none
     */
    public OatDialog(String id, IModel<String> triggerLabel, IModel<String> header) {
        super(id);
        setRenderBodyOnly(true);

        dialog = new WebMarkupContainer("dialog");
        dialog.setOutputMarkupId(true);
        dialog.add(AttributeModifier.replace("closedby", "any"));
        add(dialog);

        WebMarkupContainer trigger = new WebMarkupContainer("trigger");
        trigger.add(new Label("triggerLabel", triggerLabel));
        trigger.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        trigger.setVisible(triggerLabel != null);
        add(trigger);

        // The dialog's own form, re-rendered as a whole by open() and after a failed confirm
        form = new Form<>("form");
        form.setOutputMarkupId(true);
        dialog.add(form);

        headerLabel = new Label("header", header);
        form.add(headerLabel);
        form.add(new WebMarkupContainer(BODY_ID));

        WebMarkupContainer cancel = new WebMarkupContainer("cancel");
        cancelLabel = new Label("cancelLabel", new StringResourceModel("OatDialog.cancel", this).setDefaultValue("Cancel"));
        cancel.add(cancelLabel);
        cancel.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        form.add(cancel);

        confirm = new AjaxButton("confirm") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                onConfirm(target);
                if (hasErrorMessages()) {
                    target.add(form);
                } else {
                    close(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(form);
            }
        };
        confirmLabel = new Label("confirmLabel", new StringResourceModel("OatDialog.confirm", this).setDefaultValue("Confirm"));
        confirm.add(confirmLabel);
        confirmButton = new ButtonBehavior();
        confirm.add(confirmButton);
        form.add(confirm);
        // Enter in a body field confirms, like clicking the button
        form.setDefaultButton(confirm);
    }

    /**
     * Called when Confirm is clicked and the body's fields are valid. The dialog closes
     * afterwards unless an error message was reported on it or a component in it, so
     * e.g. {@code nameField.error("Already taken")} keeps it open.
     */
    protected void onConfirm(AjaxRequestTarget target) {
    }

    /**
     * Opens the dialog, re-rendering its content first so it reflects the current models.
     * Input left over from an earlier failed confirm is discarded.
     */
    public OatDialog open(IPartialPageRequestHandler target) {
        form.clearInput();
        target.add(form);
        target.appendJavaScript("(function(d){if(d&&!d.open)d.showModal();})(document.getElementById('"
                + dialog.getMarkupId() + "'))");
        return this;
    }

    public OatDialog close(IPartialPageRequestHandler target) {
        target.appendJavaScript("(function(d){if(d&&d.open)d.close();})(document.getElementById('"
                + dialog.getMarkupId() + "'))");
        return this;
    }

    /**
     * Sets the dialog's body. Its id must be {@link #BODY_ID}. It can be replaced at any
     * time, e.g. right before {@link #open}.
     */
    public OatDialog setBody(Component body) {
        if (!BODY_ID.equals(body.getId())) {
            throw new IllegalArgumentException("The dialog body must use the id OatDialog.BODY_ID (\""
                    + BODY_ID + "\"), but was \"" + body.getId() + "\"");
        }
        form.addOrReplace(body);
        return this;
    }

    /** The dialog's title, e.g. to reuse one dialog for different records. */
    public OatDialog setHeader(IModel<String> header) {
        headerLabel.setDefaultModel(header);
        return this;
    }

    public OatDialog setCancelLabel(IModel<String> label) {
        cancelLabel.setDefaultModel(label);
        return this;
    }

    public OatDialog setConfirmLabel(IModel<String> label) {
        confirmLabel.setDefaultModel(label);
        return this;
    }

    /** E.g. {@code DANGER} for a destructive action. */
    public OatDialog setConfirmVariant(OatVariant variant) {
        confirmButton.setVariant(variant);
        return this;
    }

    /**
     * Whether the confirm button shows a spinner and ignores further clicks while
     * {@link #onConfirm} runs (off by default; see {@link AjaxBusyBehavior}), e.g. for a
     * slow save.
     */
    public OatDialog setBusyIndicator(boolean busyIndicator) {
        if (busyIndicator && confirm.getBehaviors(AjaxBusyBehavior.class).isEmpty()) {
            confirm.add(new AjaxBusyBehavior());
        } else if (!busyIndicator) {
            confirm.getBehaviors(AjaxBusyBehavior.class).forEach(confirm::remove);
        }
        return this;
    }

    /** The dialog's own form, which wraps its header, body and buttons. */
    public Form<Void> getForm() {
        return form;
    }

    private boolean hasErrorMessages() {
        return !new FeedbackCollector(this).collect(FeedbackMessage::isError).isEmpty();
    }
}
