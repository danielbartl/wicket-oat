package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import dev.jbaby.wicket.oat.behaviors.ContextMenuBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.visit.IVisit;
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
class TierBPagesTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
        tester.getSession().setLocale(Locale.US);
    }

    @Test
    void reportsShowEveryChartWithItsData() {
        tester.startPage(ReportsPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("oat-column-chart"));
        assertTrue(html.contains("oat-line-chart"));
        assertTrue(html.contains("oat-bar-chart"));
        assertTrue(html.contains("oat-donut-chart"));
        assertTrue(html.contains("oat-sparkline"));
        assertTrue(html.contains(">Revenue by region</figcaption>"));
        assertTrue(html.contains(">Other<"), "the seventh region is folded into Other");
        assertFalse(html.contains("style="));
    }

    private List<AjaxLink<?>> rows() {
        List<AjaxLink<?>> rows = new ArrayList<>();
        tester.getLastRenderedPage().visitChildren(AjaxLink.class, (AjaxLink<?> link, IVisit<Void> visit) -> {
            if ("open".equals(link.getId())) {
                rows.add(link);
            }
        });
        return rows;
    }

    @Test
    void inboxOpensLoadsMoreAndDeletesFromTheContextMenu() {
        tester.startPage(InboxPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Load more (20 of 64)"));
        assertTrue(html.contains("data-oat-load-on-scroll"));
        assertTrue(html.contains("data-oat-context-menu"));
        assertTrue(html.contains("We use cookies"));
        assertEquals(20, rows().size());

        tester.clickLink(rows().get(0));
        assertTrue(tester.getLastResponseAsString().contains("this is message 1 of the demo inbox."));

        tester.clickLink("inbox:firstPane:first:more");
        assertTrue(tester.getLastResponseAsString().contains("Load more (40 of 64)"));

        AjaxLink<?> first = rows().get(0);
        ContextMenuBehavior menu = first.getBehaviors(ContextMenuBehavior.class).get(0);
        tester.getRequest().getPostParameters().setParameterValue("action", "2");
        tester.executeBehavior(menu);
        assertTrue(tester.getLastResponseAsString().contains("Choose a message to read it."));
        assertTrue(tester.getLastResponseAsString().contains("Load more (20 of 63)"));
    }
}
