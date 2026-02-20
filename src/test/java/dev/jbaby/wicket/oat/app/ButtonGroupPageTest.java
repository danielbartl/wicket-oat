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

        // Check group container
        TagTester group = tester.getTagByWicketId("group");
        assertTrue(group.getAttribute("class").contains("buttons"));
        assertEquals("group", group.getAttribute("role"));

        // Check buttons inside group
        tester.assertVisible("group:left");
        tester.assertVisible("group:center");
        tester.assertVisible("group:right");

        TagTester leftBtn = tester.getTagByWicketId("left");
        assertTrue(leftBtn.getAttribute("class").contains("outline"));
    }
}
