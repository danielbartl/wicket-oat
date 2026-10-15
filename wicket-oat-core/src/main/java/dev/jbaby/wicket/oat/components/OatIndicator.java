package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Objects;

/**
 * Puts a count or a status dot on a corner of something - the unread messages on an
 * inbox link, a person's presence on their avatar. It is a Wicket {@link Border}: what
 * you put in its tag is what it marks.
 * <pre>{@code
 * <span wicket:id="inbox"><a wicket:id="link">Inbox</a></span>
 * <span wicket:id="presence"><div wicket:id="avatar"></div></span>
 * }</pre>
 * <pre>{@code
 * OatIndicator inbox = new OatIndicator("inbox", () -> messages.countUnread()).setVariant(OatVariant.DANGER);
 * inbox.add(new BookmarkablePageLink<>("link", InboxPage.class));
 * add(inbox);
 *
 * OatIndicator presence = OatIndicator.status("presence", () -> ada.isOnline() ? OatVariant.SUCCESS : OatVariant.SECONDARY,
 *         () -> ada.isOnline() ? "Online" : "Away");
 * presence.add(new OatAvatar("avatar", "AL"));
 * add(presence);
 * }</pre>
 * A count shows as a small badge on the top corner at the end, hidden while it is
 * {@code null}, 0 or empty, so it can also be a word like "New". Screen readers read it
 * after what it marks ("Inbox 3"). A status is an {@link OatStatus} dot on the bottom
 * corner at the end; {@link #setCorner} moves either. The tag gets a markup id, so
 * {@code target.add(inbox)} updates the count.
 */
public class OatIndicator extends Border {

    /** A corner of what the indicator marks; start and end follow the text direction. */
    public enum Corner {
        TOP_END("top-end"),
        TOP_START("top-start"),
        BOTTOM_END("bottom-end"),
        BOTTOM_START("bottom-start");

        private final String value;

        Corner(String value) {
            this.value = value;
        }
    }

    private static final String ITEM_ID = "item";

    private Corner corner;
    private OatVariant variant = OatVariant.DEFAULT;

    /** @param badge the count or short text in the badge, read on every render */
    public OatIndicator(String id, IModel<?> badge) {
        this(id, Corner.TOP_END);
        Label count = new Label(ITEM_ID, Objects.requireNonNull(badge)) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(OatAppLayout.isShownBadge(getDefaultModelObject()));
            }
        };
        count.add(AttributeAppender.append("class", "badge"));
        count.add(AttributeModifier.replace("data-variant", (IModel<String>) () -> variant.getValue()));
        addItem(count);
    }

    private OatIndicator(String id, Corner corner) {
        super(id);
        this.corner = corner;
        setOutputMarkupId(true);
    }

    /**
     * An indicator with a status dot (an {@link OatStatus}) on the bottom corner at the end,
     * e.g. a person's presence on their avatar.
     *
     * @param variant the dot's color, read on every render
     * @param label what the dot means, e.g. "Online", read to screen readers
     */
    public static OatIndicator status(String id, IModel<OatVariant> variant, IModel<String> label) {
        OatIndicator indicator = new OatIndicator(id, Corner.BOTTOM_END);
        indicator.addItem(new OatStatus(ITEM_ID, variant, label));
        return indicator;
    }

    /**
     * Adds the badge or dot, with its corner on itself: in development mode Wicket's own
     * tags stand between it and the indicator's tag, so CSS can't rely on the parent.
     */
    private void addItem(Component item) {
        item.add(AttributeAppender.append("class", "oat-indicator-item"));
        item.add(AttributeModifier.replace("data-corner", (IModel<String>) () -> corner.value));
        addToBorder(item);
    }

    public OatIndicator setCorner(Corner corner) {
        this.corner = Objects.requireNonNull(corner);
        return this;
    }

    /** The badge's color, e.g. {@code DANGER} for unread messages; the primary color by default. */
    public OatIndicator setVariant(OatVariant variant) {
        this.variant = Objects.requireNonNull(variant);
        return this;
    }

    /** The badge or the status dot. */
    public Component getItem() {
        return get(ITEM_ID);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-indicator", " ");
    }
}
