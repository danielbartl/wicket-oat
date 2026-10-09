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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TierAPagesTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
    }

    @Test
    void signInRejectsAWrongPasswordAndAcceptsTheDemoUser() {
        tester.startPage(SignInPage.class);
        FormTester form = tester.newFormTester("card:login:form");
        form.setValue("username:container:field", "demo");
        form.setValue("password:container:field", "wrong");
        tester.executeAjaxEvent("card:login:form:signIn", "click");
        assertTrue(tester.getLastResponseAsString().contains("Wrong username or password."));

        form = tester.newFormTester("card:login:form");
        form.setValue("username:container:field", "demo");
        form.setValue("password:container:field", "demo");
        tester.executeAjaxEvent("card:login:form:signIn", "click");
        assertTrue(tester.getLastResponseAsString().contains("Signed in as demo."));
    }

    @Test
    void customersShowTheSelectedOneAndTakeNotes() {
        tester.startPage(CustomersPage.class);
        assertTrue(tester.getLastResponseAsString().contains("Select an item to see its details."));

        tester.clickLink("customers:master:customer:0:link");
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("billing@acme.example"));
        assertTrue(html.contains("Asked for the Q3 invoices"));

        FormTester note = tester.newFormTester("customers:detailArea:detail:addNote:form");
        note.setValue("text", "Called about the overdue invoice.");
        tester.executeAjaxEvent("customers:detailArea:detail:addNote:form:send", "click");
        assertTrue(tester.getLastResponseAsString().contains("Called about the overdue invoice."));

        tester.clickLink("customers:master:customer:1:link");
        assertTrue(tester.getLastResponseAsString().contains("ap@globex.example"));
        assertFalse(tester.getLastResponseAsString().contains("Called about the overdue invoice."));
    }

    @Test
    void invoiceHasAPhoneCustomField() {
        tester.startPage(InvoicePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("aria-label=\"Country code\""));
        assertTrue(html.contains(">Phone</legend>"));
    }
}
