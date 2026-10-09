package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ContextMenuBehavior;
import dev.jbaby.wicket.oat.components.OatCookieConsent;
import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.components.OatLoadMoreList;
import dev.jbaby.wicket.oat.components.OatMenuBar;
import jakarta.servlet.http.Cookie;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A mail inbox built from Tier B components: an OatMenuBar of commands, an
 * OatSplitLayout (drag its corner to resize), an OatLoadMoreList that loads more mail
 * as you scroll, a context menu on every message (right-click or Shift+F10), OatIcons,
 * and an OatCookieConsent banner.
 */
public class InboxPage extends BasePage {

    public static final class Mail implements Serializable {
        final int id;
        final String from;
        final String subject;
        final String body;
        boolean read;

        Mail(int id, String from, String subject, String body, boolean read) {
            this.id = id;
            this.from = from;
            this.subject = subject;
            this.body = body;
            this.read = read;
        }
    }

    private static final String[] SENDERS = {"Jordan Lee", "Sam Rivera", "Alex Kim", "Taylor Morgan", "Robin Chen"};
    private static final String[] SUBJECTS = {"Invoice INV-%d is overdue", "Order #%d has shipped", "Meeting notes, week %d",
            "Quote Q-%d approved", "New support ticket #%d"};

    private final List<Mail> mail = new ArrayList<>(IntStream.rangeClosed(1, 64)
            .mapToObj(i -> new Mail(i, SENDERS[i % SENDERS.length], SUBJECTS[i % SUBJECTS.length].formatted(1000 + i),
                    "Hello,\n\nthis is message " + i + " of the demo inbox.\n\nBest regards,\n" + SENDERS[i % SENDERS.length],
                    i > 5))
            .toList());

    private final IModel<Mail> selected = new Model<>();
    private final OatLoadMoreList<Mail> list;
    private final WebMarkupContainer detail;

    public InboxPage() {
        add(Oat.Behaviors.feedbackToasts());

        list = Oat.Components.loadMoreList("first", new ListDataProvider<>(mail), 20, this::row).setLoadOnScroll(true);
        detail = new Fragment("second", "detailFragment", this);
        detail.setOutputMarkupId(true);
        detail.add(new WebMarkupContainer("message") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(selected.getObject() != null);
            }
        }.add(new Label("subject", () -> selected.getObject().subject),
                new Label("from", () -> selected.getObject().from),
                new Label("body", () -> selected.getObject().body)));
        detail.add(new WebMarkupContainer("empty") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(selected.getObject() == null);
            }
        });
        OatMenuBar commands = Oat.Components.menuBar("commands")
                .addAction("New message", target -> success("A new draft was created."))
                .addAction("Mark all read", target -> {
                    mail.forEach(m -> m.read = true);
                    list.reset(target);
                });
        commands.addMenu("Export")
                .addAction("CSV", target -> success("Exported " + mail.size() + " messages as CSV."))
                .addAction("PDF", target -> success("Exported " + mail.size() + " messages as PDF."));
        commands.addMenu("More")
                .addAction(Model.of("Delete read messages"), OatVariant.DANGER, target -> {
                    mail.removeIf(m -> m.read);
                    if (selected.getObject() != null && !mail.contains(selected.getObject())) {
                        selected.setObject(null);
                        target.add(detail);
                    }
                    list.reset(target);
                });
        commands.addLink("Reports", ReportsPage.class);
        add(commands);

        add(Oat.Components.splitLayout("inbox", id -> list, id -> detail).setSplit(40).add(AttributeModifier.append("class", "inbox")));

        add(Oat.Components.cookieConsent("cookies")
                .setPolicyLink(GettingStartedPage.class)
                .onAnswer(accepted -> success(accepted ? "Thanks! Optional cookies are on." : "Only necessary cookies are used.")));
        add(new Link<Void>("askAgain") {
            @Override
            public void onClick() {
                Cookie cookie = new Cookie(OatCookieConsent.COOKIE, "");
                cookie.setPath("/");
                ((WebResponse) getResponse()).clearCookie(cookie);
                setResponsePage(InboxPage.class);
            }
        });
    }

    /** A message in the list: a link that opens it, with a context menu. */
    private Fragment row(String id, IModel<Mail> model) {
        Mail m = model.getObject();
        Fragment row = new Fragment(id, "rowFragment", this);
        AjaxLink<Void> open = new AjaxLink<>("open") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                open(target, m, this);
            }
        };
        open.setOutputMarkupId(true);
        open.add(AttributeModifier.replace("data-read", () -> m.read ? "true" : null));
        open.add(new OatIcon("icon", (IModel<String>) () -> m.read ? "mail" : "bell"));
        open.add(new Label("subject", m.subject), new Label("from", m.from));
        open.add(new ContextMenuBehavior()
                .addAction("Open", target -> open(target, m, open))
                .addAction("Mark as unread", target -> {
                    m.read = false;
                    target.add(open);
                })
                .addAction(Model.of("Delete"), OatVariant.DANGER, target -> {
                    mail.remove(m);
                    if (m == selected.getObject()) {
                        selected.setObject(null);
                        target.add(detail);
                    }
                    list.reset(target);
                    success("Deleted “" + m.subject + "”.");
                }));
        row.add(open);
        return row;
    }

    private void open(AjaxRequestTarget target, Mail m, AjaxLink<Void> link) {
        m.read = true;
        selected.setObject(m);
        target.add(detail, link);
    }
}
