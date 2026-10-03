package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.model.Model;

import java.util.List;

public class DropdownPage extends BasePage {

    public DropdownPage() {
        List<String> items = List.of("Profile", "Settings", "Logout");

        // Show feedback messages as toasts
        add(Oat.Behaviors.feedbackToasts());

        // Each item runs the handler over Ajax; the menu closes afterwards
        add(Oat.Components.dropdown("dropdown", "Options", Model.ofList(items), item -> item,
                (target, item) -> info("You chose " + item + ".")));
    }
}
