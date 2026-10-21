package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;
import java.util.Objects;

/**
 * A row of chips choosing one value, e.g. a status above a list of orders: "All",
 * "Open", "Paid", "Overdue". The chosen chip is filled. Choosing one sets the model and
 * runs {@link #onChange} over Ajax, e.g. to re-render the list:
 * <pre>{@code
 * add(OatFilter.onChange("status", status, Model.ofList(List.of(Status.values())),
 *         target -> target.add(orders))
 *         .setChoiceRenderer(new EnumChoiceRenderer<>(this))
 *         .setNullValid(true));
 * }</pre>
 * on {@code <div wicket:id="status"></div>}. The chips are radio buttons in a
 * {@code <fieldset>}, so screen readers know them as a group of choices and the arrow keys
 * move between them; the group is named by the {@code .properties} files with the id as
 * the key, falling back to the id, or by {@link #setLabel}. Its own form makes it work
 * anywhere, also inside a page's form or an {@link OatDrawer}.
 *
 * @param <T> the type of the choices
 */
public class OatFilter<T> extends Panel {

    private final RadioGroup<T> group;
    private final IModel<? extends List<? extends T>> choices;
    private IChoiceRenderer<? super T> renderer = new ChoiceRenderer<>();
    private boolean nullValid;
    private IModel<String> label;

    /**
     * @param model the chosen value, {@code null} for none
     * @param choices the values to choose from, read on every render
     */
    public OatFilter(String id, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        super(id, model);
        this.choices = Objects.requireNonNull(choices);
        setOutputMarkupId(true);

        Form<Void> form = new Form<>("form");
        add(form);

        group = new RadioGroup<>("group", model);
        group.add(AttributeModifier.replace("aria-label", (IModel<String>) () -> getLabel().getObject()));
        group.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Not re-rendered: the browser has checked the chosen chip, which fills it,
                // and new radios would lose the focus the arrow keys move
                onChange(target);
            }
        });
        form.add(group);

        // "All": no value
        WebMarkupContainer none = new WebMarkupContainer("none") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(nullValid);
            }
        };
        none.add(new Radio<>("radio", (IModel<T>) () -> null, group));
        none.add(new Label("label", new StringResourceModel("OatFilter.all", this).setDefaultValue("All")));
        group.add(none);

        group.add(new ListView<T>("chips", (IModel<List<T>>) () -> {
            List<? extends T> list = this.choices.getObject();
            return list != null ? List.copyOf(list) : List.of();
        }) {
            @Override
            protected void populateItem(ListItem<T> item) {
                item.add(new Radio<>("radio", item.getModel(), group));
                item.add(new Label("label", () -> renderer.getDisplayValue(item.getModelObject())));
            }
        });
    }

    /**
     * Called after a chip was chosen, with the model already set; e.g. to re-render the
     * list it filters.
     */
    protected void onChange(AjaxRequestTarget target) {
    }

    private IModel<String> getLabel() {
        return label != null ? label : new StringResourceModel(getId(), this).setDefaultValue(getId());
    }

    /** The chips' text, e.g. {@code new EnumChoiceRenderer<>(this)} for translated enum values. */
    public OatFilter<T> setChoiceRenderer(IChoiceRenderer<? super T> renderer) {
        this.renderer = Objects.requireNonNull(renderer);
        return this;
    }

    /**
     * Whether a first chip chooses no value, {@code null}; named by the {@code OatFilter.all}
     * resource ("All"). Off by default.
     */
    public OatFilter<T> setNullValid(boolean nullValid) {
        this.nullValid = nullValid;
        return this;
    }

    /** The name of the group for screen readers, instead of the one looked up by id. */
    public OatFilter<T> setLabel(IModel<String> label) {
        this.label = label;
        return this;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        choices.detach();
        if (label != null) {
            label.detach();
        }
    }

    /**
     * A filter without a subclass: {@code onChange} runs after a chip was chosen, e.g.
     * {@code target -> target.add(orders)}.
     */
    public static <T> OatFilter<T> onChange(String id, IModel<T> model, IModel<? extends List<? extends T>> choices,
                                            SerializableConsumer<AjaxRequestTarget> onChange) {
        Objects.requireNonNull(onChange);
        return new OatFilter<T>(id, model, choices) {
            @Override
            protected void onChange(AjaxRequestTarget target) {
                onChange.accept(target);
            }
        };
    }
}
