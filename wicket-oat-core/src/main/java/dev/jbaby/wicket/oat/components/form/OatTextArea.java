package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatTextArea<T> extends BaseOatField<T, TextArea<T>> {

    public OatTextArea(String id, String label, IModel<T> model) {
        this(id, Model.of(label), model, null);
    }

    public OatTextArea(String id, IModel<String> label, IModel<T> model) {
        this(id, label, model, null);
    }

    public OatTextArea(String id, IModel<String> label, IModel<T> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected TextArea<T> createFormComponent(String id, IModel<T> model) {
        return new TextArea<>(id, model);
    }
}
