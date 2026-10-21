package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DialogPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testDialogPageRenders() {
        tester.startPage(DialogPage.class);
        tester.assertRenderedPage(DialogPage.class);

        TagTester dialog = tester.getTagByWicketId("dialog");
        assertEquals("dialog", dialog.getName());

        TagTester trigger = tester.getTagByWicketId("trigger");
        assertEquals(dialog.getAttribute("id"), trigger.getAttribute("commandfor"));

        tester.assertLabel("deleteDialog:dialog:form:header", "Delete item?");
        tester.assertLabel("editDialog:dialog:form:content:name:label", "Name");
    }

    @Test
    void editDialogOpensForARowValidatesAndSaves() {
        tester.startPage(DialogPage.class);

        tester.clickLink("people:rows:1:edit");
        tester.assertComponentOnAjaxResponse("editDialog");
        assertTrue(tester.getLastResponseAsString().contains("value=\"Grace Hopper\""));

        FormTester formTester = tester.newFormTester("editDialog:dialog:form");
        formTester.setValue("content:name:input", "");
        tester.executeAjaxEvent("editDialog:dialog:form:confirm", "click");
        String response = tester.getLastResponseAsString();
        assertTrue(response.contains("&#039;Name&#039; is required."));
        assertFalse(response.contains(".close()"));

        tester.cleanupFeedbackMessages();
        formTester = tester.newFormTester("editDialog:dialog:form");
        formTester.setValue("content:name:input", "Grace B. Hopper");
        tester.executeAjaxEvent("editDialog:dialog:form:confirm", "click");
        response = tester.getLastResponseAsString();
        assertTrue(response.contains(".close()"));
        assertTrue(response.contains("ot.toast(\"Saved Grace B. Hopper.\""));
        tester.assertComponentOnAjaxResponse("people");
    }

    @Test
    void theFiltersDrawerAppliesWhatWasChosen() {
        tester.startPage(DialogPage.class);
        tester.assertLabel("filtersDrawer:trigger:triggerLabel", "Filters");

        FormTester formTester = tester.newFormTester("filtersDrawer:dialog:form");
        formTester.select("content:status:input", 1);
        formTester.setValue("content:overdueOnly:input", "true");
        tester.executeAjaxEvent("filtersDrawer:dialog:form:confirm", "click");

        assertTrue(tester.getLastResponseAsString().contains("ot.toast(\"Showing paid orders, overdue only.\""));
    }

    @Test
    void aRowsDetailsOpenInADrawerWithoutCancelAndConfirm() {
        tester.startPage(DialogPage.class);

        tester.clickLink("people:rows:0:details");

        tester.assertComponentOnAjaxResponse("detailsDrawer");
        tester.assertLabel("detailsDrawer:dialog:form:header", "Ada Lovelace");
        assertTrue(tester.getLastResponseAsString().contains("ada@example.com"));
        tester.assertInvisible("detailsDrawer:dialog:form:confirm");
    }
}
