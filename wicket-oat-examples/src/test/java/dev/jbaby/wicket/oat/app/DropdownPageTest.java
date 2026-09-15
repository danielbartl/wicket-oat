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
class DropdownPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testDropdownPageRenders() {
        tester.startPage(DropdownPage.class);
        tester.assertRenderedPage(DropdownPage.class);

        TagTester menu = tester.getTagByWicketId("menu");
        TagTester trigger = tester.getTagByWicketId("trigger");
        assertEquals(menu.getAttribute("id"), trigger.getAttribute("popovertarget"));

        tester.assertLabel("dropdown:menu:items:0:label", "Profile");
    }
}
