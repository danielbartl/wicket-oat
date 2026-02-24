package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatPasswordField extends BaseOatField<String, PasswordTextField> {

    public OatPasswordField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatPasswordField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatPasswordField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected PasswordTextField createFormComponent(String id, IModel<String> model) {
        return new PasswordTextField(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "password");
                super.onComponentTag(tag);
            }
        };
    }
}
