package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="number">}
 * ({@link NumberTextField}), with fluent {@link #setMin}/{@link #setMax} bounds.
 *
 * @param <N> the numeric model type
 */
public class OatNumberField<N extends Number & Comparable<N>> extends BaseOatField<N, NumberTextField<N>> {

    public OatNumberField(String id, String label, IModel<N> model) {
        this(id, Model.of(label), model, null);
    }

    public OatNumberField(String id, IModel<String> label, IModel<N> model) {
        this(id, label, model, null);
    }

    public OatNumberField(String id, IModel<String> label, IModel<N> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    public OatNumberField<N> setMin(N min) {
        field.setMinimum(min);
        return this;
    }

    public OatNumberField<N> setMax(N max) {
        field.setMaximum(max);
        return this;
    }

    @Override
    protected NumberTextField<N> createFormComponent(String id, IModel<N> model) {
        return new NumberTextField<>(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "number");
                super.onComponentTag(tag);
            }
        };
    }
}
