package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * An Oat-styled form field for an amount of money, bound to a {@link BigDecimal}.
 * The amount is shown and parsed in the user's locale ({@code 1.234,50} in German,
 * {@code 1,234.50} in English), with as many decimals as the {@link #setCurrency currency}
 * has (2 for EUR, 0 for JPY); input with more decimals is rounded half-even.
 * <p>
 * The currency symbol is shown as an addon before or after the input, wherever the
 * locale puts it ({@code $ 12.50}, {@code 12,50 €}). Typing the symbol along with the
 * amount is fine. To show something else, call {@link #setPrefix}/{@link #setSuffix}
 * yourself; passing {@code (String) null} to one of them hides that side.
 * Without a currency, it is a plain decimal field with 2 decimals.
 */
public class OatMoneyField extends BaseOatDecimalField<OatMoneyField> {

    private static final int DEFAULT_FRACTION_DIGITS = 2;

    private IModel<Currency> currency = new Model<>();

    /**
     * Label looked up by {@code id} in the {@code .properties} files, model inherited
     * from a parent {@code CompoundPropertyModel}.
     */
    public OatMoneyField(String id) {
        this(id, null, null, null);
    }

    /** Label looked up by {@code id} in the {@code .properties} files. */
    public OatMoneyField(String id, IModel<BigDecimal> model) {
        this(id, null, model, null);
    }

    public OatMoneyField(String id, String label, IModel<BigDecimal> model) {
        this(id, Model.of(label), model, null);
    }

    public OatMoneyField(String id, IModel<String> label, IModel<BigDecimal> model) {
        this(id, label, model, null);
    }

    public OatMoneyField(String id, IModel<String> label, IModel<BigDecimal> model, IModel<String> helper) {
        super(id, label, model, helper);
        setPrefix((IModel<String>) () -> symbolBeforeAmount() ? getSymbol() : null);
        setSuffix((IModel<String>) () -> symbolBeforeAmount() ? null : getSymbol());
    }

    /** The currency of the amount, which sets its symbol and number of decimals. */
    public OatMoneyField setCurrency(Currency currency) {
        return setCurrency(Model.of(currency));
    }

    /** The currency of the amount, e.g. from the record being edited. */
    public OatMoneyField setCurrency(IModel<Currency> currency) {
        this.currency = currency != null ? currency : new Model<>();
        return this;
    }

    /** The currency, or {@code null} if none is set. */
    public Currency getCurrency() {
        return currency.getObject();
    }

    @Override
    protected int getMinFractionDigits() {
        return getMaxFractionDigits();
    }

    @Override
    protected int getMaxFractionDigits() {
        Currency c = getCurrency();
        // Pseudo-currencies such as XXX have no fraction digits (-1)
        return c != null && c.getDefaultFractionDigits() >= 0 ? c.getDefaultFractionDigits() : DEFAULT_FRACTION_DIGITS;
    }

    @Override
    protected String stripInput(String input, Locale locale) {
        Currency c = getCurrency();
        return c == null ? input : input.replace(c.getSymbol(locale), "").replace(c.getCurrencyCode(), "");
    }

    private String getSymbol() {
        Currency c = getCurrency();
        return c != null ? c.getSymbol(getLocale()) : null;
    }

    private boolean symbolBeforeAmount() {
        Currency c = getCurrency();
        if (c == null) {
            return false;
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(getLocale());
        format.setCurrency(c);
        return !(format instanceof DecimalFormat decimal) || decimal.getPositiveSuffix().isBlank();
    }

    @Override
    protected void onDetach() {
        currency.detach();
        super.onDetach();
    }
}
