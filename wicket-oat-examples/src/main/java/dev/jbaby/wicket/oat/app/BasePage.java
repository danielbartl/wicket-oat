package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.AppLayout;
import dev.jbaby.wicket.oat.components.MenuItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BasePage extends AppLayout {

    @Override
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(
                MenuItem.of("Home", HomePage.class),
                MenuItem.of("Accordion", AccordionPage.class),
                MenuItem.of("Alert", AlertPage.class),
                MenuItem.of("Badge", BadgePage.class),
                MenuItem.of("Button", ButtonPage.class),
                MenuItem.of("Button Group", ButtonGroupPage.class),
                MenuItem.of("Card", CardPage.class),
                MenuItem.of("Forms & Feedback", FormPage.class),
                MenuItem.of("Table", TablePage.class),
                MenuItem.of("More Components", ComponentsPage.class),
                MenuItem.of("Theme", ThemePage.class)
        ));
    }
}
