package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.TagInputBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A tag/chip input field backed by Oat's {@code ot-taginput} web component.
 * The model is a comma-separated string of tags, matching Oat's own
 * {@code value="apple, mango"} attribute convention.
 */
public class OatTagInput extends BaseOatField<String, TextField<String>> {

    public OatTagInput(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatTagInput(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatTagInput(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);

        WebMarkupContainer taginput = new WebMarkupContainer("taginput");
        taginput.add(new TagInputBehavior(field));
        container.add(taginput);
    }

    @Override
    protected TextField<String> createFormComponent(String id, IModel<String> model) {
        TextField<String> hidden = new TextField<>(id, model);
        hidden.add(AttributeModifier.replace("type", "hidden"));
        return hidden;
    }
}
