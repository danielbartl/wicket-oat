package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class AlertPage extends BasePage {

    public AlertPage() {
        add(Oat.Components.alert("defaultAlert"));
        
        add(Oat.Components.alert("successAlert", AlertBehavior.Variant.SUCCESS));
        
        add(Oat.Components.alert("warningAlert", AlertBehavior.Variant.WARNING));
        
        add(Oat.Components.alert("errorAlert", AlertBehavior.Variant.ERROR));

        // You can also use the behavior on any component
        Label customLabel = new Label("customBehaviorAlert", "This is a Label with AlertBehavior attached.");
        customLabel.add(Oat.Behaviors.alert(AlertBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
