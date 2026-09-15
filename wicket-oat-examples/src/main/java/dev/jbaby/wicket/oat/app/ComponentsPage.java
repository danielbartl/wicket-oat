package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.OatToastBehavior;
import dev.jbaby.wicket.oat.components.OatAvatar;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

public class ComponentsPage extends BasePage {

    public ComponentsPage() {
        // Tooltip
        add(new WebMarkupContainer("tooltipButton")
                .add(Oat.Behaviors.button())
                .add(Oat.Behaviors.tooltip("This is an Oat Tooltip!")));

        // Toasts
        add(new AjaxLink<Void>("successToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Oat.toast(target, "Operation successful!", OatToastBehavior.Variant.SUCCESS, "Great!");
            }
        }.add(Oat.Behaviors.button()));

        add(new AjaxLink<Void>("errorToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Oat.toast(target, "Something went wrong.", OatToastBehavior.Variant.DANGER, "Error");
            }
        }.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE)));

        // Typography Container
        add(new WebMarkupContainer("typography"));

        // Avatars
        add(new OatAvatar("avatar1", "JD"));
        add(new OatAvatar("avatar2", Model.of("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150")));
        add(new OatAvatar("avatar3", Model.of("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&h=150"), Model.of("AS"), OatAvatar.Size.LARGE));
    }
}
