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
class AlertPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testAlertPageRenders() {
        tester.startPage(AlertPage.class);
        tester.assertRenderedPage(AlertPage.class);

        // Check success alert
        TagTester successAlert = tester.getTagByWicketId("successAlert");
        assertEquals("alert", successAlert.getAttribute("role"));
        assertEquals("success", successAlert.getAttribute("data-variant"));

        // Check error alert
        TagTester errorAlert = tester.getTagByWicketId("errorAlert");
        assertEquals("alert", errorAlert.getAttribute("role"));
        assertEquals("error", errorAlert.getAttribute("data-variant"));
        
        // Check custom behavior alert
        TagTester customAlert = tester.getTagByWicketId("customBehaviorAlert");
        assertEquals("alert", customAlert.getAttribute("role"));
        assertEquals("success", customAlert.getAttribute("data-variant"));
    }
}
