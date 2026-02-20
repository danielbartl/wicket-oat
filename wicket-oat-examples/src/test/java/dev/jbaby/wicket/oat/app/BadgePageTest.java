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
class BadgePageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testBadgePageRenders() {
        tester.startPage(BadgePage.class);
        tester.assertRenderedPage(BadgePage.class);

        // Check success badge
        TagTester successBadge = tester.getTagByWicketId("successBadge");
        String successClass = successBadge.getAttribute("class");
        assertTrue(successClass.contains("badge"), "Should have 'badge' class");
        assertTrue(successClass.contains("success"), "Should have 'success' class");

        // Check danger badge
        TagTester dangerBadge = tester.getTagByWicketId("dangerBadge");
        String dangerClass = dangerBadge.getAttribute("class");
        assertTrue(dangerClass.contains("badge"), "Should have 'badge' class");
        assertTrue(dangerClass.contains("danger"), "Should have 'danger' class");
        
        // Check custom behavior badge
        TagTester customBadge = tester.getTagByWicketId("customBehaviorBadge");
        String customClass = customBadge.getAttribute("class");
        assertTrue(customClass.contains("badge"), "Should have 'badge' class");
        assertTrue(customClass.contains("success"), "Should have 'success' class");
    }
}
