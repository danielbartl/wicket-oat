package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An Oat form field for choosing several values from more than a list could hold -
 * the customers on a mailing, the tags of a product, the people on a team. The chosen
 * values show as chips with a remove button; a search box under them looks up more on
 * the server as the user types:
 * <pre>{@code
 * form.add(new OatMultiSelectField<User>("members", "Members", membersModel)
 *         .setChoices(text -> users.search(text))
 *         .setDisplay(User::name));
 * }</pre>
 * Choosing a suggestion adds it and empties the box, ready for the next one; values
 * already chosen aren't suggested again. The model is a {@code List} in the order they
 * were chosen; {@link #setRequired required} needs at least one. Built on Wicket's
 * {@link AutoCompleteTextField}, like {@link OatAutoCompleteField}; the chips are
 * Oat badges, the box is named by the field's label and each remove button by
 * {@code OatMultiSelectField.remove} ("Remove {0}").
 * <p>
 * Not inside a modal {@code OatDialog}: Wicket adds the suggestion list to the page's
 * {@code <body>}, and a modal dialog covers everything outside it.
 *
 * @param <T> the value type
 */
public class OatMultiSelectField<T> extends OatCustomField<List<T>> {

    private SerializableFunction<String, ? extends List<? extends T>> choices = text -> List.of();
    private SerializableFunction<? super T, String> display = String::valueOf;
    private int maxChoices = 10;

    /** Label looked up by {@code id} in the {@code .properties} files, model from a {@code CompoundPropertyModel}. */
    public OatMultiSelectField(String id) {
        super(id, ChipsInput::new);
    }

    public OatMultiSelectField(String id, String label, IModel<List<T>> model) {
        super(id, label, model, ChipsInput::new);
    }

    public OatMultiSelectField(String id, IModel<String> label, IModel<List<T>> model, IModel<String> helper) {
        super(id, label, model, helper, ChipsInput::new);
    }

    /** Finds the suggestions for what the user typed; at most {@link #setMaxChoices} are shown. */
    public OatMultiSelectField<T> setChoices(SerializableFunction<String, ? extends List<? extends T>> choices) {
        this.choices = Objects.requireNonNull(choices);
        return this;
    }

    /** The text for a value, in the suggestions and on its chip ({@code toString()} by default). */
    public OatMultiSelectField<T> setDisplay(SerializableFunction<? super T, String> display) {
        this.display = Objects.requireNonNull(display);
        return this;
    }

    /** The most suggestions shown at once (10 by default). */
    public OatMultiSelectField<T> setMaxChoices(int maxChoices) {
        if (maxChoices < 1) {
            throw new IllegalArgumentException("maxChoices must be at least 1: " + maxChoices);
        }
        this.maxChoices = maxChoices;
        return this;
    }

    /** The chips and the search box; the chosen values are kept on the server until the form is submitted. */
    static final class ChipsInput<T> extends FormComponentPanel<List<T>> {

        private List<T> chosen;
        private final WebMarkupContainer chips;
        private final AutoCompleteTextField<String> search;

        ChipsInput(String id, IModel<List<T>> model) {
            super(id, model);
            chips = new WebMarkupContainer("chips");
            chips.setOutputMarkupId(true);
            add(chips);
            chips.add(new ListView<>("chip", (IModel<List<T>>) this::chosen) {
                @Override
                protected void populateItem(ListItem<T> item) {
                    T value = item.getModelObject();
                    item.add(new Label("name", field().display.apply(value)));
                    AjaxLink<Void> remove = new AjaxLink<>("remove") {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            chosen().remove(value);
                            target.add(chips);
                            target.focusComponent(search);
                        }
                    };
                    remove.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatMultiSelectField.remove", this)
                            .setParameters(field().display.apply(value)).setDefaultValue("Remove")));
                    remove.add(new OatIcon("icon", "x"));
                    item.add(remove);
                }
            });

            AutoCompleteSettings settings = new AutoCompleteSettings()
                    .setCssClassName("oat-autocomplete")
                    .setThrottleDelay(300)
                    .setMinInputLength(1)
                    .setPreselect(true)
                    .setAdjustInputWidth(true)
                    .setMaxHeightInPx(320);
            search = new AutoCompleteTextField<>("search", Model.of(""), String.class, new AbstractAutoCompleteTextRenderer<>() {
                @Override
                protected String getTextValue(String text) {
                    return text;
                }
            }, settings) {
                @Override
                protected Iterator<String> getChoices(String input) {
                    return suggestions(input).stream().map(value -> field().display.apply(value)).iterator();
                }

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    tag.put("type", "text");
                    super.onComponentTag(tag);
                }
            };
            search.setOutputMarkupId(true);
            // A choice from the list ends with a change event; add it and get ready for the next
            search.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    String text = search.getModelObject();
                    T match = Strings.isEmpty(text) ? null : lookUp(text.strip());
                    if (match != null) {
                        chosen().add(match);
                        search.setModelObject("");
                        target.add(chips, search);
                        target.focusComponent(search);
                    }
                }
            });
            search.add(AttributeModifier.replace("aria-label", (IModel<String>) () -> {
                IModel<String> label = field().getLabel();
                return label == null ? null : label.getObject();
            }));
            search.add(AttributeModifier.replace("placeholder", new StringResourceModel("OatMultiSelectField.placeholder", this)
                    .setDefaultValue("Search...")));
            add(search);
        }

        @SuppressWarnings("unchecked")
        private OatMultiSelectField<T> field() {
            return findParent(OatMultiSelectField.class);
        }

        /** The chosen values, read from the model once and then changed by the chips and the box. */
        List<T> chosen() {
            if (chosen == null) {
                List<T> current = getModelObject();
                chosen = current == null ? new ArrayList<>() : new ArrayList<>(current);
            }
            return chosen;
        }

        private List<T> suggestions(String input) {
            List<? extends T> found = field().choices.apply(input);
            if (found == null) {
                return List.of();
            }
            return found.stream().filter(value -> !chosen().contains(value)).limit(field().maxChoices)
                    .map(value -> (T) value).toList();
        }

        /** The suggestion shown as exactly this text, or else ignoring case. */
        private T lookUp(String text) {
            T caseInsensitive = null;
            for (T choice : suggestions(text)) {
                String shown = field().display.apply(choice);
                if (text.equals(shown)) {
                    return choice;
                }
                if (caseInsensitive == null && text.equalsIgnoreCase(shown)) {
                    caseInsensitive = choice;
                }
            }
            return caseInsensitive;
        }

        @Override
        public void convertInput() {
            setConvertedInput(new ArrayList<>(chosen()));
        }

        @Override
        public boolean checkRequired() {
            return !isRequired() || !chosen().isEmpty();
        }

        @Override
        protected void onModelChanged() {
            super.onModelChanged();
            chosen = null; // read the new value
        }
    }
}
