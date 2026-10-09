package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DataTablePageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;
    private DataTablePage page;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
        page = tester.startPage(DataTablePage.class);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    private List<Component> rowCheckboxes() {
        List<Component> boxes = new ArrayList<>();
        // The selection column's checkboxes in the table body (the header has "select all")
        page.visitChildren(AjaxCheckBox.class, (box, visit) -> {
            if (box.getPageRelativePath().contains(":body:")) {
                boxes.add(box);
            }
        });
        return boxes;
    }

    @Test
    void rendersTheInvoicesWithOatColumns() {
        tester.assertRenderedPage(DataTablePage.class);
        assertEquals(8, tags("aria-label", "Select row").size());
        assertTrue(tester.getLastResponseAsString().contains("$137.50")); // INV-1001, in its own currency
        assertFalse(tags("class", "badge").isEmpty());
        assertEquals(8, tags("aria-label", "Actions").size());
    }

    @Test
    void bulkActionMarksTheSelectedInvoicesPaid() {
        Component first = rowCheckboxes().get(0);
        tester.getRequest().getPostParameters().setParameterValue(((FormComponent<?>) first).getInputName(), "on");
        tester.executeBehavior(first.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0));
        tester.assertLabel("dataTable:bulkActions:count", "1 selected");

        tester.executeAjaxEvent("dataTable:bulkActions:actions:0:action", "click");
        tester.assertInvisible("dataTable:bulkActions");
        assertTrue(tester.getLastResponseAsString().contains("1 invoices marked as paid."));
    }

    @Test
    void searchFiltersTheRows() {
        FormTester form = tester.newFormTester("dataTable:toolbar:searchForm");
        form.setValue("search:container:field", "globex");
        tester.executeAjaxEvent("dataTable:toolbar:searchForm:search:container:field", "input");

        List<TagTester> rows = tags("aria-label", "Select row");
        assertTrue(rows.size() > 0 && rows.size() < 8);
        assertTrue(tester.getLastResponseAsString().contains("Globex"));
        assertFalse(tester.getLastResponseAsString().contains("Initech"));
    }
}
