package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatDescriptionList;
import dev.jbaby.wicket.oat.components.OatWizard;
import dev.jbaby.wicket.oat.components.form.DateRange;
import dev.jbaby.wicket.oat.components.form.OatAutoCompleteField;
import dev.jbaby.wicket.oat.components.form.OatDateRangeField;
import dev.jbaby.wicket.oat.components.form.OatMoneyField;
import dev.jbaby.wicket.oat.components.form.OatNumberField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * An order placed in three steps with an OatWizard: the customer (found with an
 * OatAutoCompleteField) and delivery window (an OatDateRangeField), the items, and a
 * review.
 */
public class OrderWizardPage extends BasePage {

    public record Customer(long id, String name, String city) implements Serializable {
    }

    public static class Order implements Serializable {
        public Customer customer;
        public DateRange delivery;
        public Integer quantity = 1;
        public BigDecimal unitPrice;

        BigDecimal total() {
            return unitPrice == null || quantity == null ? null : unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    private static final List<Customer> CUSTOMERS = List.of(
            new Customer(1, "ACME GmbH", "Vienna"), new Customer(2, "Acme Logistics", "Graz"),
            new Customer(3, "Globex", "Berlin"), new Customer(4, "Initech", "Munich"),
            new Customer(5, "Umbrella", "Zurich"), new Customer(6, "Stark Industries", "Linz"),
            new Customer(7, "Wayne Enterprises", "Salzburg"));

    private static final Currency EUR = Currency.getInstance("EUR");

    private Order order = new Order();

    public OrderWizardPage() {
        add(Oat.Behaviors.feedbackToasts());

        OatWizard wizard = new OatWizard("wizard") {
            @Override
            protected void onFinish(org.apache.wicket.ajax.AjaxRequestTarget target) {
                success("Order placed for " + order.customer.name() + ".");
                order = new Order();
                setCurrentStep(0);
                target.add(this);
            }
        };
        add(wizard);

        wizard.addStep("Customer", id -> {
            Fragment step = new Fragment(id, "customerStep", this);
            step.add(new OatAutoCompleteField<Customer>("customer", Model.of("Customer"),
                    new PropertyModel<>(this, "order.customer"), Model.of("Type a few letters, e.g. \"ac\"."))
                    .setChoices(OrderWizardPage::search)
                    .setDisplay(Customer::name)
                    .setRequired(true));
            step.add(new OatDateRangeField("delivery", Model.of("Delivery window"), new PropertyModel<>(this, "order.delivery"))
                    .setRequired(true));
            return step;
        });
        wizard.addStep("Items", id -> {
            Fragment step = new Fragment(id, "itemsStep", this);
            step.add(new OatNumberField<Integer>("quantity", Model.of("Quantity"), new PropertyModel<>(this, "order.quantity"))
                    .setMin(1).setRequired(true));
            step.add(new OatMoneyField("unitPrice", Model.of("Unit price"), new PropertyModel<>(this, "order.unitPrice"))
                    .setCurrency(EUR).setMin(BigDecimal.ZERO).setRequired(true));
            return step;
        });
        wizard.addStep("Review", id -> {
            DateTimeFormatter dates = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US);
            Fragment step = new Fragment(id, "reviewStep", this);
            step.add(new OatDescriptionList("review")
                    .addItem("Customer", (IModel<String>) () -> order.customer.name() + ", " + order.customer.city())
                    .addItem("Delivery", (IModel<String>) () -> dates.format(order.delivery.from()) + " – " + dates.format(order.delivery.to()))
                    .addItem("Quantity", (IModel<Integer>) () -> order.quantity)
                    .addItem("Total", (IModel<String>) () -> {
                        NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
                        money.setCurrency(EUR);
                        return money.format(order.total());
                    }));
            return step;
        });
    }

    private static List<Customer> search(String text) {
        String query = text.toLowerCase(Locale.ROOT);
        return CUSTOMERS.stream().filter(c -> c.name().toLowerCase(Locale.ROOT).contains(query)).toList();
    }
}
