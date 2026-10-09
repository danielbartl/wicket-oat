package dev.jbaby.wicket.oat.components.form;

import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class InputAddonsAndDecimalFieldsTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
    }

    // --- Prefix and suffix ---

    @Test
    void fieldWithoutAddonsRendersNoGroup() {
        tester.startComponentInPage(new OatTextField<>("id", "Label", Model.of("Value")));

        assertThat(tester.getLastResponseAsString()).doesNotContain("<fieldset").doesNotContain("group");
        tester.assertComponent("id:container:field", org.apache.wicket.markup.html.form.TextField.class);
        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("Value");
    }

    @Test
    void prefixAndSuffixRenderAnInputGroup() {
        OatTextField<String> field = new OatTextField<>("id", "Website", Model.of("example"))
                .setPrefix("https://")
                .setSuffix(".com");
        tester.startComponentInPage(field);

        TagTester group = tester.getTagByWicketId("group");
        assertThat(group.getName()).isEqualTo("fieldset");
        assertThat(group.getAttribute("class")).isEqualTo("group");

        String inputId = field.getField().getMarkupId();
        TagTester prefix = tester.getTagByWicketId("prefix");
        assertThat(prefix.getValue()).isEqualTo("https://");
        assertThat(prefix.getAttribute("for")).isEqualTo(inputId);
        assertThat(tester.getTagByWicketId("suffix").getValue()).isEqualTo(".com");
        assertThat(tester.getTagByWicketId("suffix").getAttribute("for")).isEqualTo(inputId);

        // The group is transparent: the field keeps its usual path
        tester.assertComponent("id:container:field", org.apache.wicket.markup.html.form.TextField.class);
    }

    @Test
    void onlyTheAddonsThatAreSetAreRendered() {
        tester.startComponentInPage(new OatNumberField<Integer>("id", "Weight", Model.of(5)).setSuffix("kg"));

        assertThat(tester.getTagByWicketId("group")).isNotNull();
        assertThat(tester.getTagByWicketId("prefix")).isNull();
        assertThat(tester.getTagByWicketId("suffix").getValue()).isEqualTo("kg");
    }

    @Test
    void addonsFollowTheirModel() {
        IModel<String> unit = Model.of("kg");
        OatNumberField<Integer> field = new OatNumberField<Integer>("id", "Weight", Model.of(5)).setSuffix(unit);
        tester.startComponentInPage(field);
        assertThat(tester.getTagByWicketId("suffix").getValue()).isEqualTo("kg");

        unit.setObject(null);
        tester.startComponentInPage(field);
        assertThat(tester.getTagByWicketId("suffix")).isNull();
        assertThat(tester.getLastResponseAsString()).doesNotContain("<fieldset");
    }

    @Test
    void addonsDoNotInheritACompoundPropertyModel() {
        // The fields' own ids resolve against the bean; "prefix"/"suffix" must not
        tester.startPage(FieldTestPage.class);
        tester.assertRenderedPage(FieldTestPage.class);
    }

    // --- Money ---

    @Test
    void moneyIsFormattedInTheLocaleWithTheSymbolAfterTheAmount() {
        tester.getSession().setLocale(Locale.GERMANY);
        tester.startComponentInPage(new OatMoneyField("id", "Price", Model.of(new BigDecimal("1234.5"))).setCurrency(EUR));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("1.234,50");
        assertThat(tester.getTagByWicketId("field").getAttribute("inputmode")).isEqualTo("decimal");
        assertThat(tester.getTagByWicketId("field").getAttribute("type")).isEqualTo("text");
        assertThat(tester.getTagByWicketId("prefix")).isNull();
        assertThat(tester.getTagByWicketId("suffix").getValue()).isEqualTo("€");
    }

    @Test
    void moneyIsFormattedInTheLocaleWithTheSymbolBeforeTheAmount() {
        tester.getSession().setLocale(Locale.US);
        tester.startComponentInPage(new OatMoneyField("id", "Price", Model.of(new BigDecimal("1234.5"))).setCurrency(USD));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("1,234.50");
        assertThat(tester.getTagByWicketId("prefix").getValue()).isEqualTo("$");
        assertThat(tester.getTagByWicketId("suffix")).isNull();
    }

    @Test
    void moneyUsesTheCurrencysDecimals() {
        tester.getSession().setLocale(Locale.US);
        tester.startComponentInPage(new OatMoneyField("id", "Price", Model.of(new BigDecimal("1234"))).setCurrency(JPY));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("1,234");
    }

    @Test
    void moneyWithoutACurrencyHasNoAddonAndTwoDecimals() {
        tester.getSession().setLocale(Locale.US);
        tester.startComponentInPage(new OatMoneyField("id", "Price", Model.of(new BigDecimal("3"))));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("3.00");
        assertThat(tester.getLastResponseAsString()).doesNotContain("<fieldset");
    }

    @Test
    void anExplicitAddonReplacesTheSymbol() {
        tester.getSession().setLocale(Locale.US);
        tester.startComponentInPage(new OatMoneyField("id", "Price", Model.of(BigDecimal.ONE))
                .setCurrency(USD).setPrefix("USD"));

        assertThat(tester.getTagByWicketId("prefix").getValue()).isEqualTo("USD");
    }

    @Test
    void moneyIsParsedInTheLocale() {
        assertThat(submitMoney(Locale.GERMANY, EUR, "1.234,56")).isEqualTo(new BigDecimal("1234.56"));
        assertThat(submitMoney(Locale.US, USD, "1,234.56")).isEqualTo(new BigDecimal("1234.56"));
    }

    @Test
    void moneyMayBeTypedWithItsSymbolOrCode() {
        assertThat(submitMoney(Locale.GERMANY, EUR, "12,50 €")).isEqualTo(new BigDecimal("12.50"));
        assertThat(submitMoney(Locale.US, USD, "$12.50")).isEqualTo(new BigDecimal("12.50"));
        assertThat(submitMoney(Locale.US, USD, "12.50 USD")).isEqualTo(new BigDecimal("12.50"));
    }

    @Test
    void moneyIsRoundedHalfEvenToTheCurrencysDecimals() {
        assertThat(submitMoney(Locale.GERMANY, EUR, "12,345")).isEqualTo(new BigDecimal("12.34"));
        assertThat(submitMoney(Locale.GERMANY, EUR, "12")).isEqualTo(new BigDecimal("12.00"));
        assertThat(submitMoney(Locale.US, JPY, "1,234.5")).isEqualTo(new BigDecimal("1234"));
    }

    @Test
    void anEmptyAmountIsNull() {
        assertThat(submitMoney(Locale.US, USD, "  ")).isNull();
    }

    @Test
    void invalidMoneyShowsAnErrorInline() {
        tester.getSession().setLocale(Locale.US);
        Model<BigDecimal> model = Model.of(BigDecimal.ONE);
        OatMoneyField field = new OatMoneyField("price", "Price", model).setCurrency(USD);
        submit(field, "abc");

        assertThat(field.getField().hasErrorMessage()).isTrue();
        assertThat(model.getObject()).isEqualTo(BigDecimal.ONE);
        assertThat(tester.getTagByWicketId("container").getAttribute("data-field")).isEqualTo("error");
        assertThat(tester.getTagByWicketId("feedback").getValue()).isEqualTo("The value of &#039;Price&#039; is not a valid amount.");
    }

    @Test
    void moneyOutsideItsRangeShowsAnErrorInline() {
        tester.getSession().setLocale(Locale.US);
        Model<BigDecimal> model = Model.of(BigDecimal.ONE);
        OatMoneyField field = new OatMoneyField("price", "Price", model)
                .setCurrency(USD)
                .setMin(BigDecimal.ZERO)
                .setMax(new BigDecimal("100"));

        submit(field, "-5");
        assertThat(field.getField().hasErrorMessage()).isTrue();

        submit(field, "100.01");
        assertThat(field.getField().hasErrorMessage()).isTrue();
        assertThat(model.getObject()).isEqualTo(BigDecimal.ONE);

        submit(field, "100");
        assertThat(field.getField().hasErrorMessage()).isFalse();
        assertThat(model.getObject()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void settingTheRangeAgainReplacesIt() {
        OatMoneyField field = new OatMoneyField("price", "Price", Model.of(BigDecimal.ONE))
                .setMin(BigDecimal.ZERO).setMin(BigDecimal.ONE).setMax(BigDecimal.TEN);

        assertThat(field.getField().getValidators()).hasSize(1);
        field.setMin(null).setMax(null);
        assertThat(field.getField().getValidators()).isEmpty();
    }

    // --- Percent ---

    @Test
    void percentShowsASuffixAndOnlyTheDecimalsItHas() {
        tester.getSession().setLocale(Locale.GERMANY);
        tester.startComponentInPage(new OatPercentField("id", "Tax", Model.of(new BigDecimal("19"))));

        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("19");
        assertThat(tester.getTagByWicketId("suffix").getValue()).isEqualTo("%");

        tester.startComponentInPage(new OatPercentField("id", "Tax", Model.of(new BigDecimal("7.5"))));
        assertThat(tester.getTagByWicketId("field").getAttribute("value")).isEqualTo("7,5");
    }

    @Test
    void percentIsParsedAndRounded() {
        tester.getSession().setLocale(Locale.GERMANY);
        Model<BigDecimal> model = new Model<>();
        submit(new OatPercentField("tax", "Tax", model), "7,5 %");
        assertThat(model.getObject()).isEqualTo(new BigDecimal("7.5"));

        submit(new OatPercentField("tax", "Tax", model).setFractionDigits(1), "7,25");
        assertThat(model.getObject()).isEqualTo(new BigDecimal("7.2"));
    }

    @Test
    void percentRejectsNegativeFractionDigits() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OatPercentField("tax").setFractionDigits(-1));
    }

    @Test
    void factoriesCreateDecimalFields() {
        assertThat(Oat.Components.moneyField("id", "Price", Model.of(BigDecimal.ONE)).getLabel().getObject()).isEqualTo("Price");
        assertThat(Oat.Components.percentField("id", Model.of("Tax"), Model.of(BigDecimal.ONE), Model.of("Hint")).getLabel().getObject())
                .isEqualTo("Tax");
    }

    private BigDecimal submitMoney(Locale locale, Currency currency, String input) {
        tester.getSession().setLocale(locale);
        Model<BigDecimal> model = Model.of(new BigDecimal("999"));
        OatMoneyField field = new OatMoneyField("price", "Price", model).setCurrency(currency);
        submit(field, input);
        assertThat(field.getField().hasErrorMessage()).as("error for '%s'", input).isFalse();
        return model.getObject();
    }

    private void submit(BaseOatField<?, ?, ?> field, String input) {
        if (field.getParent() == null) {
            Form<Void> form = new Form<>("form");
            form.add(field);
            tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='" + field.getId() + "'></div></form>"));
        }
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue(field.getId() + ":container:field", input);
        formTester.submit();
    }
}
