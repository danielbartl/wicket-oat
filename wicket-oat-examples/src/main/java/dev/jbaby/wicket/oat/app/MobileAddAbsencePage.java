package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.app.DemoAbsences.Absence;
import dev.jbaby.wicket.oat.app.DemoAbsences.Type;
import dev.jbaby.wicket.oat.components.form.Html5Converters;
import dev.jbaby.wicket.oat.components.form.Html5TextField;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.time.LocalDate;

/**
 * The bar's "+": a form for adding an absence, built from Oat's markup alone - date
 * fields, radio cards and switch rows - with plain Wicket form components.
 */
public class MobileAddAbsencePage extends MobileBasePage {

    public MobileAddAbsencePage() {
        LocalDate tomorrow = today().plusDays(1);
        IModel<LocalDate> start = Model.of(tomorrow);
        IModel<LocalDate> end = Model.of(tomorrow);
        IModel<Type> type = Model.of(Type.VACATION);
        IModel<Boolean> tentative = Model.of(false);
        IModel<Boolean> halfDayStart = Model.of(false);
        IModel<Boolean> halfDayEnd = Model.of(false);

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                if (end.getObject().isBefore(start.getObject())) {
                    error("The last day can't be before the first.");
                    return;
                }
                DemoAbsences.add(new Absence("You", type.getObject(), start.getObject(), end.getObject(),
                        tentative.getObject(), halfDayStart.getObject(), halfDayEnd.getObject()));
                // A session message, so it shows on the next page
                getSession().success("Added your " + type.getObject().label.toLowerCase()
                        + (tentative.getObject() ? " as tentative." : "."));
                setResponsePage(MobileTodayPage.class);
            }
        };
        add(form);

        form.add(new Html5TextField<>("start", start, LocalDate.class, "date", Html5Converters.date()).setRequired(true));
        form.add(new Html5TextField<>("end", end, LocalDate.class, "date", Html5Converters.date()).setRequired(true));

        RadioGroup<Type> types = new RadioGroup<>("type", type);
        // A RadioGroup renders only its body by default; here its tag is the row of radio cards
        types.setRenderBodyOnly(false);
        types.add(new Radio<>("vacation", Model.of(Type.VACATION), types));
        types.add(new Radio<>("away", Model.of(Type.AWAY), types));
        form.add(types);

        form.add(new CheckBox("tentative", tentative));
        form.add(new CheckBox("halfDayStart", halfDayStart));
        form.add(new CheckBox("halfDayEnd", halfDayEnd));
    }
}
