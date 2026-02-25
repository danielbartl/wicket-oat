package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TablePage extends BasePage {

    public record User(String name, String email, String role, String status) implements Serializable {}

    public TablePage() {
        List<User> users = Arrays.asList(
                new User("Alice Johnson", "alice@example.com", "Admin", "Active"),
                new User("Bob Smith", "bob@example.com", "Editor", "Active"),
                new User("Carol White", "carol@example.com", "Viewer", "Pending")
        );

        add(new ListView<>("rows", users) {
            @Override
            protected void populateItem(ListItem<User> item) {
                User user = item.getModelObject();
                item.add(new Label("name", user.name()));
                item.add(new Label("email", user.email()));
                item.add(new Label("role", user.role()));
                
                OatBadge statusBadge = new OatBadge("status", user.status());
                if ("Active".equals(user.status())) {
                    statusBadge.add(new BadgeBehavior(BadgeBehavior.Variant.SUCCESS));
                } else {
                    statusBadge.add(new BadgeBehavior(BadgeBehavior.Variant.SECONDARY));
                }
                item.add(statusBadge);
            }
        });
    }
}
