package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * A titled section of a form: a {@code <fieldset>} with a {@code <legend>} and an
 * optional description, around whatever fields you {@link #add} to it. Use it to split
 * a long form into parts such as "Billing address" and "Shipping".
 * <p>
 * It is a Wicket {@link Border}, so it goes on a {@code <fieldset>} tag in your markup
 * and wraps that tag's content:
 * <pre>{@code
 * <fieldset wicket:id="billing">
 *     <div wicket:id="street"></div>
 *     <div wicket:id="city"></div>
 * </fieldset>
 * }</pre>
 * Components added with {@link #add} go inside the section; fields inside it still
 * inherit their models from a parent {@code CompoundPropertyModel}.
 */
public class OatFieldset extends Border {

    private final Label legend;
    private final Label description;

    /** Legend looked up by {@code id} in the {@code .properties} files (falling back to the id). */
    public OatFieldset(String id) {
        this(id, (IModel<String>) null);
    }

    public OatFieldset(String id, String legend) {
        this(id, Model.of(legend));
    }

    /**
     * @param id the component id, also the resource key for a missing legend
     * @param legend the legend, or {@code null} to look it up by {@code id}
     */
    public OatFieldset(String id, IModel<String> legend) {
        super(id);
        this.legend = new Label("legend", legend != null ? legend : new StringResourceModel(id, this).setDefaultValue(id));
        addToBorder(this.legend);

        // An explicit (empty) model, so the label never inherits one from a parent
        // CompoundPropertyModel by its id
        description = new Label("description", new Model<String>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        addToBorder(description);
    }

    /** Text shown under the legend, explaining the section; {@code null} hides it. */
    public OatFieldset setDescription(String text) {
        return setDescription(Model.of(text));
    }

    /** Text shown under the legend, explaining the section; a {@code null} model or object hides it. */
    public OatFieldset setDescription(IModel<String> text) {
        description.setDefaultModel(text != null ? text : new Model<String>());
        return this;
    }

    /** The legend model, as given or looked up by id. */
    @SuppressWarnings("unchecked")
    public IModel<String> getLegend() {
        return (IModel<String>) legend.getDefaultModel();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        checkComponentTag(tag, "fieldset");
        super.onComponentTag(tag);
    }
}
