package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.UrlTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="url">}
 * ({@link UrlTextField}).
 */
public class OatUrlField extends BaseOatInputField<String, UrlTextField, OatUrlField> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatUrlField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatUrlField(String id, IModel<String> model) {
        this(id, null, model, null);
    }

    public OatUrlField(String id, String label, IModel<String> model) {
        this(id, Model.of(label), model, null);
    }

    public OatUrlField(String id, IModel<String> label, IModel<String> model) {
        this(id, label, model, null);
    }

    public OatUrlField(String id, IModel<String> label, IModel<String> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected UrlTextField createFormComponent(String id, IModel<String> model) {
        return new UrlTextField(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "url");
                super.onComponentTag(tag);
            }
        };
    }
}
