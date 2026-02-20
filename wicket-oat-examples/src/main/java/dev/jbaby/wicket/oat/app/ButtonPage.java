package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.ButtonBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class ButtonPage extends BasePage {

    public ButtonPage() {
        // Simple links styled as buttons
        add(new Link<Void>("defaultButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior()));

        add(new Link<Void>("secondaryButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior(ButtonBehavior.Variant.SECONDARY)));

        add(new Link<Void>("dangerButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior(ButtonBehavior.Variant.DANGER)));

        // Outline and Ghost styles
        add(new Link<Void>("outlineButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));

        add(new Link<Void>("ghostButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.GHOST)));

        // Sizes
        add(new Link<Void>("smallButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setSize(ButtonBehavior.Size.SMALL)));

        add(new Link<Void>("largeButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setSize(ButtonBehavior.Size.LARGE)));

        // Icon button
        add(new Link<Void>("iconButton") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setIcon(true)));
    }
}
