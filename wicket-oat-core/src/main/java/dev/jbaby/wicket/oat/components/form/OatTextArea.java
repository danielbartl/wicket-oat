package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <textarea>} ({@link TextArea}).
 *
 * @param <T> the model object type
 */
public class OatTextArea<T> extends BaseOatField<T, TextArea<T>, OatTextArea<T>> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatTextArea(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatTextArea(String id, IModel<T> model) {
        this(id, null, model, null);
    }

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
