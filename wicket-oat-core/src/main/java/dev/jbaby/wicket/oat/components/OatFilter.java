package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;

import java.util.ArrayList;
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
 * on {@code <div wicket:id="status"></div>}. The chips are the radio buttons of a Wicket
 * {@link RadioChoice} in a {@code <fieldset>}, so screen readers know them as a group of
 * choices and the arrow keys move between them; the group is named by the
 * {@code .properties} files with the id as the key, falling back to the id, or by
 * {@link #setLabel}. Its own form makes it work anywhere, also inside a page's form or an
 * {@link OatDrawer}.
 * <p>
 * As in a {@code RadioChoice}, a chip stands for its value by the renderer's id. Strings,
 * numbers and enums are their own ids by default; give other values a renderer with an id,
 * e.g. {@code new ChoiceRenderer<>("name", "id")}, so a chip still means the same value
 * when the choices change between showing the chips and a click. Without one they go by
 * their place in the list, as with Wicket's {@code ChoiceRenderer}.
 *
 * @param <T> the type of the choices
 */
public class OatFilter<T> extends GenericPanel<T> {

    private final IModel<? extends List<? extends T>> choices;
    // The choices without null, read once per request
    private final IModel<List<T>> values;
    private IChoiceRenderer<? super T> renderer = new ValueRenderer<>();
    private boolean nullValid;
    private IModel<String> label;

    /**
     * @param model the chosen value, {@code null} for none; or {@code null} to inherit one
     *              from a parent {@code CompoundPropertyModel} by the filter's id
     * @param choices the values to choose from, read once per request; a {@code null} in
     *                them is left out ({@link #setNullValid} adds a chip for no value)
     */
    public OatFilter(String id, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        super(id, model);
        this.choices = Objects.requireNonNull(choices);
        values = new LoadableDetachableModel<List<T>>() {
            @Override
            protected List<T> load() {
                List<T> list = new ArrayList<>();
                List<? extends T> given = OatFilter.this.choices.getObject();
                if (given != null) {
                    for (T value : given) {
                        if (value != null) {
                            list.add(value);
                        }
                    }
                }
                return list;
            }
        };
        setOutputMarkupId(true);

        Form<Void> form = new Form<>("form");
        add(form);

        // "All", for no value, first
        IModel<List<T>> chips = () -> {
            List<T> list = new ArrayList<>();
            if (nullValid) {
                list.add(null);
            }
            list.addAll(values.getObject());
            return list;
        };
        RadioChoice<T> choice = new RadioChoice<T>("choice", new ChosenModel(), chips, new ChipRenderer()) {
            @Override
            protected IValueMap getAdditionalAttributesForLabel(int index, T value) {
                ValueMap attributes = new ValueMap();
                attributes.put("class", "oat-filter-chip");
                return attributes;
            }

            // Without "All", a value must be chosen, also by a request made up or out of date
            @Override
            public boolean isRequired() {
                return !nullValid;
            }
        };
        choice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_AFTER);
        choice.setLabel(new StringResourceModel(id, this).setDefaultValue(id));
        choice.add(AttributeModifier.replace("aria-label", (IModel<String>) () -> getLabel().getObject()));
        choice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Not re-rendered: the browser has checked the chosen chip, which fills it,
                // and new radios would lose the focus the arrow keys move
                onChange(target);
            }
        });
        form.add(choice);
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

    private List<T> values() {
        return values.getObject();
    }

    /**
     * The chips' text and the values' ids, e.g. {@code new EnumChoiceRenderer<>(this)} for
     * translated enum values, or {@code new ChoiceRenderer<>("name", "id")}.
     */
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
        this.label = wrap(label);
        return this;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS)); // Enter on a chip
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        values.detach();
        choices.detach();
        if (label != null) {
            label.detach();
        }
    }

    /** The filter's own model, also one inherited from a {@code CompoundPropertyModel} by its id. */
    private final class ChosenModel implements IModel<T> {

        @Override
        public T getObject() {
            return getModelObject();
        }

        @Override
        public void setObject(T object) {
            IModel<T> model = getModel();
            if (model == null) {
                throw new IllegalStateException("Filter '" + getPageRelativePath() + "' has no model: pass one "
                        + "to its constructor, or add it to a container with a CompoundPropertyModel");
            }
            model.setObject(object);
        }
    }

    /** The given renderer for the values, and "All" for no value, whose id is "". */
    private final class ChipRenderer implements IChoiceRenderer<T> {

        @Override
        public Object getDisplayValue(T object) {
            return object != null ? renderer.getDisplayValue(object) : getString("OatFilter.all", null, "All");
        }

        // The index among the values, which is what the renderer is given to find one by
        @Override
        public String getIdValue(T object, int index) {
            return object != null ? renderer.getIdValue(object, values().indexOf(object)) : "";
        }

        @Override
        @SuppressWarnings("unchecked")
        public T getObject(String id, IModel<? extends List<? extends T>> choices) {
            return Strings.isEmpty(id) ? null : (T) renderer.getObject(id, values);
        }
    }

    /** Strings, numbers and enums are their own ids; other values go by their place in the list. */
    private static final class ValueRenderer<T> extends ChoiceRenderer<T> {

        @Override
        public String getIdValue(T object, int index) {
            if (object instanceof Enum<?> value) {
                return value.name();
            }
            if (object instanceof CharSequence || object instanceof Number || object instanceof Boolean) {
                return object.toString();
            }
            return super.getIdValue(object, index);
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
