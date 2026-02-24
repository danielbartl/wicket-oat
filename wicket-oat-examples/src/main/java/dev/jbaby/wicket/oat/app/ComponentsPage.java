package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.ClientSideClickBehavior;
import dev.jbaby.wicket.oat.behaviors.OatToastBehavior;
import dev.jbaby.wicket.oat.behaviors.TooltipBehavior;
import dev.jbaby.wicket.oat.components.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class ComponentsPage extends BasePage {

    public ComponentsPage() {
        // Tooltip
        add(new WebMarkupContainer("tooltipButton")
                .add(new ButtonBehavior())
                .add(new TooltipBehavior("This is an Oat Tooltip!")));

        // Toasts
        add(new AjaxLink<Void>("successToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                OatToastBehavior.toast(target, "Operation successful!", OatToastBehavior.Variant.SUCCESS, "Great!");
            }
        }.add(new ButtonBehavior()));

        add(new AjaxLink<Void>("errorToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                OatToastBehavior.toast(target, "Something went wrong.", OatToastBehavior.Variant.ERROR, "Error");
            }
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));

        // Typography Container
        add(new WebMarkupContainer("typography"));

        // Avatars
        add(new OatAvatar("avatar1", "JD"));
        add(new OatAvatar("avatar2", Model.of("https://i.pravatar.cc/150?u=1")));
        add(new OatAvatar("avatar3", Model.of("https://i.pravatar.cc/150?u=2"), Model.of("AS"), OatAvatar.Size.LARGE));
    }
}
