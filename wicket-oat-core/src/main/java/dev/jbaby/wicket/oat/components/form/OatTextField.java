package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatTextField<T> extends BaseOatField<T, TextField<T>> {

    public OatTextField(String id, String label, IModel<T> model) {
        this(id, Model.of(label), model, null);
    }

    public OatTextField(String id, IModel<String> label, IModel<T> model) {
        this(id, label, model, null);
    }

    public OatTextField(String id, IModel<String> label, IModel<T> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected TextField<T> createFormComponent(String id, IModel<T> model) {
        return new TextField<>(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "text");
                super.onComponentTag(tag);
            }
        };
    }
}
