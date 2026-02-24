package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.HintBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;

public abstract class BaseOatField<T, C extends FormComponent<T>> extends Panel {

    protected final C field;
    protected final WebMarkupContainer container;
    protected final IModel<String> helperText;

    public BaseOatField(String id, IModel<String> label, IModel<T> model, IModel<String> helperText) {

        super(id);
        this.helperText = helperText;
        setRenderBodyOnly(true);

        // The Semantic Container (<section>)
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true); // Essential for AJAX
        add(container);

        field = createFormComponent("field", model); // Subclasses implement this
        field.setLabel(label);
        field.setOutputMarkupId(true);

        Label feedback = new Label("feedback", this::getFeedbackContent);
        feedback.setOutputMarkupId(true);
        feedback.add(new HintBehavior()); // Add standard Oat hint styling

        // Link input to feedback for screen readers
        field.add(AttributeModifier.replace("aria-describedby", feedback.getMarkupId()));

        container.add(new Label("label", label).add(AttributeModifier.replace("for", field.getMarkupId())));
        container.add(field);
        container.add(feedback);
    }

    protected abstract C createFormComponent(String id, IModel<T> model);

    private String getFeedbackContent() {
        if (field.hasErrorMessage()) {
            return field.getFeedbackMessages().first().getMessage().toString();
        }
        return (helperText != null) ? helperText.getObject() : "";
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        boolean invalid = field.hasErrorMessage();
        // Oat's magic: data-field="error" on the container, aria-invalid on the input
        container.add(AttributeModifier.replace("data-field", invalid ? "error" : ""));
        field.add(AttributeModifier.replace("aria-invalid", invalid ? "true" : "false"));
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