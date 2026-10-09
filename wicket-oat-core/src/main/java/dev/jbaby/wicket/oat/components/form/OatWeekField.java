package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="week">}.
 */
public class OatWeekField extends BaseOatInputField<String, Html5TextField<String>, OatWeekField> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatWeekField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatWeekField(String id, IModel<String> model) {
        this(id, null, model, null);
    }

    public OatWeekField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatWeekField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatWeekField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<String> createFormComponent(String id, IModel<String> model) {
        return new Html5TextField<>(id, model, String.class, "week");
    }
}
