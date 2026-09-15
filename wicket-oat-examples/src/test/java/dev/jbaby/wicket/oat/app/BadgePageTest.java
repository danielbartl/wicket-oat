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
        assertTrue(successBadge.getAttribute("class").contains("badge"), "Should have 'badge' class");
        assertEquals("success", successBadge.getAttribute("data-variant"));

        // Check danger badge
        TagTester dangerBadge = tester.getTagByWicketId("dangerBadge");
        assertTrue(dangerBadge.getAttribute("class").contains("badge"), "Should have 'badge' class");
        assertEquals("danger", dangerBadge.getAttribute("data-variant"));

        // Check outline badge (secondary color + outline style modifier)
        TagTester outlineBadge = tester.getTagByWicketId("outlineBadge");
        assertTrue(outlineBadge.getAttribute("class").contains("outline"), "Should have 'outline' class");
        assertEquals("secondary", outlineBadge.getAttribute("data-variant"));

        // Check custom behavior badge
        TagTester customBadge = tester.getTagByWicketId("customBehaviorBadge");
        assertTrue(customBadge.getAttribute("class").contains("badge"), "Should have 'badge' class");
        assertEquals("success", customBadge.getAttribute("data-variant"));
    }
}
