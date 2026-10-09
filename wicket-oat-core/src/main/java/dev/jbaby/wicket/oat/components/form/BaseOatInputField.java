package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * Base class for Oat form fields that render a single {@code <input>}. On top of
 * {@link BaseOatField} it can show text addons before and after the input - a currency
 * symbol, a unit, a URL scheme - using Oat's input group ({@code fieldset.group}), and
 * {@linkplain #setSuggestions suggest values} as the user types.
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
    private final WebMarkupContainer datalist;
    private IModel<? extends List<?>> suggestions;

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

        datalist = new WebMarkupContainer("suggestions") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(hasSuggestions());
            }
        };
        datalist.setOutputMarkupId(true);
        datalist.add(new ListView<Object>("option", (IModel<List<Object>>) () -> {
            List<?> values = suggestions != null ? suggestions.getObject() : null;
            return values != null ? List.copyOf(values) : List.of();
        }) {
            @Override
            protected void populateItem(ListItem<Object> item) {
                item.add(AttributeModifier.replace("value", String.valueOf(item.getModelObject())));
            }
        });
        container.add(datalist);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        // The browser offers the datalist's values as the user types
        getField().add(AttributeModifier.replace("list", (IModel<String>) () -> hasSuggestions() ? datalist.getMarkupId() : null));
    }

    private boolean hasSuggestions() {
        return suggestions != null && suggestions.getObject() != null && !suggestions.getObject().isEmpty();
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

    /**
     * Values the browser suggests as the user types, from a native {@code <datalist>}:
     * e.g. existing tags, cities or product codes. The user can still type anything else;
     * add a validator to restrict the value.
     */
    public F setSuggestions(List<String> suggestions) {
        return setSuggestions(Model.ofList(suggestions));
    }

    /** Values the browser suggests as the user types, read on every render. */
    public F setSuggestions(IModel<? extends List<?>> suggestions) {
        this.suggestions = suggestions;
        return self();
    }

    @Override
    protected void onDetach() {
        if (suggestions != null) {
            suggestions.detach();
        }
        super.onDetach();
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
