package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatEmailField extends BaseOatField<String, EmailTextField> {

    public OatEmailField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatEmailField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatEmailField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected EmailTextField createFormComponent(String id, IModel<String> model) {
        return new EmailTextField(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "email");
                super.onComponentTag(tag);
            }
        };
    }
}
