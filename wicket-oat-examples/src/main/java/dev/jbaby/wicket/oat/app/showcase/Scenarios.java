package dev.jbaby.wicket.oat.app.showcase;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.app.GettingStartedPage;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatAlert;
import dev.jbaby.wicket.oat.components.OatAvatar;
import dev.jbaby.wicket.oat.components.OatAvatarGroup;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.components.OatButton;
import dev.jbaby.wicket.oat.components.OatCard;
import dev.jbaby.wicket.oat.components.OatCookieConsent;
import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.components.OatIndicator;
import dev.jbaby.wicket.oat.components.OatSplitButton;
import dev.jbaby.wicket.oat.components.OatStatCard;
import dev.jbaby.wicket.oat.components.OatStatus;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

/**
 * The showcase's scenarios, in the groups of the website's component showcase. Each is
 * what a card there shows, built from the real components.
 */
public final class Scenarios {

    /** A group of scenarios under a heading. */
    public record Group(String name, List<Scenario> scenarios) implements Serializable {
    }

    // For buttons and menu entries whose action doesn't matter here
    private static final SerializableConsumer<AjaxRequestTarget> NOTHING = target -> {
    };

    public static final List<Group> ALL = List.of(
            new Group("General", general()));

    private Scenarios() {
    }

    /** The scenario with the given id, or {@code null}. */
    public static Scenario find(String id) {
        return ALL.stream().flatMap(group -> group.scenarios().stream())
                .filter(scenario -> scenario.id().equals(id)).findFirst().orElse(null);
    }

    private static List<Scenario> general() {
        return List.of(
                Scenario.of("button", "OatButton / OatSubmitButton", "OatButton.onClick(id, label, onClick)", """
                        <button wicket:id="primary"></button>
                        <button wicket:id="secondary"></button>
                        <button wicket:id="outline"></button>""", c -> c.add(
                        OatButton.onClick("primary", "Primary", NOTHING),
                        OatButton.onClick("secondary", "Secondary", NOTHING).setVariant(OatVariant.SECONDARY),
                        OatButton.onClick("outline", "Outline", NOTHING).setStyle(ButtonBehavior.Style.OUTLINE))),

                Scenario.of("badge", "OatBadge", "new OatBadge(id, label, OatVariant)", """
                        <span wicket:id="default"></span>
                        <span wicket:id="success"></span>
                        <span wicket:id="danger"></span>""", c -> c.add(
                        new OatBadge("default", "Default"),
                        new OatBadge("success", "Success", OatVariant.SUCCESS),
                        new OatBadge("danger", "Danger", OatVariant.DANGER).setOutline(true))),

                Scenario.of("avatar", "OatAvatar / OatAvatarGroup", "new OatAvatar(id, imageUrl)", """
                        <figure wicket:id="team" data-variant="avatar" role="group">
                            <div wicket:id="avatars"><div wicket:id="avatar"></div></div>
                        </figure>""", c -> c.add(
                        OatAvatarGroup.of("team", Model.ofList(List.of(
                                        new Person("Jane Doe", square("#f97316")),
                                        new Person("John Smith", square("#0ea5e9")),
                                        new Person("Oat User", ""))),
                                (item, person) -> item.add(new OatAvatar("avatar", Model.of(person.picture()),
                                        Model.of(OatAvatar.initialsOf(person.name())), OatAvatar.Size.DEFAULT))))),

                Scenario.of("alert", "OatAlert", "new OatAlert(id, message, OatVariant)", """
                        <div wicket:id="alert"></div>""", c -> c.add(
                        new OatAlert("alert", "Saved. Your changes were applied.", OatVariant.SUCCESS))).asBlock(),

                Scenario.of("card", "OatCard", "new OatCard(id)", """
                        <article wicket:id="card">
                            <header><h4>Plan</h4></header>
                            <p>Any HTML can live inside.</p>
                        </article>""", c -> c.add(new OatCard("card"))).asBlock(),

                Scenario.of("split-button", "OatSplitButton", "save.addAction(\"Save as draft\", target -> ...)", """
                        <div wicket:id="newInvoice"></div>""", c -> {
                    OatSplitButton newInvoice = new OatSplitButton("newInvoice", "New invoice", NOTHING);
                    newInvoice.addAction("New quote", NOTHING);
                    newInvoice.addAction("New credit note", NOTHING);
                    newInvoice.addAction("Archive customer", NOTHING).setVariant(OatVariant.DANGER);
                    c.add(newInvoice);
                }),

                Scenario.of("stat-card", "OatStatCard",
                        "new OatStatCard(id, \"Orders\", value).setChange(percent).setTrend(last12Months)", """
                        <article wicket:id="orders"></article>""", c -> c.add(
                        new OatStatCard("orders", "Orders", Model.of(6054))
                                .setChange(Model.of(9.1))
                                .setHint(Model.of("vs. last year"))
                                .setTrend(Model.ofList(List.of(4120, 3980, 4510, 4660, 4950, 4820, 5090, 4740, 5380, 5590, 5770, 6054))))).asBlock(),

                Scenario.of("icon", "OatIcon", "new OatIcon(id, \"pencil\") · .setLabel(\"Edit\") when it stands alone", """
                        <div class="hstack gap-4 showcase-icons"><svg wicket:id="icons"></svg></div>""", c -> {
                    RepeatingView icons = new RepeatingView("icons");
                    for (String name : List.of("pencil", "trash-2", "mail", "search", "download", "settings", "users", "chart-line")) {
                        icons.add(new OatIcon(icons.newChildId(), name));
                    }
                    c.add(icons);
                }).asBlock(),

                Scenario.of("cookie-consent", "OatCookieConsent",
                        "new OatCookieConsent(id).setPolicyLink(PrivacyPage.class) · OatCookieConsent.isAccepted()", """
                        <div wicket:id="cookies"></div>""", c -> c.add(
                        new OatCookieConsent("cookies").setPolicyLink(GettingStartedPage.class))).asBlock(),

                Scenario.of("indicator", "OatIndicator / OatStatus",
                        "new OatIndicator(id, () -> unread) · OatIndicator.status(id, variant, label) · new OatStatus(...)", """
                        <div class="hstack gap-6 items-center justify-center">
                            <span wicket:id="inbox"><button wicket:id="inboxButton" type="button">Inbox</button></span>
                            <span wicket:id="presence"><div wicket:id="avatar"></div></span>
                        </div>
                        <ul class="unstyled vstack gap-1 mt-4 showcase-statuses">
                            <li><span wicket:id="up"></span> API online</li>
                            <li><span wicket:id="slow"></span> Search slow</li>
                            <li><span wicket:id="down"></span> Mail down</li>
                        </ul>""", c -> c.add(
                        new OatIndicator("inbox", Model.of(3)).setVariant(OatVariant.DANGER)
                                .add(new WebMarkupContainer("inboxButton").add(Oat.Behaviors.button().setStyle(ButtonBehavior.Style.OUTLINE))),
                        OatIndicator.status("presence", Model.of(OatVariant.SUCCESS), Model.of("Online"))
                                .add(new OatAvatar("avatar", "MN")),
                        new OatStatus("up", OatVariant.SUCCESS, null),
                        new OatStatus("slow", OatVariant.WARNING, null),
                        new OatStatus("down", OatVariant.DANGER, null))).asBlock(),

                Scenario.of("keys-divider", "Keys and divider",
                        "<kbd>Ctrl</kbd> · <div class=\"oat-divider\">or</div>", """
                        <p>Press <kbd>Ctrl</kbd> + <kbd>K</kbd> to search, or <kbd><kbd>Shift</kbd>+<kbd>?</kbd></kbd> for all shortcuts.</p>
                        <div class="vstack gap-2">
                            <button type="button" class="outline">Continue with your company account</button>
                            <div class="oat-divider">or</div>
                            <button type="button">Sign in with email</button>
                        </div>""").asBlock());
    }

    private record Person(String name, String picture) implements Serializable {
    }

    // A square in one color, as a stand-in for a photo
    private static String square(String color) {
        return "data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='64'%20height='64'%3E"
                + "%3Crect%20width='64'%20height='64'%20fill='" + color.replace("#", "%23") + "'/%3E%3C/svg%3E";
    }
}
