package dev.jbaby.wicket.oat;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BasePage extends AppLayout {

    @Override
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of(
                MenuItem.of("Home", HomePage.class),
                MenuItem.of("Users", UsersPage.class),
                MenuItem.of("Settings", SettingsPage.class)
        ));
    }
}
