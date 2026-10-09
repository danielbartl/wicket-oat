package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.OatDataTable;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.SetModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/** An invoices table with every Oat column, selection, row and bulk actions. Two rows per page. */
public class InvoiceTablePage extends WebPage implements IMarkupResourceStreamProvider {

    public enum Status { OPEN, PAID, OVERDUE }

    public record Invoice(long id, String number, String customer, Status status, BigDecimal total,
                          Currency currency, LocalDate due, Boolean exported) implements Serializable {
    }

    public final List<Invoice> invoices = new ArrayList<>(List.of(
            new Invoice(1, "INV-1", "ACME", Status.PAID, new BigDecimal("1234.5"), Currency.getInstance("EUR"), LocalDate.of(2026, 10, 9), true),
            new Invoice(2, "INV-2", "Globex", Status.OVERDUE, new BigDecimal("99"), Currency.getInstance("USD"), LocalDate.of(2026, 1, 31), false),
            new Invoice(3, "INV-3", "Initech", Status.OPEN, null, Currency.getInstance("JPY"), null, null)));
    public final List<String> log = new ArrayList<>();
    public final IModel<java.util.Set<Invoice>> selected = new SetModel<>(new HashSet<>());
    public final OatDataTable<Invoice, String> table;

    public InvoiceTablePage() {
        List<IColumn<Invoice, String>> columns = new ArrayList<>();
        columns.add(new OatSelectionColumn<>(selected));
        columns.add(OatLinkColumn.toPage(Model.of("Number"), "number", Invoice::number,
                InvoiceTablePage.class, invoice -> new PageParameters().add("id", invoice.id())));
        columns.add(OatLinkColumn.onClick(Model.of("Customer"), null, Invoice::customer,
                (target, invoice) -> log.add("customer " + invoice.customer())));
        columns.add(new OatBadgeColumn<>(Model.of("Status"), "status", Invoice::status,
                invoice -> switch (invoice.status()) {
                    case PAID -> OatVariant.SUCCESS;
                    case OVERDUE -> OatVariant.DANGER;
                    default -> OatVariant.DEFAULT;
                }));
        columns.add(new OatNumberColumn<Invoice, String>(Model.of("Total"), "total", Invoice::total).setCurrency(Invoice::currency));
        columns.add(new OatDateColumn<>(Model.of("Due"), "due", Invoice::due));
        columns.add(new OatBooleanColumn<>(Model.of("Exported"), Invoice::exported));
        OatActionsColumn<Invoice, String> actions = new OatActionsColumn<>();
        actions.addAction("Edit", (target, invoice) -> log.add("edit " + invoice.number()));
        actions.addAction("Mark as paid", (target, invoice) -> log.add("paid " + invoice.number()))
                .setVisibleWhen(invoice -> invoice.status() != Status.PAID);
        actions.addAction("Delete", (target, invoice) -> log.add("delete " + invoice.number()))
                .setVariant(OatVariant.DANGER);
        columns.add(actions);

        table = new OatDataTable<>("table", columns, new Provider(), 2);
        table.addBulkAction("Export", (target, rows) -> {
            log.add("export " + rows.stream().map(Invoice::number).sorted().toList());
            table.clearSelection(target);
        });
        table.setToolbar(id -> new Label(id, "Toolbar"));
        add(table);
    }

    @Override
    public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
        return new StringResourceStream("<html><body><div wicket:id='table'></div></body></html>");
    }

    private class Provider extends SortableDataProvider<Invoice, String> {
        @Override
        public Iterator<? extends Invoice> iterator(long first, long count) {
            List<Invoice> rows = new ArrayList<>(invoices);
            if (getSort() != null && "number".equals(getSort().getProperty())) {
                Comparator<Invoice> byNumber = Comparator.comparing(Invoice::number);
                rows.sort(getSort().isAscending() ? byNumber : byNumber.reversed());
            }
            return rows.subList((int) first, (int) (first + count)).iterator();
        }

        @Override
        public long size() {
            return invoices.size();
        }

        @Override
        public IModel<Invoice> model(Invoice object) {
            return Model.of(object);
        }
    }
}
