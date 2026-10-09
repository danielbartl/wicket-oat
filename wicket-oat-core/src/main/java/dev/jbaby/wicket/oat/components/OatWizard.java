package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A form filled in over several steps - onboarding, an order, an application - with a
 * row of numbered steps showing where the user is, and Back, Next and Finish buttons:
 * <pre>{@code
 * OatWizard wizard = new OatWizard("wizard") {
 *     @Override
 *     protected void onFinish(AjaxRequestTarget target) {
 *         orders.place(order);
 *         setResponsePage(OrderPlacedPage.class);
 *     }
 * };
 * wizard.addStep("Customer", id -> new CustomerStep(id, order));
 * wizard.addStep("Items", id -> new ItemsStep(id, order));
 * wizard.addStep("Review", id -> new ReviewStep(id, order));
 * add(wizard);
 * }</pre>
 * on {@code <div wicket:id="wizard"></div>}. Each step is a component (usually a panel
 * of Oat form fields) created when the step is shown, so keep the data in a model all
 * steps share, like {@code order} above. Next validates the current step's fields and
 * shows their errors inline; Back and the numbers of earlier steps go back without
 * validating. {@link #onNext} can check a step further and keep the user on it.
 * <p>
 * The buttons' texts are the {@code OatWizard.back}, {@code OatWizard.next} and
 * {@code OatWizard.finish} resources, and the step list is named by
 * {@code OatWizard.steps} for screen readers.
 */
public abstract class OatWizard extends Panel {

    private static final String BODY_ID = "body";

    private final List<Step> steps = new ArrayList<>();
    private final Form<Void> form;
    private final AjaxButton next;
    private final AjaxButton finish;
    private int current;

    public OatWizard(String id) {
        super(id);
        setOutputMarkupId(true);

        add(new ListView<>("steps", (IModel<List<Integer>>) () -> IntStream.range(0, steps.size()).boxed().toList()) {
            @Override
            protected void populateItem(ListItem<Integer> item) {
                int index = item.getModelObject();
                item.add(AttributeModifier.replace("data-state", index < current ? "done" : index == current ? "current" : "upcoming"));
                item.add(AttributeModifier.replace("aria-current", index == current ? "step" : null));
                AjaxLink<Void> link = new AjaxLink<>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        showStep(index, target);
                    }
                };
                // Only finished steps can be revisited; the others need Next and its validation
                link.setEnabled(index < current);
                link.add(new Label("number", String.valueOf(index + 1)));
                link.add(new Label("title", steps.get(index).title()));
                item.add(link);
            }
        });

        form = new Form<>("form") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // Enter in a field moves on, like clicking Next - or Finish on the last step
                setDefaultButton(current < steps.size() - 1 ? next : finish);
            }
        };
        add(form);
        form.add(new Label(BODY_ID)); // replaced by the current step on render

        form.add(new AjaxLink<Void>("back") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                showStep(current - 1, target);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(current > 0);
            }
        }.setBody(new StringResourceModel("OatWizard.back", this).setDefaultValue("Back")));

        next = new AjaxButton("next", new StringResourceModel("OatWizard.next", this).setDefaultValue("Next")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                onNext(current, target);
                if (!hasErrors()) {
                    showStep(current + 1, target);
                } else {
                    target.add(OatWizard.this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(OatWizard.this);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(current < steps.size() - 1);
            }
        };
        form.add(next);

        finish = new AjaxButton("finish", new StringResourceModel("OatWizard.finish", this).setDefaultValue("Finish")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                onNext(current, target);
                if (!hasErrors()) {
                    onFinish(target);
                } else {
                    target.add(OatWizard.this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(OatWizard.this);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(current == steps.size() - 1);
            }
        };
        form.add(finish);
    }

    /** Adds a step; the factory creates its component with the id it is given. */
    public OatWizard addStep(String title, SerializableFunction<String, ? extends Component> step) {
        return addStep(Model.of(title), step);
    }

    /** Adds a step; the factory creates its component with the id it is given. */
    public OatWizard addStep(IModel<String> title, SerializableFunction<String, ? extends Component> step) {
        steps.add(new Step(title, step));
        return this;
    }

    /** The index of the step shown, from 0. */
    public int getCurrentStep() {
        return current;
    }

    /** Shows a step, e.g. to start somewhere else than at the first. */
    public OatWizard setCurrentStep(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException("No step " + index + " of " + steps.size());
        }
        current = index;
        showCurrent();
        return this;
    }

    private void showStep(int index, AjaxRequestTarget target) {
        setCurrentStep(index);
        target.add(this);
    }

    private void showCurrent() {
        Component body = steps.get(current).factory().apply(BODY_ID);
        if (body == null || !BODY_ID.equals(body.getId())) {
            throw new IllegalArgumentException("A wizard step must use the id passed to its factory (\""
                    + BODY_ID + "\"), but was " + (body == null ? "null" : "\"" + body.getId() + "\""));
        }
        form.addOrReplace(body);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (steps.isEmpty()) {
            throw new IllegalStateException("An OatWizard needs at least one step");
        }
        if (form.get(BODY_ID) instanceof Label) {
            showCurrent();
        }
    }

    /**
     * Called when Next (or Finish) is clicked and the step's fields are valid. Report an
     * error on a component - e.g. {@code emailField.getField().error("Already registered")}
     * - to keep the user on the step.
     *
     * @param step the index of the step being left, from 0
     */
    protected void onNext(int step, AjaxRequestTarget target) {
    }

    /** Called when Finish is clicked on the last step and its fields are valid. */
    protected abstract void onFinish(AjaxRequestTarget target);

    private boolean hasErrors() {
        return !new FeedbackCollector(this).collect(FeedbackMessage::isError).isEmpty();
    }

    private record Step(IModel<String> title, SerializableFunction<String, ? extends Component> factory) implements Serializable {
    }
}
