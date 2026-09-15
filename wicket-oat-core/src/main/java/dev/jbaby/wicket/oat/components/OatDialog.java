package dev.jbaby.wicket.oat.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A self-contained modal dialog using the native &lt;dialog&gt; element with
 * command/commandfor attributes. Opening and cancelling are pure native browser
 * behavior (no JavaScript beyond what Oat already vendors); the confirm button
 * additionally fires an Ajax callback.
 */
public class OatDialog extends Panel {

    private final WebMarkupContainer dialog;
    private final WebMarkupContainer body;

    public OatDialog(String id, IModel<String> triggerLabel, IModel<String> headerModel) {
        super(id);
        setRenderBodyOnly(true);

        dialog = new WebMarkupContainer("dialog");
        dialog.setOutputMarkupId(true);
        dialog.add(AttributeModifier.replace("closedby", "any"));
        add(dialog);

        WebMarkupContainer trigger = new WebMarkupContainer("trigger");
        trigger.add(new Label("triggerLabel", triggerLabel));
        trigger.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        add(trigger);

        dialog.add(new Label("header", headerModel));

        body = new WebMarkupContainer("body");
        dialog.add(body);

        WebMarkupContainer cancel = new WebMarkupContainer("cancel");
        cancel.add(new Label("cancelLabel", Model.of("Cancel")));
        cancel.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        dialog.add(cancel);

        AjaxLink<Void> confirm = new AjaxLink<>("confirm") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                OatDialog.this.onConfirm(target);
            }
        };
        confirm.add(new Label("confirmLabel", Model.of("Confirm")));
        confirm.add(AttributeModifier.replace("commandfor", (IModel<String>) dialog::getMarkupId));
        confirm.add(AttributeModifier.replace("command", "close"));
        dialog.add(confirm);
    }

    /**
     * Override to handle the confirm button being clicked. The dialog closes
     * natively regardless (via {@code command="close"}), independent of this hook.
     */
    protected void onConfirm(AjaxRequestTarget target) {
    }

    /**
     * Replace the placeholder body container with custom content. The replacement's
     * wicket:id must be "body". Call once, before the dialog is first rendered.
     */
    public OatDialog setBody(Component newBody) {
        body.replaceWith(newBody);
        return this;
    }
}
