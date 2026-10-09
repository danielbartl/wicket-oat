package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="month">}.
 */
public class OatMonthField extends BaseOatInputField<String, Html5TextField<String>, OatMonthField> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatMonthField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatMonthField(String id, IModel<String> model) {
        this(id, null, model, null);
    }

    public OatMonthField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatMonthField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatMonthField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<String> createFormComponent(String id, IModel<String> model) {
        return new Html5TextField<>(id, model, String.class, "month");
    }
}
