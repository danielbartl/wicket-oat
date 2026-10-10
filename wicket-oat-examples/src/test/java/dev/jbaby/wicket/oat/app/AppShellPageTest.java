package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppShellPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    private List<TagTester> tags(String wicketId) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "wicket:id", wicketId, false);
    }

    @Test
    void sidebarShowsGroupsWithTheCurrentOneOpen() {
        tester.startPage(AppShellPage.class);
        tester.assertRenderedPage(AppShellPage.class);

        List<TagTester> groups = tags("menuGroup");
        assertEquals(3, groups.size());
        assertEquals("open", groups.get(0).getAttribute("open")); // Demos, containing this page
        assertEquals(null, groups.get(1).getAttribute("open"));
        assertEquals(List.of("4"), tags("menuBadge").stream().map(TagTester::getValue).toList());
    }

    @Test
    void deletingATaskAsksFirstAndUpdatesTheBadge() {
        tester.startPage(AppShellPage.class);
        tester.executeAjaxEvent("tasks:task:0:delete", "click");
        assertTrue(tester.getLastResponseAsString().contains("Delete “Send the Q3 invoices”?"));

        tester.executeAjaxEvent("confirm:dialog:form:confirm", "click");
        String response = tester.getLastResponseAsString();
        assertTrue(response.contains("Renew the TLS certificate"));
        assertTrue(!response.contains(">Send the Q3 invoices<"));
        // The sidebar was re-rendered with the new count
        assertEquals(List.of("3"), tags("menuBadge").stream().map(TagTester::getValue).toList());
    }
}
