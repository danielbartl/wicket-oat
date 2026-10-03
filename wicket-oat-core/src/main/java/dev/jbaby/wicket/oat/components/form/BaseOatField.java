package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.HintBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidator;

/**
 * Base class for every Oat form field: wraps a label, the underlying Wicket
 * {@link FormComponent}, and a feedback/hint message in Oat's standard field
 * markup. Wires accessibility automatically - the field's {@code aria-describedby}
 * points at the feedback message, and {@code aria-invalid}/{@code data-field="error"}
 * are toggled based on validation state - so subclasses get this for free.
 * <p>
 * It is meant to be used like a plain Wicket form component:
 * <ul>
 * <li>The model is this panel's model, which the inner field reads and writes. Leave it
 * out to inherit one from a parent {@code CompoundPropertyModel}, keyed by the id.</li>
 * <li>Leave the label out to look it up in the {@code .properties} files with the id
 * as the key (falling back to the id itself), as Wicket does for {@code ${label}} in
 * validation messages.</li>
 * <li>The panel renders its own tag with a markup id, so it can be re-rendered with
 * {@code target.add(field)}, e.g. to show validation errors after an Ajax submit.</li>
 * <li>Validators and {@link AjaxFormComponentUpdatingBehavior}s passed to
 * {@link #add(Behavior...)} are added to the inner field; any other behavior applies
 * to the panel's own tag.</li>
 * </ul>
 *
 * @param <T> the model object type
 * @param <C> the concrete {@link FormComponent} type this field wraps
 * @param <F> the concrete field type, returned by the fluent setters
 */
public abstract class BaseOatField<T, C extends FormComponent<T>, F extends BaseOatField<T, C, F>> extends GenericPanel<T> {

    private C field;
    protected final WebMarkupContainer container;
    protected final IModel<String> helperText;
    private final Label label;
    private final Label feedback;

    /**
     * @param id the component id, also the resource key for a missing label
     * @param label the label, or {@code null} to look it up by {@code id}
     * @param model the model, or {@code null} to inherit one from a parent {@code CompoundPropertyModel}
     * @param helperText hint shown below the field while it has no error, or {@code null}
     */
    public BaseOatField(String id, IModel<String> label, IModel<T> model, IModel<String> helperText) {
        super(id, model);
        this.helperText = helperText;
        setOutputMarkupId(true);

        // The Semantic Container (<section>)
        container = new WebMarkupContainer("container");
        // Oat's magic: data-field="error" on the container (aria-invalid goes on the input)
        container.add(AttributeModifier.replace("data-field", (IModel<String>) () -> getField().hasErrorMessage() ? "error" : ""));
        add(container);

        this.label = new Label("label", label != null ? label : new StringResourceModel(id, this).setDefaultValue(id));
        this.label.add(AttributeModifier.replace("for", (IModel<String>) () -> getField().getMarkupId()));
        container.add(this.label);

        feedback = new Label("feedback", this::getFeedbackContent);
        feedback.setOutputMarkupId(true);
        feedback.add(new HintBehavior()); // Add standard Oat hint styling
        container.add(feedback);
    }

    /**
     * Creates the wrapped form component. Called lazily - on the first {@link #getField()}
     * or, at the latest, in {@link #onInitialize()} - so fields a subclass sets in its own
     * constructor are already available here.
     *
     * @param id the component id to use
     * @param model the model to use; it reads and writes this panel's model
     */
    protected abstract C createFormComponent(String id, IModel<T> model);

    /** The wrapped form component, for advanced customization. */
    public C getField() {
        if (field == null) {
            field = createFormComponent("field", new FieldModel());
            field.setLabel(getLabel());
            field.setOutputMarkupId(true);
            // Link input to feedback for screen readers
            field.add(AttributeModifier.replace("aria-describedby", (IModel<String>) feedback::getMarkupId));
            field.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () -> field.hasErrorMessage() ? "true" : "false"));
            container.add(field);
        }
        return field;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getField(); // create it now if no fluent call or subclass has done so yet
    }

    private String getFeedbackContent() {
        FeedbackMessage error = getField().getFeedbackMessages().first(FeedbackMessage.ERROR);
        if (error != null) {
            error.markRendered();
            return String.valueOf(error.getMessage());
        }
        return (helperText != null) ? helperText.getObject() : "";
    }

    /** The label model, as given or looked up by id. */
    @SuppressWarnings("unchecked")
    public IModel<String> getLabel() {
        return (IModel<String>) label.getDefaultModel();
    }

    @SuppressWarnings("unchecked")
    protected final F self() {
        return (F) this;
    }

    // Fluent API, typed to the concrete field so calls chain with its own setters

    /**
     * Validators and {@link AjaxFormComponentUpdatingBehavior}s only work on a form
     * component, so they are added to the inner {@link #getField() field}. Any other
     * behavior applies to this panel's own tag.
     */
    @Override
    public F add(Behavior... behaviors) {
        for (Behavior behavior : behaviors) {
            if (behavior instanceof IValidator<?> || behavior instanceof AjaxFormComponentUpdatingBehavior) {
                getField().add(behavior);
            } else {
                super.add(behavior);
            }
        }
        return self();
    }

    public F addValidator(IValidator<? super T> validator) {
        getField().add(validator);
        return self();
    }

    public F setRequired(boolean required) {
        getField().setRequired(required);
        return self();
    }

    public F setLabel(IModel<String> label) {
        this.label.setDefaultModel(label);
        getField().setLabel(label);
        return self();
    }

    public F setPlaceholder(IModel<String> placeholder) {
        getField().add(AttributeModifier.replace("placeholder", placeholder));
        return self();
    }

    /**
     * The inner field's model: reads and writes this panel's model, which may be one
     * inherited from a parent {@code CompoundPropertyModel}. Passes on that model's
     * object class too, so text fields can resolve their type for conversion.
     */
    private final class FieldModel implements IObjectClassAwareModel<T> {

        @Override
        public T getObject() {
            return getModelObject();
        }

        @Override
        public void setObject(T object) {
            IModel<T> model = getModel();
            if (model == null) {
                throw new IllegalStateException("Form field '" + getPageRelativePath() + "' has no model: pass one "
                        + "to its constructor, or add it to a container with a CompoundPropertyModel");
            }
            model.setObject(object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<T> getObjectClass() {
            return getModel() instanceof IObjectClassAwareModel<?> aware ? (Class<T>) aware.getObjectClass() : null;
        }
    }
}
