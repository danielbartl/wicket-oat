package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Base class for Oat form fields that render a single {@code <input>}. On top of
 * {@link BaseOatField} it can show text addons before and after the input - a currency
 * symbol, a unit, a URL scheme - using Oat's input group ({@code fieldset.group}).
 * <p>
 * Without an addon the field renders exactly like a plain {@link BaseOatField}: the
 * group's own tag is left out. The addons are {@code <label>}s for the input, so
 * clicking one focuses it and screen readers read it with the field's label.
 *
 * @param <T> the model object type
 * @param <C> the concrete {@link FormComponent} type this field wraps
 * @param <F> the concrete field type, returned by the fluent setters
 */
public abstract class BaseOatInputField<T, C extends FormComponent<T>, F extends BaseOatInputField<T, C, F>> extends BaseOatField<T, C, F> {

    private final Label prefix;
    private final Label suffix;

    /**
     * @param id the component id, also the resource key for a missing label
     * @param label the label, or {@code null} to look it up by {@code id}
     * @param model the model, or {@code null} to inherit one from a parent {@code CompoundPropertyModel}
     * @param helperText hint shown below the field while it has no error, or {@code null}
     */
    public BaseOatInputField(String id, IModel<String> label, IModel<T> model, IModel<String> helperText) {
        super(id, label, model, helperText);

        // Transparent, so the field and addons stay children of the container and keep
        // their component paths whether or not the group is rendered
        container.add(new TransparentWebMarkupContainer("group") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // Checks the models: the addons are configured after the group
                setRenderBodyOnly(prefix.getDefaultModelObject() == null && suffix.getDefaultModelObject() == null);
            }
        });

        prefix = newAddon("prefix");
        suffix = newAddon("suffix");
        container.add(prefix, suffix);
    }

    private Label newAddon(String id) {
        // An explicit (empty) model, so the label never inherits one from a parent
        // CompoundPropertyModel by its id
        Label addon = new Label(id, new Model<String>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        addon.add(new Behavior() {
            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("for", getField().getMarkupId());
            }
        });
        return addon;
    }

    /** Text shown before the input, such as {@code €} or {@code https://}. */
    public F setPrefix(String text) {
        return setPrefix(Model.of(text));
    }

    /** Text shown before the input; a {@code null} model or object hides it. */
    public F setPrefix(IModel<String> text) {
        prefix.setDefaultModel(text != null ? text : new Model<String>());
        return self();
    }

    /** Text shown after the input, such as {@code kg} or {@code %}. */
    public F setSuffix(String text) {
        return setSuffix(Model.of(text));
    }

    /** Text shown after the input; a {@code null} model or object hides it. */
    public F setSuffix(IModel<String> text) {
        suffix.setDefaultModel(text != null ? text : new Model<String>());
        return self();
    }
}
