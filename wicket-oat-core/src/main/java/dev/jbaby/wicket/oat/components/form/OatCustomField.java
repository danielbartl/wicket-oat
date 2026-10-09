package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Objects;

/**
 * An Oat form field around an input of your own that is made of several inputs - a
 * phone number as country code and number, an amount with its currency, a size as
 * width and height - usually a Wicket {@link FormComponentPanel} that converts them
 * into one value:
 * <pre>{@code
 * form.add(new OatCustomField<Phone>("phone", "Phone", phoneModel, PhoneInput::new));   // (id, model) -> input
 * }</pre>
 * Like the built-in fields, it shows a label (a {@code <legend>}, as it names a group of
 * inputs), a hint and the input's validation errors inline, takes its label from the
 * {@code .properties} files and its model from a {@code CompoundPropertyModel} when they
 * are left out, and marks required fields. Every form component inside the input also
 * gets {@code aria-describedby} pointing at the hint or error and {@code aria-invalid},
 * so your inputs are as accessible as Oat's own; give each its own {@code aria-label}.
 *
 * @param <T> the model object type
 */
public class OatCustomField<T> extends BaseOatField<T, FormComponent<T>, OatCustomField<T>> {

    private final SerializableBiFunction<String, IModel<T>, ? extends FormComponent<T>> input;
    private boolean ariaWired;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     *
     * @param input creates the input with the id and model it is given
     */
    public OatCustomField(String id, SerializableBiFunction<String, IModel<T>, ? extends FormComponent<T>> input) {
        this(id, null, null, null, input);
    }

    public OatCustomField(String id, String label, IModel<T> model,
                          SerializableBiFunction<String, IModel<T>, ? extends FormComponent<T>> input) {
        this(id, Model.of(label), model, null, input);
    }

    public OatCustomField(String id, IModel<String> label, IModel<T> model, IModel<String> helper,
                          SerializableBiFunction<String, IModel<T>, ? extends FormComponent<T>> input) {
        super(id, label, model, helper);
        this.input = Objects.requireNonNull(input);
    }

    @Override
    protected FormComponent<T> createFormComponent(String id, IModel<T> model) {
        FormComponent<T> component = input.apply(id, model);
        if (component == null || !id.equals(component.getId())) {
            throw new IllegalArgumentException("The input must use the id passed to the factory (\"" + id + "\"), but was "
                    + (component == null ? "null" : "\"" + component.getId() + "\""));
        }
        return component;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        if (ariaWired) {
            return;
        }
        ariaWired = true;
        // Once the input's own components exist (also those added in its onInitialize).
        // Every form component is a container; visit the inputs of a FormComponentPanel.
        FormComponent<T> field = getField();
        field.visitChildren(FormComponent.class, (inner, visit) -> {
            inner.add(AttributeModifier.replace("aria-describedby", (IModel<String>) this::getFeedbackMarkupId));
            inner.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () ->
                    field.hasErrorMessage() || ((FormComponent<?>) inner).hasErrorMessage() ? "true" : "false"));
        });
    }
}
