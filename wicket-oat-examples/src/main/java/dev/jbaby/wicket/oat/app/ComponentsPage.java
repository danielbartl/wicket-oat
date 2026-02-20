package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.ClientSideClickBehavior;
import dev.jbaby.wicket.oat.behaviors.TooltipBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

public class ComponentsPage extends BasePage {

    public ComponentsPage() {
        // Tooltip
        add(new WebMarkupContainer("tooltipButton")
                .add(new ButtonBehavior())
                .add(new TooltipBehavior("This is an Oat Tooltip!")));

        // Dialog trigger
        add(new WebMarkupContainer("openDialog")
                .add(new ClientSideClickBehavior("document.getElementById('my-dialog').showModal()")));

        // Dialog container
        WebMarkupContainer dialog = new WebMarkupContainer("dialog");
        add(dialog);
        dialog.add(new Label("dialogTitle", "Confirmation"));

        // Dialog close buttons
        dialog.add(new WebMarkupContainer("closeDialog1")
                .add(new ClientSideClickBehavior("this.closest('dialog').close()")));
        dialog.add(new WebMarkupContainer("closeDialog2")
                .add(new ClientSideClickBehavior("this.closest('dialog').close()")));

        // Tabs
        add(new WebMarkupContainer("tabs"));

        // Toasts
        add(new WebMarkupContainer("successToast")
                .add(new ButtonBehavior())
                .add(new ClientSideClickBehavior("ot.toast('Hello from Oat! This is a success message.', {variant: 'success'})")));

        add(new WebMarkupContainer("errorToast")
                .add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE))
                .add(new ClientSideClickBehavior("ot.toast('This is an error!', {variant: 'error'})")));

        // Typography Container
        add(new WebMarkupContainer("typography"));
    }
}
