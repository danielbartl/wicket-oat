package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.SwitchBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="checkbox">}
 * ({@link CheckBox}) that renders as an Oat toggle switch ({@code role="switch"})
 * via {@link SwitchBehavior}, instead of a plain checkbox like {@link OatCheckBox}.
 */
public class OatSwitch extends BaseOatField<Boolean, CheckBox, OatSwitch> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatSwitch(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatSwitch(String id, IModel<Boolean> model) {
        this(id, null, model, null);
    }

    public OatSwitch(String id, String label, IModel<Boolean> model) {
        this(id, Model.of(label), model, null);
    }

    public OatSwitch(String id, IModel<String> label, IModel<Boolean> model) {
        this(id, label, model, null);
    }

    public OatSwitch(String id, IModel<String> label, IModel<Boolean> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected CheckBox createFormComponent(String id, IModel<Boolean> model) {
        CheckBox checkBox = new CheckBox(id, model);
        checkBox.add(new SwitchBehavior());
        return checkBox;
    }
}
