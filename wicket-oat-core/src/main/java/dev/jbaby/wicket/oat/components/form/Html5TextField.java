package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.model.IModel;

/**
 * A generic text component that supports any HTML5 input type without strict validation.
 */
public class Html5TextField<T> extends AbstractTextComponent<T> {

    private final String inputType;

    public Html5TextField(String id, IModel<T> model, Class<T> type, String inputType) {
        super(id, model);
        setType(type);
        this.inputType = inputType;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("type", inputType);
    }
}
