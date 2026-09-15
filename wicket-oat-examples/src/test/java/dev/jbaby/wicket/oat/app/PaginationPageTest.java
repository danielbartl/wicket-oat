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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PaginationPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testPaginationPageRenders() {
        tester.startPage(PaginationPage.class);
        tester.assertRenderedPage(PaginationPage.class);

        TagTester nav = tester.getTagByWicketId("pagination");
        assertEquals("Pagination", nav.getAttribute("aria-label"));

        TagTester menu = tester.getTagByWicketId("menu");
        assertEquals("menu", menu.getName());
        assertTrue(menu.getAttribute("class").contains("buttons"));
    }
}
