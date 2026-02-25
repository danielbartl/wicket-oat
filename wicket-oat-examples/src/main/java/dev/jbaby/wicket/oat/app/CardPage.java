package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.components.OatCard;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class CardPage extends BasePage {

    public CardPage() {
        // Simple card with header, body and footer in markup
        OatCard simpleCard = new OatCard("simpleCard");
        add(simpleCard);

        simpleCard.add(new Label("title", "Project Alpha"));
        simpleCard.add(new Label("description", "A groundbreaking initiative to redefine UI development."));
        simpleCard.add(new Label("content", "This is the main body of the card. It can contain any Wicket component."));
        
        simpleCard.add(new Link<Void>("cancel") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior().setStyle(ButtonBehavior.Style.OUTLINE)));

        simpleCard.add(new Link<Void>("save") {
            @Override
            public void onClick() {}
        }.add(new ButtonBehavior()));
    }
}
