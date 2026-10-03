package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="checkbox">}
 * ({@link CheckBox}).
 */
public class OatCheckBox extends BaseOatField<Boolean, CheckBox, OatCheckBox> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatCheckBox(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatCheckBox(String id, IModel<Boolean> model) {
        this(id, null, model, null);
    }

    public OatCheckBox(String id, String label, IModel<Boolean> model) {
        this(id, Model.of(label), model, null);
    }

    public OatCheckBox(String id, IModel<String> label, IModel<Boolean> model) {
        this(id, label, model, null);
    }

    public OatCheckBox(String id, IModel<String> label, IModel<Boolean> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected CheckBox createFormComponent(String id, IModel<Boolean> model) {
        return new CheckBox(id, model);
    }
}
