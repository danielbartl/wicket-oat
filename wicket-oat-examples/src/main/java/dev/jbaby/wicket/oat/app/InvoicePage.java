package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatFieldset;
import dev.jbaby.wicket.oat.components.OatSubmitButton;
import dev.jbaby.wicket.oat.components.form.OatCustomField;
import dev.jbaby.wicket.oat.components.form.OatDateField;
import dev.jbaby.wicket.oat.components.form.OatDropdownChoice;
import dev.jbaby.wicket.oat.components.form.OatEmailField;
import dev.jbaby.wicket.oat.components.form.OatMoneyField;
import dev.jbaby.wicket.oat.components.form.OatNumberField;
import dev.jbaby.wicket.oat.components.form.OatPercentField;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * A business form: fieldset sections laid out on Oat's grid, money and percent fields
 * that follow the locale, and input addons.
 */
public class InvoicePage extends BasePage {

    public static class Invoice implements Serializable {
        public String customer;
        public String email;
        public String website;
        public String phone;
        public LocalDate dueDate = LocalDate.now().plusDays(30);
        public Currency currency = Currency.getInstance("EUR");
        public BigDecimal netAmount = new BigDecimal("1250");
        public BigDecimal discount;
        public BigDecimal taxRate = new BigDecimal("19");
        public Integer weight;

        BigDecimal total() {
            if (netAmount == null) {
                return null;
            }
            BigDecimal hundred = BigDecimal.valueOf(100);
            BigDecimal afterDiscount = discount == null ? netAmount
                    : netAmount.multiply(hundred.subtract(discount)).divide(hundred);
            BigDecimal withTax = taxRate == null ? afterDiscount
                    : afterDiscount.multiply(hundred.add(taxRate)).divide(hundred);
            return withTax.setScale(Math.max(currency.getDefaultFractionDigits(), 0), RoundingMode.HALF_EVEN);
        }
    }

    private final Invoice invoice = new Invoice();

    // The number format for this form only: the fields take their locale from the form
    private Locale formLocale = Locale.US;

    public InvoicePage() {
        add(Oat.Behaviors.feedbackToasts());

        Form<Invoice> form = new Form<>("invoiceForm", new CompoundPropertyModel<>(invoice)) {
            @Override
            public Locale getLocale() {
                return formLocale;
            }
        };
        form.setOutputMarkupId(true);
        add(form);

        form.add(new OatDropdownChoice<>("formLocale", Model.of("Number format"),
                LambdaModel.of(() -> formLocale, locale -> formLocale = locale),
                Model.ofList(List.of(Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.JAPAN)),
                new ChoiceRenderer<>() {
                    @Override
                    public Object getDisplayValue(Locale locale) {
                        return locale.getDisplayName(locale);
                    }
                })
                .add(new AjaxFormComponentUpdatingBehavior("change") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.add(form);
                    }
                }));

        // Customer: two columns on wide screens, one on phones
        OatFieldset customer = new OatFieldset("customerSection")
                .setDescription("Who the invoice goes to.");
        customer.add(Oat.Behaviors.row());
        form.add(customer);
        customer.add(new OatTextField<String>("customer").setRequired(true).add(Oat.Behaviors.col(6)));
        customer.add(new OatEmailField("email").add(Oat.Behaviors.col(6)));
        customer.add(new OatTextField<String>("website")
                .setPrefix("https://")
                .setPlaceholder(Model.of("example.com"))
                .add(Oat.Behaviors.col(6)));
        customer.add(new OatDateField("dueDate").setRequired(true).add(Oat.Behaviors.col(6)));
        // An input of the application's own (two inputs), with an Oat label, hint and errors
        customer.add(new OatCustomField<String>("phone", PhoneInput::new).add(Oat.Behaviors.col(6)));

        // Amounts: the currency sets the money field's symbol and decimals, and the
        // locale where the symbol goes
        OatFieldset amounts = new OatFieldset("amountsSection");
        amounts.add(Oat.Behaviors.row());
        form.add(amounts);

        OatMoneyField netAmount = new OatMoneyField("netAmount")
                .setCurrency(new PropertyModel<>(invoice, "currency"))
                .setRequired(true)
                .setMin(BigDecimal.ZERO);
        netAmount.add(Oat.Behaviors.col(8));

        amounts.add(new OatDropdownChoice<>("currency",
                Model.ofList(List.of(Currency.getInstance("EUR"), Currency.getInstance("USD"),
                        Currency.getInstance("GBP"), Currency.getInstance("JPY"))),
                new LambdaChoiceRenderer<>(Currency::getCurrencyCode, Currency::getCurrencyCode))
                .setRequired(true)
                .add(Oat.Behaviors.col(4))
                .add(new AjaxFormComponentUpdatingBehavior("change") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        target.add(netAmount);
                    }
                }));
        amounts.add(netAmount);
        amounts.add(new OatPercentField("discount")
                .setMin(BigDecimal.ZERO).setMax(BigDecimal.valueOf(100))
                .add(Oat.Behaviors.col(4)));
        amounts.add(new OatPercentField("taxRate")
                .setRequired(true)
                .setMin(BigDecimal.ZERO).setMax(BigDecimal.valueOf(100))
                .add(Oat.Behaviors.col(4)));
        amounts.add(new OatNumberField<Integer>("weight")
                .setMin(0)
                .setSuffix("kg")
                .add(Oat.Behaviors.col(4)));

        IModel<String> total = () -> {
            BigDecimal value = invoice.total();
            if (value == null) {
                return "–";
            }
            NumberFormat format = NumberFormat.getCurrencyInstance(formLocale);
            format.setCurrency(invoice.currency);
            format.setMaximumFractionDigits(Math.max(invoice.currency.getDefaultFractionDigits(), 0));
            format.setMinimumFractionDigits(Math.max(invoice.currency.getDefaultFractionDigits(), 0));
            return format.format(value);
        };
        form.add(new Label("total", total));

        form.add(new OatSubmitButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                success("Invoice for " + invoice.customer + " saved.");
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                error("Please fix the errors in the form.");
                target.add(form);
            }
        });
    }
}
