package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatAppLayout;
import dev.jbaby.wicket.oat.components.MenuItem;
import dev.jbaby.wicket.oat.components.OatThemeSwitcher;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BasePage extends OatAppLayout {

    @Override
    protected @NonNull IModel<String> appNameModel() {
        return Model.of("Wicket Oat Demo");
    }

    @Override
    protected Component createFooter(String id) {
        return new OatThemeSwitcher(id);
    }

    @Override
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(
                MenuItem.of("Home", HomePage.class),
                MenuItem.of("Getting Started", GettingStartedPage.class),
                MenuItem.of("Event Registration (Form Demo)", EventRegistrationPage.class),
                MenuItem.of("Accordion", AccordionPage.class),
                MenuItem.of("Alert", AlertPage.class),
                MenuItem.of("Badge", BadgePage.class),
                MenuItem.of("Button", ButtonPage.class),
                MenuItem.of("Button Group", ButtonGroupPage.class),
                MenuItem.of("Card", CardPage.class),
                MenuItem.of("Forms & Feedback", FormPage.class),
                MenuItem.of("Table (Simple)", TablePage.class),
                MenuItem.of("Data Table (Advanced)", DataTablePage.class),
                MenuItem.of("More Components", ComponentsPage.class),
                MenuItem.of("Theme", ThemePage.class)
        ));
    }
}
