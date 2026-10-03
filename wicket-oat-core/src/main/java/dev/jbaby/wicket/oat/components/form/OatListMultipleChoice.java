package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.form.ListMultipleChoice;

import java.util.Collection;
import java.util.List;

/**
 * An Oat-styled multi-select ({@code <select multiple>}, {@link ListMultipleChoice}),
 * bound to a collection of the selected choices.
 *
 * @param <T> the type of the choices
 */
public class OatListMultipleChoice<T> extends BaseOatField<Collection<T>, ListMultipleChoice<T>, OatListMultipleChoice<T>> {

    private final IModel<? extends List<? extends T>> choices;
    private final IChoiceRenderer<? super T> renderer;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatListMultipleChoice(String id, IModel<? extends List<? extends T>> choices) {
        this(id, null, null, choices, null, null);
    }

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatListMultipleChoice(String id, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, null, null, choices, renderer, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatListMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
        this(id, null, model, choices, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatListMultipleChoice(String id, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, null, model, choices, renderer, null);
    }

    public OatListMultipleChoice(String id, String label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
        this(id, Model.of(label), model, choices, null, null);
    }

    public OatListMultipleChoice(String id, IModel<String> label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices) {
        this(id, label, model, choices, null, null);
    }

    public OatListMultipleChoice(String id, IModel<String> label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, label, model, choices, renderer, null);
    }

    @SuppressWarnings("unchecked")
    public OatListMultipleChoice(String id, IModel<String> label, IModel<? extends Collection<T>> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer, IModel<String> helper) {
        super(id, label, (IModel<Collection<T>>) model, helper);
        this.choices = choices;
        this.renderer = renderer;
    }

    /** The number of visible rows of the list. */
    public OatListMultipleChoice<T> setMaxRows(int maxRows) {
        getField().setMaxRows(maxRows);
        return this;
    }

    @Override
    protected ListMultipleChoice<T> createFormComponent(String id, IModel<Collection<T>> model) {
        return new ListMultipleChoice<>(id, model, choices, renderer);
    }
}
