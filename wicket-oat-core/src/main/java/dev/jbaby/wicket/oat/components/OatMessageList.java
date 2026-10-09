package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;

/**
 * A list of messages - comments on an invoice, notes on a customer, a support ticket's
 * conversation - each with the author's avatar and name, its time and its text:
 * <pre>{@code
 * add(new OatMessageList<>("comments", () -> comments.of(ticket), Comment::author, Comment::text, Comment::at));
 * }</pre>
 * The messages are shown in the order given. The avatar shows the author's initials,
 * or an image with {@link #setAvatar}. The text is escaped and keeps its line breaks;
 * times are any {@code java.time} value, shown in the user's locale in a
 * {@code <time>} element. Pair it with an {@link OatMessageInput} to add messages,
 * re-rendering the list with {@code target.add(list)}.
 *
 * @param <T> the message type
 */
public class OatMessageList<T> extends Panel {

    private SerializableFunction<T, String> avatar;

    /**
     * @param messages the messages, in the order to show them
     * @param author a message's author, shown by name and initials
     * @param text a message's text
     * @param time when a message was written, or {@code null} to leave it out
     */
    public OatMessageList(String id, IModel<? extends List<T>> messages, SerializableFunction<T, String> author,
                          SerializableFunction<T, ?> text, SerializableFunction<T, ? extends TemporalAccessor> time) {
        super(id);
        setOutputMarkupId(true);
        add(new ListView<T>("messages", messages) {
            @Override
            protected void populateItem(ListItem<T> item) {
                T message = item.getModelObject();
                String name = author.apply(message);
                String image = avatar != null ? avatar.apply(message) : null;
                item.add(new OatAvatar("avatar", image != null ? Model.of(image) : null, Model.of(initials(name)),
                        OatAvatar.Size.SMALL));
                item.add(new Label("author", Model.of(name)));

                TemporalAccessor when = time.apply(message);
                ValueLabel timeLabel = new ValueLabel("time", new Model<>((Serializable) when));
                timeLabel.add(AttributeModifier.replace("datetime", when != null ? when.toString() : null));
                timeLabel.setVisible(when != null);
                item.add(timeLabel);

                Object body = text.apply(message);
                item.add(new Label("text", Model.of(body != null ? body.toString() : "")));
            }
        });
    }

    /** An image URL for a message's avatar, instead of the author's initials; {@code null} keeps them. */
    public OatMessageList<T> setAvatar(SerializableFunction<T, String> imageUrl) {
        this.avatar = imageUrl;
        return this;
    }

    /** "Jordan Lee" gives "JL", "ada" gives "A". */
    static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] words = name.strip().split("\\s+");
        String first = words[0].substring(0, 1);
        String last = words.length > 1 ? words[words.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }
}
