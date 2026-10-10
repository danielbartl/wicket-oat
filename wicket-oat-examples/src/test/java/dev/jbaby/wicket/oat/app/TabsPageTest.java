package dev.jbaby.wicket.oat.app;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TabsPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testTabsPageRenders() {
        tester.startPage(TabsPage.class);
        tester.assertRenderedPage(TabsPage.class);

        tester.assertLabel("tabs:tablist:tabButtons:0:tabLabel", "Account");
        tester.assertLabel("tabs:tabPanels:0:panelContent", "Manage your account information here.");
    }
}
