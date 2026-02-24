package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatColorField extends BaseOatField<String, Html5TextField<String>> {

    public OatColorField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatColorField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatColorField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<String> createFormComponent(String id, IModel<String> model) {
        return new Html5TextField<>(id, model, String.class, "color");
    }
}
