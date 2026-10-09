package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.MenuItem;
import dev.jbaby.wicket.oat.components.OatAppLayout;
import dev.jbaby.wicket.oat.components.OatThemeSwitcher;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BasePage extends OatAppLayout {

    private static final CssResourceReference EXAMPLES_CSS = new CssResourceReference(BasePage.class, "examples.css");

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(EXAMPLES_CSS));
    }

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
                MenuItem.group("Demos",
                        MenuItem.of("Event Registration", EventRegistrationPage.class),
                        MenuItem.of("Invoice", InvoicePage.class),
                        MenuItem.of("Customer", CustomerPage.class),
                        MenuItem.of("New Order (Wizard)", OrderWizardPage.class),
                        // A badge that follows the data: AppShellPage re-renders the sidebar after a delete
                        MenuItem.of("App Shell", AppShellPage.class).withBadge(() -> DemoTasks.get().size())),
                MenuItem.group("Components",
                        MenuItem.of("Accordion", AccordionPage.class),
                        MenuItem.of("Alert", AlertPage.class),
                        MenuItem.of("Badge", BadgePage.class),
                        MenuItem.of("Breadcrumb", BreadcrumbPage.class),
                        MenuItem.of("Button", ButtonPage.class),
                        MenuItem.of("Button Group", ButtonGroupPage.class),
                        MenuItem.of("Card", CardPage.class),
                        MenuItem.of("Dialog", DialogPage.class),
                        MenuItem.of("Dropdown", DropdownPage.class),
                        MenuItem.of("Pagination", PaginationPage.class),
                        MenuItem.of("Tabs", TabsPage.class),
                        MenuItem.of("More Components", ComponentsPage.class)),
                MenuItem.group("Forms & Data",
                        MenuItem.of("Forms & Feedback", FormPage.class),
                        MenuItem.of("Upload", UploadPage.class),
                        MenuItem.of("Table (Simple)", TablePage.class),
                        MenuItem.of("Data Table (Advanced)", DataTablePage.class)),
                MenuItem.of("Theme", ThemePage.class)
        ));
    }
}
