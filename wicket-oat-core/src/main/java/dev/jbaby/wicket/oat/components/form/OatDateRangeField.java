package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.ValidationError;

import java.time.LocalDate;

/**
 * An Oat-styled form field for a range of days - a report period, a booking, a filter
 * for "invoices due between" - as two native date inputs joined in Oat's input group,
 * bound to a {@link DateRange}.
 * <p>
 * Either end may be left empty for an open range; {@link #setRequired setRequired(true)}
 * asks for both. A range ending before it starts is rejected with the
 * {@code OatDateRangeField.order} message. The inputs are named "From" and "To" for
 * screen readers ({@code OatDateRangeField.from}/{@code .to} resources).
 */
public class OatDateRangeField extends BaseOatField<DateRange, OatDateRangeField.RangeInput, OatDateRangeField> {

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatDateRangeField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatDateRangeField(String id, IModel<DateRange> model) {
        this(id, null, model, null);
    }

    public OatDateRangeField(String id, String label, IModel<DateRange> model) {
        this(id, Model.of(label), model, null);
    }

    public OatDateRangeField(String id, IModel<String> label, IModel<DateRange> model) {
        this(id, label, model, null);
    }

    public OatDateRangeField(String id, IModel<String> label, IModel<DateRange> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected RangeInput createFormComponent(String id, IModel<DateRange> model) {
        return new RangeInput(id, model);
    }

    /** The two date inputs, converted together into a {@link DateRange}. */
    public final class RangeInput extends FormComponentPanel<DateRange> {

        private final Html5TextField<LocalDate> from;
        private final Html5TextField<LocalDate> to;

        RangeInput(String id, IModel<DateRange> model) {
            super(id, model);
            // The inputs show the range; the panel converts and stores it
            from = newInput("from", () -> getModelObject() != null ? getModelObject().from() : null, "OatDateRangeField.from", "From");
            to = newInput("to", () -> getModelObject() != null ? getModelObject().to() : null, "OatDateRangeField.to", "To");
            add(from, to);

            add((org.apache.wicket.validation.IValidator<DateRange>) validatable -> {
                DateRange range = validatable.getValue();
                if (range != null && range.from() != null && range.to() != null && range.to().isBefore(range.from())) {
                    validatable.error(new ValidationError().addKey("OatDateRangeField.order"));
                }
            });
        }

        private Html5TextField<LocalDate> newInput(String id, IModel<LocalDate> value, String nameKey, String defaultName) {
            Html5TextField<LocalDate> input = new Html5TextField<>(id, new IModel<>() {
                @Override
                public LocalDate getObject() {
                    return value.getObject();
                }

                @Override
                public void setObject(LocalDate object) {
                    // Stored as part of the range by the panel
                }
            }, LocalDate.class, "date", Html5Converters.date());
            input.add(AttributeModifier.replace("aria-label", new StringResourceModel(nameKey, this).setDefaultValue(defaultName)));
            input.add(AttributeModifier.replace("aria-describedby", (IModel<String>) OatDateRangeField.this::getFeedbackMarkupId));
            input.add(AttributeModifier.replace("aria-invalid", (IModel<String>) () -> hasErrorMessage() ? "true" : "false"));
            input.add(AttributeModifier.replace("aria-required", (IModel<String>) () -> isRequired() ? "true" : null));
            return input;
        }

        @Override
        public void convertInput() {
            LocalDate start = from.getConvertedInput();
            LocalDate end = to.getConvertedInput();
            setConvertedInput(start == null && end == null ? null : new DateRange(start, end));
        }

        /** Required means both ends. */
        @Override
        public boolean checkRequired() {
            return !isRequired() || (!Strings.isEmpty(from.getInput()) && !Strings.isEmpty(to.getInput()));
        }
    }
}
