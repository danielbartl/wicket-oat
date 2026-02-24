package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.time.LocalDate;

public class OatDateField extends BaseOatField<LocalDate, Html5TextField<LocalDate>> {

    public OatDateField(String id, String label, IModel<LocalDate> model) {
        this(id, Model.of(label), model, null);
    }

    public OatDateField(String id, IModel<String> label, IModel<LocalDate> model) {
        this(id, label, model, null);
    }

    public OatDateField(String id, IModel<String> label, IModel<LocalDate> model, IModel<String> helper) {
        super(id, label, model, helper);
    }

    @Override
    protected Html5TextField<LocalDate> createFormComponent(String id, IModel<LocalDate> model) {
        return new Html5TextField<>(id, model, LocalDate.class, "date");
    }
}
