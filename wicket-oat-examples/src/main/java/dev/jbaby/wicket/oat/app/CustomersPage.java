package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.components.OatDescriptionList;
import dev.jbaby.wicket.oat.components.OatMasterDetail;
import dev.jbaby.wicket.oat.components.OatMessageList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An OatMasterDetail: customers on the left, the selected one's details and notes on the
 * right (an OatMessageList with an OatMessageInput). On a phone, one at a time.
 */
public class CustomersPage extends BasePage {

    public record Customer(long id, String name, String city, String email, String manager, boolean active) implements Serializable {
    }

    public record Note(String author, String text, LocalDateTime at) implements Serializable {
    }

    private static final List<Customer> CUSTOMERS = List.of(
            new Customer(1, "ACME GmbH", "Vienna", "billing@acme.example", "Jordan Lee", true),
            new Customer(2, "Globex", "Berlin", "ap@globex.example", "Sam Rivera", true),
            new Customer(3, "Initech", "Munich", "finance@initech.example", "Jordan Lee", false),
            new Customer(4, "Umbrella", "Zurich", "accounts@umbrella.example", "Alex Kim", true));

    private final Map<Long, List<Note>> notes = new HashMap<>();

    public CustomersPage() {
        notes.put(1L, new ArrayList<>(List.of(
                new Note("Jordan Lee", "Asked for the Q3 invoices to be sent as one PDF.", LocalDateTime.of(2026, 10, 2, 9, 30)),
                new Note("Alex Kim", "Paid INV-1019.\nThanks for the quick turnaround!", LocalDateTime.of(2026, 10, 9, 14, 5)))));

        OatMasterDetail<Customer> customers = new OatMasterDetail<>("customers", new Model<>());
        add(customers);

        customers.setMaster(id -> {
            Fragment list = new Fragment(id, "listFragment", this);
            list.setOutputMarkupId(true);
            list.add(new ListView<>("customer", CUSTOMERS) {
                @Override
                protected void populateItem(ListItem<Customer> item) {
                    Customer customer = item.getModelObject();
                    AjaxLink<Void> link = new AjaxLink<>("link") {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            customers.select(target, customer);
                            target.add(list); // moves the selection mark
                        }
                    };
                    link.add(AttributeModifier.replace("aria-current", (IModel<String>) () -> customers.isSelected(customer) ? "true" : null));
                    link.add(new Label("name", customer.name()), new Label("city", customer.city()));
                    item.add(link);
                }
            });
            return list;
        });

        customers.setDetail((id, customer) -> {
            Fragment detail = new Fragment(id, "detailFragment", this);
            Customer c = customer.getObject();
            detail.add(new Label("name", c.name()));
            detail.add(new OatDescriptionList("details")
                    .addItem("City", Model.of(c.city()))
                    .addItem("Billing email", Model.of(c.email()))
                    .addItem("Account manager", Model.of(c.manager()))
                    .addItem(Model.of("Status"), badgeId -> new OatBadge(badgeId, c.active() ? "Active" : "Inactive",
                            c.active() ? OatVariant.SUCCESS : OatVariant.SECONDARY)));

            OatMessageList<Note> list = Oat.Components.messageList("notes",
                    (IModel<List<Note>>) () -> notes.getOrDefault(c.id(), List.of()), Note::author, Note::text, Note::at);
            detail.add(list);
            detail.add(Oat.Components.messageInput("addNote", (target, text) -> {
                notes.computeIfAbsent(c.id(), key -> new ArrayList<>()).add(new Note("You", text, LocalDateTime.now().withNano(0)));
                target.add(list);
            }));
            return detail;
        });
    }
}
