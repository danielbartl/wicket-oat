package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A component that renders as an Oat Progress bar.
 * Uses the native &lt;progress&gt; element.
 */
public class Progress extends WebComponent {

    private final IModel<? extends Number> valueModel;
    private final IModel<? extends Number> maxModel;

    public Progress(String id, Number value) {
        this(id, Model.of(value), Model.of(100));
    }

    public Progress(String id, IModel<? extends Number> valueModel) {
        this(id, valueModel, Model.of(100));
    }

    public Progress(String id, IModel<? extends Number> valueModel, IModel<? extends Number> maxModel) {
        super(id);
        this.valueModel = valueModel;
        this.maxModel = maxModel;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "progress");

        if (valueModel != null && valueModel.getObject() != null) {
            tag.put("value", valueModel.getObject().toString());
        }
        if (maxModel != null && maxModel.getObject() != null) {
            tag.put("max", maxModel.getObject().toString());
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (valueModel != null) valueModel.detach();
        if (maxModel != null) maxModel.detach();
    }
}
