package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.List;

public class AccordionPage extends BasePage {

    record AccordionData(String title, String content) implements Serializable {}

    public AccordionPage() {

        List<AccordionData> data = List.of(
                new AccordionData("Accordion Item 1", "This is the content for the first item. It's hidden by default."),
                new AccordionData("Accordion Item 2", "This is the second item. You can add any Wicket component inside the content."),
                new AccordionData("Accordion Item 3", "Third item content. Note how opening one closes the others due to the exclusive group behavior.")
        );

        add(Oat.Components.accordion("accordion", Model.ofList(data), (item, model) -> {
            item.add(new Label("title", model.title()));
            item.add(new Label("content", model.content()));
        }));
    }
}
