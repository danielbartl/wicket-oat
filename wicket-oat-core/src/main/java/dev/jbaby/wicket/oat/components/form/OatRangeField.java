package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.RangeTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An Oat-styled form field wrapping a native {@code <input type="range">}
 * ({@link RangeTextField}), with fluent {@link #setMin}/{@link #setMax} bounds.
 *
 * @param <N> the numeric model type
 */
public class OatRangeField<N extends Number & Comparable<N>> extends BaseOatField<N, RangeTextField<N>, OatRangeField<N>> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatRangeField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatRangeField(String id, IModel<N> model) {
        this(id, null, model, null);
    }

    public OatRangeField(String id, String label, IModel<N> model) {
        this(id, Model.of(label), model, null);
    }

    public OatRangeField(String id, IModel<String> label, IModel<N> model) {
        this(id, label, model, null);
    }

    public OatRangeField(String id, IModel<String> label, IModel<N> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    public OatRangeField<N> setMin(N min) {
        getField().setMinimum(min);
        return this;
    }

    public OatRangeField<N> setMax(N max) {
        getField().setMaximum(max);
        return this;
    }

    @Override
    protected RangeTextField<N> createFormComponent(String id, IModel<N> model) {
        return new RangeTextField<>(id, model) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "range");
                super.onComponentTag(tag);
            }
        };
    }
}
