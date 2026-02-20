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
class ButtonPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testButtonPageRenders() {
        tester.startPage(ButtonPage.class);
        tester.assertRenderedPage(ButtonPage.class);

        // Check secondary button
        TagTester secondaryBtn = tester.getTagByWicketId("secondaryButton");
        assertEquals("secondary", secondaryBtn.getAttribute("data-variant"));
        assertTrue(secondaryBtn.getAttribute("class").contains("button"));

        // Check danger button
        TagTester dangerBtn = tester.getTagByWicketId("dangerButton");
        assertEquals("danger", dangerBtn.getAttribute("data-variant"));

        // Check outline button
        TagTester outlineBtn = tester.getTagByWicketId("outlineButton");
        assertTrue(outlineBtn.getAttribute("class").contains("outline"));

        // Check small button
        TagTester smallBtn = tester.getTagByWicketId("smallButton");
        assertTrue(smallBtn.getAttribute("class").contains("small"));
    }
}
