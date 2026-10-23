package dev.jbaby.wicket.oat.components.form;

import com.github.openjson.JSONObject;
import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.behaviors.HintBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.validation.IValidator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for every Oat form field: wraps a label, the underlying Wicket
 * {@link FormComponent}, and a feedback/hint message in Oat's standard field
 * markup. Wires accessibility automatically - the input's {@code aria-describedby}
 * points at the feedback message, and {@code aria-invalid}/{@code data-field="error"}
 * are toggled based on validation state - so subclasses get this for free.
 * <p>
 * It is meant to be used like a plain Wicket form component:
 * <ul>
 * <li>The model is this panel's model, which the input reads and writes. Leave it
 * out to inherit one from a parent {@code CompoundPropertyModel}, keyed by the id.</li>
 * <li>Leave the label out to look it up in the {@code .properties} files with the id
 * as the key (falling back to the id itself), as Wicket does for {@code ${label}} in
 * validation messages.</li>
 * <li>After an Ajax request that validated it, e.g. a submit, it shows an error that
 * appeared, changed or went away by itself, without {@code target.add(field)}: it updates
 * its message and the error state in place, so the input keeps its focus and what the
 * user typed. Other Ajax requests leave the error it shows alone.</li>
 * <li>The panel renders its own tag with a markup id, so it can be re-rendered with
 * {@code target.add(field)} too.</li>
 * <li>The wrapped form component is the panel's child {@code input}, so a test sets its
 * value with {@code formTester.setValue("email:input", ...)}; {@link #getInput()}
 * returns it.</li>
 * <li>Validators and {@link AjaxFormComponentUpdatingBehavior}s passed to
 * {@link #add(Behavior...)} are added to the input, as they only work there; any other
 * behavior applies to the panel's own tag. {@link #addToInput} adds one to the input,
 * e.g. an {@code AttributeModifier} for {@code autocomplete}.</li>
 * </ul>
 *
 * @param <T> the model object type
 * @param <C> the concrete {@link FormComponent} type this field wraps
 * @param <F> the concrete field type, returned by the fluent setters
 */
public abstract class BaseOatField<T, C extends FormComponent<T>, F extends BaseOatField<T, C, F>> extends GenericPanel<T> {

    /** The id of the wrapped form component, a child of this panel. */
    public static final String INPUT_ID = "input";

    private C input;
    protected final IModel<String> helperText;
    private final Label label;
    private final Label feedback;
    // What the page shows: the error in the feedback message (null for the hint), and
    // whether a form component inside a multi-input input showed its own error
    private String shownError;
    private boolean shownInnerError;
    private boolean feedbackRendered;
    // The fields whose input's model was updated in this request. Kept with the request,
    // not in a field: a FileUploadField sets its model to null as it is detached, after
    // this panel, which would count for the next request.
    private static final MetaDataKey<Set<BaseOatField<?, ?, ?>>> INPUT_UPDATED = new MetaDataKey<>() {
    };

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

        // The field's element in Oat's markup (a <section> or <fieldset>). Transparent, so
        // the label, input and feedback inside it are children of this panel, with short
        // paths such as "email:input".
        TransparentWebMarkupContainer container = new TransparentWebMarkupContainer("container");
        // Oat's magic: data-field="error" on the container (aria-invalid goes on the input)
        container.add(AttributeModifier.replace("data-field", (IModel<String>) () -> getInput().hasErrorMessage() ? "error" : ""));
        // Marks required fields for wicket-oat.css (an asterisk after the label)
        container.add(AttributeModifier.replace("data-required", (IModel<String>) () -> getInput().isRequired() ? "" : null));
        add(container);

        this.label = new Label("label", label != null ? label : new StringResourceModel(id, this).setDefaultValue(id));
        this.label.add(new Behavior() {
            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                // Only a <label> takes "for"; group fields label themselves with a <legend>
                if ("label".equalsIgnoreCase(tag.getName())) {
                    tag.put("for", getLabelTarget().getMarkupId());
                }
            }
        });
        add(this.label);

        feedback = new Label("feedback", this::getFeedbackContent) {
            @Override
            protected void onAfterRender() {
                super.onAfterRender();
                FeedbackMessage error = currentError();
                if (error != null) {
                    error.markRendered();
                }
                shownError = text(error);
                shownInnerError = hasInnerError();
                feedbackRendered = true;
            }
        };
        feedback.setOutputMarkupId(true);
        feedback.add(new HintBehavior()); // Add standard Oat hint styling
        add(feedback);
    }

    /**
     * Creates the wrapped form component. Called lazily - on the first {@link #getInput()}
     * or, at the latest, in {@link #onInitialize()} - so fields a subclass sets in its own
     * constructor are already available here.
     *
     * @param id the component id to use ({@link #INPUT_ID})
     * @param model the model to use; it reads and writes this panel's model
     */
    protected abstract C newInput(String id, IModel<T> model);

    /**
     * The wrapped form component, for what this field doesn't offer itself: e.g.
     * {@code setNullValid(true)} on a dropdown's, or a form validator comparing two
     * fields, {@code new EqualPasswordInputValidator(password.getInput(), repeat.getInput())}.
     */
    public C getInput() {
        if (input == null) {
            input = newInput(INPUT_ID, new InputModel());
            input.setLabel(getLabel());
            input.setOutputMarkupId(true);
            // Link input to feedback for screen readers
            input.add(AttributeModifier.replace("aria-describedby", (IModel<String>) feedback::getMarkupId));
            input.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () -> input.hasErrorMessage() ? "true" : "false"));
            // Not the native "required", which would make the browser block the submit
            // with its own message before Wicket's validation could show one inline
            input.add(AttributeModifier.replace("aria-required", (IModel<String>) () -> input.isRequired() ? "true" : null));
            add(input);
        }
        return input;
    }

    /**
     * What the {@code <label>} is for: the input, unless the user types somewhere else,
     * e.g. in the text box of a widget that keeps its value in a hidden input. Its markup
     * id must be rendered.
     */
    protected Component getLabelTarget() {
        return getInput();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getInput(); // create it now if no fluent call or subclass has done so yet
    }

    private String getFeedbackContent() {
        FeedbackMessage error = currentError();
        return error != null ? text(error) : hint();
    }

    private FeedbackMessage currentError() {
        return getInput().getFeedbackMessages().first(FeedbackMessage.ERROR);
    }

    private static String text(FeedbackMessage error) {
        return error != null ? String.valueOf(error.getMessage()) : null;
    }

    private String hint() {
        return helperText != null ? Objects.toString(helperText.getObject(), "") : "";
    }

    // Whether a form component inside the input, e.g. one of an OatCustomField's, has an error
    private boolean hasInnerError() {
        Boolean found = getInput().visitChildren(FormComponent.class, (Component inner, IVisit<Boolean> visit) -> {
            if (inner.hasErrorMessage()) {
                visit.stop(true);
            }
        });
        return found != null;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS)); // WicketOat.fieldFeedback
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        // Wicket broadcasts the AjaxRequestTarget to every component before responding,
        // and maybe more than once, when a handler sends it on
        if (!(event.getPayload() instanceof AjaxRequestTarget target) || !feedbackRendered || !isVisibleInHierarchy()) {
            return;
        }
        FeedbackMessage error = currentError();
        boolean innerError = hasInnerError();
        if (error == null && !innerError) {
            if (shownError == null && !shownInnerError) {
                return;
            }
            // Wicket removes the messages it showed at the end of each request, so no error
            // only means that the shown one went away if the input was validated in this one
            if (!isInputUpdated() && !validatedByForm()) {
                return;
            }
        }
        if (innerError || shownInnerError) {
            // Only a render sets aria-invalid on each inner input from its own error
            target.add(this);
            return;
        }
        String text = text(error);
        if (Objects.equals(text, shownError) || isRenderedBy(target)) {
            if (error != null) {
                error.markRendered(); // the same error is still shown
            }
            return;
        }
        // Not a render: that would lose the input's focus, what the user typed since, a
        // chosen file or a password
        if (error != null) {
            error.markRendered();
        }
        shownError = text;
        target.appendJavaScript("WicketOat.fieldFeedback(" + JSONObject.quote(getMarkupId()) + ","
                + JSONObject.quote(feedback.getMarkupId()) + "," + JSONObject.quote(error != null ? text : hint()) + ","
                + (error != null) + ")");
    }

    // Whether the input's form validated it in this request, which a button with
    // setDefaultFormProcessing(false) skips. On its own, by an Ajax behavior, a valid
    // input updates its model.
    private boolean validatedByForm() {
        Form<?> form = Form.findForm(getInput());
        if (form == null || !form.isSubmitted()) {
            return false;
        }
        IFormSubmittingComponent submitter = form.getRootForm().findSubmitter();
        return submitter == null || submitter.getDefaultFormProcessing();
    }

    // Whether this field is rendered with a component the target has, e.g. its form
    private boolean isRenderedBy(AjaxRequestTarget target) {
        for (Component added : target.getComponents()) {
            if (added == this || added instanceof MarkupContainer container && container.contains(this, true)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInputUpdated() {
        RequestCycle cycle = RequestCycle.get();
        Set<BaseOatField<?, ?, ?>> updated = cycle != null ? cycle.getMetaData(INPUT_UPDATED) : null;
        return updated != null && updated.contains(this);
    }

    private void inputUpdated() {
        RequestCycle cycle = RequestCycle.get();
        if (cycle == null) {
            return;
        }
        Set<BaseOatField<?, ?, ?>> updated = cycle.getMetaData(INPUT_UPDATED);
        if (updated == null) {
            updated = new HashSet<>();
            cycle.setMetaData(INPUT_UPDATED, updated);
        }
        updated.add(this);
    }

    /**
     * The markup id of the feedback/hint message, for a subclass whose input is made of
     * several inputs, so each can point {@code aria-describedby} at it.
     */
    protected final String getFeedbackMarkupId() {
        return feedback.getMarkupId();
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
     * Validators and {@link AjaxFormComponentUpdatingBehavior}s (which include
     * {@code OnChangeAjaxBehavior} and {@code AjaxFormChoiceComponentUpdatingBehavior})
     * only work on a form component, so they are added to the {@link #getInput() input}.
     * Any other behavior applies to this panel's own tag, as with any component; add
     * one to the input with {@link #addToInput}.
     */
    @Override
    public F add(Behavior... behaviors) {
        for (Behavior behavior : behaviors) {
            if (behavior instanceof IValidator<?> || behavior instanceof AjaxFormComponentUpdatingBehavior) {
                getInput().add(behavior);
            } else {
                super.add(behavior);
            }
        }
        return self();
    }

    /**
     * Adds behaviors to the {@link #getInput() input} rather than to this panel, e.g.
     * {@code addToInput(AttributeModifier.replace("autocomplete", "email"))}.
     */
    public F addToInput(Behavior... behaviors) {
        getInput().add(behaviors);
        return self();
    }

    public F addValidator(IValidator<? super T> validator) {
        getInput().add(validator);
        return self();
    }

    public F setRequired(boolean required) {
        getInput().setRequired(required);
        return self();
    }

    public F setLabel(IModel<String> label) {
        this.label.setDefaultModel(label);
        getInput().setLabel(label);
        return self();
    }

    public F setPlaceholder(String placeholder) {
        return setPlaceholder(Model.of(placeholder));
    }

    public F setPlaceholder(IModel<String> placeholder) {
        getInput().add(AttributeModifier.replace("placeholder", placeholder));
        return self();
    }

    /**
     * The input's model: reads and writes this panel's model, which may be one
     * inherited from a parent {@code CompoundPropertyModel}. Passes on that model's
     * object class too, so text fields can resolve their type for conversion.
     */
    private final class InputModel implements IObjectClassAwareModel<T> {

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
            inputUpdated();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<T> getObjectClass() {
            return getModel() instanceof IObjectClassAwareModel<?> aware ? (Class<T>) aware.getObjectClass() : null;
        }
    }
}
