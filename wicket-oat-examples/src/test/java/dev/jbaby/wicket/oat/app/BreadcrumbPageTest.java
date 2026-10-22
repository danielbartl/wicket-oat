package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BreadcrumbPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testBreadcrumbPageRenders() {
        tester.startPage(BreadcrumbPage.class);
        tester.assertRenderedPage(BreadcrumbPage.class);

        TagTester nav = tester.getTagByWicketId("breadcrumb");
        assertEquals("Breadcrumb", nav.getAttribute("aria-label"));

        tester.assertLabel("breadcrumb:list:items:0:link:label", "Home");
    }

    @Test
    void aLongTrailLeavesTheMiddleLevelsToAMenu() {
        tester.startPage(BreadcrumbPage.class);

        tester.assertLabel("longBreadcrumb:list:items:0:link:label", "Home");
        tester.assertLabel("longBreadcrumb:list:more:menu:hidden:1:link:label", "Projects");
        tester.assertLabel("longBreadcrumb:list:more:menu:hidden:4:link:label", "Docs");
        tester.assertLabel("longBreadcrumb:list:tail:5:link:label", "Components");
        tester.assertLabel("longBreadcrumb:list:tail:6:link:label", "Breadcrumb");
        assertEquals("Folders", tester.getTagByWicketId("longBreadcrumb").getAttribute("aria-label"));
    }
}
