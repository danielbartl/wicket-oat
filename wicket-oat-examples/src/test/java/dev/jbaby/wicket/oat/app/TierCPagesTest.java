package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.tree.OatTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TierCPagesTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
    }

    @Test
    void productsCreatesAProduct() {
        tester.startPage(ProductsPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Antivirus, yearly"));
        assertTrue(html.contains(">Columns<"));
        assertTrue(html.contains(">Export CSV<"));

        tester.clickLink("products:table:toolbar:new");
        FormTester form = tester.newFormTester("products:editor:dialog:form");
        form.setValue("body:name:container:field", "Webcam");
        form.select("body:category:container:field", 3);
        form.setValue("body:price:container:field", "79.00");
        form.setValue("body:stock:container:field", "30");
        tester.executeAjaxEvent("products:editor:dialog:form:confirm", "click");
        assertTrue(tester.getLastResponseAsString().contains("Webcam saved."));
    }

    @Test
    void accountsShowTheTreeAndTheChart() {
        tester.startPage(AccountsPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains(">Customers<"));
        assertFalse(html.contains(">Globex<"));
        // The chart starts with its groups open
        assertTrue(html.contains(">Bank accounts<"));
        // A group's balance is the sum of its accounts: 48,210.35 + 120,000.00
        assertTrue(html.contains("€168,210.35"));

        List<OatTreeNode<?>> nodes = new ArrayList<>();
        tester.getLastRenderedPage().visitChildren(OatTreeNode.class, (OatTreeNode<?> node, org.apache.wicket.util.visit.IVisit<Void> visit) -> nodes.add(node));
        Component customers = nodes.get(0).get("junction");
        tester.clickLink(customers);
        assertTrue(tester.getLastResponseAsString().contains(">Globex<"));
    }

    @Test
    void theQuickNotePopoverHasAnAutocompleteField() {
        tester.startPage(CustomerPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains(">Assign to</label>"));
        // The script that shows the suggestions above the popover (resource names are versioned)
        assertTrue(html.contains("OatAppLayout/wicket-oat"));
    }

    @Test
    void theWizardNotifiesChosenColleagues() {
        tester.startPage(OrderWizardPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains(">Notify</legend>"));
        assertTrue(html.contains("aria-label=\"Notify\""));
    }
}
