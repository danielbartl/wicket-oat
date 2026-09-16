package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.time.LocalDateTime;

/**
 * An Oat-styled form field wrapping a native {@code <input type="datetime-local">},
 * bound to a {@link LocalDateTime} model.
 */
public class OatDateTimeLocalField extends BaseOatField<LocalDateTime, Html5TextField<LocalDateTime>> {

    public OatDateTimeLocalField(String id, String label, IModel<LocalDateTime> model) {
        this(id, Model.of(label), model, null);
    }

    public OatDateTimeLocalField(String id, IModel<String> label, IModel<LocalDateTime> model) {
        this(id, label, model, null);
    }

    public OatDateTimeLocalField(String id, IModel<String> label, IModel<LocalDateTime> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<LocalDateTime> createFormComponent(String id, IModel<LocalDateTime> model) {
        return new Html5TextField<>(id, model, LocalDateTime.class, "datetime-local");
    }
}
