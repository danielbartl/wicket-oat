package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.Alert;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class AlertPage extends BasePage {

    public AlertPage() {
        add(new Alert("defaultAlert"));
        
        add(new Alert("successAlert", AlertBehavior.Variant.SUCCESS));
        
        add(new Alert("warningAlert", AlertBehavior.Variant.WARNING));
        
        add(new Alert("errorAlert", AlertBehavior.Variant.ERROR));

        // You can also use the behavior on any component
        Label customLabel = new Label("customBehaviorAlert", "This is a Label with AlertBehavior attached.");
        customLabel.add(new AlertBehavior(AlertBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
