package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatDropdown;
import dev.jbaby.wicket.oat.components.OatUserMenu;
import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.model.Model;

import java.util.List;

public class DropdownPage extends BasePage {

    public DropdownPage() {
        List<String> items = List.of("Profile", "Settings", "Logout");

        // Show feedback messages as toasts
        add(Oat.Behaviors.feedbackToasts());

        // Each item runs the handler over Ajax; the menu closes afterwards
        add(OatDropdown.of("dropdown", "Options", Model.ofList(items), item -> item,
                (target, item) -> info("You chose " + item + ".")));

        // The top bar's user menu, here with a picture; signing out only says so
        OatUserMenu user = new OatUserMenu("userMenu", Model.of("Grace Hopper")) {
            @Override
            protected void signOut() {
                info("Grace would be signed out now.");
            }

            @Override
            protected void onSignedOut() {
                // stay on this page
            }
        };
        user.setDetail(Model.of("grace@example.com"));
        user.setPicture(Model.of("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=96&h=96"));
        user.addLink("Inbox", InboxPage.class).setIcon("mail");
        user.addLink("Theme", ThemePage.class).setIcon("settings");
        add(user);
    }
}
