package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.ButtonBehavior;
import dev.jbaby.wicket.oat.components.ButtonGroup;
import org.apache.wicket.markup.html.link.Link;

public class ButtonGroupPage extends BasePage {

    public ButtonGroupPage() {
        // Default Button Group
        ButtonGroup group = new ButtonGroup("group");
        add(group);

        group.add(new Link<Void>("left") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));

        group.add(new Link<Void>("center") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));

        group.add(new Link<Void>("right") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));
    }
}
