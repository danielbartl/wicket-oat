package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatAlert;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class AlertPage extends BasePage {

    public AlertPage() {
        add(new OatAlert("defaultAlert"));
        
        add(new OatAlert("successAlert", AlertBehavior.Variant.SUCCESS));
        
        add(new OatAlert("warningAlert", AlertBehavior.Variant.WARNING));
        
        add(new OatAlert("errorAlert", AlertBehavior.Variant.ERROR));

        // You can also use the behavior on any component
        Label customLabel = new Label("customBehaviorAlert", "This is a Label with AlertBehavior attached.");
        customLabel.add(new AlertBehavior(AlertBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
