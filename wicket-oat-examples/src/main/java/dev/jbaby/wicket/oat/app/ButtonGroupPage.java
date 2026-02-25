package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatButtonGroup;
import org.apache.wicket.markup.html.link.Link;

public class ButtonGroupPage extends BasePage {

    public ButtonGroupPage() {
        // Default Button Group
        OatButtonGroup group = new OatButtonGroup("group");
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
