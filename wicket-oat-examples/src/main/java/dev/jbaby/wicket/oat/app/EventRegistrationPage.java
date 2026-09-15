package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.OatToastBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
        RegistrationData data = new RegistrationData();
        CompoundPropertyModel<RegistrationData> model = new CompoundPropertyModel<>(data);
        Form<RegistrationData> form = new Form<>("registrationForm", model);
        add(form);

        form.add(Oat.Components.textField("fullName", "Full Name", model.bind("fullName"))
                .setRequired(true)
                .setPlaceholder(Model.of("Enter your full name")));

        form.add(Oat.Components.emailField("email", "Email Address", model.bind("email"))
                .setRequired(true)
                .setPlaceholder(Model.of("you@example.com")));

        form.add(Oat.Components.textField("organization", "Organization / Company", model.bind("organization"))
                .setPlaceholder(Model.of("Where do you work?")));

        List<String> tickets = Arrays.asList("Early Bird", "Standard", "VIP", "Student");
        form.add(Oat.Components.dropdownChoice("ticketType", "Ticket Type", model.bind("ticketType"), Model.ofList(tickets))
                .setRequired(true));

        form.add(Oat.Components.textArea("dietaryRequirements", "Dietary Requirements", model.bind("dietaryRequirements"))
                .setPlaceholder(Model.of("Allergies, preferences...")));

        form.add(Oat.Components.oatSwitch("subscribeNewsletter", "Subscribe to Newsletter", model.bind("subscribeNewsletter")));

        form.add(new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                RegistrationData submittedData = form.getModelObject();
                Oat.toast(target, "Registration successful for " + submittedData.fullName + "!", OatToastBehavior.Variant.SUCCESS, "Success");
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                Oat.toast(target, "Please fix the errors in the form.", OatToastBehavior.Variant.DANGER, "Error");
            }
        }.add(Oat.Behaviors.button()));
    }
}
