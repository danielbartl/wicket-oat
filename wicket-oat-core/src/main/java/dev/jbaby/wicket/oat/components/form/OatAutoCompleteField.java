package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.ValidationError;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * An Oat-styled form field that looks up its value on the server as the user types -
 * picking a customer, a product or an account from more than a dropdown could hold:
 * <pre>{@code
 * form.add(new OatAutoCompleteField<Customer>("customer")
 *         .setChoices(text -> customers.search(text))      // called as the user types
 *         .setDisplay(Customer::name));                     // shown in the list and the field
 * }</pre>
 * Built on Wicket's {@link AutoCompleteTextField}, with a suggestion list styled like
 * Oat's dropdown menu. On submit, the text must be one of the suggestions for it, and
 * the field's value is that object; otherwise the {@code OatAutoCompleteField.noMatch}
 * message asks the user to choose one. {@link #setFreeText} accepts other text too,
 * e.g. {@code setFreeText(text -> text)} for a {@code String} field where the
 * suggestions only help typing.
 *
 * @param <T> the model object type
 */
public class OatAutoCompleteField<T> extends BaseOatInputField<T, AutoCompleteTextField<T>, OatAutoCompleteField<T>> {

    private SerializableFunction<String, ? extends List<? extends T>> choices = text -> List.of();
    private SerializableFunction<? super T, String> display = String::valueOf;
    private SerializableFunction<String, ? extends T> freeText;
    private int maxChoices = 10;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatAutoCompleteField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatAutoCompleteField(String id, IModel<T> model) {
        this(id, null, model, null);
    }

    public OatAutoCompleteField(String id, String label, IModel<T> model) {
        this(id, Model.of(label), model, null);
    }

    public OatAutoCompleteField(String id, IModel<String> label, IModel<T> model) {
        this(id, label, model, null);
    }

    public OatAutoCompleteField(String id, IModel<String> label, IModel<T> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    /** Finds the suggestions for what the user typed; at most {@link #setMaxChoices} are shown. */
    public OatAutoCompleteField<T> setChoices(SerializableFunction<String, ? extends List<? extends T>> choices) {
        this.choices = Objects.requireNonNull(choices);
        return this;
    }

    /** The text for a value, in the list and in the field ({@code toString()} by default). */
    public OatAutoCompleteField<T> setDisplay(SerializableFunction<? super T, String> display) {
        this.display = Objects.requireNonNull(display);
        return this;
    }

    /** Accepts text that isn't one of the suggestions, turning it into a value. */
    public OatAutoCompleteField<T> setFreeText(SerializableFunction<String, ? extends T> freeText) {
        this.freeText = freeText;
        return this;
    }

    /** The most suggestions shown at once (10 by default). */
    public OatAutoCompleteField<T> setMaxChoices(int maxChoices) {
        if (maxChoices < 1) {
            throw new IllegalArgumentException("maxChoices must be at least 1: " + maxChoices);
        }
        this.maxChoices = maxChoices;
        return this;
    }

    @Override
    protected AutoCompleteTextField<T> createFormComponent(String id, IModel<T> model) {
        AutoCompleteSettings settings = new AutoCompleteSettings()
                .setCssClassName("oat-autocomplete")
                .setThrottleDelay(300)
                .setMinInputLength(1)
                .setPreselect(true)
                .setAdjustInputWidth(true)
                .setMaxHeightInPx(320);
        AbstractAutoCompleteTextRenderer<T> renderer = new AbstractAutoCompleteTextRenderer<>() {
            @Override
            protected String getTextValue(T object) {
                return display.apply(object);
            }
        };
        return new AutoCompleteTextField<>(id, model, null, renderer, settings) {
            @Override
            protected Iterator<T> getChoices(String input) {
                List<? extends T> found = choices.apply(input);
                return found == null ? List.<T>of().iterator()
                        : found.stream().limit(maxChoices).map(choice -> (T) choice).iterator();
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "text");
                super.onComponentTag(tag);
            }

            @Override
            public void convertInput() {
                String text = getInput();
                if (Strings.isEmpty(text)) {
                    setConvertedInput(null);
                    return;
                }
                T match = lookUp(text.strip());
                if (match == null && freeText != null) {
                    match = freeText.apply(text.strip());
                }
                if (match == null) {
                    error(new ValidationError().addKey("OatAutoCompleteField.noMatch").setVariable("input", text));
                } else {
                    setConvertedInput(match);
                }
            }

            @Override
            protected IConverter<?> createConverter(Class<?> type) {
                return new DisplayConverter();
            }
        };
    }

    /** The suggestion shown as exactly this text, or else ignoring case. */
    private T lookUp(String text) {
        List<? extends T> found = choices.apply(text);
        if (found == null) {
            return null;
        }
        T caseInsensitive = null;
        for (T choice : found) {
            String shown = display.apply(choice);
            if (text.equals(shown)) {
                return choice;
            }
            if (caseInsensitive == null && text.equalsIgnoreCase(shown)) {
                caseInsensitive = choice;
            }
        }
        return caseInsensitive;
    }

    /** Shows the model object in the field by its display text. */
    private final class DisplayConverter implements IConverter<Object> {

        @Override
        @SuppressWarnings("unchecked")
        public String convertToString(Object value, Locale locale) {
            return value == null ? null : display.apply((T) value);
        }

        @Override
        public Object convertToObject(String value, Locale locale) {
            return lookUp(value);
        }
    }
}
