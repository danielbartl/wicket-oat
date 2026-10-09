package dev.jbaby.wicket.oat.components;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.List;

/**
 * An OatAppLayout page with menu groups and badges. Its "id" parameter picks the current
 * page among the menu's links; the page model is a CompoundPropertyModel, which the
 * badges must not inherit from.
 */
public class MenuGroupsPage extends OatAppLayout {

    public static int unread = 3;

    public static class Bean implements Serializable {
    }

    public MenuGroupsPage(PageParameters parameters) {
        super(parameters);
        setDefaultModel(new CompoundPropertyModel<>(new Bean()));
    }

    private static PageParameters id(int id) {
        return new PageParameters().add("id", id);
    }

    @Override
    protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(
                MenuItem.of("Home", MenuGroupsPage.class, id(1)),
                MenuItem.group("Sales",
                        MenuItem.of("Orders", MenuGroupsPage.class, id(2)).withBadge(() -> unread),
                        MenuItem.of("Invoices", MenuGroupsPage.class, id(3)).withBadge("New")),
                MenuItem.group("Admin",
                        MenuItem.of("Secret", ParamLayoutPage.SecretPage.class))));
    }
}
