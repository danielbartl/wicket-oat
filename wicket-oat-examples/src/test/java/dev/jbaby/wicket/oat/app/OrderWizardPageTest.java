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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderWizardPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
        tester.startPage(OrderWizardPage.class);
    }

    @Test
    void placesAnOrderInThreeSteps() {
        FormTester form = tester.newFormTester("wizard:form");
        form.setValue("body:customer:container:field", "Globex");
        form.setValue("body:delivery:container:field:from", "2026-11-02");
        form.setValue("body:delivery:container:field:to", "2026-11-06");
        tester.executeAjaxEvent("wizard:form:next", "click");
        tester.assertNoErrorMessage();

        form = tester.newFormTester("wizard:form");
        form.setValue("body:quantity:container:field", "3");
        form.setValue("body:unitPrice:container:field", "12.50");
        tester.executeAjaxEvent("wizard:form:next", "click");
        tester.assertNoErrorMessage();

        String review = tester.getLastResponseAsString();
        assertTrue(review.contains("Globex, Berlin"));
        assertTrue(review.contains("Nov 2, 2026 – Nov 6, 2026"));
        assertTrue(review.contains("€37.50"));

        tester.executeAjaxEvent("wizard:form:finish", "click");
        assertTrue(tester.getLastResponseAsString().contains("Order placed for Globex."));
    }
}
