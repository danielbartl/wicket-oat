package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.Objects;

/**
 * A box for writing a message and a Send button, e.g. under an {@link OatMessageList}:
 * <pre>{@code
 * add(new OatMessageInput("reply", (target, text) -> {
 *     comments.add(ticket, currentUser(), text);
 *     target.add(commentList);
 * }));
 * }</pre>
 * Sending runs over Ajax with the text (stripped of surrounding blank space) and empties
 * the box; a blank message isn't sent. The box is named by the
 * {@code OatMessageInput.label} resource ("Message") for screen readers, with the
 * {@code .placeholder} and {@code .send} resources as its other texts.
 */
public class OatMessageInput extends Panel {

    private final IModel<String> text = new Model<>();
    private final SerializableBiConsumer<AjaxRequestTarget, String> onSend;

    public OatMessageInput(String id, SerializableBiConsumer<AjaxRequestTarget, String> onSend) {
        super(id);
        this.onSend = Objects.requireNonNull(onSend);

        Form<Void> form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        TextArea<String> box = new TextArea<>("text", text);
        box.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatMessageInput.label", this).setDefaultValue("Message")));
        box.add(AttributeModifier.replace("placeholder", new StringResourceModel("OatMessageInput.placeholder", this)
                .setDefaultValue("Write a message…")));
        form.add(box);

        form.add(new OatSubmitButton("send", new StringResourceModel("OatMessageInput.send", this).setDefaultValue("Send")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                String message = text.getObject() == null ? "" : text.getObject().strip();
                if (!message.isEmpty()) {
                    OatMessageInput.this.onSend.accept(target, message);
                    text.setObject(null);
                }
                target.add(form);
            }
        }.setBusyIndicator(true));
    }
}
