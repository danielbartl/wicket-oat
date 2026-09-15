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
class ButtonGroupPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testButtonGroupPageRenders() {
        tester.startPage(ButtonGroupPage.class);
        tester.assertRenderedPage(ButtonGroupPage.class);

        // Check group container - must be a <menu class="buttons" role="group">
        TagTester group = tester.getTagByWicketId("group");
        assertEquals("menu", group.getName());
        assertTrue(group.getAttribute("class").contains("buttons"));
        assertEquals("group", group.getAttribute("role"));

        // Check each button is wrapped in an <li>, which Oat's CSS requires for
        // the connected/grouped button styling to apply
        tester.assertVisible("group:items:0:button");
        tester.assertVisible("group:items:1:button");
        tester.assertVisible("group:items:2:button");

        TagTester firstItem = tester.getTagByWicketId("items");
        assertEquals("li", firstItem.getName());

        TagTester firstButton = tester.getTagByWicketId("button");
        assertTrue(firstButton.getAttribute("class").contains("outline"));
    }
}
