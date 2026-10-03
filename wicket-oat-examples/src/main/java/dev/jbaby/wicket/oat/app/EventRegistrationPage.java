package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatSubmitButton;
import dev.jbaby.wicket.oat.components.form.OatDropdownChoice;
import dev.jbaby.wicket.oat.components.form.OatEmailField;
import dev.jbaby.wicket.oat.components.form.OatSwitch;
import dev.jbaby.wicket.oat.components.form.OatTextArea;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class EventRegistrationPage extends BasePage {

    public static class RegistrationData implements Serializable {
        public String fullName;
        public String email;
        public String organization;
        public String ticketType = "Standard";
        public String dietaryRequirements;
        public boolean subscribeNewsletter = true;
    }

    public EventRegistrationPage() {
        // Show info()/success()/error() messages as toasts, on full renders and Ajax requests
        add(Oat.Behaviors.feedbackToasts());

        RegistrationData data = new RegistrationData();
        // Fields take their model from the CompoundPropertyModel by id, and their label
        // from EventRegistrationPage.properties by id - just like plain Wicket components.
        Form<RegistrationData> form = new Form<>("registrationForm", new CompoundPropertyModel<>(data));
        form.setOutputMarkupId(true);
        add(form);

        form.add(new OatTextField<String>("fullName")
                .setRequired(true)
                .setPlaceholder(Model.of("Enter your full name")));

        form.add(new OatEmailField("email")
                .setRequired(true)
                .setPlaceholder(Model.of("you@example.com")));

        form.add(new OatTextField<String>("organization")
                .setPlaceholder(Model.of("Where do you work?")));

        List<String> tickets = Arrays.asList("Early Bird", "Standard", "VIP", "Student");
        form.add(new OatDropdownChoice<String>("ticketType", Model.ofList(tickets))
                .setRequired(true));

        form.add(new OatTextArea<String>("dietaryRequirements")
                .setPlaceholder(Model.of("Allergies, preferences...")));

        form.add(new OatSwitch("subscribeNewsletter"));

        form.add(new OatSubmitButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                RegistrationData submittedData = form.getModelObject();
                success("Registration successful for " + submittedData.fullName + "!");
                target.add(form); // clear any inline errors from a previous attempt
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                error("Please fix the errors in the form."); // field errors themselves show inline
                target.add(form); // show the inline validation errors
            }
        });
    }
}
