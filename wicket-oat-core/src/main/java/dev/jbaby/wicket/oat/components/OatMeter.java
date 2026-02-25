package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A component that renders as an Oat Meter.
 * Uses the native &lt;meter&gt; element.
 */
public class OatMeter extends WebComponent {

    private final IModel<? extends Number> valueModel;
    private final IModel<? extends Number> minModel;
    private final IModel<? extends Number> maxModel;
    private final IModel<? extends Number> lowModel;
    private final IModel<? extends Number> highModel;
    private final IModel<? extends Number> optimumModel;

    public OatMeter(String id, Number value) {
        this(id, Model.of(value), Model.of(0), Model.of(1), null, null, null);
    }

    public OatMeter(String id, IModel<? extends Number> valueModel, IModel<? extends Number> minModel, IModel<? extends Number> maxModel,
                 IModel<? extends Number> lowModel, IModel<? extends Number> highModel, IModel<? extends Number> optimumModel) {
        super(id);
        this.valueModel = valueModel;
        this.minModel = minModel;
        this.maxModel = maxModel;
        this.lowModel = lowModel;
        this.highModel = highModel;
        this.optimumModel = optimumModel;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "meter");

        if (valueModel != null && valueModel.getObject() != null) tag.put("value", valueModel.getObject().toString());
        if (minModel != null && minModel.getObject() != null) tag.put("min", minModel.getObject().toString());
        if (maxModel != null && maxModel.getObject() != null) tag.put("max", maxModel.getObject().toString());
        if (lowModel != null && lowModel.getObject() != null) tag.put("low", lowModel.getObject().toString());
        if (highModel != null && highModel.getObject() != null) tag.put("high", highModel.getObject().toString());
        if (optimumModel != null && optimumModel.getObject() != null) tag.put("optimum", optimumModel.getObject().toString());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (valueModel != null) valueModel.detach();
        if (minModel != null) minModel.detach();
        if (maxModel != null) maxModel.detach();
        if (lowModel != null) lowModel.detach();
        if (highModel != null) highModel.detach();
        if (optimumModel != null) optimumModel.detach();
    }
}
