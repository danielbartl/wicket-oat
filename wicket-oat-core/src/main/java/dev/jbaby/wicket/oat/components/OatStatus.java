package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Objects;

/**
 * A small colored dot for the state of something - a service up or down, a person
 * online or away, a job running or failed:
 * <pre>{@code
 * add(new OatStatus("database", () -> db.isUp() ? OatVariant.SUCCESS : OatVariant.DANGER,
 *         () -> db.isUp() ? "Online" : "Offline"));
 * }</pre>
 * on any tag, e.g. a {@code <span>}. The dot has the variant's color: the primary color by default,
 * gray for {@code SECONDARY}, green, amber or red for {@code SUCCESS}, {@code WARNING} and
 * {@code DANGER}. Screen readers announce its label; without one, e.g. next to a text
 * that says the same, they skip the dot. {@link OatIndicator#status} puts one on a
 * corner of an avatar or icon.
 */
public class OatStatus extends WebComponent {

    private final IModel<OatVariant> variant;
    private final IModel<String> label;

    public OatStatus(String id, OatVariant variant, String label) {
        this(id, Model.of(variant), Model.of(label));
    }

    /**
     * @param variant the dot's color, read on every render
     * @param label what the dot means, e.g. "Online", or {@code null} to leave it to the text beside it
     */
    public OatStatus(String id, IModel<OatVariant> variant, IModel<String> label) {
        super(id);
        this.variant = Objects.requireNonNull(variant);
        this.label = label != null ? label : Model.of();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (tag.isOpenClose()) {
            tag.setType(TagType.OPEN);
        }
        tag.append("class", "oat-status", " ");
        OatVariant color = variant.getObject();
        if (color != null && color.getValue() != null) {
            tag.put("data-variant", color.getValue());
        } else {
            tag.remove("data-variant");
        }
        String text = label.getObject();
        if (text != null && !text.isBlank()) {
            tag.put("role", "img");
            tag.put("aria-label", text);
            tag.remove("aria-hidden");
        } else {
            tag.remove("role");
            tag.remove("aria-label");
            tag.put("aria-hidden", "true");
        }
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        // A dot, without content
        replaceComponentTagBody(markupStream, openTag, "");
    }

    @Override
    protected void onDetach() {
        variant.detach();
        label.detach();
        super.onDetach();
    }
}
