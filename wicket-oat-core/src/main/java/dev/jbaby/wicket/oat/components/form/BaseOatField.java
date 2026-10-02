package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.HintBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;

/**
 * Base class for every Oat form field: wraps a label, the underlying Wicket
 * {@link FormComponent}, and a feedback/hint message in Oat's standard field
 * markup. Wires accessibility automatically - the field's {@code aria-describedby}
 * points at the feedback message, and {@code aria-invalid}/{@code data-field="error"}
 * are toggled based on validation state - so subclasses get this for free.
 *
 * @param <T> the model object type
 * @param <C> the concrete {@link FormComponent} type this field wraps
 */
public abstract class BaseOatField<T, C extends FormComponent<T>> extends Panel {

    protected C field;
    protected final WebMarkupContainer container;
    protected final IModel<String> helperText;
    private final Label feedback;

    public BaseOatField(String id, IModel<String> label, IModel<T> model, IModel<String> helperText) {

        super(id);
        this.helperText = helperText;
        setRenderBodyOnly(true);

        // The Semantic Container (<section>)
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true); // Essential for AJAX
        add(container);

        feedback = new Label("feedback", this::getFeedbackContent);
        feedback.setOutputMarkupId(true);
        feedback.add(new HintBehavior()); // Add standard Oat hint styling

        // Oat's magic: data-field="error" on the container, aria-invalid on the input.
        // Added once with a dynamic model; re-evaluated on every render.
        container.add(AttributeModifier.replace("data-field", (IModel<String>) () -> field.hasErrorMessage() ? "error" : ""));

        field = createFormComponent("field", model); // Subclasses implement this
        wireField(label);

        // Resolved at render time so it follows a field replaced by a subclass
        container.add(new Label("label", label).add(AttributeModifier.replace("for", (IModel<String>) () -> field.getMarkupId())));
        container.add(field);
        container.add(feedback);
    }

    /**
     * Applies the standard Oat label/accessibility wiring to {@link #field}. Subclasses
     * that replace {@link #field} after construction must call this again for the new
     * instance.
     */
    protected final void wireField(IModel<String> label) {
        field.setLabel(label);
        field.setOutputMarkupId(true);
        // Link input to feedback for screen readers
        field.add(AttributeModifier.replace("aria-describedby", feedback.getMarkupId()));
        field.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () -> field.hasErrorMessage() ? "true" : "false"));
    }

    protected abstract C createFormComponent(String id, IModel<T> model);

    private String getFeedbackContent() {
        if (field.hasErrorMessage()) {
            return field.getFeedbackMessages().first().getMessage().toString();
        }
        return (helperText != null) ? helperText.getObject() : "";
    }

    // Fluent API for adding validators and setting the required status
    public BaseOatField<T, C> addValidator(IValidator<T> validator) {
        field.add(validator);
        return this;
    }

    public BaseOatField<T, C> setRequired(boolean required) {
        field.setRequired(required);
        return this;
    }

    public BaseOatField<T, C> setPlaceholder(IModel<String> placeholder) {
        field.add(AttributeModifier.replace("placeholder", placeholder));
        return this;
    }

    public C getField() { return field; } // For advanced customization
}