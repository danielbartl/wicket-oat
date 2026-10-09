package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * A banner asking whether the site may use optional cookies - for analytics, say -
 * shown until the user answers "Accept" or "Only necessary":
 * <pre>{@code
 * add(new OatCookieConsent("cookies").setPolicyLink(PrivacyPage.class));   // e.g. in your base page
 *
 * // wherever optional cookies would be set
 * if (OatCookieConsent.isAccepted()) { ... }
 * }</pre>
 * The answer is kept for a year in the {@value #COOKIE} cookie, which the server sets
 * (no JavaScript); {@link #onAnswer} runs too. It doesn't block anything by itself:
 * your application decides what optional cookies or scripts it loads, by checking
 * {@link #isAccepted()}. Texts are the {@code OatCookieConsent.*} resources.
 */
public class OatCookieConsent extends Panel {

    /** The name of the cookie holding the answer: {@code accepted} or {@code necessary}. */
    public static final String COOKIE = "oat-cookie-consent";

    private static final String ACCEPTED = "accepted";
    private static final String NECESSARY = "necessary";

    private SerializableConsumer<Boolean> onAnswer = accepted -> { };

    public OatCookieConsent(String id) {
        super(id);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("message", new StringResourceModel("OatCookieConsent.message", this)
                .setDefaultValue("We use cookies to make this site work and, with your consent, to improve it.")));
        add(new WebMarkupContainer("policy").setVisible(false));
        add(answer("accept", "OatCookieConsent.accept", "Accept", true, null));
        add(answer("necessary", "OatCookieConsent.necessary", "Only necessary", false, ButtonBehavior.Style.OUTLINE));
    }

    private AjaxLink<Void> answer(String id, String key, String defaultText, boolean accepted, ButtonBehavior.Style style) {
        AjaxLink<Void> button = new AjaxLink<>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                remember(accepted);
                onAnswer.accept(accepted);
                OatCookieConsent.this.setVisible(false);
                target.add(OatCookieConsent.this);
            }
        };
        button.setBody(new StringResourceModel(key, this).setDefaultValue(defaultText));
        button.add(new ButtonBehavior().setStyle(style).setSize(ButtonBehavior.Size.SMALL));
        return button;
    }

    /** Shows a link to the page explaining the cookies. */
    public OatCookieConsent setPolicyLink(Class<? extends Page> page) {
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("policy", page);
        link.setBody(new StringResourceModel("OatCookieConsent.policy", this).setDefaultValue("Privacy policy"));
        addOrReplace(link);
        return this;
    }

    /** Runs when the user answers, with {@code true} for "Accept". */
    public OatCookieConsent onAnswer(SerializableConsumer<Boolean> onAnswer) {
        this.onAnswer = onAnswer;
        return this;
    }

    /** Whether the current user accepted optional cookies. */
    public static boolean isAccepted() {
        return answer().map(ACCEPTED::equals).orElse(false);
    }

    /** Whether the current user has answered, either way. */
    public static boolean isAnswered() {
        return answer().isPresent();
    }

    private static Optional<String> answer() {
        RequestCycle cycle = RequestCycle.get();
        if (cycle == null || !(cycle.getRequest() instanceof WebRequest request)) {
            return Optional.empty();
        }
        Cookie cookie = request.getCookie(COOKIE);
        return cookie == null ? Optional.empty() : Optional.of(cookie.getValue())
                .filter(value -> ACCEPTED.equals(value) || NECESSARY.equals(value));
    }

    private void remember(boolean accepted) {
        Cookie cookie = new Cookie(COOKIE, accepted ? ACCEPTED : NECESSARY);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(365).toSeconds());
        cookie.setHttpOnly(true);
        cookie.setSecure(getRequest().getContainerRequest() instanceof HttpServletRequest http && http.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        ((WebResponse) getResponse()).addCookie(cookie);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        if (isAnswered()) {
            setVisible(false);
        }
    }
}
