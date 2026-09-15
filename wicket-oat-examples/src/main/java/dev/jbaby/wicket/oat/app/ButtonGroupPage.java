package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatButtonGroup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import java.util.List;

public class ButtonGroupPage extends BasePage {

    public ButtonGroupPage() {
        // Default Button Group - Oat only applies the connected/grouped button
        // styling to <li> children of a <menu class="buttons">, so OatButtonGroup
        // is a ListView driven by a model rather than a plain container of buttons.
        OatButtonGroup<String> group = Oat.Components.buttonGroup("group", Model.ofList(List.of("Left", "Center", "Right")), (item, label) -> {
            Link<Void> button = new Link<>("button") {
                @Override
                public void onClick() {}
            };
            button.add(new Label("label", Model.of(label)));
            button.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE));
            item.add(button);
        });
        add(group);
    }
}
