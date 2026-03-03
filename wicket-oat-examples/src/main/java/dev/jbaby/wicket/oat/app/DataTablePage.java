package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataTablePage extends BasePage {

    public record User(Long id, String name, String email, String role) implements Serializable {}

    public DataTablePage() {
        List<User> users = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            users.add(new User(i, "User " + i, "user" + i + "@example.com", i % 2 == 0 ? "Admin" : "User"));
        }

        List<IColumn<User, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn<>(Model.of("ID"), "id", "id"));
        columns.add(new PropertyColumn<>(Model.of("Name"), "name", "name"));
        columns.add(new PropertyColumn<>(Model.of("Email"), "email", "email"));
        columns.add(new PropertyColumn<>(Model.of("Role"), "role", "role"));

        UserDataProvider dataProvider = new UserDataProvider(users);

        add(Oat.Components.dataTable("dataTable", columns, dataProvider, 10));
        
        // Empty table demo
        List<IColumn<User, String>> emptyColumns = new ArrayList<>(columns);
        add(Oat.Components.dataTable("emptyDataTable", emptyColumns, new UserDataProvider(List.of()), 10)
                .setEmptyState(Oat.Components.emptyState("placeholder", Model.of("No users found"), Model.of("Try adjusting your filters or adding a new user."))));
    }

    private static class UserDataProvider extends SortableDataProvider<User, String> {
        private final List<User> users;

        public UserDataProvider(List<User> users) {
            this.users = users;
        }

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            List<User> data = new ArrayList<>(users);
            if (getSort() != null) {
                data.sort((u1, u2) -> {
                    int mul = getSort().isAscending() ? 1 : -1;
                    if ("name".equals(getSort().getProperty())) return mul * u1.name().compareTo(u2.name());
                    if ("email".equals(getSort().getProperty())) return mul * u1.email().compareTo(u2.email());
                    if ("role".equals(getSort().getProperty())) return mul * u1.role().compareTo(u2.role());
                    return mul * u1.id().compareTo(u2.id());
                });
            }
            return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
        }

        @Override
        public long size() {
            return users.size();
        }

        @Override
        public IModel<User> model(User object) {
            return Model.of(object);
        }
    }
}
