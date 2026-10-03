package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat button that runs {@link #onClick} over Ajax. Attach it to an {@code <a>} or a
 * {@code <button>}; the label becomes the tag's text, so no inner markup is needed:
 * {@code <a wicket:id="save"></a>}. Without a label, the tag's own body is rendered,
 * e.g. for an icon. Like any {@code AjaxLink}, a {@code <button>} is rendered with
 * {@code type="button"}, so it never submits a surrounding form; to submit a form,
 * use {@link OatSubmitButton}.
 */
public abstract class OatButton extends AjaxLink<Void> {

    private final ButtonBehavior buttonBehavior;

    /** A button rendering its markup body, e.g. an icon. */
    public OatButton(String id) {
        this(id, (IModel<String>) null);
    }

    public OatButton(String id, String label) {
        this(id, Model.of(label));
    }

    public OatButton(String id, IModel<String> labelModel) {
        super(id);
        setBody(labelModel);
        this.buttonBehavior = new ButtonBehavior();
        add(this.buttonBehavior);
    }

    public OatButton setStyle(ButtonBehavior.Style style) {
        buttonBehavior.setStyle(style);
        return this;
    }

    public OatButton setSize(ButtonBehavior.Size size) {
        buttonBehavior.setSize(size);
        return this;
    }

    public OatButton setVariant(OatVariant variant) {
        buttonBehavior.setVariant(variant);
        return this;
    }

    /** Square button sized for a single icon. */
    public OatButton setIcon(boolean icon) {
        buttonBehavior.setIcon(icon);
        return this;
    }

    @Override
    public abstract void onClick(AjaxRequestTarget target);
}
