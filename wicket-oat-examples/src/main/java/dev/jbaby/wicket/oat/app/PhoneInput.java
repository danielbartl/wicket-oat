package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.form.Html5TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

/**
 * A phone number as country code and number - an input of the application's own, made
 * of two inputs and shown with OatCustomField. Its value is e.g. "+43 1 234 5678".
 */
public class PhoneInput extends FormComponentPanel<String> {

    private static final List<String> CODES = List.of("+43", "+49", "+41", "+44", "+1");

    private final DropDownChoice<String> code;
    private final Html5TextField<String> number;

    public PhoneInput(String id, IModel<String> model) {
        super(id, model);
        code = new DropDownChoice<>("code", Model.of(CODES.get(0)), CODES);
        code.setNullValid(false);
        number = new Html5TextField<>("number", new Model<>(), String.class, "tel");
        add(code, number);
    }

    /** Shows the stored number split into its parts. */
    @Override
    protected void onBeforeRender() {
        String phone = getModelObject();
        if (phone != null && phone.contains(" ")) {
            code.setModelObject(phone.substring(0, phone.indexOf(' ')));
            number.setModelObject(phone.substring(phone.indexOf(' ') + 1));
        }
        super.onBeforeRender();
    }

    @Override
    public void convertInput() {
        String digits = number.getConvertedInput();
        setConvertedInput(digits == null || digits.isBlank() ? null : code.getConvertedInput() + " " + digits.strip());
    }
}
