package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatConfirmDialog;
import dev.jbaby.wicket.oat.components.OatDataTable;
import dev.jbaby.wicket.oat.components.form.OatSearchField;
import dev.jbaby.wicket.oat.components.table.OatActionsColumn;
import dev.jbaby.wicket.oat.components.table.OatBadgeColumn;
import dev.jbaby.wicket.oat.components.table.OatBooleanColumn;
import dev.jbaby.wicket.oat.components.table.OatDateColumn;
import dev.jbaby.wicket.oat.components.table.OatLinkColumn;
import dev.jbaby.wicket.oat.components.table.OatNumberColumn;
import dev.jbaby.wicket.oat.components.table.OatSelectionColumn;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.SetModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An invoices table using the Oat columns, row actions, selection with bulk actions,
 * and a search toolbar; plus an empty table with a placeholder.
 */
public class DataTablePage extends BasePage {

    public enum Status { OPEN, PAID, OVERDUE }

    public record Invoice(long id, String number, String customer, Status status, BigDecimal total,
                          Currency currency, LocalDate due, boolean exported) implements Serializable {

        Invoice paid() {
            return new Invoice(id, number, customer, Status.PAID, total, currency, due, exported);
        }

        // Equal by id, like an entity, so a selected invoice stays selected when it changes
        @Override
        public boolean equals(Object other) {
            return other instanceof Invoice invoice && invoice.id == id;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }
    }

    private final List<Invoice> invoices = new ArrayList<>();
    private final IModel<String> search = new Model<>();
    private final IModel<Set<Invoice>> selected = new SetModel<>(new HashSet<>());

    public DataTablePage() {
        add(Oat.Behaviors.feedbackToasts());

        String[] customers = {"ACME GmbH", "Globex", "Initech", "Umbrella", "Stark Industries", "Wayne Enterprises"};
        Currency[] currencies = {Currency.getInstance("EUR"), Currency.getInstance("USD"), Currency.getInstance("GBP")};
        for (int i = 1; i <= 23; i++) {
            Status status = i % 5 == 0 ? Status.OVERDUE : i % 2 == 0 ? Status.PAID : Status.OPEN;
            invoices.add(new Invoice(i, String.format("INV-%04d", 1000 + i), customers[i % customers.length], status,
                    BigDecimal.valueOf(137_50L * i, 2), currencies[i % currencies.length],
                    LocalDate.of(2026, 10, 1).plusDays(i * 3L), i % 3 == 0));
        }

        OatConfirmDialog confirm = Oat.Components.confirmDialog("confirm");
        add(confirm);

        List<IColumn<Invoice, String>> columns = new ArrayList<>();
        columns.add(new OatSelectionColumn<>(selected));
        columns.add(OatLinkColumn.toPage(Model.of("Number"), "number", Invoice::number,
                InvoicePage.class, invoice -> new PageParameters().add("id", invoice.id())));
        columns.add(OatLinkColumn.onClick(Model.of("Customer"), "customer", Invoice::customer,
                (target, invoice) -> info("Customer: " + invoice.customer())));
        columns.add(new OatBadgeColumn<>(Model.of("Status"), "status", invoice -> invoice.status().name().charAt(0)
                + invoice.status().name().substring(1).toLowerCase(Locale.ROOT),
                invoice -> switch (invoice.status()) {
                    case PAID -> OatVariant.SUCCESS;
                    case OVERDUE -> OatVariant.DANGER;
                    case OPEN -> OatVariant.SECONDARY;
                }));
        columns.add(new OatNumberColumn<Invoice, String>(Model.of("Total"), "total", Invoice::total)
                .setCurrency(Invoice::currency));
        columns.add(new OatDateColumn<>(Model.of("Due"), "due", Invoice::due));
        columns.add(new OatBooleanColumn<>(Model.of("Exported"), Invoice::exported));

        OatActionsColumn<Invoice, String> actions = new OatActionsColumn<>();
        columns.add(actions);

        OatDataTable<Invoice, String> table = new OatDataTable<>("dataTable", columns, new InvoiceProvider(), 8);
        add(table);

        actions.addAction("View customer", (target, invoice) -> info("Customer: " + invoice.customer()));
        actions.addAction("Mark as paid", (target, invoice) -> {
            markPaid(List.of(invoice));
            success(invoice.number() + " marked as paid.");
            target.add(table);
        }).setVisibleWhen(invoice -> invoice.status() != Status.PAID);
        actions.addAction("Delete", (target, invoice) ->
                confirm.ask(target, "Delete " + invoice.number() + "?", "This can't be undone.", t -> {
                    delete(List.of(invoice));
                    success(invoice.number() + " deleted.");
                    t.add(table);
                })).setVariant(OatVariant.DANGER);

        table.addBulkAction("Mark as paid", (target, rows) -> {
            markPaid(rows);
            success(rows.size() + " invoices marked as paid.");
            table.clearSelection(target);
        });
        table.addBulkAction(Model.of("Delete"), OatVariant.DANGER, (target, rows) ->
                confirm.ask(target, "Delete " + rows.size() + " invoices?", "This can't be undone.", t -> {
                    delete(rows);
                    success(rows.size() + " invoices deleted.");
                    table.clearSelection(t);
                }));

        // Search: the provider filters by the field's model; each keystroke shows page 1 again
        table.setToolbar(id -> {
            Fragment toolbar = new Fragment(id, "toolbarFragment", this);
            Form<Void> form = new Form<>("searchForm");
            toolbar.add(form);
            form.add(new OatSearchField("search", Model.of("Search invoices"), search)
                    .setPlaceholder(Model.of("Number or customer"))
                    .add(new AjaxFormComponentUpdatingBehavior("input") {
                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);
                            attributes.setThrottlingSettings(new ThrottlingSettings(Duration.ofMillis(300)));
                        }

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            table.getTable().setCurrentPage(0);
                            target.add(table.getTable());
                        }
                    }));
            return toolbar;
        });

        add(Oat.Components.dataTable("emptyDataTable", List.<IColumn<Invoice, String>>of(
                        OatLinkColumn.toPage(Model.of("Number"), null, Invoice::number, InvoicePage.class, invoice -> new PageParameters()),
                        new OatNumberColumn<>(Model.of("Total"), Invoice::total)),
                        new SortableDataProvider<Invoice, String>() {
                            @Override
                            public Iterator<? extends Invoice> iterator(long first, long count) {
                                return List.<Invoice>of().iterator();
                            }

                            @Override
                            public long size() {
                                return 0;
                            }

                            @Override
                            public IModel<Invoice> model(Invoice object) {
                                return Model.of(object);
                            }
                        }, 10)
                .setEmptyState(id -> Oat.Components.emptyState(id, "No invoices found", "Try adjusting your search or creating a new invoice.")));
    }

    private void markPaid(List<Invoice> rows) {
        for (Invoice row : rows) {
            invoices.replaceAll(invoice -> invoice.equals(row) ? invoice.paid() : invoice);
        }
    }

    private void delete(List<Invoice> rows) {
        invoices.removeAll(rows);
    }

    private List<Invoice> filtered() {
        String query = search.getObject() == null ? "" : search.getObject().strip().toLowerCase(Locale.ROOT);
        return invoices.stream()
                .filter(invoice -> query.isEmpty() || invoice.number().toLowerCase(Locale.ROOT).contains(query)
                        || invoice.customer().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private class InvoiceProvider extends SortableDataProvider<Invoice, String> {

        @Override
        public Iterator<? extends Invoice> iterator(long first, long count) {
            List<Invoice> rows = new ArrayList<>(filtered());
            if (getSort() != null) {
                Comparator<Invoice> order = switch (getSort().getProperty()) {
                    case "customer" -> Comparator.comparing(Invoice::customer);
                    case "status" -> Comparator.comparing(Invoice::status);
                    case "total" -> Comparator.comparing(Invoice::total);
                    case "due" -> Comparator.comparing(Invoice::due);
                    default -> Comparator.comparing(Invoice::number);
                };
                rows.sort(getSort().isAscending() ? order : order.reversed());
            }
            return rows.subList((int) first, (int) Math.min(first + count, rows.size())).iterator();
        }

        @Override
        public long size() {
            return filtered().size();
        }

        @Override
        public IModel<Invoice> model(Invoice object) {
            return Model.of(object);
        }
    }
}
