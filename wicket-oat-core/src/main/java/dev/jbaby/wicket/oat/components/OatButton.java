package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A convenience component for an Oat Button.
 * Renders as an &lt;a&gt; tag but behaves like a button.
 */
public abstract class OatButton extends AjaxLink<Void> {

    private final ButtonBehavior buttonBehavior;

    public OatButton(String id, String label) {
        this(id, Model.of(label));
    }

    public OatButton(String id, IModel<String> labelModel) {
        super(id);
        add(new Label("label", labelModel).setRenderBodyOnly(true));
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

    public OatButton setVariant(ButtonBehavior.Variant variant) {
        buttonBehavior.setVariant(variant);
        return this;
    }

    @Override
    public abstract void onClick(AjaxRequestTarget target);
}
