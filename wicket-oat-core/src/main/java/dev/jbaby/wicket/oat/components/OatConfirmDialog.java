package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Asks the user to confirm an action - typically a destructive one - before it runs:
 * "Delete invoice #12? This can't be undone." with Cancel and Confirm buttons.
 * <p>
 * Add one to a page and {@link #ask} it from any Ajax handler, passing the action to
 * run when the user confirms. The same dialog serves any number of actions, e.g. a
 * Delete button on every row of a table:
 * <pre>{@code
 * OatConfirmDialog confirm = new OatConfirmDialog("confirm");
 * add(confirm);
 * add(Oat.Components.button("delete", "Delete", target ->
 *         confirm.ask(target, "Delete invoice?", "This can't be undone.", t -> {
 *             invoices.delete(invoice);
 *             t.add(table);
 *         })));
 * }</pre>
 * The action is kept on the server until the user answers, so the browser can't run
 * anything but the action that was asked about, and only once. Cancel (or Escape)
 * closes the dialog without running it.
 * <p>
 * The confirm button is a danger button by default, since confirmation is mostly asked
 * for destructive actions; use {@link #setConfirmVariant} and {@link #setConfirmLabel}
 * (e.g. "Delete") to change it.
 */
public class OatConfirmDialog extends OatDialog {

    private final Label message;
    private SerializableConsumer<AjaxRequestTarget> action;

    public OatConfirmDialog(String id) {
        super(id, new Model<>());
        message = new Label(BODY_ID, new Model<String>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        setBody(message);
        setConfirmVariant(OatVariant.DANGER);
    }

    /**
     * Opens the dialog with the given question; {@code onConfirm} runs if the user
     * confirms. Replaces any question still open.
     *
     * @param header the question, e.g. "Delete invoice?"
     * @param message more detail, e.g. "This can't be undone.", or {@code null}
     */
    public OatConfirmDialog ask(IPartialPageRequestHandler target, String header, String message,
                                SerializableConsumer<AjaxRequestTarget> onConfirm) {
        return ask(target, Model.of(header), Model.of(message), onConfirm);
    }

    /**
     * Opens the dialog with the given question; {@code onConfirm} runs if the user
     * confirms. Replaces any question still open.
     *
     * @param header the question, e.g. "Delete invoice?"
     * @param message more detail, e.g. "This can't be undone.", or {@code null}
     */
    public OatConfirmDialog ask(IPartialPageRequestHandler target, IModel<String> header, IModel<String> message,
                                SerializableConsumer<AjaxRequestTarget> onConfirm) {
        this.action = onConfirm;
        setHeader(header);
        this.message.setDefaultModel(message != null ? message : new Model<String>());
        open(target);
        return this;
    }

    // Fluent setters typed to this class, so they chain with ask()

    @Override
    public OatConfirmDialog setHeader(IModel<String> header) {
        super.setHeader(header);
        return this;
    }

    @Override
    public OatConfirmDialog setCancelLabel(IModel<String> label) {
        super.setCancelLabel(label);
        return this;
    }

    @Override
    public OatConfirmDialog setConfirmLabel(IModel<String> label) {
        super.setConfirmLabel(label);
        return this;
    }

    @Override
    public OatConfirmDialog setConfirmVariant(OatVariant variant) {
        super.setConfirmVariant(variant);
        return this;
    }

    @Override
    public OatConfirmDialog setBusyIndicator(boolean busyIndicator) {
        super.setBusyIndicator(busyIndicator);
        return this;
    }

    /** Runs the action asked about, once; a stale or repeated confirm does nothing. */
    @Override
    protected void onConfirm(AjaxRequestTarget target) {
        SerializableConsumer<AjaxRequestTarget> pending = action;
        action = null;
        if (pending != null) {
            pending.accept(target);
        }
    }
}
