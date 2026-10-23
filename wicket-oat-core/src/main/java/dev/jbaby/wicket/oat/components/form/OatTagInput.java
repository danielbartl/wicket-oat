package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.behaviors.TagInputBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A tag/chip input field backed by Oat's {@code ot-taginput} web component, bound to
 * a {@code List<String>} of tags. Tags are trimmed and de-duplicated; no tags is an
 * empty list (and fails {@link #setRequired(boolean) required}). As in Oat's widget,
 * a comma separates tags, so a tag can't contain one. Text typed but not yet made a
 * tag with Enter or a comma is submitted as one too.
 * <p>
 * The tags are submitted in a hidden input, {@link #getInput()}; the label, the hint or
 * error and {@link #setPlaceholder the placeholder} belong to the box the user types in.
 */
public class OatTagInput extends BaseOatField<List<String>, TextField<List<String>>, OatTagInput> {

    private final WebMarkupContainer text;

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatTagInput(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatTagInput(String id, IModel<List<String>> model) {
        this(id, null, model, null);
    }

    public OatTagInput(String id, String label, IModel<List<String>> model) {
        this(id, Model.of(label), model, null);
    }

    public OatTagInput(String id, IModel<String> label, IModel<List<String>> model) {
        this(id, label, model, null);
    }

    public OatTagInput(String id, IModel<String> label, IModel<List<String>> model, IModel<String> helper) {
        super(id, label, model, helper);

        WebMarkupContainer taginput = new WebMarkupContainer("taginput");
        taginput.add(new TagInputBehavior(getInput()));
        add(taginput);

        // The box the user types in, which screen readers announce, rather than the hidden input
        text = new WebMarkupContainer("text");
        text.setOutputMarkupId(true);
        text.add(AttributeModifier.replace("aria-describedby", (IModel<String>) this::getFeedbackMarkupId));
        text.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () -> getInput().hasErrorMessage() ? "true" : "false"));
        text.add(AttributeModifier.replace("aria-required", (IModel<String>) () -> getInput().isRequired() ? "true" : null));
        taginput.add(text);
    }

    @Override
    protected Component getLabelTarget() {
        return text;
    }

    /** The text in the box while it is empty ({@code OatTagInput.placeholder}, "Add tags...", by default). */
    @Override
    public OatTagInput setPlaceholder(IModel<String> placeholder) {
        text.add(AttributeModifier.replace("placeholder", placeholder));
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected TextField<List<String>> newInput(String id, IModel<List<String>> model) {
        TextField<List<String>> hidden = new TextField<>(id, model, (Class) List.class) {
            @Override
            protected String[] getInputTypes() {
                return new String[] { "hidden" };
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("type", "hidden");
                super.onComponentTag(tag);
            }

            @Override
            protected IConverter<?> createConverter(Class<?> type) {
                return List.class.isAssignableFrom(type) ? TAGS : super.createConverter(type);
            }
        };
        // Pass "" (no tags) to the converter, which makes it an empty list rather than null
        hidden.setConvertEmptyInputStringToNull(false);
        return hidden;
    }

    /** Converts between the tag list and the comma-separated value the widget script syncs. */
    private static final IConverter<List<String>> TAGS = new IConverter<>() {
        @Override
        public List<String> convertToObject(String value, Locale locale) {
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        @Override
        public String convertToString(List<String> tags, Locale locale) {
            return tags == null ? "" : String.join(",", tags);
        }
    };
}
