package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.form.RadioChoice;

import java.util.List;

/**
 * An Oat-styled group of radio buttons ({@link RadioChoice}) in a {@code <fieldset>},
 * labelled by its {@code <legend>}. The choices are stacked; {@link #setInline} puts
 * them in a row.
 *
 * @param <T> the type of the choices
 */
public class OatRadioChoice<T> extends BaseOatField<T, RadioChoice<T>, OatRadioChoice<T>> {

    private final IModel<? extends List<? extends T>> choices;
    private final IChoiceRenderer<? super T> renderer;
    private boolean inline;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatRadioChoice(String id, IModel<? extends List<? extends T>> choices) {
        this(id, null, null, choices, null, null);
    }

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatRadioChoice(String id, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, null, null, choices, renderer, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatRadioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        this(id, null, model, choices, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatRadioChoice(String id, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, null, model, choices, renderer, null);
    }

    public OatRadioChoice(String id, String label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        this(id, Model.of(label), model, choices, null, null);
    }

    public OatRadioChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices) {
        this(id, label, model, choices, null, null);
    }

    public OatRadioChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer) {
        this(id, label, model, choices, renderer, null);
    }

    public OatRadioChoice(String id, IModel<String> label, IModel<T> model, IModel<? extends List<? extends T>> choices, IChoiceRenderer<? super T> renderer, IModel<String> helper) {
        super(id, label, model, helper);
        this.choices = choices;
        this.renderer = renderer;
    }

    /** Lays the choices out in a row instead of a column. */
    public OatRadioChoice<T> setInline(boolean inline) {
        this.inline = inline;
        return this;
    }

    @Override
    protected RadioChoice<T> createFormComponent(String id, IModel<T> model) {
        RadioChoice<T> choice = new RadioChoice<>(id, model, choices, renderer);
        // <label><input type="radio"> Text</label>, the markup Oat styles
        choice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_AFTER);
        choice.add(AttributeModifier.replace("class", (IModel<String>) () -> inline ? "hstack" : "vstack gap-2"));
        return choice;
    }
}
