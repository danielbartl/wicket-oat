package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.AlertBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class AlertPage extends BasePage {

    public AlertPage() {
        add(Oat.Components.alert("defaultAlert", "This is a default alert message."));
        
        add(Oat.Components.alert("successAlert", "Success! Your operation was completed successfully.", OatVariant.SUCCESS));
        
        add(Oat.Components.alert("warningAlert", "Warning! There might be some issues with your input.", OatVariant.WARNING));
        
        add(Oat.Components.alert("errorAlert", "Error! Something went wrong while processing your request.", OatVariant.DANGER));

        // You can also use the behavior on any component
        Label customLabel = new Label("customBehaviorAlert", "This is a Label with AlertBehavior attached.");
        customLabel.add(Oat.Behaviors.alert(OatVariant.SUCCESS));
        add(customLabel);
    }
}
