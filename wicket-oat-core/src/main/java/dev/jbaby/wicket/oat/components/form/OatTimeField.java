package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.time.LocalTime;

/**
 * An Oat-styled form field wrapping a native {@code <input type="time">},
 * bound to a {@link LocalTime} model.
 */
public class OatTimeField extends BaseOatField<LocalTime, Html5TextField<LocalTime>> {

    public OatTimeField(String id, String label, IModel<LocalTime> model) {
        this(id, Model.of(label), model, null);
    }

    public OatTimeField(String id, IModel<String> label, IModel<LocalTime> model) {
        this(id, label, model, null);
    }

    public OatTimeField(String id, IModel<String> label, IModel<LocalTime> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<LocalTime> createFormComponent(String id, IModel<LocalTime> model) {
        return new Html5TextField<>(id, model, LocalTime.class, "time");
    }
}
