package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CustomerPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
        tester.startPage(CustomerPage.class);
    }

    @Test
    void rendersTheCustomer() {
        tester.assertRenderedPage(CustomerPage.class);
        tester.assertLabel("pageHeader:title", "ACME GmbH");
        tester.assertLabel("revenue:value", "€128,400");
        tester.assertLabel("orders:value", "1,270");
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains(">Account manager</dt>"));
        assertTrue(html.contains(">Jordan Lee<"));
        assertTrue(html.contains(">—<")); // no notes
        assertTrue(html.contains("<datalist"));
    }

    @Test
    void savesAQuickNote() {
        FormTester form = tester.newFormTester("pageHeader:pageHeader_body:quickNote:popover:content:noteForm");
        form.setValue("type:container:field", "Call");
        form.setValue("text:container:field", "Asked about the Q3 invoice.");
        tester.executeAjaxEvent("pageHeader:pageHeader_body:quickNote:popover:content:noteForm:saveNote", "click");
        tester.assertNoErrorMessage();
        assertTrue(tester.getLastResponseAsString().contains("Note saved (Call)."));
        assertTrue(tester.getLastResponseAsString().contains("hidePopover()"));
    }
}
