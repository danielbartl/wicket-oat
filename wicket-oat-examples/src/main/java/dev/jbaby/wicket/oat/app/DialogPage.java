package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.OatToastBehavior;
import dev.jbaby.wicket.oat.components.OatDialog;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class DialogPage extends BasePage {

    public DialogPage() {
        OatDialog dialog = new OatDialog("dialog", Model.of("Open dialog"), Model.of("Delete item")) {
            @Override
            protected void onConfirm(AjaxRequestTarget target) {
                Oat.toast(target, "Item deleted.", OatToastBehavior.Variant.DANGER);
            }
        };
        dialog.setBody(new Label("body", "This action cannot be undone."));
        add(dialog);
    }
}
