package dev.jbaby.wicket.oat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import java.time.Instant;

public class HomePage extends BasePage {

    public HomePage() {
        add(new Label("message", "Wicket is running!"));

        final var now = new Label("now", Model.of(Instant.now().toString()));
        now.setOutputMarkupId(true);
        add(now);

        Form<Void> timeForm = new Form<>("timeForm");
        add(timeForm);

        timeForm.add(new AjaxButton("refreshTime", timeForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                now.setDefaultModelObject(Instant.now().toString());
                target.add(now);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                // no validation here, so this should not happen
            }
        });
    }

}