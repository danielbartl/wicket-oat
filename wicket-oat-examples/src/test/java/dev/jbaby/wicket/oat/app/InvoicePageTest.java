package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InvoicePageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void rendersSectionsAndAddons() {
        tester.startPage(InvoicePage.class);
        tester.assertRenderedPage(InvoicePage.class);

        tester.assertLabel("invoiceForm:customerSection:legend", "Customer");
        assertTrue(tester.getTagByWicketId("customerSection").getAttribute("class").contains("row"));
        assertTrue(tester.getTagByWicketId("customer").getAttribute("class").contains("col-6"));

        // en-US by default: grouping with commas, the euro sign before the amount
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("value=\"1,250.00\""), "formatted net amount");
        assertTrue(html.contains(">https://</label>"), "website prefix");
        assertTrue(html.contains(">%</label>"), "percent suffix");
        assertTrue(html.contains(">kg</label>"), "weight suffix");
        tester.assertLabel("invoiceForm:total", "€1,487.50");
    }

    @Test
    void savesAmountsInTheChosenNumberFormat() {
        tester.startPage(InvoicePage.class);
        FormTester form = tester.newFormTester("invoiceForm");
        form.select("formLocale:container:field", 1); // Deutsch (Deutschland)
        tester.executeAjaxEvent("invoiceForm:formLocale:container:field", "change");

        form = tester.newFormTester("invoiceForm");
        form.setValue("customerSection:customerSection_body:customer:container:field", "ACME GmbH");
        form.setValue("amountsSection:amountsSection_body:netAmount:container:field", "2.000,00 €");
        form.setValue("amountsSection:amountsSection_body:discount:container:field", "10");
        tester.executeAjaxEvent("invoiceForm:submit", "click");

        tester.assertNoErrorMessage();
        assertEquals("2.142,00 €", tester.getComponentFromLastRenderedPage("invoiceForm:total")
                .getDefaultModelObjectAsString().replace(' ', ' '));
    }
}
