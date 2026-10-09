package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.MenuItem;
import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.components.OatConfirmDialog;
import dev.jbaby.wicket.oat.components.OatDescriptionList;
import dev.jbaby.wicket.oat.components.OatPageHeader;
import dev.jbaby.wicket.oat.components.OatPopover;
import dev.jbaby.wicket.oat.components.OatSplitButton;
import dev.jbaby.wicket.oat.components.OatStatCard;
import dev.jbaby.wicket.oat.components.form.OatTextArea;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A customer's detail page: a page header with breadcrumb and actions, key figures,
 * the customer's details, and a quick-note form in a popover.
 */
public class CustomerPage extends BasePage {

    public static class Customer implements Serializable {
        public String name = "ACME GmbH";
        public String vatId = "ATU12345678";
        public String email = "billing@acme.example";
        public String phone = "+43 1 234 5678";
        public String city = "Vienna";
        public LocalDate customerSince = LocalDate.of(2019, 3, 1);
        public String accountManager = "Jordan Lee";
        public String notes;
    }

    public static class Note implements Serializable {
        public String type;
        public String text;
    }

    private final Customer customer = new Customer();
    private final Note note = new Note();
    private OatPopover quickNote;

    public CustomerPage() {
        add(Oat.Behaviors.feedbackToasts());
        OatConfirmDialog confirm = Oat.Components.confirmDialog("confirm");
        add(confirm);

        // Page header: the markup inside <header> holds the actions
        OatPageHeader header = Oat.Components.pageHeader("pageHeader", Model.of(customer.name))
                .setSubtitle("Customer since 2019 · " + customer.city)
                .setBreadcrumb(List.of(MenuItem.of("Home", HomePage.class), MenuItem.of("Invoices", DataTablePage.class)));
        add(header);

        quickNote = Oat.Components.popover("quickNote", "Quick note", id -> {
            Fragment fragment = new Fragment(id, "noteFragment", this);
            Form<Note> form = new Form<>("noteForm", new CompoundPropertyModel<>(note));
            fragment.add(form);
            // The browser suggests these as the user types; anything else is fine too
            form.add(new OatTextField<String>("type").setSuggestions(List.of("Call", "Meeting", "Email", "Complaint")));
            form.add(new OatTextArea<String>("text").setRequired(true));
            form.add(Oat.Components.submitButton("saveNote", "Save note", target -> {
                success("Note saved" + (note.type != null ? " (" + note.type + ")" : "") + ".");
                note.type = null;
                note.text = null;
                quickNote.close(target);
                target.add(fragment);
            }, target -> target.add(fragment)));
            return fragment;
        });
        header.add(quickNote);

        OatSplitButton newInvoice = Oat.Components.splitButton("newInvoice", "New invoice",
                target -> setResponsePage(InvoicePage.class));
        newInvoice.addAction("New quote", target -> info("A quote would open here."));
        newInvoice.addAction("New credit note", target -> info("A credit note would open here."));
        newInvoice.addAction("Archive customer", target ->
                confirm.ask(target, "Archive " + customer.name + "?", "Archived customers can't be invoiced.",
                        t -> success(customer.name + " archived."))).setVariant(OatVariant.DANGER);
        header.add(newInvoice);

        // Key figures
        add(new OatStatCard("revenue", Model.of("Revenue this year"), Model.of("€128,400"))
                .setChange(Model.of(12.5)).setHint(Model.of("vs. last year")));
        add(new OatStatCard("openInvoices", Model.of("Open invoices"), Model.of(4))
                .setChange(Model.of(33.3), false).setHint(Model.of("vs. last month")));
        add(new OatStatCard("daysToPay", Model.of("Average days to pay"), Model.of(18))
                .setChange(Model.of(-10), false).setHint(Model.of("vs. last year")));
        add(new OatStatCard("orders", Model.of("Orders"), Model.of(1270)).setChange(Model.of(0)));

        // Details, from the customer's properties; labels from CustomerPage.properties
        OatDescriptionList details = new OatDescriptionList("details", new CompoundPropertyModel<>(customer));
        details.addProperty("vatId")
                .addProperty("email")
                .addProperty("phone")
                .addProperty("city")
                .addProperty("customerSince")
                .addProperty("accountManager")
                .addItem(Model.of("Status"), id -> new OatBadge(id, "Active", OatVariant.SUCCESS))
                .addProperty("notes");
        add(details);

        // Recent activity, newest first
        add(Oat.Components.timeline("activity", Model.ofList(List.of(
                        new Activity("Payment received", LocalDateTime.of(2026, 10, 9, 14, 5), "INV-1019 · €2,612.50", OatVariant.SUCCESS),
                        new Activity("Invoice sent", LocalDateTime.of(2026, 10, 2, 9, 30), "INV-1019, by Jordan Lee", null),
                        new Activity("Reminder sent", LocalDateTime.of(2026, 9, 28, 8, 0), "INV-1005 is 12 days overdue", OatVariant.WARNING),
                        new Activity("Customer created", LocalDateTime.of(2019, 3, 1, 11, 15), null, null))),
                Activity::title, Activity::at)
                .setDescription(Activity::details)
                .setVariant(Activity::variant));
    }

    public record Activity(String title, LocalDateTime at, String details, OatVariant variant) implements Serializable {
    }
}
