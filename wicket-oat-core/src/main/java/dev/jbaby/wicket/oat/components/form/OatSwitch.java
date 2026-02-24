package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.SwitchBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class OatSwitch extends OatCheckBox {

    public OatSwitch(String id, String label, IModel<Boolean> model) {
        this(id, Model.of(label), model, null);
    }

    public OatSwitch(String id, IModel<String> label, IModel<Boolean> model) {
        this(id, label, model, null);
    }

    public OatSwitch(String id, IModel<String> label, IModel<Boolean> model, IModel<String> helper) {
        super(id, label, model, helper);
        field.add(new SwitchBehavior());
    }
}
