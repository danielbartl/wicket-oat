package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.components.OatIcon;
import dev.jbaby.wicket.oat.components.form.ShowsErrorsInline;
import org.apache.wicket.Component;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A data table column whose cells can be edited in place - a quantity, a price, a
 * name. A cell shows its value as a button; clicking it (or Enter/Space) turns it into
 * a text field with Save and Cancel. Enter saves, Escape cancels.
 * <pre>{@code
 * columns.add(new OatEditableColumn<Line, String, Integer>(Model.of("Quantity"), "quantity",
 *                 Line::quantity, Line::setQuantity)
 *         .setType(Integer.class)
 *         .setRequired(true)
 *         .addValidator(RangeValidator.minimum(1))
 *         .onSave((target, line) -> { lines.save(line); target.add(totals); }));
 * }</pre>
 * The typed text is converted to the column's type and validated; an invalid value
 * shows its error under the field and isn't set. A valid one is set on the row with
 * the setter, then {@link #onSave} runs, e.g. to store it. Focus goes back to the cell
 * afterwards, so keyboard users can move on. The value is exported to CSV like any
 * {@link LambdaColumn}.
 *
 * @param <T> the row type
 * @param <S> the sort property type
 * @param <V> the value type
 */
public class OatEditableColumn<T, S, V> extends LambdaColumn<T, S> {

    private final SerializableFunction<T, V> getter;
    private final SerializableBiConsumer<T, V> setter;
    private final List<IValidator<? super V>> validators = new ArrayList<>();
    private Class<V> type;
    private boolean required;
    private SerializableBiConsumer<AjaxRequestTarget, T> onSave = (target, row) -> { };

    public OatEditableColumn(IModel<String> displayModel, SerializableFunction<T, V> getter, SerializableBiConsumer<T, V> setter) {
        this(displayModel, null, getter, setter);
    }

    public OatEditableColumn(IModel<String> displayModel, S sortProperty, SerializableFunction<T, V> getter,
                             SerializableBiConsumer<T, V> setter) {
        super(displayModel, sortProperty, getter::apply);
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
    }

    /** The value's type, for converting the typed text, e.g. {@code Integer.class}; text by default. */
    public OatEditableColumn<T, S, V> setType(Class<V> type) {
        this.type = type;
        return this;
    }

    public OatEditableColumn<T, S, V> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public OatEditableColumn<T, S, V> addValidator(IValidator<? super V> validator) {
        validators.add(Objects.requireNonNull(validator));
        return this;
    }

    /** Runs after a valid value was set on the row, e.g. to store the row. */
    public OatEditableColumn<T, S, V> onSave(SerializableBiConsumer<AjaxRequestTarget, T> onSave) {
        this.onSave = Objects.requireNonNull(onSave);
        return this;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new EditableCell(componentId, rowModel));
    }

    final class EditableCell extends Panel implements ShowsErrorsInline {

        private final IModel<T> row;

        EditableCell(String id, IModel<T> row) {
            super(id);
            this.row = row;
            setOutputMarkupId(true);
            add(view());
        }

        @Override
        public boolean showsErrorsOf(Component reporter) {
            return reporter.getParent() instanceof Form<?> && "input".equals(reporter.getId()) && reporter.findParent(EditableCell.class) == this;
        }

        private Fragment view() {
            Fragment view = new Fragment("content", "viewFragment", this);
            AjaxLink<Void> edit = new AjaxLink<>("edit") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    Fragment editor = editor();
                    EditableCell.this.replace(editor);
                    target.add(EditableCell.this);
                    target.focusComponent(editor.get("form:input"));
                }
            };
            edit.setOutputMarkupId(true);
            IModel<V> value = () -> getter.apply(row.getObject());
            edit.add(new Label("value", () -> value.getObject() == null ? "\u2014" : value.getObject()) {
                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);
                    if (value.getObject() == null) {
                        tag.append("class", "text-light", " ");
                    }
                }
            });
            edit.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatEditableColumn.edit", this)
                    .setParameters(getDisplayModel(), (IModel<String>) () -> shownText(value.getObject()))
                    .setDefaultValue("Edit")));
            view.add(edit);
            return view;
        }

        /** The value as the cell shows it, for the button's name. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private String shownText(Object value) {
            if (value == null) {
                return "";
            }
            IConverter converter = getConverter(value.getClass());
            String text = converter.convertToString(value, getLocale());
            return text == null ? String.valueOf(value) : text;
        }

        private Fragment editor() {
            Fragment editor = new Fragment("content", "editFragment", this);
            Form<Void> form = new Form<>("form");
            form.add(AttributeModifier.replace("data-oat-escape", "cancel"));
            editor.add(form);

            TextField<V> input = new TextField<>("input", new IModel<>() {
                @Override
                public V getObject() {
                    return getter.apply(row.getObject());
                }

                @Override
                public void setObject(V value) {
                    setter.accept(row.getObject(), value);
                }
            }, type);
            input.setOutputMarkupId(true);
            input.setRequired(required);
            input.setLabel(getDisplayModel());
            validators.forEach(input::add);
            input.add(AttributeModifier.replace("aria-label", getDisplayModel()));
            input.add(AttributeModifier.replace("aria-invalid", () -> input.hasErrorMessage() ? "true" : null));
            input.add(AttributeModifier.replace("aria-describedby", () -> input.hasErrorMessage() ? input.getMarkupId() + "-error" : null));
            if (type != null && Number.class.isAssignableFrom(type)) {
                input.add(AttributeModifier.replace("inputmode", "decimal"));
            }
            form.add(input);

            Label error = new Label("error", () -> input.hasErrorMessage()
                    ? input.getFeedbackMessages().first(FeedbackMessage.ERROR).getMessage() : null) {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(input.hasErrorMessage());
                }
            };
            error.add(AttributeModifier.replace("id", () -> input.getMarkupId() + "-error"));
            form.add(error);

            AjaxButton save = new AjaxButton("save", form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    onSave.accept(target, row.getObject());
                    showView(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target) {
                    target.add(EditableCell.this);
                    target.focusComponent(input);
                }
            };
            save.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatEditableColumn.save", this).setDefaultValue("Save")));
            save.add(new OatIcon("icon", "check"));
            form.add(save);
            form.setDefaultButton(save);

            AjaxLink<Void> cancel = new AjaxLink<>("cancel") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    showView(target);
                }
            };
            cancel.add(AttributeModifier.replace("data-oat-cancel", ""));
            cancel.add(AttributeModifier.replace("aria-label", new StringResourceModel("OatEditableColumn.cancel", this).setDefaultValue("Cancel")));
            cancel.add(new OatIcon("icon", "x"));
            form.add(cancel);
            return editor;
        }

        private void showView(AjaxRequestTarget target) {
            Fragment view = view();
            replace(view);
            target.add(this);
            target.focusComponent(view.get("edit"));
        }

        @Override
        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS)); // Escape cancels
        }
    }
}
