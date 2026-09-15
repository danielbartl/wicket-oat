package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
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
}
