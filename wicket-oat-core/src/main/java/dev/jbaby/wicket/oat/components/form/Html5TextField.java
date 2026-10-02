package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 * A generic text component that supports any HTML5 input type without strict validation.
 * Like any {@link TextField} it renders the current model value into the {@code value}
 * attribute. An optional converter overrides the application's converter for the model
 * type, for input types whose wire format is fixed regardless of locale (see
 * {@link Html5Converters}).
 */
public class Html5TextField<T> extends TextField<T> {

    private final String inputType;
    private final IConverter<T> converter;

    public Html5TextField(String id, IModel<T> model, Class<T> type, String inputType) {
        this(id, model, type, inputType, null);
    }

    public Html5TextField(String id, IModel<T> model, Class<T> type, String inputType, IConverter<T> converter) {
        super(id, model, type);
        this.inputType = inputType;
        this.converter = converter;
    }

    @Override
    protected String[] getInputTypes() {
        return new String[] { inputType };
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        tag.put("type", inputType);
        super.onComponentTag(tag);
    }

    @Override
    protected IConverter<?> createConverter(Class<?> type) {
        if (converter != null && getType() != null && getType().isAssignableFrom(type)) {
            return converter;
        }
        return super.createConverter(type);
    }
}
