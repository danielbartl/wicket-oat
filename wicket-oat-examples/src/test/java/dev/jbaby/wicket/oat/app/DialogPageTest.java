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
class DialogPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testDialogPageRenders() {
        tester.startPage(DialogPage.class);
        tester.assertRenderedPage(DialogPage.class);

        TagTester dialog = tester.getTagByWicketId("dialog");
        assertEquals("dialog", dialog.getName());

        TagTester trigger = tester.getTagByWicketId("trigger");
        assertEquals(dialog.getAttribute("id"), trigger.getAttribute("commandfor"));

        tester.assertLabel("dialog:dialog:header", "Delete item");
    }
}
