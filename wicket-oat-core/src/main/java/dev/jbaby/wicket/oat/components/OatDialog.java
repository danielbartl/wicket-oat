package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A modal dialog using the native &lt;dialog&gt; element, with a header, content and
 * Cancel/Confirm buttons. It works like Wicket's own {@code ModalDialog}:
 * <ul>
 * <li>Set its content with {@link #setContent}, using the id {@link #CONTENT_ID}, open it
 * from any Ajax handler with {@link #open}, which re-renders it first, and close it with
 * {@link #close}. {@link #open(Component, AjaxRequestTarget)} sets content that is
 * removed again when the dialog closes. With a trigger label it also renders a button
 * that opens it in the browser, without waiting for the server.</li>
 * <li>The server knows whether it is open ({@link #isOpen}), so re-rendering it - with
 * {@code target.add(dialog)}, or a container around it - keeps it open.</li>
 * <li>The content sits inside the dialog's own {@link #getForm() form}, so form fields in
 * it are validated when Confirm is clicked. On a validation error the dialog stays open
 * and shows the errors; otherwise {@link #onConfirm} runs, and the dialog closes unless
 * an error was reported in it there.</li>
 * <li>Cancel, the Escape key and a click on the backdrop close it in the browser, which
 * tells the server. {@link #onClose} runs whenever it closes, however that happened.</li>
 * <li>It can sit inside a form of the page: submitting that form leaves the dialog's
 * fields out, so they are neither validated nor stored.</li>
 * <li>It needs a tag of its own, e.g. {@code <div wicket:id="dialog"></div>}, not
 * {@code <wicket:container>}, which Wicket leaves out in deployment mode.</li>
 * </ul>
 *
 * <pre>
 * OatDialog dialog = new OatDialog("dialog", Model.of("Delete item")) {
 *     &#64;Override
 *     protected void onConfirm(AjaxRequestTarget target) { ... }
 * };
 * dialog.setContent(new Label(OatDialog.CONTENT_ID, "This action cannot be undone."));
 * add(dialog);
 * // elsewhere, e.g. in an AjaxLink: dialog.open(target);
 * </pre>
 *
 * The button labels come from the {@code OatDialog.cancel} and {@code OatDialog.confirm}
 * resource keys, so they can be translated or overridden in your {@code .properties}
 * files, or set per dialog with {@link #setCancelLabel} and {@link #setConfirmLabel}.
 */
public class OatDialog extends Panel {

    /** The component id the content passed to {@link #setContent(Component)} must use. */
    public static final String CONTENT_ID = "content";

    /** Marks a close the server asked for, so the browser doesn't report it back. */
    private static final String CLOSING = "data-oat-closing";

    private final WebMarkupContainer dialog;
    private final Form<Void> form;
    private final Label headerLabel;
    private final Label cancelLabel;
    private final Label confirmLabel;
    private final ButtonBehavior confirmButton;
    private final AjaxButton confirm;
    private boolean open;
    /** Set by {@link #open}, so a dialog reopened in {@link #onConfirm} isn't closed after it. */
    private boolean reopened;
    private boolean removeContentOnClose;

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
        // Its own tag, so target.add(dialog) re-renders it; wicket-oat.css gives the tag no
        // box of its own, so the trigger lays out as if it stood there by itself
        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "oat-dialog"));

        dialog = new WebMarkupContainer("dialog");
        dialog.setOutputMarkupId(true);
        dialog.add(AttributeModifier.replace("closedby", "any"));
        dialog.add(new ClosedInBrowser());
        add(dialog);

        WebMarkupContainer trigger = new WebMarkupContainer("trigger");
        trigger.add(new Label("triggerLabel", triggerLabel));
        trigger.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        // The browser opens the dialog at once; this tells the server it is open
        trigger.add(new AjaxEventBehavior("click") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                open = true;
            }
        });
        trigger.setVisible(triggerLabel != null);
        add(trigger);

        // The dialog's own form, re-rendered after a failed confirm.
        // Inside a page's form it is nested; submitting that form leaves it out, so the
        // fields of a closed dialog can't block the page's submit.
        form = new Form<Void>("form") {
            @Override
            protected boolean wantSubmitOnParentFormSubmit() {
                return false;
            }
        };
        form.setOutputMarkupId(true);
        dialog.add(form);

        headerLabel = new Label("header", header);
        form.add(headerLabel);
        form.add(new WebMarkupContainer(CONTENT_ID));

        WebMarkupContainer cancel = new WebMarkupContainer("cancel");
        cancelLabel = new Label("cancelLabel", new StringResourceModel("OatDialog.cancel", this).setDefaultValue("Cancel"));
        cancel.add(cancelLabel);
        cancel.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        form.add(cancel);

        confirm = new AjaxButton("confirm") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                open = true; // it is, since its button was clicked
                reopened = false;
                onConfirm(target);
                if (hasErrorMessages()) {
                    target.add(form);
                } else if (!reopened) {
                    close(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                open = true;
                target.add(form);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                // Inside a page's form the dialog's form is a <div>, so a submit button here
                // would be the page form's: Enter in a page field could click this hidden one
                if (!form.isRootForm()) {
                    tag.put("type", "button");
                }
            }
        };
        confirmLabel = new Label("confirmLabel", new StringResourceModel("OatDialog.confirm", this).setDefaultValue("Confirm"));
        confirm.add(confirmLabel);
        confirmButton = new ButtonBehavior();
        confirm.add(confirmButton);
        form.add(confirm);
        // Enter in a field of the content confirms, like clicking the button
        form.setDefaultButton(confirm);
    }

    /**
     * Called when Confirm is clicked and the content's fields are valid. The dialog closes
     * afterwards unless an error message was reported on it or a component in it, so
     * e.g. {@code nameField.error("Already taken")} keeps it open, or unless it was opened
     * again here, e.g. with the next record's content.
     */
    protected void onConfirm(AjaxRequestTarget target) {
    }

    /**
     * Called once whenever the dialog closes: by Cancel, the Escape key or a click on the
     * backdrop in the browser, after a successful confirm, or by {@link #close}. E.g. to
     * forget the record it was editing.
     */
    protected void onClose(AjaxRequestTarget target) {
    }

    /**
     * Opens the dialog, re-rendering it first so it reflects the current models. Input
     * left over from an earlier failed confirm is discarded.
     */
    public OatDialog open(AjaxRequestTarget target) {
        form.clearInput();
        open = true;
        reopened = true;
        target.add(this);
        return this;
    }

    /**
     * Opens the dialog with the given content, which is removed again when it closes, e.g.
     * the fields for the record a row's Edit button was clicked for.
     *
     * @param content the content; its id must be {@link #CONTENT_ID}
     */
    public OatDialog open(Component content, AjaxRequestTarget target) {
        setContent(content);
        removeContentOnClose = true;
        return open(target);
    }

    /** Closes the dialog, if it is open, and runs {@link #onClose}. */
    public OatDialog close(AjaxRequestTarget target) {
        target.appendJavaScript("(function(d){if(d&&d.open){d.setAttribute('" + CLOSING + "','');d.close();}})"
                + "(document.getElementById('" + dialog.getMarkupId() + "'))");
        closed(target);
        return this;
    }

    /** Whether the dialog is open. */
    public boolean isOpen() {
        return open;
    }

    /** Notes that the dialog closed, removes content set to go with it, and runs onClose once. */
    private void closed(AjaxRequestTarget target) {
        if (!open) {
            return;
        }
        open = false;
        if (removeContentOnClose) {
            removeContentOnClose = false;
            form.replace(new WebMarkupContainer(CONTENT_ID));
            // So the browser can't show or submit the old content's inputs anymore
            target.add(form);
        }
        onClose(target);
    }

    @Override
    protected void onBeforeRender() {
        // Ajax re-renders the dialog by its own tag's id, so that tag must reach the browser
        if (getMarkupTag() instanceof WicketTag) {
            throw new MarkupException("OatDialog '" + getId() + "' is on a <wicket:" + getMarkupTag().getName()
                    + ">, which Wicket leaves out in deployment mode, so the dialog couldn't be opened there."
                    + " Put it on an element, e.g. <div wicket:id=\"" + getId() + "\"></div>; it takes no room in the layout.");
        }
        super.onBeforeRender();
    }

    /** Shows the dialog again when it is rendered while open, e.g. by {@code target.add(dialog)}. */
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (open) {
            response.render(OnDomReadyHeaderItem.forScript("(function(d){if(d&&!d.open)d.showModal();})"
                    + "(document.getElementById('" + dialog.getMarkupId() + "'))"));
        }
    }

    /**
     * Sets the dialog's content. Its id must be {@link #CONTENT_ID}. It can be replaced at
     * any time, e.g. right before {@link #open}.
     */
    public OatDialog setContent(Component content) {
        if (!CONTENT_ID.equals(content.getId())) {
            throw new IllegalArgumentException("The dialog's content must use the id OatDialog.CONTENT_ID (\""
                    + CONTENT_ID + "\"), but was \"" + content.getId() + "\"");
        }
        form.addOrReplace(content);
        removeContentOnClose = false;
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

    /** The dialog's own form, which wraps its header, content and buttons. */
    public Form<Void> getForm() {
        return form;
    }

    private boolean hasErrorMessages() {
        return !new FeedbackCollector(this).collect(FeedbackMessage::isError).isEmpty();
    }

    /**
     * Tells the server when the dialog closed in the browser: by Cancel, the Escape key or
     * a click on the backdrop. Not for a close the server asked for, which it knows about,
     * nor for a dialog element replaced by a re-render.
     */
    private final class ClosedInBrowser extends AjaxEventBehavior {

        ClosedInBrowser() {
            super("close");
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            // The element the event fired on: looked up by id, it would be the new one
            attributes.getAjaxCallListeners().add(new AjaxCallListener().onPrecondition(
                    "var d=attrs.event&&attrs.event.target;if(!d||!d.isConnected){return false;}"
                            + "if(d.hasAttribute('" + CLOSING + "')){d.removeAttribute('" + CLOSING + "');return false;}"
                            + "return true;"));
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            closed(target);
        }
    }
}
