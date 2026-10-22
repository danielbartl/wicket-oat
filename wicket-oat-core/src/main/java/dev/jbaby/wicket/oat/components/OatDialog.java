package dev.jbaby.wicket.oat.components;

import com.github.openjson.JSONObject;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.behaviors.AjaxBusyBehavior;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.NestedFormButtonBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.time.Duration;
import java.util.Map;

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
 * tells the server. Once the dialog is out of sight, its fields show the models again,
 * without what was changed but not confirmed. {@link #onClose} runs whenever it closes,
 * however that happened.</li>
 * <li>Its content scrolls between the header and the buttons, which stay in view.</li>
 * <li>It can sit inside a form of the page: submitting that form leaves the dialog's
 * fields out, so they are neither validated nor stored.</li>
 * <li>Give it a tag of its own, e.g. {@code <div wicket:id="dialog"></div>}, which takes
 * no room in the layout, so {@code target.add(dialog)} can re-render it. On a
 * {@code <wicket:container>}, which Wicket leaves out in deployment mode, open and
 * close it only with {@link #open} and {@link #close}.</li>
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

    /** How long the browser takes to close a dialog (Oat's transition), before telling the server. */
    private static final Duration CLOSE_TRANSITION = Duration.ofMillis(300);

    private final WebMarkupContainer dialog;
    private final Form<Void> form;
    private final Label headerLabel;
    private final WebMarkupContainer closeButton;
    private final WebMarkupContainer footer;
    private final WebMarkupContainer cancel;
    private final Label cancelLabel;
    private final Label confirmLabel;
    private final ButtonBehavior confirmButton;
    private final AjaxButton confirm;
    private boolean open;
    /** Set by {@link #open}, so a dialog reopened in {@link #onConfirm} isn't closed after it. */
    private boolean reopened;
    private boolean removeContentOnClose;
    /** Set by {@link #open} on a closed dialog, which then slides or fades in; one open already shows at once. */
    private boolean appearing;
    /** The browser still shows content removed on a close the server asked for, until the form renders again. */
    private boolean staleContent;

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

    /**
     * @param triggerLabel the label of a button that opens the dialog, or {@code null} for none
     */
    public OatDialog(String id, String triggerLabel, String header) {
        this(id, triggerLabel != null ? Model.of(triggerLabel) : null, Model.of(header));
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

        dialog = new WebMarkupContainer("dialog") {
            /** Shows the dialog again when it is rendered while open, by {@link #open} or a re-render around it. */
            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                if (open) {
                    response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS));
                    response.render(OnDomReadyHeaderItem.forScript("WicketOat.showDialog(" + JSONObject.quote(getMarkupId()) + ")"));
                }
            }

            @Override
            protected void onAfterRender() {
                super.onAfterRender();
                appearing = false;
            }
        };
        dialog.setOutputMarkupId(true);
        dialog.add(AttributeModifier.replace("closedby", "any"));
        // Rendered again while open, it shows at once rather than sliding or fading in again
        dialog.add(AttributeModifier.replace("data-oat-instant", (IModel<String>) () -> open && !appearing ? "" : null));
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
                if (staleContent) {
                    target.add(form);
                }
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

            @Override
            protected void onAfterRender() {
                super.onAfterRender();
                staleContent = false;
            }
        };
        form.setOutputMarkupId(true);
        dialog.add(form);

        headerLabel = new Label("header", header);
        form.add(headerLabel);
        // An "×" in the corner, closing it in the browser like Cancel. First in the markup, as it
        // is shown; wicket-oat.js focuses the first field after it on opening.
        closeButton = new WebMarkupContainer("close");
        closeButton.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        closeButton.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatDialog.close", this).setDefaultValue("Close")));
        closeButton.add(new OatIcon("icon", "x"));
        closeButton.setVisible(false);
        form.add(closeButton);
        form.add(new WebMarkupContainer(CONTENT_ID));

        // Transparent, so the buttons keep their paths ("form:confirm"); hidden with them
        footer = new TransparentWebMarkupContainer("footer");
        form.add(footer);

        cancel = new WebMarkupContainer("cancel");
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
        };
        // Inside a page's form, so Enter in a page field can't click this hidden button
        confirm.add(new NestedFormButtonBehavior());
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
        appearing = !open;
        open = true;
        reopened = true;
        // The panel; on a <wicket:container>, which deployment mode leaves out, the <dialog>
        // inside, which has an id of its own
        target.add(getMarkupTag() instanceof WicketTag ? dialog : this);
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

    /**
     * Closes the dialog, if it is open, and runs {@link #onClose}. Opened again in the same
     * request, e.g. by {@code onClose}, it stays open.
     */
    public OatDialog close(AjaxRequestTarget target) {
        // Closed in the browser at the end of the request: the script runs after the
        // components are re-rendered, so it would also close a dialog opened again since
        target.addListener(new AjaxRequestTarget.IListener() {
            @Override
            public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget respondingTarget) {
                if (!open) {
                    respondingTarget.appendJavaScript("(function(d){if(d&&d.open){d.setAttribute('" + CLOSING + "','');d.close();}})"
                            + "(document.getElementById('" + dialog.getMarkupId() + "'))");
                }
            }
        });
        closed(target, false);
        return this;
    }

    /** Whether the dialog is open. */
    public boolean isOpen() {
        return open;
    }

    /** Notes that the dialog closed, removes content set to go with it, and runs onClose once. */
    private void closed(AjaxRequestTarget target, boolean inBrowser) {
        if (!open) {
            return;
        }
        open = false;
        if (removeContentOnClose) {
            removeContentOnClose = false;
            form.replace(new WebMarkupContainer(CONTENT_ID));
            // Not rendered yet, so the dialog doesn't close empty; opening it renders the form
            staleContent = true;
        }
        if (inBrowser) {
            // Out of sight by now (ClosedInBrowser waits): the fields show the models again,
            // without input that wasn't confirmed or the errors of a failed confirm
            form.clearInput();
            target.add(form);
        }
        onClose(target);
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

    /** Whether an "×" in the header's corner closes the dialog, like Cancel; off by default. */
    public OatDialog setCloseButton(boolean shown) {
        closeButton.setVisible(shown);
        return this;
    }

    /**
     * Whether the footer with the Cancel and Confirm buttons shows; on by default. Without
     * them, e.g. for a dialog that only shows something, give it the
     * {@link #setCloseButton "×"}; the Escape key and a click on the backdrop close it too,
     * and Enter in a field submits nothing.
     */
    public OatDialog setFooterVisible(boolean visible) {
        footer.setVisible(visible);
        cancel.setVisible(visible);
        confirm.setVisible(visible);
        return this;
    }

    /** The {@code <dialog>} element, e.g. for a subclass to style it. */
    protected final WebMarkupContainer getDialogElement() {
        return dialog;
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

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // Focuses the first field on opening, and Enter in one does nothing without a footer
        response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS));
    }

    private boolean hasErrorMessages() {
        return !new FeedbackCollector(this).collect(FeedbackMessage::isError).isEmpty();
    }

    /**
     * Tells the server when the dialog closed in the browser: by Cancel, the Escape key or
     * a click on the backdrop. Not for a close the server asked for, which it knows about,
     * nor for a dialog element replaced by a re-render. Once the dialog is out of sight, so
     * re-rendering its form doesn't show while it slides or fades out; not at all if it was
     * opened again by then.
     */
    private final class ClosedInBrowser extends AjaxEventBehavior {

        ClosedInBrowser() {
            super("close");
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            attributes.setThrottlingSettings(new ThrottlingSettings(CLOSE_TRANSITION));
            // The element the event fired on: looked up by id, it would be the new one
            attributes.getAjaxCallListeners().add(new AjaxCallListener().onPrecondition(
                    "var d=attrs.event&&attrs.event.target;if(!d||!d.isConnected){return false;}"
                            + "if(d.hasAttribute('" + CLOSING + "')){d.removeAttribute('" + CLOSING + "');return false;}"
                            + "return !d.open;"));
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            closed(target, true);
        }
    }
}
