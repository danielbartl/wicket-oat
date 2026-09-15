package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import java.util.List;

public class DropdownPage extends BasePage {

    public DropdownPage() {
        List<String> items = List.of("Profile", "Settings", "Logout");

        add(Oat.Components.dropdown("dropdown", "Options", Model.ofList(items), (item, label) ->
                item.add(new Label("label", label))));
    }
}
