package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatSkeleton;
import dev.jbaby.wicket.oat.components.OatSpinner;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.behaviors.SkeletonBehavior;
import dev.jbaby.wicket.oat.components.OatAvatar;
import dev.jbaby.wicket.oat.components.OatAvatarGroup;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.components.OatIndicator;
import dev.jbaby.wicket.oat.components.OatList;
import dev.jbaby.wicket.oat.components.OatStatus;
import dev.jbaby.wicket.oat.components.OatSteps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

public class ComponentsPage extends BasePage {

    /** A person in the list demo. */
    record Member(String name, String initials, String role, boolean online) implements Serializable {
    }

    private static final List<Member> TEAM = List.of(
            new Member("Mira Novak", "MN", "Product owner", true),
            new Member("Jonas Berger", "JB", "Developer", false),
            new Member("Lea Hofer", "LH", "Designer", true));

    private int stage = 2;

    public ComponentsPage() {
        // Tooltip
        add(new WebMarkupContainer("tooltipButton")
                .add(Oat.Behaviors.button())
                .add(Oat.Behaviors.tooltip("This is an Oat Tooltip!")));

        // Toasts
        add(new AjaxLink<Void>("successToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Oat.toast(target, "Operation successful!", OatVariant.SUCCESS, "Great!");
            }
        }.add(Oat.Behaviors.button()));

        add(new AjaxLink<Void>("errorToast") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Oat.toast(target, "Something went wrong.", OatVariant.DANGER, "Error");
            }
        }.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE)));

        // Typography Container
        add(new WebMarkupContainer("typography"));

        // Avatars
        add(new OatAvatar("avatar1", "JD"));
        add(new OatAvatar("avatar2", Model.of("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150")));
        add(new OatAvatar("avatar3", Model.of("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&h=150"), Model.of("AS"), OatAvatar.Size.LARGE));

        // Avatar Group
        add(new OatAvatarGroup<String>("avatarGroup", Model.ofList(List.of("JD", "AS", "KP"))) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new OatAvatar("avatar", item.getModelObject()));
            }
        });

        // Spinner and Skeleton
        add(new OatSpinner("spinnerComponent"));
        add(new OatSkeleton("skeletonComponent", SkeletonBehavior.Shape.BOX));

        // Steps: where an order stands; "Next" moves it on
        OatSteps steps = new OatSteps("orderSteps", List.of("Ordered", "Paid", "Shipped", "Delivered"), () -> stage);
        steps.setLabel(Model.of("Order status"));
        add(steps);
        add(new AjaxLink<Void>("nextStage") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                stage = (stage + 1) % 5; // past "Delivered" all are done, then it starts over
                target.add(steps);
            }
        }.add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE).setSize(ButtonBehavior.Size.SMALL)));

        // Indicator and Status
        add(new OatIndicator("inbox", Model.of(3)).setVariant(OatVariant.DANGER)
                .add(new WebMarkupContainer("inboxButton").add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE))));
        add(new OatIndicator("reports", Model.of("New"))
                .add(new WebMarkupContainer("reportsButton").add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE))));
        add(OatIndicator.status("presence", Model.of(OatVariant.SUCCESS), Model.of("Online"))
                .add(new OatAvatar("presenceAvatar", "MN")));
        add(new OatStatus("statusUp", OatVariant.SUCCESS, null));
        add(new OatStatus("statusSlow", OatVariant.WARNING, null));
        add(new OatStatus("statusDown", OatVariant.DANGER, null));

        // List
        add(new OatList<>("team", Model.ofList(TEAM), Member::name)
                .setSubtitle(Member::role)
                // A fragment, as the indicator wraps markup of its own: the avatar's tag
                .setLeading((id, member) -> new Fragment(id, "memberAvatar", this)
                        .add(OatIndicator.status("presence",
                                        Model.of(member.getObject().online() ? OatVariant.SUCCESS : OatVariant.SECONDARY),
                                        Model.of(member.getObject().online() ? "Online" : "Away"))
                                .add(new OatAvatar("avatar", member.getObject().initials()))))
                .setTrailing((id, member) -> new OatBadge(id, member.getObject().online() ? "In" : "Out",
                        member.getObject().online() ? OatVariant.SUCCESS : OatVariant.SECONDARY))
                .setOnClick((target, member) -> Oat.toast(target, member.role(), OatVariant.DEFAULT, member.name())));
    }
}
